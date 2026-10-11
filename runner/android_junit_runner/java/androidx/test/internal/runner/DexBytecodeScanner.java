/*
 * Copyright (C) 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.internal.runner;

import androidx.test.internal.runner.ClassPathScanner.ClassNameFilter;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Lightweight zero-dependency DEX bytecode scanner that filters candidate JUnit3 and JUnit4 test
 * classes directly from DEX header/table structures before class loading.
 *
 * <p>By binary-searching DEX {@code string_ids}/{@code type_ids} tables and resolving intra-DEX and
 * cross-DEX {@code class_def_item} hierarchies in-place without {@code Class.forName()} or heap
 * object graphs, this scanner skips non-test shards and non-test classes in milliseconds without
 * polluting the runtime {@link ClassLoader}.
 */
final class DexBytecodeScanner {

  private static final int NO_INDEX = -1;

  /** Upper bound on superclass chain walks, guarding against malformed or cyclic hierarchies. */
  private static final int MAX_HIERARCHY_DEPTH = 64;

  // DEX header layout. See https://source.android.com/docs/core/runtime/dex-format#header-item.
  private static final int DEX_HEADER_SIZE = 0x70;
  private static final int DEX_ENDIAN_TAG_OFFSET = 0x28;
  private static final int DEX_ENDIAN_CONSTANT = 0x12345678;
  private static final int DEX_STRING_IDS_SIZE_OFFSET = 0x38;
  private static final int DEX_STRING_IDS_OFF_OFFSET = 0x3C;
  private static final int DEX_TYPE_IDS_SIZE_OFFSET = 0x40;
  private static final int DEX_TYPE_IDS_OFF_OFFSET = 0x44;
  private static final int DEX_METHOD_IDS_OFF_OFFSET = 0x5C;
  private static final int DEX_CLASS_DEFS_SIZE_OFFSET = 0x60;
  private static final int DEX_CLASS_DEFS_OFF_OFFSET = 0x64;

  // class_def_item layout.
  private static final int CLASS_DEF_ITEM_SIZE = 32;
  private static final int CLASS_DEF_ACCESS_FLAGS_OFFSET = 4;
  private static final int CLASS_DEF_SUPERCLASS_IDX_OFFSET = 8;
  private static final int CLASS_DEF_ANNOTATIONS_OFF_OFFSET = 20;
  private static final int CLASS_DEF_CLASS_DATA_OFF_OFFSET = 24;

  // DEX access_flags constants
  private static final int ACC_INTERFACE = 0x0200;
  private static final int ACC_ABSTRACT = 0x0400;
  private static final int ACC_SYNTHETIC = 0x1000;
  private static final int ACC_ANNOTATION = 0x2000;
  private static final int ACC_ENUM = 0x4000;
  private static final int NON_CONCRETE_TEST_FLAGS =
      ACC_INTERFACE | ACC_ABSTRACT | ACC_SYNTHETIC | ACC_ANNOTATION | ACC_ENUM;

  // ZIP layout. See https://pkware.cachefly.net/webdocs/casestudies/APPNOTE.TXT.
  private static final int ZIP_EOCD_SIGNATURE = 0x06054b50;
  private static final int ZIP_EOCD_MIN_SIZE = 22;

  /** The EOCD record is at most its fixed size plus a 64 KiB comment from the end of the file. */
  private static final int ZIP_EOCD_MAX_SIZE = ZIP_EOCD_MIN_SIZE + 0xFFFF;

  private static final int ZIP_CENTRAL_DIR_SIGNATURE = 0x02014b50;
  private static final int ZIP_CENTRAL_DIR_HEADER_SIZE = 46;
  private static final int ZIP_LOCAL_HEADER_SIZE = 30;
  private static final int ZIP_METHOD_STORED = 0;

  // 64-bit FNV-1a hash parameters.
  private static final long FNV64_OFFSET_BASIS = 0xcbf29ce484222325L;
  private static final long FNV64_PRIME = 0x100000001b3L;

  // Descriptors for JUnit3 / JUnit4 discovery signals. The JUnit ones are derived from class
  // literals rather than hardcoded strings: when R8/ProGuard obfuscates the test APK it renames
  // org.junit.* and junit.framework.* consistently, including these literals, so the descriptors
  // always match the names actually present in the APK's DEX files.
  private static final String DESC_JAVA_LANG_OBJECT = "Ljava/lang/Object;";
  private static final String DESC_JUNIT3_TEST_CASE = descriptorOf(TestCase.class);
  private static final String DESC_ORG_JUNIT_TEST = descriptorOf(Test.class);
  private static final String DESC_ORG_JUNIT_RUN_WITH = descriptorOf(RunWith.class);
  private static final String DESC_ORG_JUNIT_IGNORE = descriptorOf(Ignore.class);
  private static final String METHOD_NAME_SUITE = "suite";

  private DexBytecodeScanner() {}

  private static String descriptorOf(Class<?> clazz) {
    return "L" + clazz.getName().replace('.', '/') + ";";
  }

  /**
   * Scans {@code path} (either a standalone {@code .dex} file or an {@code .apk}/{@code
   * .zip}/{@code .jar} archive containing {@code classes*.dex}) and appends candidate JUnit test
   * class names matching {@code filter} into {@code entryNames}.
   *
   * @return {@code true} if {@code path} was scanned via DEX bytecode parsing; {@code false} if
   *     {@code path} should fall back to {@link dalvik.system.DexFile}. Nothing is added to {@code
   *     entryNames} when returning {@code false}.
   */
  static boolean scanPath(Set<String> entryNames, String path, ClassNameFilter filter)
      throws IOException {
    File file = new File(path);
    if (!file.isFile() || !file.canRead()) {
      return false;
    }

    Set<String> found = new HashSet<>();
    CrossDexState crossDexState = new CrossDexState();
    try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
      if (path.endsWith(".dex")) {
        ByteBuffer mapped = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, raf.length());
        mapped.order(ByteOrder.LITTLE_ENDIAN);
        if (!isValidDexHeader(mapped)) {
          return false;
        }
        processDexShard(mapped, found, filter, crossDexState);
      } else if (!tryStreamStoredDexEntries(raf, found, filter, crossDexState)) {
        crossDexState.reset();
        found.clear();
        if (!scanZipDexEntries(file, found, filter, crossDexState)) {
          return false;
        }
      }
    }

    crossDexState.resolvePendingCandidates(found, filter);

    // Safety net: if the JUnit4 marker types don't appear in any shard (e.g. they were renamed
    // differently from this class's view of them) or nothing was found at all, the pre-filter
    // can't be trusted. Fall back to DexFile enumeration, which costs speed but never drops
    // tests.
    if (!crossDexState.sawJUnit4MarkerType || found.isEmpty()) {
      return false;
    }
    entryNames.addAll(found);
    return true;
  }

  /**
   * Reads each {@code *.dex} entry of the archive at {@code file} through {@link ZipFile}, which
   * handles compressed entries. Returns {@code false} if the archive has no usable DEX entries.
   */
  private static boolean scanZipDexEntries(
      File file, Set<String> entryNames, ClassNameFilter filter, CrossDexState crossDexState)
      throws IOException {
    try (ZipFile zipFile = new ZipFile(file)) {
      List<ZipEntry> dexEntries = getSortedDexEntries(zipFile);
      if (dexEntries.isEmpty()) {
        return false;
      }
      byte[] scratch = new byte[0];
      for (ZipEntry entry : dexEntries) {
        long size = entry.getSize();
        if (size <= 0 || size > Integer.MAX_VALUE) {
          return false;
        }
        int byteLen = (int) size;
        if (scratch.length < byteLen) {
          scratch = new byte[byteLen];
        }
        readZipEntryInto(zipFile, entry, scratch, byteLen);
        ByteBuffer buf = ByteBuffer.wrap(scratch, 0, byteLen);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        if (!isValidDexHeader(buf)) {
          return false;
        }
        processDexShard(buf, entryNames, filter, crossDexState);
      }
    }
    return true;
  }

  /**
   * Streams all {@code *.dex} entries directly from {@code raf} one shard at a time via read-only
   * {@code MappedByteBuffer}s if all DEX entries in the ZIP central directory are stored
   * uncompressed ({@code STORED}). Returns {@code false} if any DEX entry is compressed or if the
   * archive is not a standard 32-bit ZIP.
   */
  private static boolean tryStreamStoredDexEntries(
      RandomAccessFile raf,
      Set<String> entryNames,
      ClassNameFilter filter,
      CrossDexState crossDexState)
      throws IOException {
    FileChannel channel = raf.getChannel();
    long fileLen = channel.size();
    if (fileLen < ZIP_EOCD_MIN_SIZE) {
      return false;
    }
    int tailLen = (int) Math.min(fileLen, ZIP_EOCD_MAX_SIZE);
    ByteBuffer tail = channel.map(FileChannel.MapMode.READ_ONLY, fileLen - tailLen, tailLen);
    tail.order(ByteOrder.LITTLE_ENDIAN);

    int eocdPos = NO_INDEX;
    for (int i = tailLen - ZIP_EOCD_MIN_SIZE; i >= 0; i--) {
      if (tail.getInt(i) == ZIP_EOCD_SIGNATURE) {
        eocdPos = i;
        break;
      }
    }
    if (eocdPos < 0) {
      return false;
    }

    int totalEntries = tail.getShort(eocdPos + 10) & 0xFFFF;
    long cdSize = tail.getInt(eocdPos + 12) & 0xFFFFFFFFL;
    long cdOffset = tail.getInt(eocdPos + 16) & 0xFFFFFFFFL;
    if (cdOffset + cdSize > fileLen || cdSize > Integer.MAX_VALUE) {
      return false;
    }

    ByteBuffer cd = channel.map(FileChannel.MapMode.READ_ONLY, cdOffset, cdSize);
    cd.order(ByteOrder.LITTLE_ENDIAN);

    List<Long> dexDataOffsets = new ArrayList<>();
    List<Long> dexSizes = new ArrayList<>();
    int pos = 0;
    for (int i = 0; i < totalEntries && pos + ZIP_CENTRAL_DIR_HEADER_SIZE <= cd.limit(); i++) {
      if (cd.getInt(pos) != ZIP_CENTRAL_DIR_SIGNATURE) {
        return false;
      }
      int compression = cd.getShort(pos + 10) & 0xFFFF;
      long uncompSize = cd.getInt(pos + 24) & 0xFFFFFFFFL;
      int nameLen = cd.getShort(pos + 28) & 0xFFFF;
      int extraLen = cd.getShort(pos + 30) & 0xFFFF;
      int commentLen = cd.getShort(pos + 32) & 0xFFFF;
      long localHeaderOff = cd.getInt(pos + 42) & 0xFFFFFFFFL;

      int nameEnd = pos + ZIP_CENTRAL_DIR_HEADER_SIZE + nameLen;
      if (nameLen >= 4
          && cd.get(nameEnd - 4) == '.'
          && cd.get(nameEnd - 3) == 'd'
          && cd.get(nameEnd - 2) == 'e'
          && cd.get(nameEnd - 1) == 'x') {
        if (compression != ZIP_METHOD_STORED
            || uncompSize <= 0
            || localHeaderOff + ZIP_LOCAL_HEADER_SIZE > fileLen) {
          return false;
        }
        ByteBuffer lfh =
            channel.map(FileChannel.MapMode.READ_ONLY, localHeaderOff, ZIP_LOCAL_HEADER_SIZE);
        lfh.order(ByteOrder.LITTLE_ENDIAN);
        int lfhNameLen = lfh.getShort(26) & 0xFFFF;
        int lfhExtraLen = lfh.getShort(28) & 0xFFFF;
        long dataOff = localHeaderOff + ZIP_LOCAL_HEADER_SIZE + lfhNameLen + lfhExtraLen;
        if (dataOff + uncompSize > fileLen) {
          return false;
        }
        dexDataOffsets.add(dataOff);
        dexSizes.add(uncompSize);
      }
      pos = nameEnd + extraLen + commentLen;
    }

    if (dexDataOffsets.isEmpty()) {
      return false;
    }

    for (int i = 0; i < dexDataOffsets.size(); i++) {
      ByteBuffer mapped =
          channel.map(FileChannel.MapMode.READ_ONLY, dexDataOffsets.get(i), dexSizes.get(i));
      mapped.order(ByteOrder.LITTLE_ENDIAN);
      if (!isValidDexHeader(mapped)) {
        return false;
      }
      processDexShard(mapped, entryNames, filter, crossDexState);
    }
    return true;
  }

  /**
   * Returns the {@code classes.dex}, {@code classes2.dex}, ... entries of {@code zipFile} in
   * multidex order. Archives with other DEX layouts return an empty list and fall back to {@link
   * dalvik.system.DexFile}.
   */
  private static List<ZipEntry> getSortedDexEntries(ZipFile zipFile) {
    List<ZipEntry> entries = new ArrayList<>();
    ZipEntry primary = zipFile.getEntry("classes.dex");
    if (primary == null) {
      return entries;
    }
    entries.add(primary);
    for (int idx = 2; ; idx++) {
      ZipEntry next = zipFile.getEntry("classes" + idx + ".dex");
      if (next == null) {
        return entries;
      }
      entries.add(next);
    }
  }

  private static void readZipEntryInto(ZipFile zipFile, ZipEntry entry, byte[] dest, int byteLen)
      throws IOException {
    try (InputStream in = zipFile.getInputStream(entry)) {
      int offset = 0;
      while (offset < byteLen) {
        int read = in.read(dest, offset, byteLen - offset);
        if (read < 0) {
          throw new EOFException("Unexpected EOF reading DEX entry");
        }
        offset += read;
      }
    }
  }

  private static boolean isValidDexHeader(ByteBuffer buf) {
    if (buf.limit() < DEX_HEADER_SIZE) {
      return false;
    }
    return buf.get(0) == 'd'
        && buf.get(1) == 'e'
        && buf.get(2) == 'x'
        && buf.get(3) == '\n'
        && buf.get(7) == 0
        && buf.getInt(DEX_ENDIAN_TAG_OFFSET) == DEX_ENDIAN_CONSTANT;
  }

  private static final class CrossDexState {
    /**
     * Open-addressing hash set of descriptor hashes for every class defined in any shard scanned so
     * far. {@code 0} marks an empty slot, which {@link DexBytecodeScanner#hashDescriptor64} never
     * returns.
     */
    private long[] definedHashes = new long[8192];

    private int definedCount;
    private final Set<Long> knownTestHashes = new HashSet<>();
    private final Map<Long, Long> externalSuperHash = new HashMap<>();
    private final List<String> pendingDotNames = new ArrayList<>();
    private final List<Long> pendingSuperHashes = new ArrayList<>();

    /** Whether any shard's {@code type_ids} contained the {@code Test} or {@code RunWith} type. */
    boolean sawJUnit4MarkerType;

    void reset() {
      Arrays.fill(definedHashes, 0L);
      definedCount = 0;
      knownTestHashes.clear();
      externalSuperHash.clear();
      pendingDotNames.clear();
      pendingSuperHashes.clear();
      sawJUnit4MarkerType = false;
    }

    void addDefinedHash(long h) {
      if (definedCount * 10 >= definedHashes.length * 7) {
        rehashDefined();
      }
      int mask = definedHashes.length - 1;
      int idx = ((int) (h ^ (h >>> 32))) & mask;
      while (definedHashes[idx] != 0L) {
        if (definedHashes[idx] == h) {
          return;
        }
        idx = (idx + 1) & mask;
      }
      definedHashes[idx] = h;
      definedCount++;
    }

    boolean containsDefinedHash(long h) {
      int mask = definedHashes.length - 1;
      int idx = ((int) (h ^ (h >>> 32))) & mask;
      while (definedHashes[idx] != 0L) {
        if (definedHashes[idx] == h) {
          return true;
        }
        idx = (idx + 1) & mask;
      }
      return false;
    }

    private void rehashDefined() {
      long[] old = definedHashes;
      definedHashes = new long[old.length * 2];
      int mask = definedHashes.length - 1;
      for (long h : old) {
        if (h != 0L) {
          int idx = ((int) (h ^ (h >>> 32))) & mask;
          while (definedHashes[idx] != 0L) {
            idx = (idx + 1) & mask;
          }
          definedHashes[idx] = h;
        }
      }
    }

    /**
     * Adds pending classes whose superclass chain crosses DEX shards to {@code entryNames} if the
     * chain reaches a known test class or leaves the APK (e.g. to a test base class in a library).
     */
    void resolvePendingCandidates(Set<String> entryNames, ClassNameFilter filter) {
      for (int i = 0; i < pendingDotNames.size(); i++) {
        String dotName = pendingDotNames.get(i);
        long cur = pendingSuperHashes.get(i);
        for (int depth = 0; depth < MAX_HIERARCHY_DEPTH; depth++) {
          if (knownTestHashes.contains(cur)) {
            if (filter.accept(dotName)) {
              entryNames.add(dotName);
            }
            break;
          }
          Long nextSuper = externalSuperHash.get(cur);
          if (nextSuper == null) {
            if (!containsDefinedHash(cur) && filter.accept(dotName)) {
              entryNames.add(dotName);
            }
            break;
          }
          cur = nextSuper;
        }
      }
    }
  }

  /** Returns a non-zero 64-bit FNV-1a hash of the MUTF-8 descriptor of type {@code typeIdx}. */
  private static long hashDescriptor64(
      ByteBuffer buf, int stringIdsOff, int typeIdsOff, int typeIdx) {
    int stringIdx = buf.getInt(typeIdsOff + typeIdx * 4);
    int dataOff = buf.getInt(stringIdsOff + stringIdx * 4);
    int pos = skipUleb128(buf, dataOff);
    long h = FNV64_OFFSET_BASIS;
    while (true) {
      byte b = buf.get(pos++);
      if (b == 0) {
        break;
      }
      h ^= (b & 0xFFL);
      h *= FNV64_PRIME;
    }
    return h == 0L ? 1L : h;
  }

  private static void processDexShard(
      ByteBuffer buf, Set<String> entryNames, ClassNameFilter filter, CrossDexState crossDexState) {
    int stringIdsSize = buf.getInt(DEX_STRING_IDS_SIZE_OFFSET);
    int stringIdsOff = buf.getInt(DEX_STRING_IDS_OFF_OFFSET);
    int typeIdsSize = buf.getInt(DEX_TYPE_IDS_SIZE_OFFSET);
    int typeIdsOff = buf.getInt(DEX_TYPE_IDS_OFF_OFFSET);
    int methodIdsOff = buf.getInt(DEX_METHOD_IDS_OFF_OFFSET);
    int classDefsSize = buf.getInt(DEX_CLASS_DEFS_SIZE_OFFSET);
    int classDefsOff = buf.getInt(DEX_CLASS_DEFS_OFF_OFFSET);

    int javaObjectTypeIdx =
        findTypeIdx(
            buf, stringIdsSize, stringIdsOff, typeIdsSize, typeIdsOff, DESC_JAVA_LANG_OBJECT);
    int junitTestCaseTypeIdx =
        findTypeIdx(
            buf, stringIdsSize, stringIdsOff, typeIdsSize, typeIdsOff, DESC_JUNIT3_TEST_CASE);
    int orgJunitTestTypeIdx =
        findTypeIdx(buf, stringIdsSize, stringIdsOff, typeIdsSize, typeIdsOff, DESC_ORG_JUNIT_TEST);
    int orgJunitRunWithTypeIdx =
        findTypeIdx(
            buf, stringIdsSize, stringIdsOff, typeIdsSize, typeIdsOff, DESC_ORG_JUNIT_RUN_WITH);
    int orgJunitIgnoreTypeIdx =
        findTypeIdx(
            buf, stringIdsSize, stringIdsOff, typeIdsSize, typeIdsOff, DESC_ORG_JUNIT_IGNORE);
    int suiteMethodStringIdx = findStringIdx(buf, stringIdsSize, stringIdsOff, METHOD_NAME_SUITE);

    if (orgJunitTestTypeIdx >= 0 || orgJunitRunWithTypeIdx >= 0) {
      crossDexState.sawJUnit4MarkerType = true;
    }

    boolean shardCanContainDirectSignals =
        junitTestCaseTypeIdx >= 0
            || orgJunitTestTypeIdx >= 0
            || orgJunitRunWithTypeIdx >= 0
            || orgJunitIgnoreTypeIdx >= 0
            || suiteMethodStringIdx >= 0;

    int[] typeToClassDef = new int[typeIdsSize];
    Arrays.fill(typeToClassDef, NO_INDEX);

    boolean[] directSignal = new boolean[classDefsSize];
    for (int i = 0; i < classDefsSize; i++) {
      int defOff = classDefsOff + i * CLASS_DEF_ITEM_SIZE;
      int classIdx = buf.getInt(defOff);
      typeToClassDef[classIdx] = i;

      if (shardCanContainDirectSignals) {
        int superIdx = buf.getInt(defOff + CLASS_DEF_SUPERCLASS_IDX_OFFSET);
        if (classIdx == junitTestCaseTypeIdx || superIdx == junitTestCaseTypeIdx) {
          directSignal[i] = true;
          continue;
        }
        int annotationsOff = buf.getInt(defOff + CLASS_DEF_ANNOTATIONS_OFF_OFFSET);
        if (annotationsOff != 0
            && hasJUnitAnnotation(
                buf,
                annotationsOff,
                orgJunitTestTypeIdx,
                orgJunitRunWithTypeIdx,
                orgJunitIgnoreTypeIdx)) {
          directSignal[i] = true;
          continue;
        }
        int classDataOff = buf.getInt(defOff + CLASS_DEF_CLASS_DATA_OFF_OFFSET);
        if (suiteMethodStringIdx >= 0
            && classDataOff != 0
            && hasSuiteMethod(buf, classDataOff, methodIdsOff, suiteMethodStringIdx)) {
          directSignal[i] = true;
        }
      }
    }

    for (int i = 0; i < classDefsSize; i++) {
      int defOff = classDefsOff + i * CLASS_DEF_ITEM_SIZE;
      int classIdx = buf.getInt(defOff);
      long selfHash = hashDescriptor64(buf, stringIdsOff, typeIdsOff, classIdx);
      crossDexState.addDefinedHash(selfHash);

      // Walk the superclass chain within this shard until it hits a direct JUnit signal, ends at
      // Object, or leaves the shard (recorded in externalSuperTypeIdx).
      int curDef = i;
      int externalSuperTypeIdx = NO_INDEX;
      boolean matched = false;
      for (int depth = 0; depth < MAX_HIERARCHY_DEPTH; depth++) {
        if (directSignal[curDef]) {
          matched = true;
          break;
        }
        int supIdx =
            buf.getInt(
                classDefsOff + curDef * CLASS_DEF_ITEM_SIZE + CLASS_DEF_SUPERCLASS_IDX_OFFSET);
        if (supIdx == NO_INDEX || supIdx == javaObjectTypeIdx) {
          break;
        }
        int parentDef = typeToClassDef[supIdx];
        if (parentDef == NO_INDEX) {
          externalSuperTypeIdx = supIdx;
          break;
        }
        curDef = parentDef;
      }

      int accessFlags = buf.getInt(defOff + CLASS_DEF_ACCESS_FLAGS_OFFSET);
      boolean concreteTopLevel =
          (accessFlags & NON_CONCRETE_TEST_FLAGS) == 0
              && !descriptorContainsDollar(buf, stringIdsOff, typeIdsOff, classIdx);

      if (matched) {
        crossDexState.knownTestHashes.add(selfHash);
        if (concreteTopLevel) {
          String dotName = decodeTypeToDotName(buf, stringIdsOff, typeIdsOff, classIdx);
          if (filter.accept(dotName)) {
            entryNames.add(dotName);
          }
        }
      } else if (externalSuperTypeIdx != NO_INDEX
          && !isKnownNonTestBootClasspathTypeIdx(
              buf, stringIdsOff, typeIdsOff, externalSuperTypeIdx)) {
        long extSuperHash = hashDescriptor64(buf, stringIdsOff, typeIdsOff, externalSuperTypeIdx);
        crossDexState.externalSuperHash.put(selfHash, extSuperHash);
        if (concreteTopLevel) {
          String dotName = decodeTypeToDotName(buf, stringIdsOff, typeIdsOff, classIdx);
          crossDexState.pendingDotNames.add(dotName);
          crossDexState.pendingSuperHashes.add(extSuperHash);
        }
      }
    }
  }

  private static boolean hasJUnitAnnotation(
      ByteBuffer buf,
      int annotationsOff,
      int orgJunitTestTypeIdx,
      int orgJunitRunWithTypeIdx,
      int orgJunitIgnoreTypeIdx) {
    int classAnnotationsOff = buf.getInt(annotationsOff);
    int fieldsSize = buf.getInt(annotationsOff + 4);
    int methodsSize = buf.getInt(annotationsOff + 8);

    if (classAnnotationsOff != 0
        && (orgJunitRunWithTypeIdx >= 0 || orgJunitIgnoreTypeIdx >= 0)
        && annotationSetContainsEither(
            buf, classAnnotationsOff, orgJunitRunWithTypeIdx, orgJunitIgnoreTypeIdx)) {
      return true;
    }

    if (methodsSize > 0 && orgJunitTestTypeIdx >= 0) {
      int methodAnnBase = annotationsOff + 16 + fieldsSize * 8;
      for (int m = 0; m < methodsSize; m++) {
        int setOff = buf.getInt(methodAnnBase + m * 8 + 4);
        if (setOff != 0 && annotationSetContains(buf, setOff, orgJunitTestTypeIdx)) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean annotationSetContains(ByteBuffer buf, int setOff, int targetTypeIdx) {
    int size = buf.getInt(setOff);
    for (int i = 0; i < size; i++) {
      int itemOff = buf.getInt(setOff + 4 + i * 4);
      int typeIdx = readUleb128At(buf, itemOff + 1);
      if (typeIdx == targetTypeIdx) {
        return true;
      }
    }
    return false;
  }

  private static boolean annotationSetContainsEither(
      ByteBuffer buf, int setOff, int targetTypeIdx1, int targetTypeIdx2) {
    int size = buf.getInt(setOff);
    for (int i = 0; i < size; i++) {
      int itemOff = buf.getInt(setOff + 4 + i * 4);
      int typeIdx = readUleb128At(buf, itemOff + 1);
      if (typeIdx == targetTypeIdx1 || typeIdx == targetTypeIdx2) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasSuiteMethod(
      ByteBuffer buf, int classDataOff, int methodIdsOff, int suiteStringIdx) {
    int[] pos = new int[] {classDataOff};
    int staticFields = readUleb128(buf, pos);
    int instanceFields = readUleb128(buf, pos);
    int directMethods = readUleb128(buf, pos);
    if (directMethods == 0) {
      return false;
    }
    advanceUleb128(buf, pos); // virtualMethods
    for (int i = 0; i < staticFields + instanceFields; i++) {
      advanceUleb128(buf, pos); // field_idx_diff
      advanceUleb128(buf, pos); // access_flags
    }
    int methodIdx = 0;
    for (int i = 0; i < directMethods; i++) {
      methodIdx += readUleb128(buf, pos);
      advanceUleb128(buf, pos); // access_flags
      advanceUleb128(buf, pos); // code_off
      int nameIdx = buf.getInt(methodIdsOff + methodIdx * 8 + 4);
      if (nameIdx == suiteStringIdx) {
        return true;
      }
    }
    return false;
  }

  private static void advanceUleb128(ByteBuffer buf, int[] pos) {
    int p = pos[0];
    while ((buf.get(p++) & 0x80) != 0) {}
    pos[0] = p;
  }

  private static boolean descriptorContainsDollar(
      ByteBuffer buf, int stringIdsOff, int typeIdsOff, int typeIdx) {
    int stringIdx = buf.getInt(typeIdsOff + typeIdx * 4);
    int dataOff = buf.getInt(stringIdsOff + stringIdx * 4);
    int pos = skipUleb128(buf, dataOff);
    while (true) {
      byte b = buf.get(pos++);
      if (b == 0) {
        return false;
      }
      if (b == '$') {
        return true;
      }
    }
  }

  private static boolean isKnownNonTestBootClasspathTypeIdx(
      ByteBuffer buf, int stringIdsOff, int typeIdsOff, int typeIdx) {
    int stringIdx = buf.getInt(typeIdsOff + typeIdx * 4);
    int dataOff = buf.getInt(stringIdsOff + stringIdx * 4);
    int pos = skipUleb128(buf, dataOff);
    if (buf.get(pos) != 'L') {
      return true;
    }
    byte b1 = buf.get(pos + 1);
    if (b1 == 'j') {
      return startsWithAscii(buf, pos + 1, "java/");
    }
    if (b1 == 'a') {
      if (startsWithAscii(buf, pos + 1, "android/")) {
        return !startsWithAscii(buf, pos + 9, "test/");
      }
    }
    if (b1 == 'd') {
      return startsWithAscii(buf, pos + 1, "dalvik/");
    }
    return false;
  }

  private static boolean startsWithAscii(ByteBuffer buf, int offset, String prefix) {
    if (offset + prefix.length() > buf.limit()) {
      return false;
    }
    for (int i = 0; i < prefix.length(); i++) {
      if (buf.get(offset + i) != (byte) prefix.charAt(i)) {
        return false;
      }
    }
    return true;
  }

  private static String decodeTypeToDotName(
      ByteBuffer buf, int stringIdsOff, int typeIdsOff, int typeIdx) {
    String desc = decodeTypeDescriptor(buf, stringIdsOff, typeIdsOff, typeIdx);
    if (desc.length() > 2 && desc.charAt(0) == 'L' && desc.charAt(desc.length() - 1) == ';') {
      return desc.substring(1, desc.length() - 1).replace('/', '.');
    }
    return desc.replace('/', '.');
  }

  private static String decodeTypeDescriptor(
      ByteBuffer buf, int stringIdsOff, int typeIdsOff, int typeIdx) {
    int stringIdx = buf.getInt(typeIdsOff + typeIdx * 4);
    int dataOff = buf.getInt(stringIdsOff + stringIdx * 4);
    int[] pos = new int[] {dataOff};
    int utf16Len = readUleb128(buf, pos);
    char[] chars = new char[utf16Len];
    int out = 0;
    while (out < utf16Len) {
      int a = buf.get(pos[0]++) & 0xFF;
      if (a == 0) {
        break;
      }
      if ((a & 0x80) == 0) {
        chars[out++] = (char) a;
      } else if ((a & 0xE0) == 0xC0) {
        int b = buf.get(pos[0]++) & 0x3F;
        chars[out++] = (char) (((a & 0x1F) << 6) | b);
      } else {
        int b = buf.get(pos[0]++) & 0x3F;
        int c = buf.get(pos[0]++) & 0x3F;
        chars[out++] = (char) (((a & 0x0F) << 12) | (b << 6) | c);
      }
    }
    return new String(chars, 0, out);
  }

  private static int findTypeIdx(
      ByteBuffer buf,
      int stringIdsSize,
      int stringIdsOff,
      int typeIdsSize,
      int typeIdsOff,
      String descriptor) {
    int stringIdx = findStringIdx(buf, stringIdsSize, stringIdsOff, descriptor);
    if (stringIdx < 0) {
      return NO_INDEX;
    }
    int lo = 0;
    int hi = typeIdsSize - 1;
    while (lo <= hi) {
      int mid = (lo + hi) >>> 1;
      int descIdx = buf.getInt(typeIdsOff + mid * 4);
      if (descIdx < stringIdx) {
        lo = mid + 1;
      } else if (descIdx > stringIdx) {
        hi = mid - 1;
      } else {
        return mid;
      }
    }
    return NO_INDEX;
  }

  private static int findStringIdx(
      ByteBuffer buf, int stringIdsSize, int stringIdsOff, String target) {
    int lo = 0;
    int hi = stringIdsSize - 1;
    while (lo <= hi) {
      int mid = (lo + hi) >>> 1;
      int strDataOff = buf.getInt(stringIdsOff + mid * 4);
      int cmp = compareMutf8To(buf, strDataOff, target);
      if (cmp < 0) {
        lo = mid + 1;
      } else if (cmp > 0) {
        hi = mid - 1;
      } else {
        return mid;
      }
    }
    return NO_INDEX;
  }

  /**
   * Compares the MUTF-8 {@code string_data_item} at {@code strDataOff} with {@code target} by
   * UTF-16 code unit, matching the DEX {@code string_ids} sort order.
   */
  private static int compareMutf8To(ByteBuffer buf, int strDataOff, String target) {
    int pos = skipUleb128(buf, strDataOff);
    int idx = 0;
    while (true) {
      int a = buf.get(pos++) & 0xFF;
      if (a == 0) {
        return idx < target.length() ? -1 : 0;
      }
      int codeUnit;
      if ((a & 0x80) == 0) {
        codeUnit = a;
      } else if ((a & 0xE0) == 0xC0) {
        int b = buf.get(pos++) & 0x3F;
        codeUnit = ((a & 0x1F) << 6) | b;
      } else {
        int b = buf.get(pos++) & 0x3F;
        int c = buf.get(pos++) & 0x3F;
        codeUnit = ((a & 0x0F) << 12) | (b << 6) | c;
      }
      if (idx >= target.length()) {
        return 1;
      }
      int targetUnit = target.charAt(idx++);
      if (codeUnit != targetUnit) {
        return codeUnit < targetUnit ? -1 : 1;
      }
    }
  }

  private static int skipUleb128(ByteBuffer buf, int pos) {
    while ((buf.get(pos++) & 0x80) != 0) {}
    return pos;
  }

  private static int readUleb128At(ByteBuffer buf, int pos) {
    int result = 0;
    int shift = 0;
    while (true) {
      int b = buf.get(pos++);
      result |= (b & 0x7F) << shift;
      if ((b & 0x80) == 0) {
        return result;
      }
      shift += 7;
    }
  }

  private static int readUleb128(ByteBuffer buf, int[] pos) {
    int result = 0;
    int shift = 0;
    int p = pos[0];
    while (true) {
      int b = buf.get(p++);
      result |= (b & 0x7F) << shift;
      if ((b & 0x80) == 0) {
        pos[0] = p;
        return result;
      }
      shift += 7;
    }
  }
}
