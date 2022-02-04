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

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.window.java.layout.WindowInfoTrackerCallbackAdapter;
import androidx.window.layout.DisplayFeature;
import androidx.window.layout.FoldingFeature;
import androidx.window.layout.WindowInfoTracker;
import androidx.window.layout.WindowLayoutInfo;
import java.util.List;
import java.util.concurrent.Executor;

/** Activity that updates a TextView with its device mode when it is folded or unfolded. */
public class FoldableActivity extends Activity {
  private static final String TAG = "FoldableActivity";

  @Nullable private WindowInfoTrackerCallbackAdapter windowInfoTracker;
  private final Consumer<WindowLayoutInfo> stateContainer = new WindowLayoutInfoConsumer();
  private final Handler handler = new Handler(Looper.getMainLooper());
  private final Executor executor = command -> handler.post(() -> handler.post(command));

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.foldable_activity);

    windowInfoTracker = new WindowInfoTrackerCallbackAdapter(WindowInfoTracker.getOrCreate(this));
  }

  @Override
  public void onStart() {
    super.onStart();
    windowInfoTracker.addWindowLayoutInfoListener(this, executor, stateContainer);
  }

  @Override
  public void onStop() {
    super.onStop();
    windowInfoTracker.removeWindowLayoutInfoListener(stateContainer);
  }

  private class WindowLayoutInfoConsumer implements Consumer<WindowLayoutInfo> {

    public WindowLayoutInfoConsumer() {}

    @Override
    public void accept(WindowLayoutInfo windowLayoutInfo) {
      TextView textView = (TextView) findViewById(R.id.current_fold_mode);
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
}
