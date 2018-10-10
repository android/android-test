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

package androidx.test.internal.runner;

import static androidx.test.InstrumentationRegistry.getInstrumentation;

import android.os.Bundle;
import androidx.test.filters.SmallTest;
import androidx.test.internal.runner.listener.InstrumentationRunListener;
import androidx.test.runner.AndroidJUnit4;
import java.io.PrintStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Unit tests for TestExecutor */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class TestExecutorTest {

  @Mock private Request mMockRequest;
  @Mock private InstrumentationRunListener mMockListener;

  private TestExecutor mExecutor;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    mExecutor =
        new TestExecutor.Builder(getInstrumentation()).addRunListener(mMockListener).build();
  }

  /** Simple normal case execution */
  @Test
  public void testExecute() {
    mExecutor.execute(mMockRequest);
    Mockito.verify(mMockListener)
        .instrumentationRunFinished(
            (PrintStream) Mockito.anyObject(),
            (Bundle) Mockito.anyObject(),
            (Result) Mockito.anyObject());
  }
}
