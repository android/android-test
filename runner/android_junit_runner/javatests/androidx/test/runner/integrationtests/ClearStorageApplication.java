/*
 * Copyright (C) 2022 The Android Open Source Project
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
package androidx.test.runner.integrationtests;

import static androidx.test.internal.util.Checks.checkState;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.IOException;

/**
 * An application that creates a file, and fails if its already created.
 *
 * <p>Intended to be used with ClearStorageTest to verify application state is cleared.
 */
public class ClearStorageApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    createAndVerifyFile(this, "applicationoncreate.txt");
  }

  static void createAndVerifyFile(Context context, String fileName) {
    Log.i("ClearStorageApplication", "createAndVerifyFile: " + fileName);
    File file = new File(context.getFilesDir(), fileName);
    checkState(!file.exists(), "file not cleared!");

    try {
      file.createNewFile();
    } catch (IOException e) {
      throw new IllegalStateException("failed to create file", e);
    }

    checkState(file.exists(), "file does not exist after creation");
  }
}
