/*
 * Copyright (C) 2015 The Android Open Source Project
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

package androidx.test.espresso.contrib;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnHolderItem;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollTo;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToHolder;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.base.Preconditions.checkArgument;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.fail;

import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ActivityScenario.ActivityAction;
import androidx.test.espresso.PerformException;
import androidx.test.filters.Suppress;
import androidx.test.ui.app.ItemListAdapter.CustomViewHolder;
import androidx.test.ui.app.R;
import androidx.test.ui.app.RecyclerViewActivity;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

public abstract class RecyclerViewIntegrationTest {

  private static final String ITEM_0 = "Item: 0";
  private static final String ITEM_16 = "Item: 16";
  private static final String ITEM_64 = "Item: 64";
  private static final String ITEM_100 = "Item: 100";
  private static final String ITEM_128 = "Item: 128";
  private static final String ITEM_200 = "Item: 200";
  private static final String ITEM_256 = "Item: 256";
  private static final String ITEM_512 = "Item: 512";
  private static final String ITEM_998 = "Item: 998";
  private static final String ITEM_10_PREFIX = "Item: 10";

  private int rvLayoutId;
  private int selectedItemId;

  protected ActivityScenario<RecyclerViewActivity> recyclerViewActivityScenario;

  @Before
  public void setUp() throws Exception {
    rvLayoutId = getRVLayoutId();
    selectedItemId = getSelectedItemId();
    recyclerViewActivityScenario = ActivityScenario.launch(RecyclerViewActivity.class);
  }

  @Test
  public void testScrolling_scrollToView() {
    onView(withItemText(ITEM_64)).check(doesNotExist());
    onView((withId(rvLayoutId))).perform(scrollTo(hasDescendant(withText(ITEM_64))));
    onView(withItemText(ITEM_64)).check(matches(isDisplayed()));
  }

  @Test
  public void testScrolling_scrollToViewWithViewHolderMatcher() {
    onView(withText(ITEM_100)).check(doesNotExist());
    onView((withId(rvLayoutId)))
        .perform(
            scrollToHolder(
                new CustomViewHolderMatcher(hasDescendant(withText(startsWith(ITEM_10_PREFIX))))));
    onView(withText(ITEM_100)).check(matches(isDisplayed()));
  }

  @Test
  public void testScrolling_scrollToViewWithViewHolderMatcherWithAmbiguousViewError() {
    try {
      onView((withId(rvLayoutId))).perform(scrollToHolder(new CustomViewHolderMatcher()));
      fail("PerformException expected.");
    } catch (PerformException expected) {
    }
  }

  @Test
  public void testScrolling_scrollToViewWithViewHolderMatcherWithPositionOutOfRange() {
    try {
      onView((withId(rvLayoutId)))
          .perform(scrollToHolder(new CustomViewHolderMatcher()).atPosition(100));
      fail("PerformException expected.");
    } catch (PerformException expected) {
    }
  }

  @Test
  public void testScrolling_scrollToViewWithViewHolderMatcherWithPosition() {
    onView(withText(ITEM_200)).check(doesNotExist());
    onView((withId(rvLayoutId)))
        .perform(scrollToHolder(new CustomViewHolderMatcher()).atPosition(1));
    onView(withText(ITEM_200)).check(matches(isDisplayed()));
  }

  @Test
  public void testScrolling_scrollToMultipleViews() {
    onView(withId(rvLayoutId)).perform(scrollTo(hasDescendant(withText(ITEM_64))));
    onView(withItemText(ITEM_64)).check(matches(isDisplayed()));

    onView(withId(rvLayoutId)).perform(scrollTo(hasDescendant(withText(ITEM_128))));
    onView(withItemText(ITEM_128)).check(matches(isDisplayed()));

    onView(withId(rvLayoutId)).perform(scrollTo(hasDescendant(withText(ITEM_256))));
    onView(withItemText(ITEM_256)).check(matches(isDisplayed()));

    onView(withId(rvLayoutId)).perform(scrollTo(hasDescendant(withText(ITEM_512))));
    onView(withItemText(ITEM_512)).check(matches(isDisplayed()));

    // Scroll to top
    onView(withId(rvLayoutId)).perform(scrollTo(hasDescendant(withText(ITEM_0))));
    onView(withItemText(ITEM_0)).check(matches(isDisplayed()));
  }

  @Test
  public void testScrolling_scrollToLastScrollToFirst() {
    String firstItem = ITEM_0;
    String lastItem = ITEM_998;
    onView(withId(rvLayoutId)).perform(scrollTo(hasDescendant(withText(lastItem))));
    onView(withItemText(ITEM_998)).check(matches(isDisplayed()));
    onView(withId(rvLayoutId)).perform(scrollTo(hasDescendant(withText(firstItem))));
    onView(withItemText(ITEM_0)).check(matches(isDisplayed()));
  }

  @Test
  public void testErrorMessages_onNonRecyclerViewThrows() {
    String targetViewText = "Scrolling a non RV should throw";
    try {
      onView(withId(R.id.rv_view_pager)).perform(scrollTo(hasDescendant(withText(targetViewText))));
      fail("PerformException expected!");
    } catch (PerformException expected) {
    }
  }

  @Test
  public void testErrorMessages_viewNotInHierarchyThrows() {
    String targetViewText = "Not in hierarchy";
    try {
      onView(withId(rvLayoutId)).perform(scrollTo(hasDescendant(withText(targetViewText))));
      fail("PerformException expected!");
    } catch (PerformException expected) {
    }
  }

  @Test
  public void testErrorMessages_duplicateViewsInHierarchyThrows() throws Throwable {
    final String targetViewText = ITEM_64;
    initWithDuplicateItems(targetViewText, 5);
    try {
      onView(withId(rvLayoutId)).perform(scrollTo(hasDescendant(withText(targetViewText))));
      fail("PerformException expected!");
    } catch (PerformException expected) {
    }
  }

  @Test
  public void testScrolling_scrollToItemAndClick() {
    onView(withId(rvLayoutId)).perform(scrollTo(hasDescendant(withText(ITEM_64))));
    onView(withItemText(ITEM_64)).perform(click());
    String expectedItemText = "Selected: " + ITEM_64;
    onView(withId(selectedItemId)).check(matches(withText(expectedItemText)));
  }

  // TODO(b/68003948): flaky
  @Suppress
  @Test
  public void testScrolling_scrollToMultipleViewsAndClick() {
    onView(withId(rvLayoutId)).perform(scrollTo(hasDescendant(withText(ITEM_16))));
    onView(withItemText(ITEM_16)).perform(click());
    String expectedItem16Text = "Selected: " + ITEM_16;
    onView(withId(selectedItemId)).check(matches(withText(expectedItem16Text)));

    onView(withId(rvLayoutId)).perform(scrollTo(hasDescendant(withText(ITEM_128))));
    onView(withItemText(ITEM_128)).perform(click());
    String expectedItem128Text = "Selected: " + ITEM_128;
    onView(withId(selectedItemId)).check(matches(withText(expectedItem128Text)));

    onView(withId(rvLayoutId)).perform(scrollTo(hasDescendant(withText(ITEM_256))));
    onView(withItemText(ITEM_256)).perform(click());
    String expectedItem256Text = "Selected: " + ITEM_256;
    onView(withId(selectedItemId)).check(matches(withText(expectedItem256Text)));

    onView(withId(rvLayoutId)).perform(scrollTo(hasDescendant(withText(ITEM_512))));
    onView(withItemText(ITEM_512)).perform(click());
    String expectedItem512Text = "Selected: " + ITEM_512;
    onView(withId(selectedItemId)).check(matches(withText(expectedItem512Text)));

    onView(withId(rvLayoutId)).perform(scrollTo(hasDescendant(withText(ITEM_998))));
    onView(withItemText(ITEM_998)).perform(click());
    String expectedItem998Text = "Selected: " + ITEM_998;
    onView(withId(selectedItemId)).check(matches(withText(expectedItem998Text)));

    // Scroll to top
    onView(withId(rvLayoutId)).perform(scrollTo(hasDescendant(withText(ITEM_0))));
    onView(withItemText(ITEM_0)).perform(click());
    String expectedItem0Text = "Selected: " + ITEM_0;
    onView(withId(selectedItemId)).check(matches(withText(expectedItem0Text)));
  }

  @Test
  public void testScrolling_scrollToViewAtPosition() {
    onView(withItemText(ITEM_64)).check(doesNotExist());
    onView((withId(rvLayoutId))).perform(scrollToPosition(64));
    onView(withItemText(ITEM_64)).check(matches(isDisplayed()));
  }

  @Test
  public void testScrolling_scrollToMultiplePositions() {
    onView(withItemText(ITEM_64)).check(doesNotExist());
    onView(withId(rvLayoutId)).perform(scrollToPosition(64));
    onView(withItemText(ITEM_64)).check(matches(isDisplayed()));

    onView(withItemText(ITEM_128)).check(doesNotExist());
    onView(withId(rvLayoutId)).perform(scrollToPosition(128));
    onView(withItemText(ITEM_128)).check(matches(isDisplayed()));

    onView(withItemText(ITEM_256)).check(doesNotExist());
    onView(withId(rvLayoutId)).perform(scrollToPosition(256));
    onView(withItemText(ITEM_256)).check(matches(isDisplayed()));

    onView(withItemText(ITEM_512)).check(doesNotExist());
    onView(withId(rvLayoutId)).perform(scrollToPosition(512));
    onView(withItemText(ITEM_512)).check(matches(isDisplayed()));

    // Scroll to top position
    onView(withId(rvLayoutId)).perform(scrollToPosition(0));
    onView(withItemText(ITEM_0)).check(matches(isDisplayed()));
  }

  @Test
  public void testScrolling_scrollToLastScrollToFirstPosition() {
    onView(withId(rvLayoutId)).perform(scrollToPosition(998));
    onView(withId(rvLayoutId)).perform(scrollToPosition(0));
  }

  @Test
  public void testActionOnItem_clickOnItem() {
    onView(withId(rvLayoutId)).perform(actionOnItem(hasDescendant(withText(ITEM_256)), click()));
    String expectedItemText = "Selected: " + ITEM_256;
    onView(withId(selectedItemId)).check(matches(withText(expectedItemText)));
  }

  @Test
  public void testActionOnItem_clickOnItemWithViewHolderMatcher() {
    onView(withId(rvLayoutId))
        .perform(
            actionOnHolderItem(
                new CustomViewHolderMatcher(hasDescendant(withText(startsWith(ITEM_10_PREFIX)))),
                click()));
    String expectedItemText = "Selected: " + ITEM_100;
    onView(withId(selectedItemId)).check(matches(withText(expectedItemText)));
  }

  @Test
  public void testActionOnItem_clickOnItemWithViewHolderMatcherWithAmbiguousViewError() {
    try {
      onView((withId(rvLayoutId)))
          .perform(actionOnHolderItem(new CustomViewHolderMatcher(), click()));
      fail("PerformException expected.");
    } catch (PerformException expected) {
    }
  }

  @Test
  public void testActionOnItem_clickOnItemWithViewHolderMatcherWithPositionOutOfRange() {
    try {
      onView((withId(rvLayoutId)))
          .perform(actionOnHolderItem(new CustomViewHolderMatcher(), click()).atPosition(100));
      fail("PerformException expected.");
    } catch (PerformException expected) {
    }
  }

  @Test
  public void testActionOnItem_clickOnItemWithViewHolderMatcherWithPosition() {
    onView(withId(rvLayoutId))
        .perform(actionOnHolderItem(new CustomViewHolderMatcher(), click()).atPosition(1));
    String expectedItemText = "Selected: " + ITEM_200;
    onView(withId(selectedItemId)).check(matches(withText(expectedItemText)));
  }

  @Test
  public void testActionOnItem_clickOnLastAndFirstItem() {
    onView(withId(rvLayoutId)).perform(actionOnItem(hasDescendant(withText(ITEM_998)), click()));
    String expectedItem998Text = "Selected: " + ITEM_998;
    onView(withId(selectedItemId)).check(matches(withText(expectedItem998Text)));

    onView(withId(rvLayoutId)).perform(actionOnItem(hasDescendant(withText(ITEM_0)), click()));
    String expectedItem0Text = "Selected: " + ITEM_0;
    onView(withId(selectedItemId)).check(matches(withText(expectedItem0Text)));
  }

  @Test
  public void testActionOnItemAtPosition_clickOnItem() {
    onView(withId(rvLayoutId)).perform(actionOnItemAtPosition(256, click()));
    String expectedItemText = "Selected: " + ITEM_256;
    onView(withId(selectedItemId)).check(matches(withText(expectedItemText)));
  }

  @Test
  public void testActionOnItemAtPosition_clickOnLastAndFirstItem() {
    onView(withId(rvLayoutId)).perform(actionOnItemAtPosition(998, click()));
    String expectedItem998Text = "Selected: " + ITEM_998;
    onView(withId(selectedItemId)).check(matches(withText(expectedItem998Text)));

    onView(withId(rvLayoutId)).perform(actionOnItemAtPosition(0, click()));
    String expectedItem0Text = "Selected: " + ITEM_0;
    onView(withId(selectedItemId)).check(matches(withText(expectedItem0Text)));
  }

  // TODO(b/68003948): flaky
  @Suppress
  @Test
  public void testActionsOnItem_clickMultipleItems() throws InterruptedException {
    onView(withItemText(ITEM_64)).check(doesNotExist());
    onView(withId(rvLayoutId)).perform(actionOnItem(hasDescendant(withText(ITEM_64)), click()));
    String expectedItem64Text = "Selected: " + ITEM_64;
    onView(withId(selectedItemId)).check(matches(withText(expectedItem64Text)));

    onView(withItemText(ITEM_512)).check(doesNotExist());
    onView(withId(rvLayoutId)).perform(actionOnItemAtPosition(512, click()));
    String expectedItem512Text = "Selected: " + ITEM_512;
    onView(withId(selectedItemId)).check(matches(withText(expectedItem512Text)));

    onView(withItemText(ITEM_998)).check(doesNotExist());
    onView(withId(rvLayoutId)).perform(actionOnItem(hasDescendant(withText(ITEM_998)), click()));
    String expectedItem998Text = "Selected: " + ITEM_998;
    onView(withId(selectedItemId)).check(matches(withText(expectedItem998Text)));

    onView(withItemText(ITEM_0)).check(doesNotExist());
    onView(withId(rvLayoutId)).perform(actionOnItemAtPosition(0, click()));
    String expectedItem0Text = "Selected: " + ITEM_0;
    onView(withId(selectedItemId)).check(matches(withText(expectedItem0Text)));
  }

  /** @return the layout id of the {@link androidx.recyclerview.widget.RecyclerView} used. */
  protected abstract int getRVLayoutId();

  /** @return the view id of the selected item {@link android.widget.TextView} */
  protected abstract int getSelectedItemId();

  private void initWithDuplicateItems(final String targetViewText, final int numberOfDuplicates)
      throws Throwable {
    recyclerViewActivityScenario.onActivity(
        new ActivityAction<RecyclerViewActivity>() {
          @Override
          public void perform(RecyclerViewActivity recyclerViewActivity) {
            for (int i = 0; i < numberOfDuplicates; i++) {
              recyclerViewActivity.addItems(targetViewText);
            }
          }
        });
  }

  /** Matches a {@link android.widget.TextView} text with a rvLayoutId as parent. */
  private Matcher<View> withItemText(final String itemText) {
    checkArgument(!TextUtils.isEmpty(itemText), "itemText cannot be null or empty");
    return new TypeSafeMatcher<View>() {
      @Override
      public boolean matchesSafely(View item) {
        return allOf(isDescendantOfA(withId(rvLayoutId)), withText(itemText)).matches(item);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("is isDescendantOfA RV with text " + itemText);
      }
    };
  }

  private static class CustomViewHolderMatcher extends TypeSafeMatcher<RecyclerView.ViewHolder> {
    private Matcher<View> itemMatcher = any(View.class);

    public CustomViewHolderMatcher() {}

    public CustomViewHolderMatcher(Matcher<View> itemMatcher) {
      this.itemMatcher = itemMatcher;
    }

    @Override
    public boolean matchesSafely(RecyclerView.ViewHolder viewHolder) {
      return CustomViewHolder.class.isAssignableFrom(viewHolder.getClass())
          && itemMatcher.matches(viewHolder.itemView);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("is assignable from CustomViewHolder");
    }
  }
}
