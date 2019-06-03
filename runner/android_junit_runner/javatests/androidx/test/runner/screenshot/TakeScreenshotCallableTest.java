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

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import android.graphics.Bitmap;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import java.util.concurrent.Callable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Tests for {@link TakeScreenshotCallable} */
@RunWith(AndroidJUnit4.class)
@MediumTest
public final class TakeScreenshotCallableTest {
  @Mock public View view;

  private TakeScreenshotCallable.Factory takeScreenshotCallableFactory =
      new TakeScreenshotCallable.Factory();
  private Callable callable;
  private Bitmap fakeBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);

  @Before
  public void before() throws Exception {
    MockitoAnnotations.initMocks(this);
    callable = takeScreenshotCallableFactory.create(view);
  }

  @Test
  public void call_shouldReturnBitmap() throws Exception {
    initWithStubbedDrawingCache();

    callable.call();
    verify(view).getDrawingCache();
  }

  private void initWithStubbedDrawingCache() {
    doNothing().when(view).setDrawingCacheEnabled(anyBoolean());
    doReturn(fakeBitmap).when(view).getDrawingCache();
  }
}
