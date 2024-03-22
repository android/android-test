/*
 * Copyright 2021 The Android Open Source Project
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
@file:JvmName("ViewCapture")

package androidx.test.core.view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import android.view.Surface
import android.view.SurfaceView
import android.view.View
import android.view.ViewTreeObserver.OnDrawListener
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.concurrent.futures.SuspendToFutureAdapter
import androidx.test.annotation.ExperimentalTestApi
import androidx.test.core.internal.os.HandlerExecutor
import androidx.test.internal.platform.ServiceLoaderWrapper
import androidx.test.internal.platform.os.ControlledLooper
import androidx.test.internal.platform.reflect.ReflectiveField
import androidx.test.internal.platform.reflect.ReflectiveMethod
import androidx.test.internal.util.Checks.checkState
import androidx.test.platform.graphics.HardwareRendererCompat
import com.google.common.util.concurrent.ListenableFuture
import java.util.function.Consumer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Suspend function for capturing an image of the underlying view into a [Bitmap].
 *
 * For devices below [Build.VERSION_CODES#O] (or if the view's window cannot be determined), the
 * image is obtained using [View#draw]. Otherwise, [PixelCopy] is used.
 *
 * This method will also enable [HardwareRendererCompat#setDrawingEnabled(boolean)] if required.
 *
 * This API is primarily intended for use in lower layer libraries or frameworks. For test authors,
 * it's recommended to use Espresso or Compose's captureToImage.
 *
 * If a rect is supplied, this will further crop locally from the bounds of the given view. For
 * example, if the given view is at (10, 10 - 30, 30) and the rect is (5, 5 - 10, 10), the final
 * bitmap will be a 5x5 bitmap that spans (15, 15 - 20, 20). This is particularly useful for
 * Compose, which only has a singular view that contains a hierarchy of nodes.
 *
 * This API must be called on the View's handler thread. If you're calling this from another
 * context, eg directly from the test thread, you can use something like
 * <pre>{@code
 *   runBlocking(view.handler.asCoroutineDispatcher()) {
 *     withTimeout(10.seconds) {
 *       view.captureToBitmap(rect)
 *     }
 *   }
 * }</pre>
 *
 * This API is currently experimental and subject to change or removal.
 */
@ExperimentalTestApi
suspend fun View.captureToBitmap(rect: Rect? = null): Bitmap {
  checkState(isAttachedToWindow, "View must be attached to a window")
  checkState(
    handler.looper.isCurrentThread,
    "Must be called from view's handler thread. Current: ${Thread.currentThread().name}, view handler: ${handler.looper.thread.name}",
  )

  var bitmap: Bitmap? = null
  val hardwareDrawingEnabled = HardwareRendererCompat.isDrawingEnabled()
  HardwareRendererCompat.setDrawingEnabled(true)
  try {
    forceRedraw()
    bitmap = generateBitmap(rect)
  } finally {
    HardwareRendererCompat.setDrawingEnabled(hardwareDrawingEnabled)
  }

  return bitmap!!
}

private fun getControlledLooper(): ControlledLooper {
  return ServiceLoaderWrapper.loadSingleService(ControlledLooper::class.java) {
    ControlledLooper.NO_OP_CONTROLLED_LOOPER
  }
}

/** A ListenableFuture variant of captureToBitmap intended for use from Java. */
@ExperimentalTestApi
fun View.captureToBitmapAsync(rect: Rect? = null): ListenableFuture<Bitmap> {
  return SuspendToFutureAdapter.launchFuture(Dispatchers.Main) { captureToBitmap(rect) }
}

/**
 * Trigger a redraw of the given view.
 *
 * Should only be called on UI thread.
 */
// TODO(b/316921934): uncomment once @ExperimentalTestApi is removed
// @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@ExperimentalTestApi
suspend fun View.forceRedraw() {
  checkState(handler.looper.isCurrentThread, "Must be called from view's handler thread")
  if (!getControlledLooper().areDrawCallbacksSupported()) {
    Log.i("ViewCapture", "Skipping forceRedraw as it is not supported")
    return
  }

  var drawListener: OnDrawListener? = null
  try {
    return suspendCancellableCoroutine<Unit> { cont ->
      if (Build.VERSION.SDK_INT >= 29 && isHardwareAccelerated) {
        viewTreeObserver.registerFrameCommitCallback() { cont.resume(Unit) }
      } else {
        drawListener =
          object : OnDrawListener {
            var handled = false

            override fun onDraw() {
              if (!handled) {
                handled = true

                cont.resume(Unit)
              }
            }
          }
        viewTreeObserver.addOnDrawListener(drawListener)
      }
      invalidate()
    }
  } finally {
    // cannot remove on draw listener inside of onDraw
    if (drawListener != null) {

      viewTreeObserver.removeOnDrawListener(drawListener)
    }
  }
}

private suspend fun View.generateBitmap(rect: Rect? = null): Bitmap {
  val rectWidth = rect?.width() ?: width
  val rectHeight = rect?.height() ?: height
  val destBitmap = Bitmap.createBitmap(rectWidth, rectHeight, Bitmap.Config.ARGB_8888)

  return when {
    Build.VERSION.SDK_INT < 26 -> generateBitmapFromDraw(destBitmap, rect)
    Build.VERSION.SDK_INT >= 34 -> generateBitmapFromPixelCopy(destBitmap, rect)
    this is SurfaceView -> generateBitmapFromSurfaceViewPixelCopy(destBitmap, rect)
    else -> generateBitmapFromPixelCopy(this.getSurface(), destBitmap, rect)
  }
}

@RequiresApi(Build.VERSION_CODES.O)
private suspend fun SurfaceView.generateBitmapFromSurfaceViewPixelCopy(
  destBitmap: Bitmap,
  rect: Rect?,
): Bitmap {
  return suspendCancellableCoroutine<Bitmap> { cont ->
    val onCopyFinished =
      PixelCopy.OnPixelCopyFinishedListener { result ->
        if (result == PixelCopy.SUCCESS) {
          cont.resume(destBitmap)
        } else {
          cont.resumeWithException(RuntimeException(String.format("PixelCopy failed: %d", result)))
        }
      }
    PixelCopy.request(this, rect, destBitmap, onCopyFinished, handler)
  }
}

internal fun View.generateBitmapFromDraw(destBitmap: Bitmap, rect: Rect?): Bitmap {
  destBitmap.density = resources.displayMetrics.densityDpi
  computeScroll()
  val canvas = Canvas(destBitmap)
  canvas.translate((-scrollX).toFloat(), (-scrollY).toFloat())
  if (rect != null) {
    canvas.translate((-rect.left).toFloat(), (-rect.top).toFloat())
  }

  draw(canvas)
  return destBitmap
}

/**
 * Generates a bitmap from the given surface using [PixelCopy].
 *
 * This method is effectively the backwards compatibility version of android U's
 * [PixelCopy.ofWindow(View)], and will be called when running on Android API levels O to T.
 */
@RequiresApi(Build.VERSION_CODES.O)
private suspend fun View.generateBitmapFromPixelCopy(
  surface: Surface,
  destBitmap: Bitmap,
  rect: Rect?,
): Bitmap {
  return suspendCancellableCoroutine<Bitmap> { cont ->
    var bounds = getBoundsInSurface()
    if (rect != null) {
      bounds =
        Rect(
          bounds.left + rect.left,
          bounds.top + rect.top,
          bounds.left + rect.right,
          bounds.top + rect.bottom,
        )
    }
    val onCopyFinished =
      PixelCopy.OnPixelCopyFinishedListener { result ->
        if (result == PixelCopy.SUCCESS) {
          cont.resume(destBitmap)
        } else {
          cont.resumeWithException(RuntimeException("PixelCopy failed: $result"))
        }
      }
    PixelCopy.request(surface, bounds, destBitmap, onCopyFinished, Handler(Looper.getMainLooper()))
  }
}

/** Returns the Rect indicating the View's coordinates within its containing window. */
private fun View.getBoundsInWindow(): Rect {
  val locationInWindow = intArrayOf(0, 0)
  getLocationInWindow(locationInWindow)
  val x = locationInWindow[0]
  val y = locationInWindow[1]
  return Rect(x, y, x + width, y + height)
}

/** Returns the Rect indicating the View's coordinates within its containing surface. */
private fun View.getBoundsInSurface(): Rect {
  val locationInSurface = intArrayOf(0, 0)
  if (Build.VERSION.SDK_INT < 29) {
    reflectivelyGetLocationInSurface(locationInSurface)
  } else {
    getLocationInSurface(locationInSurface)
  }
  val x = locationInSurface[0]
  val y = locationInSurface[1]
  val bounds = Rect(x, y, x + width, y + height)

  Log.d("ViewCapture", "getBoundsInSurface $bounds")

  return bounds
}

private fun View.getSurface(): Surface {
  // copy the implementation of API 34's PixelCopy.ofWindow to get the surface from view
  val viewRootImpl = ReflectiveMethod<Any>(View::class.java, "getViewRootImpl").invoke(this)
  return ReflectiveField<Surface>("android.view.ViewRootImpl", "mSurface").get(viewRootImpl)
}

/**
 * The backwards compatible version of API 29's [View.getLocationInSurface].
 *
 * It makes a best effort attempt to replicate the API 29 logic.
 */
@SuppressLint("NewApi")
private fun View.reflectivelyGetLocationInSurface(locationInSurface: IntArray) {
  // copy the implementation of API 29+ getLocationInSurface
  getLocationInWindow(locationInSurface)
  if (Build.VERSION.SDK_INT < 28) {
    val viewRootImpl = ReflectiveMethod<Any>(View::class.java, "getViewRootImpl").invoke(this)
    val windowAttributes =
      ReflectiveField<WindowManager.LayoutParams>("android.view.ViewRootImpl", "mWindowAttributes")
        .get(viewRootImpl)
    val surfaceInsets =
      ReflectiveField<Rect>(WindowManager.LayoutParams::class.java, "surfaceInsets")
        .get(windowAttributes)
    locationInSurface[0] += surfaceInsets.left
    locationInSurface[1] += surfaceInsets.top
  } else {
    // ART restrictions introduced in API 29 disallow reflective access to mWindowAttributes
    Log.w(
      "ViewCapture",
      "Could not calculate offset of view in surface on API 28, resulting image may have incorrect positioning",
    )
  }
}

private suspend fun View.generateBitmapFromPixelCopy(
  destBitmap: Bitmap,
  rect: Rect? = null,
): Bitmap {
  return suspendCancellableCoroutine<Bitmap> { cont ->
    val request =
      PixelCopy.Request.Builder.ofWindow(this)
        .setSourceRect(rect ?: getBoundsInWindow())
        .setDestinationBitmap(destBitmap)
        .build()
    val onCopyFinished =
      Consumer<PixelCopy.Result> { result ->
        if (result.status == PixelCopy.SUCCESS) {
          cont.resume(result.bitmap)
        } else {
          cont.resumeWithException(RuntimeException("PixelCopy failed: $(result.status)"))
        }
      }
    PixelCopy.request(request, HandlerExecutor(handler), onCopyFinished)
  }
}
