package androidx.test.gradletests.espresso.idling_resource

import android.os.Handler

internal class MessageDelayer {

  internal interface DelayerCallback {
    fun onDone(text: String?)
  }

  companion object {
    private const val DELAY_MILLIS = 3000L

    /**
     * Takes a String and returns it after [.DELAY_MILLIS] via a [DelayerCallback].
     *
     * @param message the String that will be returned via the callback
     * @param callback used to notify the caller asynchronously
     */
    fun processMessage(
      message: String,
      callback: DelayerCallback?,
      idlingResource: SimpleIdlingResource
    ) {
      idlingResource.setIdleState(false)

      // Delay the execution, return message via callback.
      val handler = Handler()
      handler.postDelayed(
        {
          if (callback != null) {
            callback.onDone(message)
            idlingResource.setIdleState(true)
          }
        },
        DELAY_MILLIS
      )
    }
  }
}
