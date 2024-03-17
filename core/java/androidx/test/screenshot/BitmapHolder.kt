package androidx.test.screenshot

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.annotation.ExperimentalTestApi
import java.io.ByteArrayOutputStream

/**
 * Holds either a bitmap, the bytes of a bitmap or null. This is to hide away the compression that
 * happens in emulators to reduce the size of bitmaps. Robolectric doesn't need to compress, so this
 * allows it to avoid doing that.
 */
@ExperimentalTestApi
class BitmapHolder {

  private var bitmap: Bitmap? = null
  private var bytes: ByteArray? = null

  constructor(bitmap: Bitmap?, compression: Boolean = false) {
    if (!compression) {
      this.bitmap = bitmap
    } else if (bitmap != null) {
      ByteArrayOutputStream().use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        this.bytes = it.toByteArray()
      }
    }
  }

  /** Returns true if bytes are not null and the bitmap is null. */
  fun isCompressed(): Boolean = bitmap == null && bytes != null

  /** Returns true if this was given a non-null Bitmap. */
  fun exists(): Boolean = bitmap != null || bytes != null

  /** Acquires the bitmap, either by decoding the bytes or just returning it. */
  fun get(): Bitmap? {
    if (bitmap != null) {
      return bitmap
    } else if (bytes != null) {
      return BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)
    }
    return null
  }
}
