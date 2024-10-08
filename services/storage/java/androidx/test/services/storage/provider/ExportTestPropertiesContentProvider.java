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

import androidx.test.services.storage.TestStorageConstants;

/** Hosts the properties file that are exported to the testing infrastructure for tests. */
public final class ExportTestPropertiesContentProvider extends TestFileContentProvider {

  @Override
  protected Access getAccess() {
    return AbstractFileContentProvider.Access.READ_WRITE;
  }

  @Override
  protected String getHostedRelativePath() {
    return TestStorageConstants.ON_DEVICE_PATH_TEST_PROPERTIES;
  }
}
