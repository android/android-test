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

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.test.services.speakeasy.SpeakEasyProtocol.PublishResult;
import androidx.test.services.speakeasy.client.PublishResultReceiver;
import androidx.test.services.speakeasy.client.ToolConnection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Synchronously publishes a {@link IBinder} and returns a {@link PublishResult} from SpeakEasy.
 *
 * @throws InterruptedException if find not successful in 5 seconds.
 */
final class BlockingPublish extends PublishResultReceiver {

  private static final String TAG = "PublishResultReceiver";
  private static final int WAIT_TIME = 30;
  private static final int WAIT_INTERVAL = 5;

  private final CountDownLatch latch;
  private volatile PublishResult publishResult;

  private BlockingPublish(Handler h) {
    super(h);
    latch = new CountDownLatch(1);
  }

  @Override
  protected void handlePublishResult(PublishResult findResult) {
    this.publishResult = findResult;
    latch.countDown();
  }

  private PublishResult waitOnResult() throws InterruptedException {
    int awaitTime = 0;
    while (true) {
      if (latch.await(WAIT_INTERVAL, TimeUnit.SECONDS)) {
        Log.d(TAG, "Publish successful");
        return publishResult;
      } else {
        awaitTime += WAIT_INTERVAL;
        if (awaitTime < WAIT_TIME) {
          Log.i(TAG, "Waiting " + awaitTime + " for SpeakEasy publish");
        } else {
          throw new InterruptedException(
              "Timed out after " + WAIT_TIME + " seconds while waiting for SpeakEasy publish");
        }
      }
    }
  }

  public static PublishResult getResult(Looper looper, IBinder binder) throws InterruptedException {
    BlockingPublish receiver = new BlockingPublish(new Handler(looper));
    ToolConnection.makeConnection().publish(binder, receiver);
    return receiver.waitOnResult();
  }
};
