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
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Choreographer
import androidx.annotation.RestrictTo
import androidx.test.annotation.ExperimentalTestApi
import androidx.test.core.internal.os.HandlerExecutor
import androidx.test.core.view.forceRedraw
import androidx.test.internal.util.Checks
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.platform.graphics.HardwareRendererCompat
import androidx.test.platform.view.inspector.WindowInspectorCompat
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/**
 * Returns false if calling [takeScreenshot] will fail.
 *
 * Taking a screenshot requires [UiAutomation] and can only be called off of the main thread. If
 * this method returns false then attempting to take a screenshot will fail. Note that taking a
 * screenshot may still fail if this method returns true, for example if the call to [UiAutomation]
 * fails.
 */
@ExperimentalTestApi
fun canTakeScreenshot(): Boolean =
  getInstrumentation().uiAutomation != null && Looper.myLooper() != Looper.getMainLooper()

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
 *   to UiAutomation, [RuntimeException] if UiAutomation fails to take the screenshot
 */
@ExperimentalTestApi
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
 *   to UiAutomation, [RuntimeException] if UiAutomation fails to take the screenshot
 * @hide
 */
@ExperimentalTestApi
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Suppress("FutureReturnValueIgnored")
@Throws(RuntimeException::class)
fun takeScreenshotNoSync(): Bitmap {
  Checks.checkState(canTakeScreenshot())

  var bitmap: Bitmap? = null
  var exception: Exception? = null
  val mainHandlerDispatcher =
    HandlerExecutor(Handler(Looper.getMainLooper())).asCoroutineDispatcher()
  val uiAutomation = getInstrumentation().uiAutomation
  if (uiAutomation == null) {
    throw RuntimeException("uiautomation is null")
  }

  val hardwareDrawingEnabled = HardwareRendererCompat.isDrawingEnabled()
  HardwareRendererCompat.setDrawingEnabled(true)

  try {
    runBlocking(mainHandlerDispatcher) { withTimeout(5.seconds)  { forceRedrawGlobalWindowViews(mainHandlerDispatcher) } }
  } catch (e: Exception) {
    Log.w("takeScreenshot", "force redraw failed. Proceeding with screenshot", e)
  }
  // wait on the next frame to increase probability the draw from previous step is
  // committed
  // TODO(b/289244795): use a transaction callback instead
  val job = Job()
  CoroutineScope(mainHandlerDispatcher).launch {
    Choreographer.getInstance().postFrameCallback {
      // do multiple retries of uiAutomation.takeScreenshot because it is known to return null
      // on API 31+ b/257274080
      for (i in 1..3) {
        bitmap = uiAutomation.takeScreenshot()
        if (bitmap != null) {
          break
        }
      }
      if (bitmap == null) {
        exception = RuntimeException("uiAutomation.takeScreenshot returned null")
      }
      HardwareRendererCompat.setDrawingEnabled(hardwareDrawingEnabled)
      job.complete()
    }
  }

  return runBlocking {
    try {
      withTimeout(5.seconds) { job.join() }
    } catch (e: TimeoutCancellationException) {
      throw RuntimeException("Uiautomation.takeScreenshot failed to complete in 5 seconds", e)
    }

    exception?.let { throw it }
    bitmap!!
  }
}

private suspend fun forceRedrawGlobalWindowViews(context: CoroutineContext) {
      val views = WindowInspectorCompat.getGlobalWindowViews()
  Log.d("takeScreenshot", "Found ${views.size} global views to redraw")
      for (view in views) {
        //redrawJobs.add(launch { view.forceRedraw() })
        view.forceRedraw()
      }
    }
