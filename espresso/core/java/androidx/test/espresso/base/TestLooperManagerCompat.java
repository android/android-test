package androidx.test.espresso.base;

import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import androidx.annotation.Nullable;
import androidx.test.internal.platform.reflect.ReflectiveField;
import androidx.test.internal.platform.reflect.ReflectiveMethod;

/**
 * Compat class that supports the {@link android.os.TestLooperManager} Baklava+ functionality on
 * older Android SDKs.
 *
 * <p>Unlike the real TestLooperManager this only supports being used from the Looper's thread.
 */
class TestLooperManagerCompat {

  private static final ReflectiveMethod<Message> messageQueueNextMethod =
      new ReflectiveMethod<>(MessageQueue.class, "next");

  private static final ReflectiveField<Message> messageQueueHeadField =
      new ReflectiveField<>(MessageQueue.class, "mMessages");

  private static final ReflectiveMethod<Void> recycleUncheckedMethod =
      new ReflectiveMethod<>(Message.class, "recycleUnchecked");
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

  private @Nullable Message legacyPeek() {
    synchronized (queue) {
      return messageQueueHeadField.get(queue);
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
    return messageQueueNextMethod.invoke(queue);
  }

  void recycle(Message m) {
    recycleUncheckedMethod.invoke(m);
  }
}
