package androidx.test.espresso.action

import androidx.test.espresso.IdlingResource
import java.util.concurrent.atomic.AtomicBoolean

/** An idling resource that waits for a callback to mark it as idle. */
internal class RunnableIdlingResource : IdlingResource, Runnable {

  private var idle: AtomicBoolean = AtomicBoolean(false)
  private var resourceCallback: IdlingResource.ResourceCallback? = null

  override fun getName(): String {
    return "RunnableIdlingResource"
  }

  override fun isIdleNow(): Boolean {
    return idle.get()
  }

  override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
    this.resourceCallback = callback
  }

  override fun run() {
    idle.set(true)
    resourceCallback?.onTransitionToIdle()
  }
}
