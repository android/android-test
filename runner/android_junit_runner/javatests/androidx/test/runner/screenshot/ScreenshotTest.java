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
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.View;
import android.view.Window;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Tests for {@link Screenshot} */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class ScreenshotTest {
  @Mock private UiAutomationWrapper uiAutomationWrapper;
  @Mock private Callable callable;
  @Mock private ScreenCaptureProcessor screenCaptureProcessor;
  @Mock private ScreenCaptureProcessor screenCaptureProcessorAdditional;
  @Mock private Activity activity;
  @Mock private Window window;
  @Mock private View view;

  @Rule public final ExpectedException expectedException = ExpectedException.none();

  private Bitmap stubBitmap = Bitmap.createBitmap(10, 10, ARGB_8888);
  private Bitmap stubBitmapLegacy = Bitmap.createBitmap(10, 10, ARGB_8888);

  private TakeScreenshotCallable.Factory stubCallableFactory =
      new TakeScreenshotCallable.Factory() {
        @Override
        Callable<Bitmap> create(View view) {
          return callable;
        }
      };

  @Before
  public void before() throws Exception {
    MockitoAnnotations.initMocks(this);
    initWithStubbedDecorView();

    Screenshot.setUiAutomationWrapper(uiAutomationWrapper);
    Screenshot.setTakeScreenshotCallableFactory(stubCallableFactory);
    Screenshot.setAndroidRuntimeVersion(Build.VERSION.SDK_INT);
    Screenshot.setScreenshotProcessors(new HashSet<ScreenCaptureProcessor>());

    doReturn(stubBitmap).when(uiAutomationWrapper).takeScreenshot();
    doReturn(stubBitmapLegacy).when(callable).call();

    doReturn(null).when(screenCaptureProcessor).process(any(ScreenCapture.class));
  }

  @Test
  public void captureScreenshotAbove18WithoutActivity_shouldCapture() throws Exception {
    Screenshot.setAndroidRuntimeVersion(18);
    ScreenCapture comparableScreenCapture = new ScreenCapture(stubBitmap);

    assertEquals(Screenshot.capture(), comparableScreenCapture);
    verify(uiAutomationWrapper).takeScreenshot();
    verify(callable, never()).call();
  }

  @Test
  public void captureScreenshotAbove18WithActivity_shouldCapture() throws Exception {
    Screenshot.setAndroidRuntimeVersion(18);
    ScreenCapture comparableScreenCapture = new ScreenCapture(stubBitmapLegacy);

    assertEquals(Screenshot.capture(activity), comparableScreenCapture);
    verify(uiAutomationWrapper, never()).takeScreenshot();
    verify(callable).call();
  }

  @Test
  public void captureScreenshotAbove18WithView_shouldCapture() throws Exception {
    Screenshot.setAndroidRuntimeVersion(18);
    ScreenCapture comparableScreenCapture = new ScreenCapture(stubBitmapLegacy);

    assertEquals(Screenshot.capture(view), comparableScreenCapture);
    verify(uiAutomationWrapper, never()).takeScreenshot();
    verify(callable).call();
  }

  @Test
  public void captureScreenshotBelow18WithActivity_shouldCapture() throws Exception {
    Screenshot.setAndroidRuntimeVersion(17);
    ScreenCapture comparableScreenCapture = new ScreenCapture(stubBitmapLegacy);

    assertEquals(Screenshot.capture(activity), comparableScreenCapture);
    verify(uiAutomationWrapper, never()).takeScreenshot();
    verify(callable).call();
  }

  @Test
  public void captureScreenshotBelow18WithView_shouldCapture() throws Exception {
    Screenshot.setAndroidRuntimeVersion(17);
    ScreenCapture comparableScreenCapture = new ScreenCapture(stubBitmapLegacy);

    assertEquals(Screenshot.capture(view), comparableScreenCapture);
    verify(uiAutomationWrapper, never()).takeScreenshot();
    verify(callable).call();
  }

  @Test
  public void captureScreenshotBelow18_WithoutActivityOrView_shouldThrowIllegalState()
      throws Exception {
    Screenshot.setAndroidRuntimeVersion(17);
    expectedException.expect(IllegalStateException.class);

    Screenshot.capture();
    verify(callable, never()).call();
    verify(uiAutomationWrapper, never()).takeScreenshot();
  }

  @Test
  public void capture_ShouldAddProcessor() throws Exception {
    Screenshot.setAndroidRuntimeVersion(18);
    Set screenshotProcessorSet = new HashSet<>();
    screenshotProcessorSet.add(screenCaptureProcessor);
    Screenshot.setScreenshotProcessors(screenshotProcessorSet);

    ScreenCapture capture = Screenshot.capture();
    assertTrue(capture.getProcessors() == screenshotProcessorSet);
  }

  @Test
  public void capture_ShouldAddMultipleProcessors() throws Exception {
    Screenshot.setAndroidRuntimeVersion(18);
    Set screenshotProcessorSet = new HashSet<>();
    screenshotProcessorSet.add(screenCaptureProcessor);
    screenshotProcessorSet.add(screenCaptureProcessorAdditional);
    Screenshot.setScreenshotProcessors(screenshotProcessorSet);

    ScreenCapture capture = Screenshot.capture();
    assertTrue(capture.getProcessors() == screenshotProcessorSet);
  }

  private void initWithStubbedDecorView() {
    doReturn(window).when(activity).getWindow();
    doReturn(view).when(window).getDecorView();
    doReturn(view).when(view).getRootView();
  }
}
