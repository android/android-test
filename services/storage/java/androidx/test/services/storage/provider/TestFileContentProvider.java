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
import androidx.test.services.storage.file.HostedFile;
import java.io.File;

/**
 * Content Provider that allows access to reading/writing files that were written to disk for tests.
 */
abstract class TestFileContentProvider extends AbstractFileContentProvider {

  protected abstract String getHostedRelativePath();

  @Override
  protected final File getHostedDirectory(Context context) {
    return new File(HostedFile.getOutputRootDirectory(context), getHostedRelativePath());
  }
}
