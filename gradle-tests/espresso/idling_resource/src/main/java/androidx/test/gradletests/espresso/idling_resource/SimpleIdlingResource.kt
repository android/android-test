package androidx.test.gradletests.espresso.idling_resource

import androidx.annotation.Nullable
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.IdlingResource.ResourceCallback
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A very simple implementation of {@link IdlingResource}.
 *
 * <p>
 * Consider using CountingIdlingResource from espresso-contrib package if you use this class from
 * multiple threads or need to keep a count of pending operations.
 */
class SimpleIdlingResource : IdlingResource {
  @Volatile @Nullable private var callback: ResourceCallback? = null

  private val isIdleNow = AtomicBoolean(true)

  override fun getName() = this::class.java.name

  override fun isIdleNow() = isIdleNow.get()

  override fun registerIdleTransitionCallback(callback: ResourceCallback) {
    this.callback = callback
  }

  fun setIdleState(isIdleNow: Boolean) {
    this.isIdleNow.set(isIdleNow)
    if (isIdleNow && callback != null) {
      callback!!.onTransitionToIdle()
    }
  }
}
