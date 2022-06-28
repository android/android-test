package androidx.test.core.view;

import android.view.MotionEvent.PointerCoords;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/** Builder for {@link PointerCoords}. */
public class PointerCoordsBuilder {

  private float x = 0f;
  private float y = 0f;
  private float pressure = 1.0f;
  private float size = 1.0f;
  private float touchMajor;
  private float touchMinor;
  private float toolMajor;
  private float toolMinor;
  private float orientation;

  private PointerCoordsBuilder() {}

  public static PointerCoordsBuilder newBuilder() {
    return new PointerCoordsBuilder();
  }

  @CanIgnoreReturnValue
  public PointerCoordsBuilder setCoords(float x, float y) {
    this.x = x;
    this.y = y;
    return this;
  }

  @CanIgnoreReturnValue
  public PointerCoordsBuilder setPressure(float pressure) {
    this.pressure = pressure;
    return this;
  }

  @CanIgnoreReturnValue
  public PointerCoordsBuilder setSize(float size) {
    this.size = size;
    return this;
  }

  @CanIgnoreReturnValue
  public PointerCoordsBuilder setTouch(float touchMajor, float touchMinor) {
    this.touchMajor = touchMajor;
    this.touchMinor = touchMinor;
    return this;
  }

  @CanIgnoreReturnValue
  public PointerCoordsBuilder setTool(float toolMajor, float toolMinor) {
    this.toolMajor = toolMajor;
    this.toolMinor = toolMinor;
    return this;
  }

  @CanIgnoreReturnValue
  public PointerCoordsBuilder setOrientation(float orientation) {
    this.orientation = orientation;
    return this;
  }

  public PointerCoords build() {
    final PointerCoords pointerCoords = new PointerCoords();
    pointerCoords.x = x;
    pointerCoords.y = y;
    pointerCoords.pressure = pressure;
    pointerCoords.size = size;
    pointerCoords.touchMajor = touchMajor;
    pointerCoords.touchMinor = touchMinor;
    pointerCoords.toolMajor = toolMajor;
    pointerCoords.toolMinor = toolMinor;
    pointerCoords.orientation = orientation;
    return pointerCoords;
  }
}
