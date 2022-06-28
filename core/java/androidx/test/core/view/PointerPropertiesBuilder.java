package androidx.test.core.view;

import android.view.MotionEvent.PointerProperties;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/** Builder for {@link PointerProperties}. */
public class PointerPropertiesBuilder {

  private int id;
  private int toolType;

  private PointerPropertiesBuilder() {}

  @CanIgnoreReturnValue
  public PointerPropertiesBuilder setId(int id) {
    this.id = id;
    return this;
  }

  @CanIgnoreReturnValue
  public PointerPropertiesBuilder setToolType(int toolType) {
    this.toolType = toolType;
    return this;
  }

  public PointerProperties build() {
    final PointerProperties pointerProperties = new PointerProperties();
    pointerProperties.id = id;
    pointerProperties.toolType = toolType;
    return pointerProperties;
  }

  public static PointerPropertiesBuilder newBuilder() {
    return new PointerPropertiesBuilder();
  }
}
