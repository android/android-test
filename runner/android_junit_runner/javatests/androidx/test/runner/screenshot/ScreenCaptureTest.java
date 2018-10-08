/*
 * Copyright (C) 2016 The Android Open Source Project
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

package androidx.test.runner.screenshot;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.graphics.Bitmap;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Tests for {@link Screenshot} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class ScreenCaptureTest {

  private Bitmap mStubBitmap = Bitmap.createBitmap(10, 10, ARGB_8888);
  private Bitmap mStubBitmapDifferent = Bitmap.createBitmap(20, 20, ARGB_8888);
  private ScreenCapture mCapture;

  @Mock ScreenCaptureProcessor mBasicProcessor;
  @Mock ScreenCaptureProcessor mGlobalProcessor;
  @Mock ScreenCaptureProcessor mPassedProcessor;

  @Before
  public void before() throws Exception {
    MockitoAnnotations.initMocks(this);
    mCapture = new ScreenCapture(mStubBitmap, mBasicProcessor);
  }

  @Test
  public void process_shouldCallBasicProcessorWhenNonePresent() throws Exception {
    doReturn(null).when(mBasicProcessor).process(eq(mCapture));

    mCapture.process();

    verify(mBasicProcessor).process(eq(mCapture));
  }

  @Test
  public void process_shouldCallSetOfGlobalProcessorsNotBasicProcessor() throws Exception {
    // These are the processors that are contained in the global Set (set before process is run)
    doReturn(null).when(mGlobalProcessor).process(eq(mCapture));
    Set<ScreenCaptureProcessor> processorSet = new HashSet<>();
    processorSet.add(mGlobalProcessor);
    mCapture.setProcessors(processorSet);

    mCapture.process();

    verify(mGlobalProcessor).process(eq(mCapture));
    verify(mBasicProcessor, never()).process(eq(mCapture));
  }

  @Test
  public void process_shouldCallGivenSetOfProcessorsInsteadOfBasic() throws Exception {
    // These are the processors that are passes to the process method and override the globally
    // set processors.
    doReturn(null).when(mPassedProcessor).process(eq(mCapture));
    Set<ScreenCaptureProcessor> processorSet = new HashSet<>();
    processorSet.add(mPassedProcessor);

    mCapture.process(processorSet);

    verify(mPassedProcessor).process(eq(mCapture));
    verify(mBasicProcessor, never()).process(eq(mCapture));
    verify(mGlobalProcessor, never()).process(eq(mCapture));
  }

  @Test
  public void process_shouldCallGivenSetOfProcessorsInsteadOfGlobalSet() throws Exception {
    doReturn(null).when(mGlobalProcessor).process(eq(mCapture));
    doReturn(null).when(mPassedProcessor).process(eq(mCapture));
    Set<ScreenCaptureProcessor> processorSetGlobal = new HashSet<>();
    processorSetGlobal.add(mGlobalProcessor);
    mCapture.setProcessors(processorSetGlobal);
    Set<ScreenCaptureProcessor> processorSetPassed = new HashSet<>();
    processorSetPassed.add(mPassedProcessor);

    mCapture.process(processorSetPassed);

    verify(mPassedProcessor).process(eq(mCapture));
    verify(mBasicProcessor, never()).process(eq(mCapture));
    verify(mGlobalProcessor, never()).process(eq(mCapture));
  }

  @Test
  public void equalScreenCapturesShouldBeEqualAndHaveSameHashcode() throws Exception {
    ScreenCapture capture2 = new ScreenCapture(mStubBitmap);

    assertTrue(mCapture.equals(capture2));
    assertTrue(mCapture.hashCode() == capture2.hashCode());
  }

  @Test
  public void nonEqualScreenCapturesShouldNotBeEqual() throws Exception {
    ScreenCapture capture2 = new ScreenCapture(mStubBitmapDifferent);

    assertFalse(mCapture.equals(capture2));
    assertFalse(mCapture.hashCode() == capture2.hashCode());
  }
}
