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

package androidx.test.runner;

import static org.junit.Assert.assertNotNull;

import android.os.Bundle;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests that verify {@link AndroidJUnit4} can delegate to the Robolectric runner. */
@RunWith(AndroidJUnit4.class)
public class AndroidJUnit4RoboTest {

  /** Verifies a basic empty test. */
  @Test
  public void basic() {}

  /**
   * The first 'real' test. Verify Android code can be referenced.
   *
   * <p>This should test that robo class loader environment is correctly initialized. If that
   * doesn't happen, this test should fail with a 'Stub!' exception.
   */
  @Test
  public void androidReferences() {
    assertNotNull(new Bundle());
  }
}
