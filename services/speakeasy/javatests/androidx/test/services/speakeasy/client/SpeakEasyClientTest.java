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

import static com.google.common.truth.Truth.assertWithMessage;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import androidx.test.InstrumentationRegistry;
import androidx.test.services.speakeasy.SpeakEasyProtocol;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.concurrent.GuardedBy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Integration tests for SpeakEasy */
@RunWith(JUnit4.class)
public class SpeakEasyClientTest {

  private AppConnection appConnection;
  private Messenger messenger;
  private RecordingHandler recordingHandler;

  @Before
  public void setUp() {
    appConnection =
        new AppConnection(
            InstrumentationRegistry.getTargetContext(),
            "androidx.test.services.speakeasy.server.testapp",
            AppConnection.SERVICE,
            new SecureRandom());
    recordingHandler = new RecordingHandler(Looper.getMainLooper());
    messenger = new Messenger(recordingHandler);
  }

  @Test
  public void testPublishAndFind() throws Exception {
    SpeakEasyProtocol.PublishResult pr = publish(messenger.getBinder());
    assertWithMessage("binder got published: " + pr.error).that(pr.published).isTrue();
    SpeakEasyProtocol.FindResult fr = find(pr.key);
    assertWithMessage("binder was found").that(fr.found).isTrue();
    Messenger myMessenger = new Messenger(fr.binder);
    Message m = Message.obtain();
    m.what = 15;

    myMessenger.send(m);
    boolean found = false;
    for (int i = 0; i < 10 && !found; i++) {
      List<Integer> whats = recordingHandler.getAndResetWhats();
      if (whats.size() != 0) {
        found = true;
        assertWithMessage("message was delivered").that(whats).containsExactly(15);
      }
      Thread.sleep(200);
    }
    assertWithMessage("Did we find the message?").that(found).isTrue();
  }


  private SpeakEasyProtocol.FindResult find(String key) {
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<SpeakEasyProtocol.FindResult> fr = new AtomicReference<>(null);
    appConnection.find(
        key,
        new FindResultReceiver(new Handler(Looper.getMainLooper())) {
          @Override
          public void handleFindResult(SpeakEasyProtocol.FindResult findResult) {
            fr.set(findResult);
            latch.countDown();
          }
        });

    try {
      assertWithMessage("ResultReceiver has run").that(latch.await(10, TimeUnit.SECONDS)).isTrue();
    } catch (InterruptedException ie) {
      throw new RuntimeException(ie);
    }
    return fr.get();
  }

  private SpeakEasyProtocol.PublishResult publish(IBinder val) {
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<SpeakEasyProtocol.PublishResult> pr = new AtomicReference<>(null);
    appConnection.publish(
        val,
        new PublishResultReceiver(new Handler(Looper.getMainLooper())) {
          @Override
          public void handlePublishResult(SpeakEasyProtocol.PublishResult publishResult) {
            pr.set(publishResult);
            latch.countDown();
          }
        });

    try {
      assertWithMessage("ResultReceiver has run").that(latch.await(10, TimeUnit.SECONDS)).isTrue();
    } catch (InterruptedException ie) {
      throw new RuntimeException(ie);
    }
    return pr.get();
  }

  private static class RecordingHandler extends Handler {
    private final Object lock = new Object();

    @GuardedBy("lock")
    private List<Integer> whats = new ArrayList<>();

    @GuardedBy("lock")
    private List<String> reverses = new ArrayList<>();

    public RecordingHandler(Looper l) {
      super(l);
    }

    @Override
    public void handleMessage(Message m) {
      synchronized (lock) {
        whats.add(m.what);
        if (m.getData().getString("reverse") != null) {
          reverses.add(m.getData().getString("reverse"));
        }
      }
    }

    public List<String> getAndResetReverses() {
      synchronized (lock) {
        List<String> myReverses = new ArrayList<>(reverses);
        reverses.clear();
        return myReverses;
      }
    }

    public List<Integer> getAndResetWhats() {
      synchronized (lock) {
        List<Integer> myWhats = new ArrayList<>(whats);
        whats.clear();
        return myWhats;
      }
    }
  }
}
