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

package androidx.test.espresso.base;

import static androidx.test.internal.util.Checks.checkNotNull;
import static androidx.test.internal.util.Checks.checkState;

import android.os.Binder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import androidx.annotation.VisibleForTesting;

/** Isolates the nasty details of touching the message queue. */
final class Interrogator {

  private static final String TAG = "Interrogator";

  @VisibleForTesting static final int LOOKAHEAD_MILLIS = 15;
  private static final ThreadLocal<Boolean> interrogating =
      new ThreadLocal<Boolean>() {
        @Override
        public Boolean initialValue() {
          return Boolean.FALSE;
        }
      };



  /** Informed of the state of the queue and controls whether to continue interrogation or quit. */
  interface QueueInterrogationHandler<R> {
    /**
     * called when the queue is empty
     *
     * @return true to continue interrogating, false otherwise.
     */
    public boolean queueEmpty();

    /**
     * called when the next task on the queue will be executed soon.
     *
     * @return true to continue interrogating, false otherwise.
     */
    public boolean taskDueSoon();

    /**
     * called when the next task on the queue will be executed in a long time.
     *
     * @return true to continue interrogating, false otherwise.
     */
    public boolean taskDueLong();

    /** Called when a barrier has been detected. */
    public boolean barrierUp();

    /** Called after interrogation has requested to end. */
    public R get();
  }

  /**
   * Informed of the state of the looper/queue and controls whether to continue interrogation or
   * quit.
   */
  interface InterrogationHandler<R> extends QueueInterrogationHandler<R> {
    /**
     * Notifies that the queue is about to dispatch a task.
     *
     * @return true to continue interrogating, false otherwise. execution happens regardless.
     */
    public boolean beforeTaskDispatch();

    /** Called when the looper / message queue being interrogated is about to quit. */
    public void quitting();

    public void setMessage(Message m);

    public String getMessage();
  }

  Interrogator() {}

  /**
   * Loops the main thread and informs the interrogation handler at interesting points in the exec
   * state.
   *
   * @param handler an interrogation handler that controls whether to continue looping or not.
   */
  <T> T loopAndInterrogate(
      TestLooperManagerCompat testLooperManager, InterrogationHandler<T> handler) {
    checkSanity();
    interrogating.set(Boolean.TRUE);
    boolean stillInterested = true;

    // We may have an identity when we're called - we want to restore it at the end of the fn.
    final long entryIdentity = Binder.clearCallingIdentity();
    try {
      // this identity should not get changed by dispatching the loop until the observer is happy.
      final long threadIdentity = Binder.clearCallingIdentity();
      while (stillInterested) {
        // run until the observer is no longer interested.
        stillInterested = interrogateQueueState(testLooperManager, handler);
        if (stillInterested) {
          Message m = testLooperManager.next();

          // the observer cannot stop us from dispatching this message - but we need to let it know
          // that we're about to dispatch.
          if (null == m) {
            handler.quitting();
            return handler.get();
          }
          stillInterested = handler.beforeTaskDispatch();
          handler.setMessage(m);
          testLooperManager.execute(m);

          // ensure looper invariants
          final long newIdentity = Binder.clearCallingIdentity();
          // Detect binder id corruption.
          if (newIdentity != threadIdentity) {
            Log.wtf(
                TAG,
                "Thread identity changed from 0x"
                    + Long.toHexString(threadIdentity)
                    + " to 0x"
                    + Long.toHexString(newIdentity)
                    + " while dispatching to "
                    + m.getTarget().getClass().getName()
                    + " "
                    + m.getCallback()
                    + " what="
                    + m.what);
          }
          testLooperManager.recycle(m);
        }
      }
    } finally {
      Binder.restoreCallingIdentity(entryIdentity);
      interrogating.set(Boolean.FALSE);
    }
    return handler.get();
  }

  /**
   * Allows caller to see if the message queue is empty, has a task due soon / long, or has a
   * barrier.
   *
   * <p>This method can be called from any thread. It has limitations though - if a task is
   * currently being executed in the interrogation loop, you will not know about it. If the Looper
   * is quitting you will not know about it. You can only see the state of the message queue - which
   * is seperate from the state of the overall loop.
   *
   * @param q the message queue you wish to inspect
   * @param handler a callback that will have one of the following methods invoked on it:
   *     queueEmpty(), taskDueSoon(), taskDueLong() or barrierUp(). once and only once.
   * @return the result of handler.get()
   */
  <T> T peekAtQueueState(
      TestLooperManagerCompat testLooperManager, QueueInterrogationHandler<T> handler) {
    checkNotNull(handler);
    checkState(
        !interrogateQueueState(testLooperManager, handler),
        "It is expected that %s would stop interrogation after a single peak at the queue.",
        handler);
    return handler.get();
  }

  private boolean interrogateQueueState(
      TestLooperManagerCompat testLooperManager, QueueInterrogationHandler<?> handler) {
    synchronized (testLooperManager.getQueue()) {
      if (testLooperManager.isBlockedOnSyncBarrier()) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
          Log.d(TAG, "barrier is up");
        }
        return handler.barrierUp();
      }
      Long headWhen = testLooperManager.peekWhen();
      if (headWhen == null) {
        return handler.queueEmpty();
      }

      long nowFuz = SystemClock.uptimeMillis() + LOOKAHEAD_MILLIS;
      if (Log.isLoggable(TAG, Log.DEBUG)) {
        Log.d(
            TAG,
            "headWhen: " + headWhen + " nowFuz: " + nowFuz + " due long: " + (nowFuz < headWhen));
      }
      if (nowFuz > headWhen) {
        return handler.taskDueSoon();
      }
      return handler.taskDueLong();
    }
  }

  private void checkSanity() {
    checkState(Looper.myLooper() != null, "Calling non-looper thread!");
    checkState(Boolean.FALSE.equals(interrogating.get()), "Already interrogating!");
  }
}
