/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.apps.common.testing.broker;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.Closeables.close;
import static com.google.common.io.Closeables.closeQuietly;

import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.TestTempDir;
import com.google.common.io.ByteStreams;
import com.google.inject.Provider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.inject.Inject;

/**
 * A temporary work around to load resources before each test.
 *
 * TODO: This should be moved into bazel as an implicit dependency of all tests and
 * automatically included in the apks_to_install flag.
 */
class ResourceProvider implements Provider<String> {
  private final Provider<String> resourcePathProvider;

  @Inject
  @TestTempDir private File tempDir;
  private String resourceOnFileSystemLocation;

  ResourceProvider(File tempDir, Provider<String> resourcePathProvider) {
    this.tempDir = tempDir;
    this.resourcePathProvider = resourcePathProvider;
  }

  ResourceProvider(Provider<String> resourcePathProvider) {
    this(null, checkNotNull(resourcePathProvider));
  }

  @Override
  public synchronized String get() {
    if (null == resourceOnFileSystemLocation) {
      String resourcePath = resourcePathProvider.get();
      File resourceInFs = new File(tempDir, resourcePath);
      if (!resourceInFs.exists()) {
        resourceInFs.getParentFile().mkdirs();
        InputStream in = null;
        OutputStream out = null;
        try {
          in = getClass().getResourceAsStream(resourcePath);
          out = new FileOutputStream(resourceInFs);
          ByteStreams.copy(in, out);
        } catch (IOException ioe) {
          throw new RuntimeException(ioe);
        } finally {
          closeQuietly(in);
          try {
            close(out, true);
          } catch (IOException e) {
            // Exception logged as Level.WARNING by close()
          }
        }
      }
      resourceInFs.setExecutable(true);
      resourceOnFileSystemLocation = resourceInFs.getPath();
    }
    return resourceOnFileSystemLocation;
  }
}
