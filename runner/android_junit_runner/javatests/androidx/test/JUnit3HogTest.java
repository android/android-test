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
import junit.framework.TestCase;

/**
 * A set of JUnit3 tests that allocates substantial memory into a member variable.
 *
 * <p>Intended to ensure test objects references are not retained by runner (unlike upstream
 * junit3), and can get garbage collected.
 */
@SmallTest
public class JUnit3HogTest extends TestCase {

  @SuppressWarnings("unused")
  private byte[] mByteBuffer;

  @Override
  public void setUp() {
    mByteBuffer = new byte[19 * 1024 * 1024];
  }

  // have 10 sample tests - means 190MB total mem if mByteBuffer not freed

  public void test1() {}

  public void test2() {}

  public void test3() {}

  public void test4() {}

  public void test5() {}

  public void test6() {}

  public void test7() {}

  public void test8() {}

  public void test9() {}

  public void test10() {}
}
