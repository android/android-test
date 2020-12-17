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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents the I/O operations needed in the test runner.
 *
 * <p>Any runner implementation that requires file I/O should try to use this interface to interact
 * with the input/output data, instead of depending on the underlying implementations.
 */
public interface RunnerIO {

  /**
   * Returns an {@code InputStream} to the file represented by the given file path.
   *
   * @param pathname the file path to read from.
   * @throws FileNotFoundException when the file represented by the given file path cannot be found.
   */
  InputStream openInputStream(String pathname) throws FileNotFoundException;

  /**
   * Returns an {@code OutputStream} to the file represented by the given file path.
   *
   * @param pathname the file path to write to.
   * @throws FileNotFoundException when the file represented by the given file path cannot be found.
   */
  OutputStream openOutputStream(String pathname) throws FileNotFoundException;
}
