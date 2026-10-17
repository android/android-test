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

import android.content.Context;
import androidx.test.services.storage.TestStorageConstants;
import androidx.test.services.storage.file.HostedFile;
import java.io.File;

/** Hosts an SD Card directory for the test framework to read/write internal files to. */
public final class InternalUseOnlyFilesContentProvider extends AbstractFileContentProvider {

  @Override
  protected File getHostedDirectory(Context context) {
    // Uses the output root directory since the provider is Read/Write and only the output directory
    // is guaranteed to be writable.
    return new File(
        HostedFile.getOutputRootDirectory(context),
        TestStorageConstants.ON_DEVICE_PATH_INTERNAL_USE);
  }

  @Override
  protected Access getAccess() {
    return AbstractFileContentProvider.Access.READ_WRITE;
  }
}
