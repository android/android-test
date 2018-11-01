package androidx.test.core.view;

import static androidx.test.core.view.MotionEventBuilder.newBuilder;
import static com.google.common.truth.Truth.assertThat;

import android.os.SystemClock;
import android.view.MotionEvent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.Range;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link MotionEventBuilder}. */
@RunWith(AndroidJUnit4.class)
public final class MotionEventBuilderTest {

  @Test
  public void emptyBuilder() throws Exception {
    MotionEvent event = newBuilder().build();
    assertThat(event.getDownTime()).isEqualTo(0);
    assertThat(event.getEventTime())
        .isIn(Range.closed(SystemClock.uptimeMillis() - 1000, SystemClock.uptimeMillis()));
    assertThat(event.getAction()).isEqualTo(MotionEvent.ACTION_DOWN);
    assertThat(event.getPointerCount()).isEqualTo(1);
    assertThat(event.getX()).isEqualTo(0f);
    assertThat(event.getY()).isEqualTo(0f);
    assertThat(event.getRawX()).isEqualTo(0f);
    assertThat(event.getRawY()).isEqualTo(0f);
    assertThat(event.getMetaState()).isEqualTo(0);
    assertThat(event.getButtonState()).isEqualTo(0);
    assertThat(event.getXPrecision()).isEqualTo(0f);
    assertThat(event.getYPrecision()).isEqualTo(0f);
    assertThat(event.getDeviceId()).isEqualTo(0);
    assertThat(event.getEdgeFlags()).isEqualTo(0);
  }

  @Test
  public void buildAllFields() {
    MotionEvent event =
        newBuilder()
            .setDownTime(1)
            .setEventTime(2)
            .setAction(MotionEvent.ACTION_CANCEL)
            .setPointer(3f, 4f)
            .setPointer(5f, 6f)
            .setMetaState(7)
            .setButtonState(8)
            .setXPrecision(9f)
            .setYPrecision(10f)
            .setDeviceId(11)
            .setEdgeFlags(12)
            .setSource(13)
            .setFlags(14)
            .build();

    assertThat(event.getDownTime()).isEqualTo(1);
    assertThat(event.getEventTime()).isEqualTo(2);
    assertThat(event.getAction()).isEqualTo(MotionEvent.ACTION_CANCEL);
    assertThat(event.getPointerCount()).isEqualTo(2);
    assertThat(event.getX()).isEqualTo(3f);
    assertThat(event.getY()).isEqualTo(4f);
    assertThat(event.getRawX()).isEqualTo(3f);
    assertThat(event.getRawY()).isEqualTo(4f);
    assertThat(event.getX(1)).isEqualTo(5f);
    assertThat(event.getY(1)).isEqualTo(6f);
    assertThat(event.getMetaState()).isEqualTo(7);
    assertThat(event.getButtonState()).isEqualTo(8);
    assertThat(event.getXPrecision()).isEqualTo(9f);
    assertThat(event.getYPrecision()).isEqualTo(10f);
    assertThat(event.getDeviceId()).isEqualTo(11);
    assertThat(event.getEdgeFlags()).isEqualTo(12);
  }

  @Test
  public void withActionIndex() throws Exception {
    MotionEvent event = newBuilder().setAction(MotionEvent.ACTION_POINTER_UP).build();
    assertThat(event.getActionMasked()).isEqualTo(MotionEvent.ACTION_POINTER_UP);
    assertThat(event.getActionIndex()).isEqualTo(0);

    event = newBuilder().setAction(MotionEvent.ACTION_POINTER_UP).setActionIndex(1).build();
    assertThat(event.getActionMasked()).isEqualTo(MotionEvent.ACTION_POINTER_UP);
    assertThat(event.getActionIndex()).isEqualTo(1);
  }
}
