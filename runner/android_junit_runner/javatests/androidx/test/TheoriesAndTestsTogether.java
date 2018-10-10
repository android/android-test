/*
 * Copyright (C) 2015 The Android Open Source Project
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

package androidx.test;

import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

import androidx.test.filters.SmallTest;
import androidx.test.filters.Suppress;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@Suppress // b/26110951
@RunWith(Theories.class)
@SmallTest
public class TheoriesAndTestsTogether {

  @DataPoint public static String GOOD_USERNAME = "Optimus";
  @DataPoint public static String GOOD_USERNAME_2 = "Bumblebee";
  @DataPoint public static String USERNAME_WITH_SLASH = "Optimus/prime";

  @Theory
  public void commandIncludesUsername(String username) {
    assumeThat(username, not(containsString("/")));
    assertThat(new User(username).configCommand(), containsString(username));
  }

  @Test
  public void noTheoryAnnotationMeansAssumeShouldIgnore() {
    Assume.assumeTrue(false);
    fail();
  }

  private static class User {
    String mName;

    public User(String mName) {
      this.mName = mName;
    }

    public String configCommand() {
      return mName + "roll out!";
    }
  }
}
