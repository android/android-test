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
package androidx.test.internal.runner;

import androidx.test.filters.SmallTest;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Work around for lack of support for Parameterized tests in Google 3.
 *
 * <p>TODO(b/26110951) remove when super class can be Parameterized.
 */
@RunWith(JUnit4.class)
@SmallTest
public class AndroidLogOnlyBuilderScanningPathTest extends AndroidLogOnlyBuilderTest {

  public AndroidLogOnlyBuilderScanningPathTest() {
    super(true);
  }
}
