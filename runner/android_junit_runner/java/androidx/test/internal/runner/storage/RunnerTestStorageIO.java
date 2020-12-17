/*
 * Copyright (C) 2020 The Android Open Source Project
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
package androidx.test.internal.runner.storage;

import androidx.test.services.storage.internal.InternalTestStorage;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

/** A class that manages the runner input/output using the AndroidX test storage service. */
public final class RunnerTestStorageIO implements RunnerIO {

  private final InternalTestStorage testStorage;

  public RunnerTestStorageIO() {
    testStorage = new InternalTestStorage();
  }

  @Override
  public InputStream openInputStream(String pathname) throws FileNotFoundException {
    return testStorage.openInternalInputStream(pathname);
  }

  @Override
  public OutputStream openOutputStream(String pathname) throws FileNotFoundException {
    return testStorage.openInternalOutputStream(pathname);
  }
}
