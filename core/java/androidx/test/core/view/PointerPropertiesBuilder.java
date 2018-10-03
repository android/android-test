package androidx.test.core.view;

import android.view.MotionEvent.PointerProperties;

/** Builder for {@link PointerProperties}. */
public class PointerPropertiesBuilder {

  private int id;
  private int toolType;

  private PointerPropertiesBuilder() {}

  public PointerPropertiesBuilder setId(int id) {
    this.id = id;
    return this;
  }

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
