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

package androidx.test.espresso.assertion;

import static androidx.test.espresso.assertion.PositionAssertions.isRelativePosition;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import android.view.View;
import androidx.test.espresso.assertion.PositionAssertions.Position;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/** Test case for {@link PositionAssertions}. */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class PositionAssertionsUnitTest {
  private static final int AXIS_X = 0;
  private static final int AXIS_Y = 1;

  @Test
  public void isRelativePosition_NoOverlap() {
    View v1 = setupView(10 /* x */, 10 /* y */, 30 /* width */, 20 /* height */);
    View v2 = setupView(100 /* x */, 100 /* y */, 15 /* width */, 10 /* height */);

    assertTrue(
        "v1 is NOT completely left of v2, " + getDebugDetails(v1, v2),
        isRelativePosition(v1, v2, Position.COMPLETELY_LEFT_OF));
    assertTrue(
        "v2 is NOT completely right of v1, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.COMPLETELY_RIGHT_OF));
    assertTrue(
        "v1 is NOT completely above v2, " + getDebugDetails(v1, v2),
        isRelativePosition(v1, v2, Position.COMPLETELY_ABOVE));
    assertTrue(
        "v2 is NOT completely below v1, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.COMPLETELY_BELOW));
    assertFalse(
        "v1 is partially left of v2, although they don't overlap " + getDebugDetails(v1, v2),
        isRelativePosition(v1, v2, Position.PARTIALLY_LEFT_OF));
    assertFalse(
        "v2 is partially right of v1, although they don't overlap, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.PARTIALLY_RIGHT_OF));
    assertFalse(
        "v1 is partially above v2, although they don't overlap, " + getDebugDetails(v1, v2),
        isRelativePosition(v1, v2, Position.PARTIALLY_ABOVE));
    assertFalse(
        "v2 is partially below v1, although they don't overlap, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.PARTIALLY_BELOW));
    assertFalse(
        "Unexpected left alignment of v1 and v2, " + getDebugDetails(v1, v2),
        isRelativePosition(v1, v2, Position.LEFT_ALIGNED));
    assertFalse(
        "Unexpected right alignment of v1 and v2, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.RIGHT_ALIGNED));
    assertFalse(
        "Unexpected top alignment of v1 and v2, " + getDebugDetails(v1, v2),
        isRelativePosition(v1, v2, Position.TOP_ALIGNED));
    assertFalse(
        "Unexpected bottom alignment of v1 and v2, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.BOTTOM_ALIGNED));
  }

  @Test
  public void isRelativePosition_VerticalOverlap() {
    // 2 views v1 is overlapping with v2
    View v1 = setupView(10 /* x */, 10 /* y */, 30 /* width */, 20 /* height */);
    // Test the case of 2 Views, v1 left of v2 with no horizontal overlap
    // and overlapped vertically from pixel 12 to 22.
    View v2 = setupView(40 /* x */, 12 /* y */, 15 /* width */, 10 /* height */);

    assertTrue(
        "v1 is NOT completely left of v2, " + getDebugDetails(v1, v2),
        isRelativePosition(v1, v2, Position.COMPLETELY_LEFT_OF));
    assertTrue(
        "v2 is NOT completely right of v1, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.COMPLETELY_RIGHT_OF));
    assertFalse(
        "v1 is completely above v2, although they overlap, " + getDebugDetails(v1, v2),
        isRelativePosition(v1, v2, Position.COMPLETELY_ABOVE));
    assertFalse(
        "v2 is completely below v1, although they overlap, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.COMPLETELY_BELOW));
    assertFalse(
        "v1 is partially left of v2, although they don't overlap " + getDebugDetails(v1, v2),
        isRelativePosition(v1, v2, Position.PARTIALLY_LEFT_OF));
    assertFalse(
        "v2 is partially right of v1, although they don't overlap " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.PARTIALLY_RIGHT_OF));
    assertTrue(
        "v1 is NOT partially above v2, although they overlap, " + getDebugDetails(v1, v2),
        isRelativePosition(v1, v2, Position.PARTIALLY_ABOVE));
    assertTrue(
        "v2 is NOT partially below v1, although they overlap, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.PARTIALLY_BELOW));
    assertFalse(
        "Unexpected left alignment of v1 and v2, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.LEFT_ALIGNED));
    assertFalse(
        "Unexpected right alignment of v1 and v2, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.RIGHT_ALIGNED));
    assertFalse(
        "Unexpected top alignment of v1 and v2, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.TOP_ALIGNED));
    assertFalse(
        "Unexpected bottom alignment of v1 and v2, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.BOTTOM_ALIGNED));
  }

  @Test
  public void isRelativePosition_AlignedLeft() {
    // 2 views aligned left at x=10
    View v1 = setupView(10 /* x */, 10 /* y */, 30 /* width */, 20 /* height */);
    View v2 = setupView(10 /* x */, 100 /* y */, 15 /* width */, 10 /* height */);

    assertTrue(
        "v1 and v2 are NOT left aligned, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.LEFT_ALIGNED));
    assertFalse(
        "Unexpected right alignment of v1 and v2, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.RIGHT_ALIGNED));
    assertFalse(
        "v1 is completely left of v2, although they are left aligned, " + getDebugDetails(v1, v2),
        isRelativePosition(v1, v2, Position.COMPLETELY_LEFT_OF));
    assertFalse(
        "v2 is completely right of v1, although they are left aligned, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.COMPLETELY_RIGHT_OF));
    assertFalse(
        "v1 is partially left of v2, although they are left aligned, " + getDebugDetails(v1, v2),
        isRelativePosition(v1, v2, Position.PARTIALLY_LEFT_OF));
    assertFalse(
        "v2 is partially right of v1, although they are left aligned, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.PARTIALLY_RIGHT_OF));
  }

  @Test
  public void isRelativePosition_AlignedRight() {
    // 2 views that aligned to right at x = 40
    View v1 = setupView(10 /* x */, 10 /* y */, 30 /* width */, 30 /* height */);
    View v2 = setupView(30 /* x */, 20 /* y */, 10 /* width */, 10 /* height */);

    assertTrue(
        "v1 and v2 are NOT right aligned, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.RIGHT_ALIGNED));
    assertFalse(
        "Unexpected left alignment of v1 and v2, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.LEFT_ALIGNED));
    assertFalse(
        "v1 is completely left of v2 although they are horizontally overlapped, "
            + getDebugDetails(v1, v2),
        isRelativePosition(v1, v2, Position.COMPLETELY_LEFT_OF));
    assertFalse(
        "v2 is completely right of v1 although they are right aligned, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.COMPLETELY_RIGHT_OF));
    assertTrue(
        "v1 is NOT partially left of v2 although they are horizontally overlapped, "
            + getDebugDetails(v1, v2),
        isRelativePosition(v1, v2, Position.PARTIALLY_LEFT_OF));
    assertTrue(
        "v2 is NOT partially right of v1 although they are horizontally overlapped, "
            + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.PARTIALLY_RIGHT_OF));
  }

  @Test
  public void isRelativePosition_AlignedTop() {
    // 2 views that aligned TOP at y = 10
    View v1 = setupView(10 /* x */, 10 /* y */, 30 /* width */, 30 /* height */);
    View v2 = setupView(50 /* x */, 10 /* y */, 10 /* width */, 10 /* height */);

    assertTrue(
        "v1 and v2 are NOT Top aligned, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.TOP_ALIGNED));
    assertFalse(
        "Unexpected bottom alignment of v1 and v2, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.BOTTOM_ALIGNED));
    assertFalse(
        "v1 is completely above v2 although they are top aligned, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.COMPLETELY_ABOVE));
    assertFalse(
        "v2 is completely below v1 although they are vertically overlapped, "
            + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.COMPLETELY_BELOW));
    assertFalse(
        "v1 is partially above v2 although they are top aligned, " + getDebugDetails(v1, v2),
        isRelativePosition(v1, v2, Position.PARTIALLY_ABOVE));
    assertFalse(
        "v2 is partially below v1 although they are top aligned, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.PARTIALLY_BELOW));
  }

  @Test
  public void isRelativePosition_AlignedBottom() {
    // 2 views that aligned Bottom at y = 100
    View v1 = setupView(10 /* x */, 10 /* y */, 30 /* width */, 90 /* height */);
    View v2 = setupView(20 /* x */, 50 /* y */, 10 /* width */, 50 /* height */);

    assertTrue(
        "v1 and v2 are NOT bottom aligned, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.BOTTOM_ALIGNED));
    assertFalse(
        "Unexpected top alignment of v1 and v2, " + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.TOP_ALIGNED));
    assertFalse(
        "v1 is completely above v2 although they are bottom aligned, " + getDebugDetails(v1, v2),
        isRelativePosition(v1, v2, Position.COMPLETELY_ABOVE));
    assertFalse(
        "v1 is completely below v2 although they are vertically overlapped, "
            + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.COMPLETELY_BELOW));
    assertTrue(
        "v1 is NOT partially above v2 although they are vertically overlapped, "
            + getDebugDetails(v1, v2),
        isRelativePosition(v1, v2, Position.PARTIALLY_ABOVE));
    assertTrue(
        "v2 is NOT partially below v1 although they are vertically overlapped, "
            + getDebugDetails(v1, v2),
        isRelativePosition(v2, v1, Position.PARTIALLY_BELOW));
  }

  // Helper methods
  private View setupView(final int x, final int y, final int width, final int height) {
    View mockView = spy(new View(getInstrumentation().getContext()));
    doAnswer(
            new Answer<Void>() {
              @Override
              public Void answer(InvocationOnMock invocation) throws Throwable {
                int[] array = (int[]) invocation.getArguments()[0];
                array[AXIS_X] = x;
                array[AXIS_Y] = y;
                return null;
              }
            })
        .when(mockView)
        .getLocationOnScreen(any(int[].class));
    mockView.layout(x, y, x + width, y + height);
    return mockView;
  }

  private String getDebugDetails(View v1, View v2) {
    int[] location1 = new int[2];
    int[] location2 = new int[2];
    v1.getLocationOnScreen(location1);
    v2.getLocationOnScreen(location2);

    return new StringBuilder()
        .append("v1 coordinates: (")
        .append("")
        .append(location1[0])
        .append(", ")
        .append(location1[1])
        .append("), v2 coordinates: (")
        .append(location2[0])
        .append(", ")
        .append(location2[1])
        .append(")")
        .toString();
  }
}
