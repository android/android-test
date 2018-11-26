/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Lice`nse is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.ui.app;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import androidx.annotation.Nullable;
import android.util.Log;

/** a simple service that converts the string in request to uppercases and send it back */
public class BinderIPCRemoteServer extends Service {
  private static final String TAG = "BinderIPCRemoteServer";

  final Messenger msger = new Messenger(new MessageHandler());

  static final int TO_UPPER_CASE = -1;

  static final int TO_UPPER_CASE_RESPONSE = -2;

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return msger.getBinder();
  }

  static class MessageHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      int task = msg.what;
      switch (task) {
        case TO_UPPER_CASE:
          {
            String data = msg.getData().getString("data");
            Message response = Message.obtain(null, TO_UPPER_CASE_RESPONSE);
            Bundle bundle = new Bundle();
            bundle.putString("respData", data.toUpperCase());
            response.setData(bundle);
            try {
              Log.i(TAG, "Service processing request for 500ms");
              Thread.sleep(500); // sleep for 500 ms
              msg.replyTo.send(response);
            } catch (RemoteException re) {
              Log.e(TAG, "Service encountered unknown error", re);
            } catch (InterruptedException ie) {
              Log.e(TAG, "Service working thread got interrupted", ie);
            }
            break;
          }
        default:
          super.handleMessage(msg);
      }
    }
  }
}
