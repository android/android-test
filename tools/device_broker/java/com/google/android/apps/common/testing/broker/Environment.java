/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.apps.common.testing.broker;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Get data that is only pass through environment variables here.
 *
 * <p>Some runtime environment information is only ever provided through environment variables.
 *
 * <p>If that is the case, use this class to access the information.
 *
 * <p>For things that can be passed through commandline or environment or some other method, do not
 * add them here. For example, which device broker type to use is based off of both environment and
 * flags.
 */
public final class Environment {
  private final Optional<String> xDisplay;
  private final Optional<String> outputsDir;
  private final String tmpDir;
  private final String runfilesDir;
  private final String workspaceName;
  private final ImmutableMap<String, String> environmentMap;

  public Optional<String> getDisplay() {
    return xDisplay;
  }

  public String getRunfilesDir() {
    return runfilesDir;
  }

  public String getTmpDir() {
    return tmpDir;
  }

  public File createTempFile(String prefix, String suffix) throws IOException {
    return File.createTempFile(prefix, suffix, new File(tmpDir));
  }

  public File createTempFile(String prefix) throws IOException {
    return createTempFile(prefix, null);
  }

  public File createTempDir(String prefix) throws IOException {
    return Files.createTempDirectory(new File(tmpDir).toPath(), prefix).toFile();
  }

  public File getWorkspaceDir() {
    return new File(runfilesDir, workspaceName);
  }

  public Optional<File> getOutputsDir() {
    return outputsDir.transform(
        new Function<String, File>() {
          @Override
          public File apply(String path) {
            return new File(path);
          }
        });
  }

  public ImmutableMap<String, String> asMap() {
    return environmentMap;
  }

  private Environment(Builder b) {
    xDisplay = Optional.fromNullable(b.xDisplay);
    outputsDir = Optional.fromNullable(b.outputsDir);
    tmpDir = checkNotNull(b.tmpDir);
    runfilesDir = checkNotNull(b.runfilesDir);
    workspaceName = checkNotNull(b.workspaceName);
    environmentMap = ImmutableMap.copyOf(b.environmentMap);
  }

  /** {@link Environment} Builder */
  public static class Builder {
    private static final String DISPLAY = "DISPLAY";
    private static final String OUTPUTS = "TEST_UNDECLARED_OUTPUTS_DIR";
    private static final String TEST_TMPDIR = "TEST_TMPDIR";
    private static final String TMPDIR = "TMPDIR";
    private static final String TEST_SRCDIR = "TEST_SRCDIR";
    private static final String DEVICE_RUNFILES = "DEVICE_RUNFILES";
    private static final String TEST_WORKSPACE = "TEST_WORKSPACE";

    @Nullable private String xDisplay;
    @Nullable private String outputsDir;
    private String tmpDir;
    private String runfilesDir;
    private String workspaceName;
    private Map<String, String> environmentMap = new HashMap<>();

    public Environment build() {
      return new Environment(this);
    }

    public Builder fromMap(Map<String, String> environment) {
      environmentMap.putAll(environment);
      String tmpDir = environment.get(TEST_TMPDIR);
      if (null == tmpDir) {
        tmpDir = environment.get(TMPDIR);
        if (null == tmpDir) {
          tmpDir = "/tmp"; // jerks.
        }
      }

      return withXDisplay(environment.get(DISPLAY))
          .withOutputsDir(environment.get(OUTPUTS))
          .withTmpDir(tmpDir)
          .withWorkspaceName(
              environment.containsKey(TEST_WORKSPACE) ? environment.get(TEST_WORKSPACE) : "android_test_support")
          .withRunfilesDir(
              environment.containsKey(TEST_SRCDIR)
                  ? environment.get(TEST_SRCDIR)
                  : environment.get(DEVICE_RUNFILES));
    }

    public Builder withWorkspaceName(String name) {
      this.workspaceName = name;
      environmentMap.put(TEST_WORKSPACE, name);
      return this;
    }

    public Builder withXDisplay(String xDisplay) {
      this.xDisplay = xDisplay;
      if (xDisplay != null) {
        environmentMap.put(DISPLAY, xDisplay);
      }
      return this;
    }

    public Builder withOutputsDir(String outputsDir) {
      this.outputsDir = outputsDir;
      if (outputsDir != null) {
        environmentMap.put(OUTPUTS, outputsDir);
      }
      return this;
    }

    public Builder withTmpDir(String tmpDir) {
      this.tmpDir = checkNotNull(tmpDir);
      environmentMap.put(TEST_TMPDIR, tmpDir);
      return this;
    }

    public Builder withRunfilesDir(String runfilesDir) {
      this.runfilesDir = checkNotNull(runfilesDir);
      environmentMap.put(TEST_SRCDIR, runfilesDir);
      return this;
    }
  }
}
