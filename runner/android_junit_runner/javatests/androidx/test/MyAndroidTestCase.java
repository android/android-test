/*
 * Copyright (C) 2012 The Android Open Source Project
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
package androidx.test;

import android.content.Context;
import android.test.AndroidTestCase;
import android.util.Log;
import androidx.test.filters.SmallTest;

/** Placeholder test to verify {@link Context} gets injected to {@link AndroidTestCase}. */
@SmallTest
public class MyAndroidTestCase extends AndroidTestCase {

  public MyAndroidTestCase() {
    Log.i("MyAndroidTestCase", "I'm created");
  }

  public void testContextSet() {
    assertNotNull(getContext());
  }
}
