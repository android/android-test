/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.test.core.view;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import java.util.ArrayList;
import java.util.List;

/**
 * A helper builder for creating {@link MotionEvent}'s.
 *
 * <p>Default values for unspecified attributes are 0 unless otherwise noted.
 */
public class MotionEventBuilder {

  private long downTime = 0;
  private long eventTime = SystemClock.uptimeMillis();
  private int action = MotionEvent.ACTION_DOWN;
  private int actionIndex = -1;
  private List<PointerProperties> pointerPropertiesList = new ArrayList<>();
  private List<PointerCoords> pointerCoordsList = new ArrayList<>();
  private int metaState = 0;
  private int buttonState = 0;
  private float xPrecision = 0f;
  private float yPrecision = 0f;
  private int deviceId = 0;
  private int edgeFlags = 0;
  private int source = 0;
  private int flags = 0;

  private MotionEventBuilder() {}

  /**
   * Start building a new MotionEvent.
   *
   * @return a new MotionEventBuilder.
   */
  public static MotionEventBuilder newBuilder() {
    return new MotionEventBuilder();
  }

  /**
   * Sets the down time.
   *
   * @see MotionEvent#getDownTime()
   */
  public MotionEventBuilder setDownTime(long downTime) {
    this.downTime = downTime;
    return this;
  }

  /**
   * Sets the event time. Default is SystemClock.uptimeMillis().
   *
   * @see MotionEvent#getEventTime()
   */
  public MotionEventBuilder setEventTime(long eventTime) {
    this.eventTime = eventTime;
    return this;
  }

  /**
   * Sets the action. Default is {@link MotionEvent.ACTION_DOWN}.
   *
   * @see MotionEvent#getAction()
   */
  public MotionEventBuilder setAction(int action) {
    this.action = action;
    return this;
  }

  /**
   * Sets the pointer index associated with the action.
   *
   * @see MotionEvent#getActionIndex()
   */
  public MotionEventBuilder setActionIndex(int pointerIndex) {
    checkState(pointerIndex <= 0xFF, "pointerIndex must be less than 0xff");
    this.actionIndex = pointerIndex;
    return this;
  }

  /**
   * Sets the metaState.
   *
   * @see MotionEvent#getMetaState()
   */
  public MotionEventBuilder setMetaState(int metastate) {
    this.metaState = metastate;
    return this;
  }

  /**
   * Sets the button state.
   *
   * @see MotionEvent#getButtonState()
   */
  public MotionEventBuilder setButtonState(int buttonState) {
    this.buttonState = buttonState;
    return this;
  }

  /**
   * Sets the x precision.
   *
   * @see MotionEvent#getXPrecision()
   */
  public MotionEventBuilder setXPrecision(float xPrecision) {
    this.xPrecision = xPrecision;
    return this;
  }

  /**
   * Sets the y precision.
   *
   * @see MotionEvent#getYPrecision()
   */
  public MotionEventBuilder setYPrecision(float yPrecision) {
    this.yPrecision = yPrecision;
    return this;
  }

  /**
   * Sets the device id.
   *
   * @see MotionEvent#getDeviceId()
   */
  public MotionEventBuilder setDeviceId(int deviceId) {
    this.deviceId = deviceId;
    return this;
  }

  /**
   * Sets the edge flags.
   *
   * @see MotionEvent#getEdgeFlags()
   */
  public MotionEventBuilder setEdgeFlags(int edgeFlags) {
    this.edgeFlags = edgeFlags;
    return this;
  }

  /**
   * Sets the source.
   *
   * @see MotionEvent#getSource()
   */
  public MotionEventBuilder setSource(int source) {
    this.source = source;
    return this;
  }

  /**
   * Sets the flags.
   *
   * @see MotionEvent#getFlags()
   */
  public MotionEventBuilder setFlags(int flags) {
    this.flags = flags;
    return this;
  }

  /**
   * Simple mechanism to add a pointer to the MotionEvent.
   *
   * <p>Can be called multiple times to add multiple pointers to the event.
   */
  public MotionEventBuilder setPointer(float x, float y) {
    PointerProperties pointerProperties = new PointerProperties();
    pointerProperties.id = pointerPropertiesList.size();
    PointerCoords pointerCoords = new PointerCoords();
    pointerCoords.x = x;
    pointerCoords.y = y;
    return setPointer(pointerProperties, pointerCoords);
  }

  /**
   * An expanded variant of {@link #setPointer(float, float)} that supports specifying all pointer
   * properties and coords data.
   */
  public MotionEventBuilder setPointer(
      PointerProperties pointerProperties, PointerCoords pointerCoords) {
    pointerPropertiesList.add(pointerProperties);
    pointerCoordsList.add(pointerCoords);
    return this;
  }

  /** Returns a MotionEvent with the provided data or reasonable defaults. */
  public MotionEvent build() {
    if (pointerPropertiesList.size() == 0) {
      setPointer(0, 0);
    }
    if (actionIndex != -1) {
      action = action | (actionIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
    }
    return MotionEvent.obtain(
        downTime,
        eventTime,
        action,
        pointerPropertiesList.size(),
        pointerPropertiesList.toArray(new PointerProperties[pointerPropertiesList.size()]),
        pointerCoordsList.toArray(new MotionEvent.PointerCoords[pointerCoordsList.size()]),
        metaState,
        buttonState,
        xPrecision,
        yPrecision,
        deviceId,
        edgeFlags,
        source,
        flags);
  }

  private static void checkState(boolean expression, String message) {
    if (!expression) {
      throw new IllegalStateException(message);
    }
  }
}
