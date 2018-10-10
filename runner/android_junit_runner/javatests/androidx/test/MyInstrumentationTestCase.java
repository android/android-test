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

import android.app.Instrumentation;
import android.test.InstrumentationTestCase;
import android.util.Log;
import androidx.test.filters.SmallTest;

/**
 * Placeholder test to verify {@link Instrumentation} gets injected to {@link
 * InstrumentationTestCase}.
 */
@SmallTest
public class MyInstrumentationTestCase extends InstrumentationTestCase {

  public MyInstrumentationTestCase() {
    Log.i("MyInstrumentationTestCase", "I'm created");
  }

  public void testInstrumentationInjected() {
    assertNotNull("instrumentation was not injected", getInstrumentation());
  }
}
