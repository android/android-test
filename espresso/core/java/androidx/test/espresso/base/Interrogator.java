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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Throwables.throwIfUnchecked;

import android.os.Binder;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** Isolates the nasty details of touching the message queue. */
final class Interrogator {

  private static final String TAG = "Interrogator";
  private static final Method messageQueueNextMethod;
  private static final Field messageQueueHeadField;
  private static final Method recycleUncheckedMethod;

  private static final int LOOKAHEAD_MILLIS = 15;
  private static final ThreadLocal<Boolean> interrogating =
      new ThreadLocal<Boolean>() {
        @Override
        public Boolean initialValue() {
          return Boolean.FALSE;
        }
      };

  static {
    try {
      messageQueueNextMethod = MessageQueue.class.getDeclaredMethod("next");
      messageQueueNextMethod.setAccessible(true);

      messageQueueHeadField = MessageQueue.class.getDeclaredField("mMessages");
      messageQueueHeadField.setAccessible(true);
    } catch (IllegalArgumentException
        | NoSuchFieldException
        | SecurityException
        | NoSuchMethodException e) {
      Log.e(TAG, "Could not initialize interrogator!", e);
      throw new RuntimeException("Could not initialize interrogator!", e);
    }

    Method recycleUnchecked = null;
    try {
      recycleUnchecked = Message.class.getDeclaredMethod("recycleUnchecked");
      recycleUnchecked.setAccessible(true);
    } catch (NoSuchMethodException expectedOnLowerApiLevels) {
    }
    recycleUncheckedMethod = recycleUnchecked;
  }

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


  /**
   * Loops the main thread and informs the interrogation handler at interesting points in the exec
   * state.
   *
   * @param handler an interrogation handler that controls whether to continue looping or not.
   */
  static <R> R loopAndInterrogate(InterrogationHandler<R> handler) {
    checkSanity();
    interrogating.set(Boolean.TRUE);
    boolean stillInterested = true;
    MessageQueue q = Looper.myQueue();
    // We may have an identity when we're called - we want to restore it at the end of the fn.
    final long entryIdentity = Binder.clearCallingIdentity();
    try {
      // this identity should not get changed by dispatching the loop until the observer is happy.
      final long threadIdentity = Binder.clearCallingIdentity();
      while (stillInterested) {
        // run until the observer is no longer interested.
        stillInterested = interrogateQueueState(q, handler);
        if (stillInterested) {
          Message m = getNextMessage();

          // the observer cannot stop us from dispatching this message - but we need to let it know
          // that we're about to dispatch.
          if (null == m) {
            handler.quitting();
            return handler.get();
          }
          stillInterested = handler.beforeTaskDispatch();
          handler.setMessage(m);
          m.getTarget().dispatchMessage(m);

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
          recycle(m);
        }
      }
    } finally {
      Binder.restoreCallingIdentity(entryIdentity);
      interrogating.set(Boolean.FALSE);
    }
    return handler.get();
  }

  private static void recycle(Message m) {
    if (recycleUncheckedMethod != null) {
      try {
        recycleUncheckedMethod.invoke(m);
      } catch (IllegalAccessException | IllegalArgumentException | SecurityException e) {
        throwIfUnchecked(e);
        throw new RuntimeException(e);
      } catch (InvocationTargetException ite) {
        if (ite.getCause() != null) {
          throwIfUnchecked(ite.getCause());
          throw new RuntimeException(ite.getCause());
        } else {
          throw new RuntimeException(ite);
        }
      }
    } else {
      m.recycle();
    }
  }

  private static Message getNextMessage() {
    try {
      return (Message) messageQueueNextMethod.invoke(Looper.myQueue());
    } catch (IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | SecurityException e) {
      throwIfUnchecked(e);
      throw new RuntimeException(e);
    }
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
  static <R> R peekAtQueueState(MessageQueue q, QueueInterrogationHandler<R> handler) {
    checkNotNull(q);
    checkNotNull(handler);
    checkState(
        !interrogateQueueState(q, handler),
        "It is expected that %s would stop interrogation after a single peak at the queue.",
        handler);
    return handler.get();
  }

  private static boolean interrogateQueueState(
      MessageQueue q, QueueInterrogationHandler<?> handler) {
    synchronized (q) {
      final Message head;
      try {
        head = (Message) messageQueueHeadField.get(q);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
      if (null == head) {
        // no messages pending - AT ALL!
        return handler.queueEmpty();
      } else if (null == head.getTarget()) {
        // null target is a sync barrier token.
        if (Log.isLoggable(TAG, Log.DEBUG)) {
          Log.d(TAG, "barrier is up");
        }
        return handler.barrierUp();
      }
      long headWhen = head.getWhen();
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

  private static void checkSanity() {
    checkState(Looper.myLooper() != null, "Calling non-looper thread!");
    checkState(Boolean.FALSE.equals(interrogating.get()), "Already interrogating!");
  }
}
