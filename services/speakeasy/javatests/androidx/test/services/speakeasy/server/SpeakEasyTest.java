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

import static com.google.common.truth.Truth.assertThat;

import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.ResultReceiver;
import androidx.test.services.speakeasy.SpeakEasyProtocol;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.GuardedBy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link SpeakEasy}. */
@RunWith(JUnit4.class)
public class SpeakEasyTest {

  private BinderDeaths binderDeaths;
  private HandlerThread handlerThread;
  private SpeakEasy speakEasy;

  @Before
  public void setUp() {
    binderDeaths = new BinderDeaths();
    speakEasy = new SpeakEasy(binderDeaths);
    handlerThread = new HandlerThread("SpeakEasyTest");
    handlerThread.start();
  }

  @After
  public void tearDown() {
    handlerThread.quit();
  }

  @Test
  public void testPublishDead() {
    TestBinder toPublish = new TestBinder();
    toPublish.kill();
    BlockingResultReceiver rr = new BlockingResultReceiver();

    Bundle b = SpeakEasyProtocol.Publish.asBundle("foobar", toPublish, rr);
    speakEasy.serve(SpeakEasyProtocol.fromBundle(b));
    assertThat(rr.getResult(100)).isNotNull();
    SpeakEasyProtocol resp = SpeakEasyProtocol.fromBundle(rr.getResult(1));

    assertThat(resp.type).isEqualTo(SpeakEasyProtocol.PUBLISH_RESULT_TYPE);
    assertThat(resp.publishResult).isNotNull();
    assertThat(resp.publishResult.key).isEqualTo("foobar");
    assertThat(resp.publishResult.published).isFalse();

    assertThat(toPublish.death).isNull();
    assertThat(binderDeaths.deaths).containsEntry("foobar", toPublish);
    assertThat(speakEasy.size()).isEqualTo(0);
  }

  @Test
  public void testRemoveNonExistant() {
    Bundle b = SpeakEasyProtocol.Remove.asBundle("foobar");
    speakEasy.serve(SpeakEasyProtocol.fromBundle(b));
    assertThat(speakEasy.size()).isEqualTo(0);
  }

  @Test
  public void testQueryNothing() {
    BlockingResultReceiver rr = new BlockingResultReceiver();
    Bundle b = SpeakEasyProtocol.Find.asBundle("foobar", rr);
    speakEasy.serve(SpeakEasyProtocol.fromBundle(b));
    assertThat(rr.getResult(100)).isNotNull();
    SpeakEasyProtocol resp = SpeakEasyProtocol.fromBundle(rr.getResult(1));
    assertThat(resp).isNotNull();
    assertThat(resp.type).isEqualTo(SpeakEasyProtocol.FIND_RESULT_TYPE);
    assertThat(resp.findResult).isNotNull();
    assertThat(resp.findResult.found).isFalse();
  }

  @Test
  public void testPublishQueryDieRemove() {
    TestBinder toPublish = new TestBinder();
    BlockingResultReceiver rr = new BlockingResultReceiver();

    Bundle b = SpeakEasyProtocol.Publish.asBundle("foobar", toPublish, rr);
    speakEasy.serve(SpeakEasyProtocol.fromBundle(b));
    assertThat(rr.getResult(100)).isNotNull();

    SpeakEasyProtocol resp = SpeakEasyProtocol.fromBundle(rr.getResult(1));
    assertThat(resp).isNotNull();
    assertThat(resp.type).isEqualTo(SpeakEasyProtocol.PUBLISH_RESULT_TYPE);
    assertThat(resp.publishResult).isNotNull();
    assertThat(resp.publishResult.key).isEqualTo("foobar");
    assertThat(resp.publishResult.published).isTrue();
    assertThat(toPublish.death).isNotNull();

    assertThat(speakEasy.size()).isEqualTo(1);

    // double publishing is an error.
    rr = new BlockingResultReceiver();
    b = SpeakEasyProtocol.Publish.asBundle("foobar", toPublish, rr);
    speakEasy.serve(SpeakEasyProtocol.fromBundle(b));
    assertThat(rr.getResult(100)).isNotNull();
    resp = SpeakEasyProtocol.fromBundle(rr.getResult(1));
    assertThat(resp).isNotNull();
    assertThat(resp.type).isEqualTo(SpeakEasyProtocol.PUBLISH_RESULT_TYPE);
    assertThat(resp.publishResult.key).isEqualTo("foobar");
    assertThat(resp.publishResult.error).isNotNull();
    assertThat(resp.publishResult.published).isFalse();

    assertThat(speakEasy.size()).isEqualTo(1);

    // query.
    rr = new BlockingResultReceiver();
    b = SpeakEasyProtocol.Find.asBundle("foobar", rr);
    speakEasy.serve(SpeakEasyProtocol.fromBundle(b));
    assertThat(rr.getResult(100)).isNotNull();
    resp = SpeakEasyProtocol.fromBundle(rr.getResult(1));
    assertThat(resp).isNotNull();
    assertThat(resp.type).isEqualTo(SpeakEasyProtocol.FIND_RESULT_TYPE);
    assertThat(resp.findResult).isNotNull();
    assertThat(resp.findResult.found).isTrue();
    assertThat(resp.findResult.binder).isEqualTo(toPublish);
    toPublish.kill();
    assertThat(binderDeaths.deaths).containsEntry("foobar", toPublish);

    // still need to remove it!
    assertThat(speakEasy.size()).isEqualTo(1);

    b = SpeakEasyProtocol.Remove.asBundle("foobar");
    speakEasy.serve(SpeakEasyProtocol.fromBundle(b));
    assertThat(speakEasy.size()).isEqualTo(0);
    assertThat(toPublish.death).isNull();
  }

  @Test
  public void testPublishQueryRemove() {
    TestBinder toPublish = new TestBinder();
    BlockingResultReceiver rr = new BlockingResultReceiver();

    Bundle b = SpeakEasyProtocol.Publish.asBundle("foobar", toPublish, rr);
    speakEasy.serve(SpeakEasyProtocol.fromBundle(b));
    assertThat(rr.getResult(100)).isNotNull();

    SpeakEasyProtocol resp = SpeakEasyProtocol.fromBundle(rr.getResult(1));
    assertThat(resp).isNotNull();
    assertThat(resp.type).isEqualTo(SpeakEasyProtocol.PUBLISH_RESULT_TYPE);
    assertThat(resp.publishResult).isNotNull();
    assertThat(resp.publishResult.key).isEqualTo("foobar");
    assertThat(resp.publishResult.published).isTrue();
    assertThat(toPublish.death).isNotNull();

    assertThat(speakEasy.size()).isEqualTo(1);

    // double publishing is an error.
    rr = new BlockingResultReceiver();
    b = SpeakEasyProtocol.Publish.asBundle("foobar", toPublish, rr);
    speakEasy.serve(SpeakEasyProtocol.fromBundle(b));
    assertThat(rr.getResult(100)).isNotNull();
    resp = SpeakEasyProtocol.fromBundle(rr.getResult(1));
    assertThat(resp).isNotNull();
    assertThat(resp.type).isEqualTo(SpeakEasyProtocol.PUBLISH_RESULT_TYPE);
    assertThat(resp.publishResult.key).isEqualTo("foobar");
    assertThat(resp.publishResult.error).isNotNull();
    assertThat(resp.publishResult.published).isFalse();

    assertThat(speakEasy.size()).isEqualTo(1);

    // query.
    rr = new BlockingResultReceiver();
    b = SpeakEasyProtocol.Find.asBundle("foobar", rr);
    speakEasy.serve(SpeakEasyProtocol.fromBundle(b));
    assertThat(rr.getResult(100)).isNotNull();
    resp = SpeakEasyProtocol.fromBundle(rr.getResult(1));
    assertThat(resp).isNotNull();
    assertThat(resp.type).isEqualTo(SpeakEasyProtocol.FIND_RESULT_TYPE);
    assertThat(resp.findResult).isNotNull();
    assertThat(resp.findResult.found).isTrue();
    assertThat(resp.findResult.binder).isEqualTo(toPublish);

    // still need to remove it!
    assertThat(speakEasy.size()).isEqualTo(1);

    b = SpeakEasyProtocol.Remove.asBundle("foobar");
    speakEasy.serve(SpeakEasyProtocol.fromBundle(b));
    assertThat(speakEasy.size()).isEqualTo(0);
    assertThat(toPublish.death).isNull();
    assertThat(binderDeaths.deaths).isEmpty();
  }

  private class BlockingResultReceiver extends ResultReceiver {
    BlockingResultReceiver() {
      super(new Handler(handlerThread.getLooper()));
    }

    private final Object lock = new Object();

    @GuardedBy("lock")
    private Bundle data;

    @GuardedBy("lock")
    private boolean responded = false;

    @Override
    public void onReceiveResult(int code, Bundle data) {
      synchronized (lock) {
        this.data = data;
        this.responded = true;
        lock.notifyAll();
      }
    }

    public Bundle getResult(long millis) {
      long dueAt = System.currentTimeMillis() + millis;
      synchronized (lock) {
        while (!responded && System.currentTimeMillis() < dueAt) {
          try {
            lock.wait(millis);
          } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
          }
        }
        if (!responded) {
          throw new RuntimeException("Waited over: " + millis + " ms and no response came.");
        }
        return data;
      }
    }
  }

  private static class TestBinder extends Binder {
    private boolean dead = false;
    private IBinder.DeathRecipient death;

    @Override
    public void linkToDeath(IBinder.DeathRecipient death, int flags) {
      if (dead) {
        throw new RuntimeException("I daed");
      }
      this.death = death;
    }

    @Override
    public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
      if (recipient != death) {
        throw new RuntimeException("bogus");
      }
      this.death = null;
      return !dead;
    }

    void kill() {
      dead = true;
      if (null != death) {
        this.death.binderDied();
      }
    }
  }

  private static class BinderDeaths implements SpeakEasy.BinderDeathCallback {
    private final Map<String, IBinder> deaths = new HashMap<>();

    @Override
    public void binderDeath(String key, IBinder dead) {
      this.deaths.put(key, dead);
    }
  }
}
