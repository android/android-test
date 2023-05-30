package androidx.test.espresso.device.util

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.provider.Settings.System
import java.util.concurrent.CountDownLatch

class SettingsObserver(
  handler: Handler,
  val context: Context,
  val latch: CountDownLatch,
  val settingToObserve: String
) : ContentObserver(handler) {
  fun observe() {
    val resolver: ContentResolver = context.getContentResolver()
    resolver.registerContentObserver(System.getUriFor(settingToObserve), false, this)
  }

  fun stopObserver() {
    val resolver: ContentResolver = context.getContentResolver()
    resolver.unregisterContentObserver(this)
  }

  override fun onChange(selfChange: Boolean) {
    latch.countDown()
  }
}
