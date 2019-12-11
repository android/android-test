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

/** Provides access to files in the test data section. */
public final class TestDataContentProvider extends AbstractFileContentProvider {
  private static final String TAG = TestDataContentProvider.class.getSimpleName();

  public TestDataContentProvider() {
    super(
        new File(
            Environment.getExternalStorageDirectory(),
            TestStorageConstants.ON_DEVICE_TEST_RUNFILES),
        AbstractFileContentProvider.Access.READ_ONLY);
  }

  @Override
  protected boolean onCreateHook() {
    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      Log.e(TAG, "sdcard in bad state: " + Environment.getExternalStorageState());
      return false;
    } else {
      return true;
    }
  }
}
