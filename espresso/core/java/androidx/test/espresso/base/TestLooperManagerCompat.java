package androidx.test.espresso.base;

import static androidx.test.internal.util.Checks.checkNotNull;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.TestLooperManager;
import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Compat class that supports the {@link android.os.TestLooperManager} Baklava+ functionality on
 * older Android SDKs.
 *
 * <p>Unlike the real TestLooperManager this only supports being used from the Looper's thread.
 */
@SuppressWarnings("NonFinalStaticField")
final class TestLooperManagerCompat {

  private static Method messageQueueNextMethod;
  private static Field messageQueueHeadField;
  private static Method recycleUncheckedMethod;
  private static Method peekWhenMethod;
  private static Method blockedOnBarrierMethod;

  private static boolean initTestLooperManager() {
    // TODO(b/112000181): update this check and remove reflection when compiling against Baklava
    if (VERSION.SDK_INT >= VERSION_CODES.VANILLA_ICE_CREAM) {
      try {
        peekWhenMethod = TestLooperManager.class.getDeclaredMethod("peekWhen");
        blockedOnBarrierMethod =
            TestLooperManager.class.getDeclaredMethod("isBlockedOnSyncBarrier");
        return true;
      } catch (ReflectiveOperationException e) {
        // fall through
      }
    }
    return false;
  }

  static {
    try {
      if (!initTestLooperManager()) {
        messageQueueNextMethod = MessageQueue.class.getDeclaredMethod("next");
        messageQueueNextMethod.setAccessible(true);
        messageQueueHeadField = MessageQueue.class.getDeclaredField("mMessages");
        messageQueueHeadField.setAccessible(true);
        recycleUncheckedMethod = Message.class.getDeclaredMethod("recycleUnchecked");
        recycleUncheckedMethod.setAccessible(true);
      }
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private final MessageQueue queue;

  // the TestLooperManager to defer to. Will only be non-null if running
  // on an Android API level that supports it
  private final TestLooperManager delegate;

  private TestLooperManagerCompat(MessageQueue queue) {
    this.queue = queue;
    this.delegate = null;
  }

  private TestLooperManagerCompat(TestLooperManager testLooperManager) {
    this.queue = null;
    this.delegate = testLooperManager;
  }

  static TestLooperManagerCompat acquire(Looper looper) {
    if (peekWhenMethod != null) {
      // running on a newer Android version that has the supported TestLooperManagerCompat changes
      TestLooperManager testLooperManager =
          InstrumentationRegistry.getInstrumentation().acquireLooperManager(looper);
      return new TestLooperManagerCompat(testLooperManager);
    } else {
      return new TestLooperManagerCompat(looper.getQueue());
    }
  }

  @Nullable
  Long peekWhen() {
    try {
      if (delegate != null) {
        return (Long) peekWhenMethod.invoke(delegate);
      } else {
        Message msg = legacyPeek();
        if (msg != null && msg.getTarget() == null) {
          return null;
        }
        return msg == null ? null : msg.getWhen();
      }
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  @Nullable
  private Message legacyPeek() throws IllegalAccessException {
    checkNotNull(queue);
    // the legacy MessageQueue implementation synchronizes on itself,
    // so this uses the same lock
    synchronized (queue) {
      return (Message) messageQueueHeadField.get(queue);
    }
  }

  void execute(Message message) {
    if (delegate != null) {
      delegate.execute(message);
    } else {
      message.getTarget().dispatchMessage(message);
    }
  }

  void release() {
    if (delegate != null) {
      delegate.release();
    }
  }

  boolean isBlockedOnSyncBarrier() {
    try {
      if (delegate != null) {
        return (boolean) blockedOnBarrierMethod.invoke(delegate);
      } else {
        Message msg = legacyPeek();
        return msg != null && msg.getTarget() == null;
      }
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  Message next() {
    try {
      if (delegate != null) {
        return delegate.next();
      } else {
        return (Message) messageQueueNextMethod.invoke(queue);
      }
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  void recycle(Message m) {
    try {
      if (delegate != null) {
        delegate.recycle(m);
      } else {
        recycleUncheckedMethod.invoke(m);
      }
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  MessageQueue getQueue() {
    return queue;
  }
}
