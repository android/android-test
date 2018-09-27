/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.ui.app;

import static com.google.common.base.Preconditions.checkNotNull;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;
import com.google.common.annotations.VisibleForTesting;
import java.util.Random;

/**
 * Displays "hello world" with a random delay of 2 to 7s after the user clicks on a button. This is
 * used to demonstrate how Espresso can synchronize with any part of your application, which may
 * cause the application state to be unstable (e.g. a network call).
 */
public class SyncActivity extends Activity {

  /**
   * A server that returns a hello world string
   */
  public interface HelloWorldServer {
    String getHelloWorld();
  }

  private HelloWorldServer helloWorldServer;
  private TextView statusTextView;

  @Override
  protected void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    setContentView(R.layout.sync_activity);

    statusTextView = checkNotNull(((TextView) findViewById(R.id.status_text)));

    setHelloWorldServer(new HelloWorldServer() {
      @Override
      public String getHelloWorld() {
        Random rand = new Random();
        SystemClock.sleep(rand.nextInt(5000) + 2000);
        return getString(R.string.hello_world);
      }
    });
  }

  public void onRequestButtonClick(@SuppressWarnings("unused") View view) {
    Thread t = new Thread() {
      @Override
      public void run() {
        final String helloworld = helloWorldServer.getHelloWorld();
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            setStatus(helloworld);
          }
        });
      }
    };
    t.start();
  }

  private void setStatus(String text) {
    statusTextView.setText(text);
  }

  @VisibleForTesting
  public HelloWorldServer getHelloWorldServer() {
    return helloWorldServer;
  }

  @VisibleForTesting
  public void setHelloWorldServer(HelloWorldServer helloWorldServer) {
    this.helloWorldServer = helloWorldServer;
  }
}
