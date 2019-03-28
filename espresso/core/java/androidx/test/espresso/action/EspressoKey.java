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

package androidx.test.espresso.action;

import static com.google.common.base.Preconditions.checkState;

import android.os.Build;
import android.view.KeyEvent;
import androidx.test.espresso.remote.annotation.RemoteMsgConstructor;
import androidx.test.espresso.remote.annotation.RemoteMsgField;
import java.util.Locale;

/** Class that wraps the key code and meta state of the desired key press. */
public final class EspressoKey {
  @RemoteMsgField(order = 0)
  private final int keyCode;

  @RemoteMsgField(order = 1)
  private final int metaState;

  private EspressoKey(Builder builder) {
    this(builder.builderKeyCode, builder.getMetaState());
  }

  @RemoteMsgConstructor
  EspressoKey(int keyCode, int metaState) {
    this.keyCode = keyCode;
    this.metaState = metaState;
  }

  public int getKeyCode() {
    return keyCode;
  }

  public int getMetaState() {
    return metaState;
  }

  @Override
  public String toString() {
    return String.format(Locale.ROOT, "keyCode: %s, metaState: %s", keyCode, metaState);
  }

  /** Builder for the EspressoKey class. */
  public static class Builder {
    private int builderKeyCode = -1;
    private boolean isShiftPressed;
    private boolean isAltPressed;
    private boolean isCtrlPressed;

    public Builder withKeyCode(int keyCode) {
      builderKeyCode = keyCode;
      return this;
    }

    /** Sets the SHIFT_ON meta state of the resulting key. */
    public Builder withShiftPressed(boolean shiftPressed) {
      isShiftPressed = shiftPressed;
      return this;
    }

    /**
     * On Honeycomb and above, sets the CTRL_ON meta state of the resulting key. On Gingerbread and
     * below, this is a noop.
     */
    public Builder withCtrlPressed(boolean ctrlPressed) {
      isCtrlPressed = ctrlPressed;
      return this;
    }

    /** Sets the ALT_ON meta state of the resulting key. */
    public Builder withAltPressed(boolean altPressed) {
      isAltPressed = altPressed;
      return this;
    }

    private int getMetaState() {
      int metaState = 0;
      if (isShiftPressed) {
        metaState |= KeyEvent.META_SHIFT_ON;
      }

      if (isAltPressed) {
        metaState |= KeyEvent.META_ALT_ON;
      }

      if (isCtrlPressed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        metaState |= KeyEvent.META_CTRL_ON;
      }

      return metaState;
    }

    public EspressoKey build() {
      checkState(
          builderKeyCode > 0 && builderKeyCode < KeyEvent.getMaxKeyCode(),
          "Invalid key code: %s",
          builderKeyCode);
      return new EspressoKey(this);
    }
  }
}
