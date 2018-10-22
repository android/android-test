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
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
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

  private Bitmap stubBitmap = Bitmap.createBitmap(10, 10, ARGB_8888);
  private Bitmap stubBitmapDifferent = Bitmap.createBitmap(20, 20, ARGB_8888);
  private ScreenCapture capture;

  @Mock ScreenCaptureProcessor basicProcessor;
  @Mock ScreenCaptureProcessor globalProcessor;
  @Mock ScreenCaptureProcessor passedProcessor;

  @Before
  public void before() throws Exception {
    MockitoAnnotations.initMocks(this);
    capture = new ScreenCapture(stubBitmap, basicProcessor);
  }

  @Test
  public void process_shouldCallBasicProcessorWhenNonePresent() throws Exception {
    doReturn(null).when(basicProcessor).process(eq(capture));

    capture.process();

    verify(basicProcessor).process(eq(capture));
  }

  @Test
  public void process_shouldCallSetOfGlobalProcessorsNotBasicProcessor() throws Exception {
    // These are the processors that are contained in the global Set (set before process is run)
    doReturn(null).when(globalProcessor).process(eq(capture));
    Set<ScreenCaptureProcessor> processorSet = new HashSet<>();
    processorSet.add(globalProcessor);
    capture.setProcessors(processorSet);

    capture.process();

    verify(globalProcessor).process(eq(capture));
    verify(basicProcessor, never()).process(eq(capture));
  }

  @Test
  public void process_shouldCallGivenSetOfProcessorsInsteadOfBasic() throws Exception {
    // These are the processors that are passes to the process method and override the globally
    // set processors.
    doReturn(null).when(passedProcessor).process(eq(capture));
    Set<ScreenCaptureProcessor> processorSet = new HashSet<>();
    processorSet.add(passedProcessor);

    capture.process(processorSet);

    verify(passedProcessor).process(eq(capture));
    verify(basicProcessor, never()).process(eq(capture));
    verify(globalProcessor, never()).process(eq(capture));
  }

  @Test
  public void process_shouldCallGivenSetOfProcessorsInsteadOfGlobalSet() throws Exception {
    doReturn(null).when(globalProcessor).process(eq(capture));
    doReturn(null).when(passedProcessor).process(eq(capture));
    Set<ScreenCaptureProcessor> processorSetGlobal = new HashSet<>();
    processorSetGlobal.add(globalProcessor);
    capture.setProcessors(processorSetGlobal);
    Set<ScreenCaptureProcessor> processorSetPassed = new HashSet<>();
    processorSetPassed.add(passedProcessor);

    capture.process(processorSetPassed);

    verify(passedProcessor).process(eq(capture));
    verify(basicProcessor, never()).process(eq(capture));
    verify(globalProcessor, never()).process(eq(capture));
  }

  @Test
  public void equalScreenCapturesShouldBeEqualAndHaveSameHashcode() throws Exception {
    ScreenCapture capture2 = new ScreenCapture(stubBitmap);

    assertTrue(capture.equals(capture2));
    assertTrue(capture.hashCode() == capture2.hashCode());
  }

  @Test
  public void nonEqualScreenCapturesShouldNotBeEqual() throws Exception {
    ScreenCapture capture2 = new ScreenCapture(stubBitmapDifferent);

    assertFalse(capture.equals(capture2));
    assertFalse(capture.hashCode() == capture2.hashCode());
  }
}
