/*
 * Copyright (C) 2016 The Android Open Source Project
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

package androidx.test.espresso.base;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import androidx.test.espresso.IdlingResource;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/** An InterrogationHandler which determines whether a looper is in an idle state. */
class LooperIdlingResourceInterrogationHandler
    implements Interrogator.InterrogationHandler<Void>, IdlingResource {
  private static final ConcurrentHashMap<String, LooperIdlingResourceInterrogationHandler> insts =
      new ConcurrentHashMap<>();

  private final Interrogator.QueueInterrogationHandler<Boolean> queueHasNewTasks =
      new Interrogator.QueueInterrogationHandler<Boolean>() {
        private Boolean hasTasks = Boolean.FALSE;

        @Override
        public Boolean get() {
          return hasTasks;
        }

        @Override
        public boolean queueEmpty() {
          hasTasks = Boolean.FALSE;
          return false;
        }

        @Override
        public boolean taskDueLong() {
          // far in the future, don't care.
          hasTasks = Boolean.FALSE;
          return false;
        }

        @Override
        public boolean taskDueSoon() {
          hasTasks = Boolean.TRUE;
          return false;
        }

        @Override
        public boolean barrierUp() {
          hasTasks = Boolean.TRUE;
          return false;
        }
      };

  private final String name;

  // read on main - written on looper
  private volatile boolean started = false;
  private volatile MessageQueue queue = null;
  private volatile boolean idle = true;

  // written on main - read on looper
  private volatile IdlingResource.ResourceCallback cb = null;

  private LooperIdlingResourceInterrogationHandler(String name) {
    this.name = name;
  }

  static LooperIdlingResourceInterrogationHandler forLooper(Looper l) {
    String name =
        String.format(
            Locale.ROOT,
            "LooperIdlingResource-%s-%s",
            l.getThread().getId(),
            l.getThread().getName());
    final LooperIdlingResourceInterrogationHandler ir =
        new LooperIdlingResourceInterrogationHandler(name);
    LooperIdlingResourceInterrogationHandler previous = insts.putIfAbsent(name, ir);
    if (null != previous) {
      return previous;
    }
    new Handler(l)
        .post(
            new Runnable() {
              @Override
              public void run() {
                ir.queue = Looper.myQueue();
                ir.started = true;
                Interrogator.loopAndInterrogate(ir);
              }
            });

    return ir;
  }

  @Override
  public void setMessage(Message m) {}

  @Override
  public String getMessage() {
    return null;
  }

  @Override
  public void quitting() {
    transitionToIdle();
  }

  @Override
  public boolean queueEmpty() {
    transitionToIdle();
    return true;
  }

  @Override
  public boolean taskDueLong() {
    transitionToIdle();
    return true;
  }

  @Override
  public boolean beforeTaskDispatch() {
    idle = false;
    return true;
  }

  @Override
  public boolean taskDueSoon() {
    idle = false;
    return true;
  }

  @Override
  public boolean barrierUp() {
    idle = false;
    return true;
  }

  @Override
  public Void get() {
    return null;
  }

  @Override
  public boolean isIdleNow() {
    if (!started) {
      return false;
    }
    if (idle) {
      // make sure nothing has arrived in the queue while the looper thread is waiting to pull a
      // new task out of it. There can be some delay between a new message entering the queue and
      // the looper thread pulling it out and processing it.
      return Boolean.FALSE.equals(Interrogator.peekAtQueueState(queue, queueHasNewTasks));
    }
    return false;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void registerIdleTransitionCallback(IdlingResource.ResourceCallback cb) {
    this.cb = cb;
  }

  private void transitionToIdle() {
    idle = true;
    if (null != cb) {
      cb.onTransitionToIdle();
    }
  }
}
