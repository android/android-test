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

package androidx.test.services.speakeasy.client;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import androidx.test.services.speakeasy.SpeakEasyProtocol;

/**
 * A daemon that reverses the strings it receives.
 *
 * <p>This daemon is used to test the ability of our ToolConnection to register itself with
 * SpeakEasy without having a context object.
 */
class Reverser {
  private static final String TAG = "Reverse";

  private Messenger m =
      new Messenger(
          new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message m) {
              String s = m.getData().getString("reverse");
              Message reply = Message.obtain();
              reply.getData().putString("reverse", new StringBuilder(s).reverse().toString());
              Log.i("REVERSE", " Got: " + m + " sending: " + reply);
              try {
                m.replyTo.send(reply);
              } catch (RemoteException re) {
                Log.e(TAG, "Could not send: " + reply, re);
              }
            }
          });

  IBinder getBinder() {
    return m.getBinder();
  }

  public static class Register implements Runnable {
    private Reverser r;

    public Register(Reverser r) {
      this.r = r;
    }

    @Override
    public void run() {
      Connection c =
          ToolConnection.makeConnection(
              "androidx.test.services.speakeasy.server.testapp",
              "testapp.androidx_test_services.speak_easy");
      ((ToolConnection) c)
          .publish(
              "REVERSER",
              r.getBinder(),
              new PublishResultReceiver(new Handler(Looper.getMainLooper())) {
                @Override
                public void handlePublishResult(SpeakEasyProtocol.PublishResult publishResult) {
                  Log.i(TAG, "Publish result: " + publishResult);
                  if (!publishResult.published) {
                    Log.e(TAG, "Failed to publish: " + publishResult.error);
                  }
                }
              });
    }
  }

  public static void main(String[] args) throws Exception {
    Looper.prepareMainLooper();
    Reverser r = new Reverser();

    new Handler(Looper.myLooper()).post(new Register(r));
    Looper.myLooper().loop();
  }
}
