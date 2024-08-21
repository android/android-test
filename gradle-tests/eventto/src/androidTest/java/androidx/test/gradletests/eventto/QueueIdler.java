package androidx.test.gradletests.eventto;

import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;

import android.os.Message;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.util.Log;

import androidx.test.internal.platform.reflect.ReflectionException;
import androidx.test.internal.platform.reflect.ReflectiveField;
import androidx.test.internal.platform.reflect.ReflectiveMethod;

class QueueIdler {

    private final ReflectiveField<Message> messagesField = new ReflectiveField<> (MessageQueue.class, "mMessages");
    private final ReflectiveMethod<Message> nextMethod = new ReflectiveMethod<>(MessageQueue.class, "next");

    void idle() {
      idleUntil(new IdleCondition());
    }

    void idleUntil(Condition condition) {
        try {
            while (!condition.check()) {
                Message message = nextMethod.invoke(myLooper().getQueue());
                message.getTarget().dispatchMessage(message);
                message.recycle();
            }
        } catch (ReflectionException e) {
            throw new IllegalStateException(e);
        }
    }

    interface Condition {
        boolean check();
    }

    private class IdleCondition implements Condition {

        @Override
        public boolean check() {
            try {
                Message nextMsg = messagesField.get(myLooper().getQueue());
                if (nextMsg != null) {
                    if (nextMsg.getTarget() == null) {
                        // sync barrier
                        return false;
                    }
                    long nowFuz = SystemClock.uptimeMillis() + 15;
                    if (nextMsg.getWhen() <= nowFuz) {
                        return false;
                    }
                }
                return true;
            } catch ( ReflectionException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
