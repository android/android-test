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
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.HardwareRenderer
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RenderNode
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import android.view.Surface
import android.view.SurfaceView
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.concurrent.futures.ResolvableFuture
import androidx.test.annotation.ExperimentalTestApi
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.internal.os.HandlerExecutor
import androidx.test.internal.platform.reflect.ReflectiveField
import androidx.test.internal.platform.reflect.ReflectiveMethod
import androidx.test.platform.graphics.HardwareRendererCompat
import com.google.common.util.concurrent.ListenableFuture
import java.nio.ByteOrder
import java.util.function.Consumer

/**
 * Asynchronously captures an image of the underlying view into a [Bitmap].
 *
 * For devices below [Build.VERSION_CODES#O] (or if the view's window cannot be determined), the
 * image is obtained using [View#draw]. Otherwise, [PixelCopy] is used.
 *
 * This method will also enable [HardwareRendererCompat#setDrawingEnabled(boolean)] if required.
 *
 * This API is primarily intended for use in lower layer libraries or frameworks. For test authors,
 * its recommended to use espresso or compose's captureToImage.
 *
 * If a rect is supplied, this will further crop locally from the bounds of the given view. For
 * example, if the given view is at (10, 10 - 30, 30) and the rect is (5, 5 - 10, 10), the final
 * bitmap will be a 5x5 bitmap that spans (15, 15 - 20, 20). This is particularly useful for
 * Compose, which only has a singular view that contains a hierarchy of nodes.
 *
 * This API is currently experimental and subject to change or removal.
 */
@ExperimentalTestApi
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
fun View.captureToBitmap(rect: Rect? = null): ListenableFuture<Bitmap> {
  val bitmapFuture: ResolvableFuture<Bitmap> = ResolvableFuture.create()
  val mainExecutor = HandlerExecutor(Handler(Looper.getMainLooper()))

  // disable drawing again if necessary once work is complete
  if (!HardwareRendererCompat.isDrawingEnabled()) {
    HardwareRendererCompat.setDrawingEnabled(true)
    bitmapFuture.addListener({ HardwareRendererCompat.setDrawingEnabled(false) }, mainExecutor)
  }

  mainExecutor.execute {
    if (Build.FINGERPRINT.contains("robolectric")) {
      generateBitmap(bitmapFuture, rect)
    } else {
      val forceRedrawFuture = forceRedraw()
      forceRedrawFuture.addListener({ generateBitmap(bitmapFuture, rect) }, mainExecutor)
    }
  }

  return bitmapFuture
}

/**
 * Trigger a redraw of the given view.
 *
 * Should only be called on UI thread.
 *
 * @return a [ListenableFuture] that will be complete once ui drawing is complete
 */
// NoClassDefFoundError occurs on API 15
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
// @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@ExperimentalTestApi
fun View.forceRedraw(): ListenableFuture<Void> {
  val future: ResolvableFuture<Void> = ResolvableFuture.create()

  if (Build.VERSION.SDK_INT >= 29 && isHardwareAccelerated) {
    viewTreeObserver.registerFrameCommitCallback() { future.set(null) }
  } else {
    viewTreeObserver.addOnDrawListener(
      object : ViewTreeObserver.OnDrawListener {
        var handled = false

        override fun onDraw() {
          if (!handled) {
            handled = true
            future.set(null)
            // cannot remove on draw listener inside of onDraw
            Handler(Looper.getMainLooper()).post { viewTreeObserver.removeOnDrawListener(this) }
          }
        }
      }
    )
  }
  invalidate()
  return future
}

private fun View.generateBitmap(bitmapFuture: ResolvableFuture<Bitmap>, rect: Rect? = null) {
  if (bitmapFuture.isCancelled) {
    return
  }

  val rectWidth = rect?.width() ?: width
  val rectHeight = rect?.height() ?: height
  val destBitmap = Bitmap.createBitmap(rectWidth, rectHeight, Bitmap.Config.ARGB_8888)
  when {
    Build.VERSION.SDK_INT < 26 -> generateBitmapFromDraw(destBitmap, bitmapFuture, rect)
    Build.VERSION.SDK_INT >= 34 -> generateBitmapFromPixelCopy(destBitmap, bitmapFuture, rect)
    Build.VERSION.SDK_INT >= 31 && useHardwareRendererNative() ->
      generateBitmapFromHardwareRenderNative(destBitmap, bitmapFuture, rect)
    this is SurfaceView -> generateBitmapFromSurfaceViewPixelCopy(destBitmap, bitmapFuture, rect)
    else -> generateBitmapFromPixelCopy(this.getSurface(), destBitmap, bitmapFuture, rect)
  }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun SurfaceView.generateBitmapFromSurfaceViewPixelCopy(
  destBitmap: Bitmap,
  bitmapFuture: ResolvableFuture<Bitmap>,
  rect: Rect?,
) {
  val onCopyFinished =
    PixelCopy.OnPixelCopyFinishedListener { result ->
      if (result == PixelCopy.SUCCESS) {
        bitmapFuture.set(destBitmap)
      } else {
        bitmapFuture.setException(RuntimeException(String.format("PixelCopy failed: %d", result)))
      }
    }
  PixelCopy.request(this, rect, destBitmap, onCopyFinished, handler)
}

internal fun View.generateBitmapFromDraw(
  destBitmap: Bitmap,
  bitmapFuture: ResolvableFuture<Bitmap>,
  rect: Rect?,
) {
  destBitmap.density = resources.displayMetrics.densityDpi
  computeScroll()
  val canvas = Canvas(destBitmap)
  canvas.translate((-scrollX).toFloat(), (-scrollY).toFloat())
  if (rect != null) {
    canvas.translate((-rect.left).toFloat(), (-rect.top).toFloat())
  }

  draw(canvas)
  bitmapFuture.set(destBitmap)
}

/**
 * Generates a bitmap from the given surface using [PixelCopy].
 *
 * This method is effectively the backwards compatibility version of android U's
 * [PixelCopy.ofWindow(View)], and will be called when running on Android API levels O to T.
 */
@RequiresApi(Build.VERSION_CODES.O)
private fun View.generateBitmapFromPixelCopy(
  surface: Surface,
  destBitmap: Bitmap,
  bitmapFuture: ResolvableFuture<Bitmap>,
  rect: Rect?,
) {
  val onCopyFinished =
    PixelCopy.OnPixelCopyFinishedListener { result ->
      if (result == PixelCopy.SUCCESS) {
        bitmapFuture.set(destBitmap)
      } else {
        bitmapFuture.setException(RuntimeException("PixelCopy failed: $result"))
      }
    }

  var bounds = getBoundsInSurface()
  if (rect != null) {
    bounds =
      Rect(
        bounds.left + rect.left,
        bounds.top + rect.top,
        bounds.left + rect.right,
        bounds.top + rect.bottom
      )
  }
  PixelCopy.request(surface, bounds, destBitmap, onCopyFinished, Handler(Looper.getMainLooper()))
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
      "Could not calculate offset of view in surface on API 28, resulting image may have incorrect positioning"
    )
  }
}

/** Generates a bitmap given the current view using android U's [PixelCopy.ofWindow(View)]. */
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
private fun View.generateBitmapFromPixelCopy(
  destBitmap: Bitmap,
  bitmapFuture: ResolvableFuture<Bitmap>,
  rect: Rect?,
) {
  val request =
    PixelCopy.Request.Builder.ofWindow(this)
      .setSourceRect(rect ?: getBoundsInWindow())
      .setDestinationBitmap(destBitmap)
      .build()
  val mainExecutor = HandlerExecutor(Handler(Looper.getMainLooper()))

  val onCopyFinished =
    Consumer<PixelCopy.Result> { result ->
      if (result.status == PixelCopy.SUCCESS) {
        bitmapFuture.set(result.bitmap)
      } else {
        bitmapFuture.setException(RuntimeException("PixelCopy failed: $(result.status)"))
      }
    }
  PixelCopy.request(request, mainExecutor, onCopyFinished)
}

@RequiresApi(Build.VERSION_CODES.R)
private fun View.getRenderNode(): RenderNode {
  return ReflectiveMethod<RenderNode>(View::class.java, "updateDisplayListIfDirty").invoke(this)
}

const val USE_HARDWARE_RENDERER_NATIVE_ENV = "robolectric.screenshot.hwrdr.native"

private fun useHardwareRendererNative() =
  System.getProperty(USE_HARDWARE_RENDERER_NATIVE_ENV) == "true"

/**
 * Generates a bitmap given the current view using HardwareRenderer with native graphics calls.
 * Requires API 31+ (S).
 */
@RequiresApi(Build.VERSION_CODES.S)
private fun View.generateBitmapFromHardwareRenderNative(
  destBitmap: Bitmap,
  bitmapFuture: ResolvableFuture<Bitmap>,
  rect: Rect?,
) {
  val bounds = rect ?: getBoundsInWindow()
  val rw = bounds.right
  val rh = bounds.bottom
  val sx = bounds.left
  val sy = bounds.top
  val dw = rw - sx
  val dh = rh - sy

  val imageReader = ImageReader.newInstance(rw, rh, PixelFormat.RGBA_8888, 1)
  val renderer = HardwareRenderer()
  renderer.setSurface(imageReader.surface)
  val nativeImage = imageReader.acquireNextImage()

  setupRendererShadowProperties(renderer)

  val node: RenderNode = this.getRenderNode()
  renderer.setContentRoot(node)

  renderer.createRenderRequest().syncAndDraw()

  val renderPixels = IntArray(rw * rh)

  val planes = nativeImage.planes
  val srcBuff = planes[0].buffer.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer()
  val len = srcBuff.remaining()
  srcBuff.get(renderPixels, 0, len)

  // Reading the buffer gives us ABGR data. Transform to ARGB.
  for (i in renderPixels.indices) {
    val s = renderPixels[i]
    val r = s and 0xFF
    val b = s shr 16 and 0xFF
    renderPixels[i] = (s and -0xff0100) + (r shl 16) + b
  }

  destBitmap.setPixels(
    renderPixels,
    /*offset=*/ sx + sy * rw,
    /*stride=*/ rw,
    /*x=*/ 0,
    /*y=*/ 0,
    dw,
    dh
  )
  bitmapFuture.set(destBitmap)
}

@RequiresApi(Build.VERSION_CODES.S)
private fun setupRendererShadowProperties(renderer: HardwareRenderer) {
  val context = ApplicationProvider.getApplicationContext<Context>()
  val resources = context.resources
  val displayMetrics = resources.displayMetrics

  // Get the LightSourceGeometry and LightSourceAlpha from resources.
  // The default values are the ones recommended by the getLightSourceGeometry() and
  // getLightSourceAlpha() documentation.
  // This matches LayoutLib's RenderSessionImpl#renderAndBuildResult() implementation.

  val styleable: Class<*> = Class.forName("com.android.internal.R\$styleable")
  val lighting = getField<IntArray>(styleable, "Lighting")
  val lightingLightY = getField<Int>(styleable, "Lighting_lightY")
  val lightingLightZ = getField<Int>(styleable, "Lighting_lightZ")
  val lightingLightRadius = getField<Int>(styleable, "Lighting_lightRadius")
  val lightingAmbientShadowAlpha = getField<Int>(styleable, "Lighting_ambientShadowAlpha")
  val lightingSpotShadowAlpha = getField<Int>(styleable, "Lighting_spotShadowAlpha")

  val a = context.obtainStyledAttributes(null, lighting, 0, 0)
  val lightX = displayMetrics.widthPixels / 2f
  val lightY = a.getDimension(lightingLightY, 0f)
  val lightZ = a.getDimension(lightingLightZ, 600f * displayMetrics.density)
  val lightRadius = a.getDimension(lightingLightRadius, 800f * displayMetrics.density)
  val ambientShadowAlpha = a.getFloat(lightingAmbientShadowAlpha, 0.039f)
  val spotShadowAlpha = a.getFloat(lightingSpotShadowAlpha, 0.19f)
  a.recycle()

  renderer.setLightSourceGeometry(lightX, lightY, lightZ, lightRadius)
  renderer.setLightSourceAlpha(ambientShadowAlpha, spotShadowAlpha)
}

@SuppressWarnings("unchecked")
@RequiresApi(Build.VERSION_CODES.S)
private fun <T> getField(clazz: Class<*>, fieldName: String): T {
  val field = clazz.getDeclaredField(fieldName)
  field.isAccessible = true
  return field.get(clazz) as T
}
