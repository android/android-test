package androidx.test.espresso.action

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.test.annotation.ExperimentalTestApi
import androidx.test.core.internal.os.HandlerExecutor
import androidx.test.core.view.captureToBitmapAsync
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import java.util.Locale
import java.util.concurrent.TimeUnit
import org.hamcrest.Matcher
import org.hamcrest.Matchers.any

@ExperimentalTestApi
class CaptureToBitmapAction(val bitmapReceiver: ViewActions.BitmapReceiver) : ViewAction {
  override fun getConstraints(): Matcher<View> {
    return any(View::class.java)
  }

  override fun getDescription(): String {
    return String.format(Locale.ROOT, "capture view to Bitmap")
  }

  override fun perform(uiController: UiController, view: View) {
    uiController.loopMainThreadUntilIdle()
    // Create an idling resource for the bitmap creation work.
    val captureIdlingResource = RunnableIdlingResource()
    IdlingRegistry.getInstance().register(captureIdlingResource)

    // Have the bitmap mark the idling resource as idle when it completes.
    val futureBitmap = view.captureToBitmapAsync()
    val mainExecutor = HandlerExecutor(Handler(Looper.getMainLooper()))
    futureBitmap.addListener(captureIdlingResource, mainExecutor)

    // Wait for it to complete and unregister
    uiController.loopMainThreadUntilIdle()
    IdlingRegistry.getInstance().unregister(captureIdlingResource)

    // Acquire the bitmap (should be instant)
    val destBitmap = futureBitmap.get(1, TimeUnit.MILLISECONDS)
    bitmapReceiver.onBitmapCaptured(destBitmap)
  }
}
