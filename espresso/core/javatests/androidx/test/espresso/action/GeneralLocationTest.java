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

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/** Unit tests for {@link GeneralLocation}. */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class GeneralLocationTest {

  private static final int VIEW_POSITION_X = 100;
  private static final int VIEW_POSITION_Y = 50;
  private static final int VIEW_WIDTH = 150;
  private static final int VIEW_HEIGHT = 300;

  private static final int AXIS_X = 0;
  private static final int AXIS_Y = 1;

  private View mockView;

  @Before
  public void setUp() throws Exception {
    mockView = spy(new View(getInstrumentation().getContext()));
    doAnswer(
            new Answer<Void>() {
              @Override
              public Void answer(InvocationOnMock invocation) throws Throwable {
                int[] array = (int[]) invocation.getArguments()[0];
                array[AXIS_X] = VIEW_POSITION_X;
                array[AXIS_Y] = VIEW_POSITION_Y;
                return null;
              }
            })
        .when(mockView)
        .getLocationOnScreen(any(int[].class));

    mockView.layout(
        VIEW_POSITION_X,
        VIEW_POSITION_Y,
        VIEW_POSITION_X + VIEW_WIDTH,
        VIEW_POSITION_Y + VIEW_HEIGHT);
  }

  @Test
  public void leftLocationsX() {
    assertPositionEquals(VIEW_POSITION_X, GeneralLocation.TOP_LEFT, AXIS_X);
    assertPositionEquals(VIEW_POSITION_X, GeneralLocation.CENTER_LEFT, AXIS_X);
    assertPositionEquals(VIEW_POSITION_X, GeneralLocation.BOTTOM_LEFT, AXIS_X);
  }

  @Test
  public void rightLocationsX() {
    assertPositionEquals(VIEW_POSITION_X + VIEW_WIDTH - 1, GeneralLocation.TOP_RIGHT, AXIS_X);
    assertPositionEquals(VIEW_POSITION_X + VIEW_WIDTH - 1, GeneralLocation.CENTER_RIGHT, AXIS_X);
    assertPositionEquals(VIEW_POSITION_X + VIEW_WIDTH - 1, GeneralLocation.BOTTOM_RIGHT, AXIS_X);
  }

  @Test
  public void topLocationsY() {
    assertPositionEquals(VIEW_POSITION_Y, GeneralLocation.TOP_LEFT, AXIS_Y);
    assertPositionEquals(VIEW_POSITION_Y, GeneralLocation.TOP_CENTER, AXIS_Y);
    assertPositionEquals(VIEW_POSITION_Y, GeneralLocation.TOP_RIGHT, AXIS_Y);
  }

  @Test
  public void bottomLocationsY() {
    assertPositionEquals(VIEW_POSITION_Y + VIEW_HEIGHT - 1, GeneralLocation.BOTTOM_LEFT, AXIS_Y);
    assertPositionEquals(VIEW_POSITION_Y + VIEW_HEIGHT - 1, GeneralLocation.BOTTOM_CENTER, AXIS_Y);
    assertPositionEquals(VIEW_POSITION_Y + VIEW_HEIGHT - 1, GeneralLocation.BOTTOM_RIGHT, AXIS_Y);
  }

  @Test
  public void centerLocationsX() {
    assertPositionEquals(VIEW_POSITION_X + (VIEW_WIDTH - 1) / 2.0f, GeneralLocation.CENTER, AXIS_X);
    assertPositionEquals(
        VIEW_POSITION_X + (VIEW_WIDTH - 1) / 2.0f, GeneralLocation.TOP_CENTER, AXIS_X);
    assertPositionEquals(
        VIEW_POSITION_X + (VIEW_WIDTH - 1) / 2.0f, GeneralLocation.BOTTOM_CENTER, AXIS_X);
  }

  @Test
  public void centerLocationsY() {
    assertPositionEquals(
        VIEW_POSITION_Y + (VIEW_HEIGHT - 1) / 2.0f, GeneralLocation.CENTER, AXIS_Y);
    assertPositionEquals(
        VIEW_POSITION_Y + (VIEW_HEIGHT - 1) / 2.0f, GeneralLocation.CENTER_LEFT, AXIS_Y);
    assertPositionEquals(
        VIEW_POSITION_Y + (VIEW_HEIGHT - 1) / 2.0f, GeneralLocation.CENTER_RIGHT, AXIS_Y);
  }

  private void assertPositionEquals(float expected, GeneralLocation location, int axis) {
    assertEquals(expected, location.calculateCoordinates(mockView)[axis], 0.1f);
  }
}
