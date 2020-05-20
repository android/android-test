/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.test.services.speakeasy.server;

import static com.google.common.base.Preconditions.checkNotNull;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import androidx.core.app.NotificationCompat;
import androidx.test.services.speakeasy.SpeakEasyProtocol;

/** Serves SpeakEasy requests. */
public class SpeakEasyService extends Service {

  private Looper backgroundLooper;
  private Handler backgroundHandler;
  // Owned by backgroundLooper thread
  private SpeakEasy speakEasy;
  // Owned by backgroundLooper thread
  private boolean runningInForeground;
  // Owned by backgroundLooper thread
  private int leavingStartId;

  @Override
  public void onCreate() {
    super.onCreate();
    HandlerThread backgroundThread = new HandlerThread("SpeakEasyService");
    backgroundThread.start();
    backgroundLooper = backgroundThread.getLooper();
    backgroundHandler =
        new Handler(backgroundLooper) {
          @Override
          public void handleMessage(Message m) {
            serveIntent((Intent) m.obj, m.arg1);
          }
        };
  }

  @Override
  public void onStart(Intent intent, int id) {
    Message m = backgroundHandler.obtainMessage();
    m.obj = intent;
    m.arg1 = id;
    m.sendToTarget();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    onStart(intent, startId);
    return START_NOT_STICKY;
  }

  @Override
  public void onDestroy() {
    backgroundLooper.quit();
    super.onDestroy();
  }

  private void leaveForeground(final int startId) {
    // When the last task leaves SpeakEasy, wait before leaving the foreground.
    //
    // We wait 5 seconds in case another request comes in.
    //
    // In the case where we have a ton of short running unit tests, the service
    // gets started and stopped very frequently. Unfortunately, the code paths
    // around foreground services do not expect the same service to blink in and
    // out every <100 millis and we have seen cases where this behaviour
    // generates ANRs in system_server or systemui.
    leavingStartId = startId;
    backgroundHandler.postDelayed(
        new Runnable() {
          @Override
          public void run() {
            if (startId == leavingStartId) {
              leavingStartId = -1;
              runningInForeground = false;
              stopForeground(true);
              stopSelf(startId);
            }
          }
        },
        5000);
  }

  private void serveIntent(Intent in, int startId) {
    SpeakEasyProtocol sep = SpeakEasyProtocol.fromBundle(in.getExtras());
    if (null == sep) {
      return;
    }
    if (null == speakEasy) {
      speakEasy = new SpeakEasy(new DeathCallback(getApplicationContext()));
      runningInForeground = false;
    }
    speakEasy.serve(sep);
    if (speakEasy.size() == 0) {
      leaveForeground(startId);
      return;
    }

    leavingStartId = -1;
    if (!runningInForeground) {
      Intent launcher = new Intent(Intent.ACTION_MAIN);
      launcher.addCategory(Intent.CATEGORY_HOME);
      // from https://developer.android.com/training/notify-user/channels
      String channelid = "test_services_channel_id";
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel =
            new NotificationChannel(
                channelid, "AndroidX Test Services", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager =
            (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
      }
      Notification notif =
          new NotificationCompat.Builder(this, channelid)
              .setSmallIcon(R.drawable.ic_shortcut_axt_logo)
              .setContentTitle("AndroidX Test Services")
              .setContentText("SpeakEasy Binder Registry")
              .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, launcher, 0))
              .build();
      startForeground(R.id.speak_easy_svc_foreground_notification, notif);
      runningInForeground = true;
    }
  }

  @Override
  public IBinder onBind(Intent i) {
    return null;
  }

  private static class DeathCallback implements SpeakEasy.BinderDeathCallback {
    private final Context context;

    DeathCallback(Context context) {
      this.context = checkNotNull(context);
    }

    @Override
    public void binderDeath(String key, IBinder dead) {
      Intent msg = new Intent(context, SpeakEasyService.class);
      msg.putExtras(SpeakEasyProtocol.Remove.asBundle(key));
      startForegroundService(context, msg);
    }
  }

  // copy of ContentCompat.startForegroundService
  private static void startForegroundService(Context context, Intent intent) {
    if (Build.VERSION.SDK_INT >= 26) {
      context.startForegroundService(intent);
    } else {
      // Pre-O behavior.
      context.startService(intent);
    }
  }
}
