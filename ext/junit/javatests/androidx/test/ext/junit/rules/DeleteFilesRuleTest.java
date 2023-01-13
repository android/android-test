/*
 * Copyright 2018 The Android Open Source Project
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

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;

/** Test cases for {@link DeleteFilesRule}. */
@RunWith(AndroidJUnit4.class)
public final class DeleteFilesRuleTest {
  @Rule
  public RuleChain chain = RuleChain.outerRule(EXISTING_FILE_RULE).around(new DeleteFilesRule());

  private static final TestRule EXISTING_FILE_RULE =
      (base, description) ->
          new Statement() {
            @Override
            public void evaluate() throws Throwable {
              getApplicationContext()
                  .openOrCreateDatabase("existing.db", Context.MODE_PRIVATE, null);
              File existingDb = getApplicationContext().getDatabasePath("existing.db");
              File existingFile = new File(getFilesDir(), "existingFile.txt");
              File existingDir = new File(getFilesDir(), "existingDir");
              try {
                existingFile.createNewFile();
                existingDir.mkdir();
                base.evaluate();
              } finally {
                assertThat(existingDb.exists()).isTrue();
                assertThat(existingDb.delete()).isTrue();
                assertThat(existingFile.exists()).isTrue();
                assertThat(existingFile.delete()).isTrue();
                assertThat(existingDir.exists()).isTrue();
                assertThat(existingDir.delete()).isTrue();
              }
            }
          };

  @Test
  public void testDeleteFilesRule1() throws Exception {
    checkDatabaseDoesNotAlreadyExist();
    checkFileDoesNotAlreadyExist();
  }

  @Test
  public void testDeleteFilesRule2() throws Exception {
    checkDatabaseDoesNotAlreadyExist();
    checkFileDoesNotAlreadyExist();
  }

  private void checkDatabaseDoesNotAlreadyExist() {
    String name = "cool.db";
    Context context = getApplicationContext();

    // Create a new database and make sure that it did not already exist.
    assertThat(context.getDatabasePath(name).exists()).isFalse();
    context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null);
    assertThat(context.getDatabasePath(name).exists()).isTrue();
  }

  private void checkFileDoesNotAlreadyExist() throws Exception {
    File filesDir = getFilesDir();

    // Create a new file and make sure that it did not already exist
    assertThat(new File(filesDir, "deletemeorforeverbeflaky.txt").createNewFile()).isTrue();

    // Create a new file in a subdirectory and make sure that it did not already exist
    File subdirectory = new File(filesDir, "wehavefiles");
    assertThat(subdirectory.mkdir()).isTrue();
    assertThat(new File(subdirectory, "wehavethebestfiles.txt").createNewFile()).isTrue();
  }

  private static File getFilesDir() {
    return getApplicationContext().getFilesDir();
  }
}
