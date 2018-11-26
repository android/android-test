/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Lice`nse is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.ui.app.provider;

import static com.google.common.base.Preconditions.checkNotNull;

import android.Manifest.permission;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.ParcelFileDescriptor;
import androidx.annotation.NonNull;
import android.util.Log;
import android.webkit.MimeTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A ContentProvider to provide read/write access to files in a hosted directory.
 */
public class SimpleFileContentProvider extends ContentProvider {

  private static final String TAG = "SimpleFileCP";
  private final File hostedDirectory;

  SimpleFileContentProvider(File hostedDirectory) {
    try {
      this.hostedDirectory = checkNotNull(hostedDirectory).getCanonicalFile();
    } catch (IOException ioe) {
      Log.e(TAG, "Error getting canonical form of hosted directory " + hostedDirectory);
      throw new RuntimeException(ioe);
    }
  }

  @Override
  public boolean onCreate() {
    if (!hostedDirectory.exists() && !hostedDirectory.mkdirs()) {
      Log.e(TAG, String.format("Fail to create hosted directory %s", hostedDirectory));
      return false;
    }
    if (!hostedDirectory.isDirectory()) {
      Log.e(TAG, String.format("Hosted directory %s is not a directory", hostedDirectory));
      return false;
    }
    if (!hostedDirectory.canWrite()) {
      Log.e(TAG, String.format("Hosted directory %s is not writable", hostedDirectory));
      return false;
    }
    return true;
  }

  @Override
  public String getType(@NonNull Uri uri) {
    checkNotNull(uri);
    String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
    MimeTypeMap map = MimeTypeMap.getSingleton();
    return map.getMimeTypeFromExtension(extension);
  }

  @Override
  public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode)
      throws FileNotFoundException {
    checkNotNull(uri);
    checkNotNull(mode);
    String lowerMode = mode.toLowerCase();
    boolean withWriteAccess = lowerMode.contains("w") || lowerMode.contains("t");
    if (withWriteAccess) {
      // Requires caller has WRITE_EXTERNAL_STORAGE permission for "w" or "t"
      String requiredPermission = permission.WRITE_EXTERNAL_STORAGE;
      if (checkNotNull(getContext()).checkCallingPermission(requiredPermission)
          == PackageManager.PERMISSION_DENIED) {
        throw new SecurityException(
            String.format("Caller does not have permission %s to call openFile with mode %s!",
                requiredPermission, mode));
      }
    }
    File requestedFile = getFileFromUri(uri);
    // ParcelFileDescriptor.open won't create parent directory when necessary
    File parentDir = requestedFile.getParentFile();
    if (withWriteAccess && !parentDir.exists() && !parentDir.mkdirs()) {
      throw new FileNotFoundException(
          String.format("error happened creating parent dir for file %s", requestedFile));
    }

    return ParcelFileDescriptor.open(requestedFile, parseMode(mode));
  }

  @Override
  public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Cursor query(@NonNull Uri uri, String[] strings, String s, String[] strings1, String s1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int update(@NonNull Uri uri, ContentValues contentValues, String s, String[] strings) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int delete(@NonNull Uri uri, String s, String[] strings) {
    throw new UnsupportedOperationException();
  }

  private File getFileFromUri(Uri uri) throws FileNotFoundException {
    File requestedFile = null;
    try {
      requestedFile = new File(hostedDirectory, uri.getPath()).getCanonicalFile();
    } catch (IOException ioe) {
      throw new FileNotFoundException(
          String.format("Error getting file from uri %s, exception: %s", uri, ioe.getMessage()));
    }

    File checkFile = requestedFile.getAbsoluteFile();
    while (null != checkFile) {
      if (checkFile.equals(hostedDirectory)) {
        return requestedFile;
      }
      checkFile = checkFile.getParentFile();
    }

    throw new SecurityException(
        String.format("Uri %s refers to a file not managed by this provider", uri));
  }

  private static int parseMode(String mode) {
    if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
      return ParcelFileDescriptor.parseMode(mode);
    } else {
      final int modeBits;
      if ("r".equals(mode)) {
        modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
      } else if ("w".equals(mode) || "wt".equals(mode)) {
        modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
            | ParcelFileDescriptor.MODE_CREATE
            | ParcelFileDescriptor.MODE_TRUNCATE;
      } else if ("wa".equals(mode)) {
        modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
            | ParcelFileDescriptor.MODE_CREATE
            | ParcelFileDescriptor.MODE_APPEND;
      } else if ("rw".equals(mode)) {
        modeBits = ParcelFileDescriptor.MODE_READ_WRITE
            | ParcelFileDescriptor.MODE_CREATE;
      } else if ("rwt".equals(mode)) {
        modeBits = ParcelFileDescriptor.MODE_READ_WRITE
            | ParcelFileDescriptor.MODE_CREATE
            | ParcelFileDescriptor.MODE_TRUNCATE;
      } else {
        throw new IllegalArgumentException("Bad mode '" + mode + "'");
      }
      return modeBits;
    }
  }
}
