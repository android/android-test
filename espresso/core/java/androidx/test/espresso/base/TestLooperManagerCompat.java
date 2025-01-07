package androidx.test.espresso.base;

import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import androidx.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Compat class that supports the {@link android.os.TestLooperManager} Baklava+ functionality on
 * older Android SDKs.
 *
 * <p>Unlike the real TestLooperManager this only supports being used from the Looper's thread.
 */
class TestLooperManagerCompat {

  private static final Method messageQueueNextMethod;
  private static final Field messageQueueHeadField;
  private static final Method recycleUncheckedMethod;

  static {
    try {
      messageQueueNextMethod = MessageQueue.class.getDeclaredMethod("next");
      messageQueueNextMethod.setAccessible(true);
      messageQueueHeadField = MessageQueue.class.getDeclaredField("mMessages");
      messageQueueHeadField.setAccessible(true);
      recycleUncheckedMethod = Message.class.getDeclaredMethod("recycleUnchecked");
      recycleUncheckedMethod.setAccessible(true);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private final MessageQueue queue;

  private TestLooperManagerCompat(MessageQueue queue) {
    this.queue = queue;
  }

  static TestLooperManagerCompat acquire(Looper looper) {
    return new TestLooperManagerCompat(looper.getQueue());
  }

  @Nullable
  Long peekWhen() {
    Message msg = legacyPeek();
    if (msg != null && msg.getTarget() == null) {
      return null;
    }
    return msg == null ? null : msg.getWhen();
  }

  @Nullable
  private Message legacyPeek() {
    // the legacy MessageQueue implementation synchronizes on itself,
    // so this uses the same lock
    synchronized (queue) {
      try {
        return (Message) messageQueueHeadField.get(queue);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }

  void execute(Message message) {
    message.getTarget().dispatchMessage(message);
  }

  void release() {
    // ignore for now
  }

  boolean isBlockedOnSyncBarrier() {
    Message msg = legacyPeek();
    return msg != null && msg.getTarget() == null;
  }

  Message next() {
    try {
      return (Message) messageQueueNextMethod.invoke(queue);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  void recycle(Message m) {
    try {
      recycleUncheckedMethod.invoke(m);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  MessageQueue getQueue() {
    return queue;
  }
}
