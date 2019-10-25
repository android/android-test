/*
 * Copyright (C) 2019 The Android Open Source Project
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
 */
package androidx.test.services.storage.provider;

import android.os.Environment;
import android.util.Log;
import androidx.test.services.storage.TestStorageConstants;
import java.io.File;

/** Hosts an SD Card directory for the test framework to read/write internal files to. */
public final class InternalUseOnlyFilesContentProvider extends AbstractFileContentProvider {
  private static final String TAG = "InternalUseOnlyFilesContentProvider";

  private final File outputDirectory;

  public InternalUseOnlyFilesContentProvider() {
    super(
        new File(
            Environment.getExternalStorageDirectory(),
            TestStorageConstants.ON_DEVICE_PATH_INTERNAL_USE),
        AbstractFileContentProvider.Access.READ_WRITE);
    outputDirectory =
        new File(
            Environment.getExternalStorageDirectory(),
            TestStorageConstants.ON_DEVICE_PATH_INTERNAL_USE);
  }

  @Override
  protected boolean onCreateHook() {
    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      Log.e(TAG, "sdcard in bad state: " + Environment.getExternalStorageState());
      return false;
    } else {
      if (!outputDirectory.exists()) {
        if (!outputDirectory.mkdirs()) {
          Log.e(TAG, String.format("'%s': could not create output dir! ", outputDirectory));
          return false;
        }
      }
      return true;
    }
  }
}
