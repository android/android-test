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

package androidx.test.espresso;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import androidx.test.espresso.util.EspressoOptional;
import androidx.test.espresso.util.HumanReadables;
import com.google.common.base.MoreObjects.ToStringHelper;

/**
 * Represents a root view in the application and optionally the layout params of the window holding
 * it.
 *
 * <p>This class is used internally to determine which view root to run user provided matchers
 * against it is not part of the public api.
 */
public final class Root {
  private final View decorView;
  private final EspressoOptional<LayoutParams> windowLayoutParams;

  private Root(Builder builder) {
    this.decorView = checkNotNull(builder.decorView);
    this.windowLayoutParams = EspressoOptional.fromNullable(builder.windowLayoutParams);
  }

  public View getDecorView() {
    return decorView;
  }

  public EspressoOptional<WindowManager.LayoutParams> getWindowLayoutParams() {
    return windowLayoutParams;
  }

  /**
   * Checks if the {@link Root} is ready. The UI is no longer in flux if layout of the root view is
   * not being requested and the root view has window focus or is focusable.
   *
   * @return if the root view has focus
   */
  public boolean isReady() {
    if (!decorView.isLayoutRequested()) {
      int flags = windowLayoutParams.get().flags;
      return decorView.hasWindowFocus()
          || (flags & WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
              == WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    }
    return false;
  }

  @Override
  public String toString() {
    ToStringHelper helper =
        toStringHelper(this)
            .add("application-window-token", decorView.getApplicationWindowToken())
            .add("window-token", decorView.getWindowToken())
            .add("has-window-focus", decorView.hasWindowFocus());
    if (windowLayoutParams.isPresent()) {
      helper
          .add("layout-params-type", windowLayoutParams.get().type)
          .add("layout-params-string", windowLayoutParams.get());
    }
    helper.add("decor-view-string", HumanReadables.describe(decorView));
    return helper.toString();
  }

  public static class Builder {
    private View decorView;
    private WindowManager.LayoutParams windowLayoutParams;

    public Root build() {
      return new Root(this);
    }

    public Builder withDecorView(View view) {
      this.decorView = view;
      return this;
    }

    public Builder withWindowLayoutParams(WindowManager.LayoutParams windowLayoutParams) {
      this.windowLayoutParams = windowLayoutParams;
      return this;
    }
  }
}
