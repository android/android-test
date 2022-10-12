package androidx.test.espresso.device.util

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log

class EmptyConfigChangeActivity : Activity() {
  private val receiver: BroadcastReceiver =
    object : BroadcastReceiver() {
      override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "recieved broadcast")
        finish()
      }
    }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    Log.d(TAG, "empty activity on config changed")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    Log.d(TAG, "oncreate")
    super.onCreate(savedInstanceState)
    registerBroadcastReceiver(this, receiver, IntentFilter(FINISH_EMPTY_ACTIVITIES))

    // disable starting animations
    overridePendingTransition(0, 0)
  }

  override fun finish() {
    Log.d(TAG, "finish called")
    super.finish()
    // disable closing animations
    overridePendingTransition(0, 0)
  }

  override fun onResume() {
    Log.d(TAG, "onResumed")
    super.onResume()
    sendBroadcast(Intent(EMPTY_ACTIVITY_RESUMED))
  }

  override fun onDestroy() {
    Log.d(TAG, "ondestroy")
    super.onDestroy()
    unregisterReceiver(receiver)
  }

  companion object {
    private val TAG = EmptyConfigChangeActivity::class.java.simpleName
    /**
     * An intent action broadcasted by {@link EmptyConfigChangeActivity} notifying the activity
     * becomes resumed state.
     */
    val EMPTY_ACTIVITY_RESUMED: String =
      "androidx.test.device.util.EmptyConfigChangeActivity.EMPTY_ACTIVITY_RESUMED"
    /**
     * An intent action to notify {@link EmptyConfigChangeActivity} to be finished.
     */
    val FINISH_EMPTY_ACTIVITIES: String =
      "androidx.test.device.util.EmptyConfigChangeActivity.FINISH_EMPTY_ACTIVITIES"

    fun registerBroadcastReceiver(
      context: Context,
      broadcastReceiver: BroadcastReceiver,
      intentFilter: IntentFilter
    ) {
      intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY)
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        context.registerReceiver(broadcastReceiver, intentFilter)
      } else {
        context.registerReceiver(
          broadcastReceiver,
          intentFilter,
          Context.RECEIVER_EXPORTED
        )
      }
    }
  }
}
