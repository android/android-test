/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package androidx.test.espresso.web.assertion;

import androidx.annotation.VisibleForTesting;
import androidx.test.internal.util.LogUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import kotlin.io.ByteStreamsKt;

/** Compresses an input source. By default this class uses the GZIP format for compression. */
final class CompressorDecompressor {
  private static final String TAG = "CompressorDecompressor";
  private static final Compressor DEFAULT_COMPRESSOR = new GZIPCompressor();
  private static final Decompressor DEFAULT_DECOMPRESSOR = new GZIPDecompressor();

  public static byte[] compress(byte[] source) throws IOException {
    long startTime = System.currentTimeMillis();
    byte[] compressed = DEFAULT_COMPRESSOR.compress(source);
    long endTime = System.currentTimeMillis();
    LogUtil.logDebugWithProcess(
        TAG,
        "Compressed input with size %d (bytes) to output with size %d (bytes). Compression factor: "
            + "%f (%d bytes). Total time %d ms",
        source.length,
        compressed.length,
        (float) compressed.length / (float) source.length,
        Math.abs(compressed.length - source.length),
        endTime - startTime);
    return compressed;
  }

  public static byte[] decompress(byte[] bytes) throws IOException {
    long startTime = System.currentTimeMillis();
    byte[] decompressed = DEFAULT_DECOMPRESSOR.decompress(bytes);
    long endTime = System.currentTimeMillis();
    LogUtil.logDebugWithProcess(
        TAG,
        "Decompressed input with size %d (bytes) to output with size %d (bytes). Total time %d ms",
        bytes.length,
        decompressed.length,
        endTime - startTime);
    return decompressed;
  }

  interface Compressor {
    byte[] compress(byte[] bytes) throws IOException;
  }

  interface Decompressor {
    byte[] decompress(byte[] bytes) throws IOException;
  }

  @VisibleForTesting
  static final class GZIPCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) throws IOException {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
      try {
        ByteStreamsKt.copyTo(new ByteArrayInputStream(bytes), gzipOutputStream, bytes.length);
      } finally {
        if (gzipOutputStream != null) {
          gzipOutputStream.close();
        }
      }
      return byteArrayOutputStream.toByteArray();
    }
  }

  @VisibleForTesting
  static final class GZIPDecompressor implements Decompressor {
    @Override
    public byte[] decompress(byte[] bytes) throws IOException {
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
      GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
      try {
        return ByteStreamsKt.readBytes(gzipInputStream);
      } finally {
        if (gzipInputStream != null) {
          gzipInputStream.close();
        }
      }
    }
  }
}
