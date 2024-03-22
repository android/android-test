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
@file:JvmName("WindowCapture")

package androidx.test.core.view

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.Window
import androidx.concurrent.futures.SuspendToFutureAdapter
import androidx.test.annotation.ExperimentalTestApi
import androidx.test.platform.graphics.HardwareRendererCompat
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

/**
 * Suspend function that captures an image of the underlying window into a [Bitmap].
 *
 * For devices below [Build.VERSION_CODES#O] the image is obtained using [View#draw] on the windows
 * decorView. Otherwise, [PixelCopy] is used.
 *
 * This method will also enable [HardwareRendererCompat#setDrawingEnabled(boolean)] if required.
 *
 * This API is primarily intended for use in lower layer libraries or frameworks. For test authors,
 * its recommended to use espresso or compose's captureToImage.
 *
 * This API must be called from the UI thread.
 *
 * This API is currently experimental and subject to change or removal.
 */
@ExperimentalTestApi
suspend fun Window.captureRegionToBitmap(boundsInWindow: Rect? = null): Bitmap {
  var bitmap: Bitmap? = null

  val hardwareDrawingEnabled = HardwareRendererCompat.isDrawingEnabled()
  HardwareRendererCompat.setDrawingEnabled(true)
  try {
    decorView.forceRedraw()
    bitmap = generateBitmap(boundsInWindow)
  } finally {
    HardwareRendererCompat.setDrawingEnabled(hardwareDrawingEnabled)
  }

  return bitmap!!
}

/** A ListenableFuture variant of captureRegionToBitmap intended for use from Java. */
@ExperimentalTestApi
fun Window.captureRegionToBitmapAsync(boundsInWindow: Rect? = null): ListenableFuture<Bitmap> {
  return SuspendToFutureAdapter.launchFuture(Dispatchers.Default + Job()) {
    captureRegionToBitmap(boundsInWindow)
  }
}

internal suspend fun Window.generateBitmap(boundsInWindow: Rect? = null): Bitmap {
  val destBitmap =
    Bitmap.createBitmap(
      boundsInWindow?.width() ?: decorView.width,
      boundsInWindow?.height() ?: decorView.height,
      Bitmap.Config.ARGB_8888,
    )
  when {
    Build.VERSION.SDK_INT < 26 ->
      // TODO: handle boundsInWindow
      decorView.generateBitmapFromDraw(destBitmap, boundsInWindow)
    else -> generateBitmapFromPixelCopy(boundsInWindow, destBitmap)
  }

  return destBitmap
}

@SuppressWarnings("NewApi")
internal suspend fun Window.generateBitmapFromPixelCopy(
  boundsInWindow: Rect? = null,
  destBitmap: Bitmap,
) {
  val job = Job()
  var exception: Exception? = null
  val onCopyFinished =
    PixelCopy.OnPixelCopyFinishedListener { result ->
      if (result != PixelCopy.SUCCESS) {
        exception = RuntimeException(String.format("PixelCopy failed: %d", result))
      }
      job.complete()
    }

  PixelCopy.request(
    this,
    boundsInWindow,
    destBitmap,
    onCopyFinished,
    Handler(Looper.getMainLooper()),
  )

  job.join()
  exception?.let { throw it }
}
