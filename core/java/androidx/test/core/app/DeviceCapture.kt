/*
 * Copyright (C) 2021 The Android Open Source Project
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
@file:JvmName("DeviceCapture")

package androidx.test.core.app

import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Choreographer
import androidx.annotation.RequiresApi
import androidx.annotation.RestrictTo
import androidx.concurrent.futures.ResolvableFuture
import androidx.test.annotation.ExperimentalTestApi
import androidx.test.core.internal.os.HandlerExecutor
import androidx.test.core.view.forceRedraw
import androidx.test.internal.util.Checks
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.platform.graphics.HardwareRendererCompat
import androidx.test.platform.view.inspector.WindowInspectorCompat
import com.google.common.util.concurrent.ListenableFuture
import java.lang.RuntimeException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Captures an image of the device's screen into a [Bitmap].
 *
 * This is essentially a wrapper for [UIAutomation#takeScreenshot()] that attempts to get a stable
 * screenshot by forcing all the current application's root window views to redraw, and also handles
 * cases where hardware renderer drawing is disabled.
 *
 * This API is intended for use cases like debugging where an image of the entire screen is needed.
 * For use cases where the image will be used for validation, its recommended to take a more
 * isolated, targeted screenshot of a specific view or compose node. See
 * [androidx.test.core.view.captureToBitmap], [androidx.test.espresso.screenshot.captureToBitmap]
 * and [androidx.compose.ui.test.captureToImage].
 *
 * This API does not support concurrent usage.
 *
 * This API is currently experimental and subject to change or removal.
 *
 * @return a [Bitmap] that contains the image
 * @throws [IllegalStateException] if called on the main thread. This is a limitation of connecting
 * to UiAutomation, [RuntimeException] if UiAutomation fails to take the screenshot
 */
@ExperimentalTestApi
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
@Suppress("FutureReturnValueIgnored")
@Throws(RuntimeException::class)
fun takeScreenshot(): Bitmap {
  getInstrumentation().waitForIdleSync()
  return takeScreenshotNoSync()
}

/**
 * An internal variant of [takeScreenshot] that skips an idle sync call.
 *
 * This intended for failure handling cases where caller does not want to wait for main thread to be
 * idle.
 *
 * @return a [Bitmap]
 * @throws [IllegalStateException] if called on the main thread. This is a limitation of connecting
 * to UiAutomation, [RuntimeException] if UiAutomation fails to take the screenshot
 *
 * @hide
 */
@ExperimentalTestApi
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
@Suppress("FutureReturnValueIgnored")
@Throws(RuntimeException::class)
fun takeScreenshotNoSync(): Bitmap {
  Checks.checkNotMainThread()

  val bitmapFuture: ResolvableFuture<Bitmap> = ResolvableFuture.create()
  val mainExecutor = HandlerExecutor(Handler(Looper.getMainLooper()))
  val uiAutomation = getInstrumentation().uiAutomation
  if (uiAutomation == null) {
    throw RuntimeException("uiautomation is null")
  }

  if (!HardwareRendererCompat.isDrawingEnabled()) {
    HardwareRendererCompat.setDrawingEnabled(true)
    bitmapFuture.addListener({ HardwareRendererCompat.setDrawingEnabled(false) }, mainExecutor)
  }

  try {
    forceRedrawGlobalWindowViews(mainExecutor).get(5, TimeUnit.SECONDS)
  } catch (e: Exception) {
    Log.w("takeScreenshot", "force redraw failed. Proceeding with screenshot", e)
  }

  // take the screenshot on the next frame to increase probability the draw from previous step is
  // committed
  mainExecutor.execute {
    Choreographer.getInstance().postFrameCallback {
      val bitmap = uiAutomation.takeScreenshot()
      if (bitmap == null) {
        bitmapFuture.setException(RuntimeException("uiAutomation.takeScreenshot returned null"))
      } else {
        bitmapFuture.set(bitmap)
      }
    }
  }

  // remap future exceptions as RuntimeExceptions
  try {
    return bitmapFuture.get(5, TimeUnit.SECONDS)
  } catch (e: ExecutionException) {
    if (e.cause is RuntimeException) {
      throw e.cause as RuntimeException
    } else {
      throw RuntimeException(
        "UiAutomation.takeScreenshot failed with unrecognized exception",
        e.cause
      )
    }
  } catch (e: TimeoutException) {
    throw RuntimeException("Uiautomation.takeScreenshot failed to complete in 5 seconds", e)
  } catch (e: InterruptedException) {
    Thread.currentThread().interrupt()
    throw RuntimeException("Uiautomation.takeScreenshot was interrupted")
  }
}

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
private fun forceRedrawGlobalWindowViews(mainExecutor: Executor): ListenableFuture<List<Void>> {
  val future: ResolvableFuture<List<Void>> = ResolvableFuture.create()
  mainExecutor.execute {
    val views = WindowInspectorCompat.getGlobalWindowViews()
    val viewFutures: MutableList<ListenableFuture<Void>> = mutableListOf()
    for (view in views) {
      viewFutures.add(view.forceRedraw())
    }
    Log.d("takeScreenshot", "Found ${views.size} global views to redraw")
    future.setFuture(ListFuture<Void>(viewFutures, true, mainExecutor))
  }
  return future
}
