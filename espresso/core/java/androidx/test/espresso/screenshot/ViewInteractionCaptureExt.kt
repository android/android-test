/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:JvmName("ViewInteractionCapture")

package androidx.test.espresso.screenshot

import android.graphics.Bitmap
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.test.annotation.ExperimentalTestApi
import androidx.test.core.view.captureToBitmap
import androidx.test.espresso.EspressoException
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import com.google.common.util.concurrent.SettableFuture
import java.util.Locale
import java.util.concurrent.ExecutionException
import org.hamcrest.Matcher
import org.hamcrest.Matchers.any

/**
 * Captures an image of the matching view into a {@link Bitmap}.
 *
 * For devices below {@link Build.VERSION_CODES#O} (or if the view's window cannot be determined),
 * the image is obtained using {@link View#draw}. Otherwise, {@link PixelCopy} is used.
 *
 * This method will also enable {@link HardwareRendererCompat#setDrawingEnabled(boolean)} if
 * required.
 *
 * This API is currently experimental and subject to change or removal.
 */
@ExperimentalTestApi
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
fun ViewInteraction.captureToBitmap(): Bitmap {
  var bitmapFuture = SettableFuture.create<Bitmap>()

  perform(ImageCaptureViewAction(bitmapFuture))
  try {
    return bitmapFuture[
      IdlingPolicies.getMasterIdlingPolicy().idleTimeout,
      IdlingPolicies.getMasterIdlingPolicy().idleTimeoutUnit]
  } catch (e: InterruptedException) {
    throw CaptureImageException("failed to capture image", e)
  } catch (e: ExecutionException) {
    throw CaptureImageException("failed to capture image", e)
  } catch (e: java.util.concurrent.TimeoutException) {
    throw CaptureImageException("failed to capture image", e)
  }
}

private class CaptureImageException internal constructor(message: String?, e: Exception?) :
  RuntimeException(message, e), EspressoException

private class ImageCaptureViewAction
internal constructor(private val bitmapFuture: SettableFuture<Bitmap>) : ViewAction {
  override fun getConstraints(): Matcher<View> {
    return any(View::class.java)
  }

  override fun getDescription(): String {
    return String.format(Locale.ROOT, "capture view to image")
  }

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
  override fun perform(uiController: UiController, view: View) {
    uiController.loopMainThreadUntilIdle()
    bitmapFuture.setFuture(view.captureToBitmap())
  }
}
