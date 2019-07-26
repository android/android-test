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

import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * Represents a root view in the application and optionally the layout params of the window holding
 * it.
 *
 * <p>This class is used internally to determine which view root to run user provided matchers
 * against it is not part of the public api.
 */
public final class Root {
  private final View decorView;
  private final LayoutParams windowLayoutParams;
  private final ViewDescriber viewDescriber;

  private Root(Builder builder, ViewDescriber viewDescriber) {
    if (builder.decorView == null) {
      throw new NullPointerException();
    }
    this.decorView = builder.decorView;
    this.windowLayoutParams = builder.windowLayoutParams;
    this.viewDescriber = viewDescriber;
  }

  public View getDecorView() {
    return decorView;
  }

  public WindowManager.LayoutParams getWindowLayoutParams() {
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
      int flags = windowLayoutParams.flags;
      return decorView.hasWindowFocus()
          || (flags & WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
              == WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    }
    return false;
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder
        .append(this.hashCode())
        .append(": application-window-token=")
        .append(decorView.getApplicationWindowToken())
        .append("window-token=")
        .append(decorView.getWindowToken())
        .append("has-window-focus=")
        .append(decorView.hasWindowFocus());
    if (windowLayoutParams != null) {
      stringBuilder
          .append("layout-params-type=")
          .append(windowLayoutParams.type)
          .append("layout-params-string=")
          .append(windowLayoutParams);
    }
    stringBuilder.append("decor-view-string=").append(viewDescriber.describeView(decorView));
    return stringBuilder.toString();
  }

  public static class Builder {
    private View decorView;
    private WindowManager.LayoutParams windowLayoutParams;
    private ViewDescriber viewDescriber;

    public Root build() {
      return new Root(this, viewDescriber);
    }

    public Builder withDecorView(View view) {
      this.decorView = view;
      return this;
    }

    public Builder withViewDescriber(ViewDescriber viewDescriber) {
      this.viewDescriber = viewDescriber;
      return this;
    }

    public Builder withWindowLayoutParams(WindowManager.LayoutParams windowLayoutParams) {
      this.windowLayoutParams = windowLayoutParams;
      return this;
    }
  }
}
