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

package androidx.test.espresso.util;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link HumanReadables}. */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class HumanReadablesTest {

  @Test
  public void describeView_layoutParamsWithObjectAddressRemoved() {
    Context context = getInstrumentation().getContext();
    FrameLayout parent = new FrameLayout(context);
    TextView view = new TextView(context);
    view.setId(42);
    view.setText("text under test");
    parent.addView(view);
    view.setLayoutParams(
        new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    String actual = HumanReadables.describe(view);

    // Expected: The typical FrameLayout$LayoutParams@12345678 is cleaned up to remove the address
    // at the end of the instance string description.
    assertThat(actual)
        .isEqualTo(
            "TextView{id=42, visibility=VISIBLE, width=0, height=0, has-focus=false,"
                + " has-focusable=false, has-window-focus=false, is-clickable=false,"
                + " is-enabled=true, is-focused=false, is-focusable=false,"
                + " is-layout-requested=true, is-selected=false,"
                + " layout-params=android.widget.FrameLayout$LayoutParams@YYYYYY, tag=null,"
                + " root-is-layout-requested=true, has-input-connection=false, x=0.0, y=0.0,"
                + " text=text under test, input-type=0, ime-target=false, has-links=false}");
  }

  @Test
  public void describeView_layoutParamsToStringUnchanged() {
    Context context = getInstrumentation().getContext();
    FrameLayout parent = new FrameLayout(context);
    TextView view = new TextView(context);
    view.setId(42);
    view.setText("text under test");
    parent.addView(view);
    view.setLayoutParams(
        new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT) {
          @Override
          public String toString() {
            return "LayoutParams@NotAnHexNumber1234";
          }
        });

    String actual = HumanReadables.describe(view);

    // Expected: The custom LayoutParams toString description is used as-is.
    assertThat(actual)
        .isEqualTo(
            "TextView{id=42, visibility=VISIBLE, width=0, height=0, has-focus=false,"
                + " has-focusable=false, has-window-focus=false, is-clickable=false,"
                + " is-enabled=true, is-focused=false, is-focusable=false,"
                + " is-layout-requested=true, is-selected=false,"
                + " layout-params=LayoutParams@NotAnHexNumber1234, tag=null,"
                + " root-is-layout-requested=true, has-input-connection=false, x=0.0, y=0.0,"
                + " text=text under test, input-type=0, ime-target=false, has-links=false}");
  }
}
