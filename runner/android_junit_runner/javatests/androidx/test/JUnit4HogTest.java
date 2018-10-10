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

package androidx.test;
/*
 * Copyright (C) 2014 The Android Open Source Project
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

import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A set of JUnit4 tests that allocates substantial memory into a member variable.
 *
 * <p>Intended to ensure test objects references are not retained by runner, and can get garbage
 * collected.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class JUnit4HogTest {

  @SuppressWarnings("unused")
  private byte[] mByteBuffer;

  @Before
  public void setUp() {
    mByteBuffer = new byte[20 * 1024 * 1024];
  }

  // have 10 sample tests - means 200MB total mem if mByteBuffer not freed

  @Test
  public void test1() {}

  @Test
  public void test2() {}

  @Test
  public void test3() {}

  @Test
  public void test4() {}

  @Test
  public void test5() {}

  @Test
  public void test6() {}

  @Test
  public void test7() {}

  @Test
  public void test8() {}

  @Test
  public void test9() {}
}
