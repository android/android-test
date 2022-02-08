/*
 * Copyright (C) 2022 The Android Open Source Project
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

package androidx.test.foldable.app;

import android.widget.TextView;
import androidx.core.util.Consumer;
import androidx.window.layout.DisplayFeature;
import androidx.window.layout.FoldingFeature;
import androidx.window.layout.WindowLayoutInfo;
import java.util.List;

/** Consumer that updates the provided text view when the device mode is changed. */
class WindowLayoutInfoConsumer implements Consumer<WindowLayoutInfo> {
  private final TextView textView;

  public WindowLayoutInfoConsumer(TextView textView) {
    this.textView = textView;
  }

  @Override
  public void accept(WindowLayoutInfo windowLayoutInfo) {
    List<DisplayFeature> displayFeatures = windowLayoutInfo.getDisplayFeatures();
    if (displayFeatures.isEmpty()) {
      textView.setText(R.string.no_display_features_found);
    }
    for (DisplayFeature displayFeature : displayFeatures) {
      if (displayFeature instanceof FoldingFeature) {
        FoldingFeature foldingFeature = (FoldingFeature) displayFeature;
        if (foldingFeature.getState().equals(FoldingFeature.State.FLAT)) {
          textView.setText(R.string.flat_mode);
        } else if (foldingFeature.getState().equals(FoldingFeature.State.HALF_OPENED)
            && foldingFeature.getOrientation().equals(FoldingFeature.Orientation.HORIZONTAL)) {
          textView.setText(R.string.tabletop_mode);
        } else if (foldingFeature.getState().equals(FoldingFeature.State.HALF_OPENED)
            && foldingFeature.getOrientation().equals(FoldingFeature.Orientation.VERTICAL)) {
          textView.setText(R.string.book_mode);
        }
      }
    }
  }
}
