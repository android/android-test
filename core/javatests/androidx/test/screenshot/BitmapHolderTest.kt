package androidx.test.screenshot

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BitmapHolderTest {

  @Test
  fun testBitmapHolder_nullBitmap_returnsNull() {
    assertNull(BitmapHolder(null, true).get())
  }

  @Test
  fun testBitmapHolder_nonCompressedBitmap_isNotCompressed() {
    val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    val holder = BitmapHolder(bitmap, false)
    assertFalse(holder.isCompressed())
  }

  @Test
  fun testBitmapHolder_compressedBitmap_isCompressed() {
    val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    val holder = BitmapHolder(bitmap, true)
    assertTrue(holder.isCompressed())
  }
}
