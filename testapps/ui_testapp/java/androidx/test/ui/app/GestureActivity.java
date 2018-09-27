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

package androidx.test.ui.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import com.google.common.collect.Lists;
import java.util.List;

/**
 * Displays a large touchable area and logs the events it receives.
 */
public class GestureActivity extends Activity {
  private static final String TAG = GestureActivity.class.getSimpleName();


  private View gestureArea;
  private List<MotionEvent> downEvents = Lists.newArrayList();
  private List<MotionEvent> scrollEvents = Lists.newArrayList();
  private List<MotionEvent> longPressEvents = Lists.newArrayList();
  private List<MotionEvent> showPresses = Lists.newArrayList();
  private List<MotionEvent> singleTaps = Lists.newArrayList();
  private List<MotionEvent> confirmedSingleTaps = Lists.newArrayList();
  private List<MotionEvent> doubleTapEvents = Lists.newArrayList();
  private List<MotionEvent> doubleTaps = Lists.newArrayList();

  public void clearDownEvents() {
    downEvents.clear();
  }

  public void clearScrollEvents() {
    scrollEvents.clear();
  }

  public void clearLongPressEvents() {
    longPressEvents.clear();
  }

  public void clearShowPresses() {
    showPresses.clear();
  }

  public void clearSingleTaps() {
    singleTaps.clear();
  }

  public void clearConfirmedSingleTaps() {
    confirmedSingleTaps.clear();
  }

  public void clearDoubleTapEvents() {
    doubleTapEvents.clear();
  }

  public void clearDoubleTaps() {
    doubleTaps.clear();
  }

  public List<MotionEvent> getDownEvents() {
    return Lists.newArrayList(downEvents);
  }

  public List<MotionEvent> getScrollEvents() {
    return Lists.newArrayList(scrollEvents);
  }

  public List<MotionEvent> getLongPressEvents() {
    return Lists.newArrayList(longPressEvents);
  }

  public List<MotionEvent> getShowPresses() {
    return Lists.newArrayList(showPresses);
  }

  public List<MotionEvent> getSingleTaps() {
    return Lists.newArrayList(singleTaps);
  }

  public List<MotionEvent> getConfirmedSingleTaps() {
    return Lists.newArrayList(confirmedSingleTaps);
  }

  public List<MotionEvent> getDoubleTapEvents() {
    return Lists.newArrayList(doubleTapEvents);
  }

  public List<MotionEvent> getDoubleTaps() {
    return Lists.newArrayList(doubleTaps);
  }

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.gesture_activity);
    gestureArea = findViewById(R.id.gesture_area);
    final GestureDetector simpleDetector = new GestureDetector(this, new GestureListener());
    simpleDetector.setIsLongpressEnabled(true);
    simpleDetector.setOnDoubleTapListener(new DoubleTapListener());
    gestureArea.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent m) {
        boolean res = simpleDetector.onTouchEvent(m);
        if (-1 != touchDelay) {
          Log.i(TAG, "sleeping for: " + touchDelay);
          SystemClock.sleep(touchDelay);

        }
        return res;
      }
    });
  }

  private volatile long touchDelay = -1;

  public void setTouchDelay(long touchDelay) {
    this.touchDelay = touchDelay;
  }

  public void areaClicked(@SuppressWarnings("unused") View v) {
    Log.v(TAG, "onClick called!");
  }

  private class DoubleTapListener implements GestureDetector.OnDoubleTapListener {
    @Override
    public boolean onDoubleTap(MotionEvent e) {
      doubleTaps.add(MotionEvent.obtain(e));
      Log.v(TAG, "onDoubleTap: " + e);
      setVisible(R.id.text_double_click);
      return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
      doubleTapEvents.add(MotionEvent.obtain(e));
      Log.v(TAG, "onDoubleTapEvent: " + e);
      return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
      confirmedSingleTaps.add(MotionEvent.obtain(e));
      Log.v(TAG, "onSingleTapConfirmed: " + e);
      return false;
    }
  }

  private class GestureListener implements GestureDetector.OnGestureListener {
    @Override
    public boolean onDown(MotionEvent e) {
      downEvents.add(MotionEvent.obtain(e));
      Log.v(TAG, "Down: " + e);
      return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      singleTaps.add(MotionEvent.obtain(e));
      Log.v(TAG, "on single tap: " + e);
      setVisible(R.id.text_click);
      return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distX, float distY) {
      scrollEvents.add(MotionEvent.obtain(e1));
      scrollEvents.add(MotionEvent.obtain(e2));
      Log.v(TAG, "Scroll: e1: " + e1 + " e2: " + e2 + " distX: " + distX + " distY: " + distY);
      setVisible(R.id.text_swipe);
      return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
      showPresses.add(MotionEvent.obtain(e));
      Log.v(TAG, "ShowPress: " + e);
    }

    @Override
    public void onLongPress(MotionEvent e) {
      longPressEvents.add(MotionEvent.obtain(e));
      Log.v(TAG, "LongPress: " + e);
      setVisible(R.id.text_long_click);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float veloX, float veloY) {
      Log.v(TAG, "Fling: e1: " + e1 + " e2: " + e2 + " veloX: " + veloX + " veloY: " + veloY);
      return false;
    }
  }

  private void setVisible(int id) {
    hideAll();
    findViewById(id).setVisibility(View.VISIBLE);
  }

  private void hideAll() {
    findViewById(R.id.text_click).setVisibility(View.GONE);
    findViewById(R.id.text_long_click).setVisibility(View.GONE);
    findViewById(R.id.text_swipe).setVisibility(View.GONE);
    findViewById(R.id.text_double_click).setVisibility(View.GONE);
  }
}
