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

package androidx.test.services.shellexecutor;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.test.services.speakeasy.SpeakEasyProtocol.FindResult;
import androidx.test.services.speakeasy.client.AppConnection;
import androidx.test.services.speakeasy.client.Connection;
import androidx.test.services.speakeasy.client.FindResultReceiver;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Synchronously gets a {@link FindResult} from SpeakEasy
 *
 * @throws InterruptedException if find not successful in 5 seconds.
 */
final class BlockingFind extends FindResultReceiver {

  private static final String TAG = "FindResultReceiver";
  private static final int WAIT_TIME = 30;
  private static final int WAIT_INTERVAL = 5;

  private final CountDownLatch latch;
  private volatile FindResult findResult;

  private BlockingFind(Handler h) {
    super(h);
    latch = new CountDownLatch(1);
  }

  @Override
  protected void handleFindResult(FindResult findResult) {
    this.findResult = findResult;
    latch.countDown();
  }

  private FindResult waitOnResult() throws InterruptedException {
    int awaitTime = 0;
    while (true) {
      if (latch.await(WAIT_INTERVAL, TimeUnit.SECONDS)) {
        return findResult;
      } else {
        awaitTime += WAIT_INTERVAL;
        if (awaitTime < WAIT_TIME) {
          Log.i(TAG, "Waiting " + awaitTime + " for SpeakEasy find");
        } else {
          throw new InterruptedException(
              "Timed out after " + WAIT_TIME + " seconds while waiting for SpeakEasy find");
        }
      }
    }
  }

  public static FindResult getResult(Looper looper, Context context, String key)
      throws InterruptedException {
    BlockingFind receiver = new BlockingFind(new Handler(looper));
    Connection connection = new AppConnection(context);
    connection.find(key, receiver);
    return receiver.waitOnResult();
  }
};
