/*
 * Copyright 2023 The Android Open Source Project
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
package androidx.test.ext.junit.rules;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import androidx.test.core.app.ApplicationProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Deletes android storage directories between tests.
 *
 * <p>Before a test, this rule recursively scans through all directories accessible to the app and
 * records all existing files. Then after the test, it recursively scans through those same
 * directories and deletes any files that were not present to begin with.
 *
 * <p>All contents of any directory whose name exactly matches one of {@link #excludedDirectories}
 * will be omitted from the deletion process.
 */
public final class DeleteFilesRule implements TestRule {

  public DeleteFilesRule() {
    this(new HashSet<>());
  }

  public DeleteFilesRule(Set<String> excludedDirectories) {
    this.excludedDirectories = excludedDirectories;
  }

  private final Set<String> excludedDirectories;

  @Override
  public Statement apply(final Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        Context context = ApplicationProvider.getApplicationContext();

        List<File> directories = new ArrayList<>();
        directories.add(new File(context.getApplicationInfo().dataDir));
        directories.add(Environment.getExternalStorageDirectory());
        directories.add(Environment.getDownloadCacheDirectory());
        if (context.getExternalCacheDir() != null) {
          directories.add(context.getExternalCacheDir());
        }
        if (Build.VERSION.SDK_INT >= 21) {
          directories.add(context.getNoBackupFilesDir());
        }
        if (Build.VERSION.SDK_INT >= 24) {
          directories.add(new File(context.getApplicationInfo().deviceProtectedDataDir));
        }

        Set<File> existingFiles = new HashSet<>();
        for (File directory : directories) {
          findFilesRecursively(existingFiles, directory);
        }
        try {
          base.evaluate();
        } finally {
          for (File directory : directories) {
            deleteFilesRecursively(existingFiles, directory);
          }
        }
      }
    };
  }

  private static boolean isConstant(File file) {
    return file.getName().endsWith(".dex");
  }

  private static void findFilesRecursively(Set<File> existingFiles, File directory) {
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        existingFiles.add(file);
        if (file.isDirectory()) {
          findFilesRecursively(existingFiles, file);
        }
      }
    }
  }

  private void deleteFilesRecursively(Set<File> existingFiles, File directory) {
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          if (excludedDirectories.contains(file.getName())) {
            continue;
          }
          deleteFilesRecursively(existingFiles, file);
          if (!existingFiles.contains(file) && file.exists()) {
            File[] filesInDirectory = file.listFiles();
            if (filesInDirectory == null && !file.delete()) {
              System.err.println(
                  "DeleteRules failed to delete (not a directory or I/O error): " + file);
            } else if (filesInDirectory.length == 0 && !file.delete()) {
              System.err.println("DeleteRules failed to delete: " + file);
            }
          }
        } else {
          if (!existingFiles.contains(file) && file.exists() && !isConstant(file)) {
            if (!file.delete()) {
              System.err.println("DeleteRules failed to delete: " + file);
            }
          }
        }
      }
    }
  }
}
