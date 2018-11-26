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

package androidx.test.espresso.contrib;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.contrib.DrawerMatchers.isOpen;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener;
import android.view.View;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import java.lang.reflect.Field;
import org.hamcrest.Matcher;

/**
 * Espresso actions for using a {@link DrawerLayout}.
 *
 * @see <a href="http://developer.android.com/design/patterns/navigation-drawer.html">Navigation
 *     drawer design guide</a>
 */
public final class DrawerActions {

  private DrawerActions() {
    // forbid instantiation
  }

  private static Field listenerField;

  private abstract static class DrawerAction implements ViewAction {

    @Override
    public final Matcher<View> getConstraints() {
      return isAssignableFrom(DrawerLayout.class);
    }

    @Override
    public final void perform(UiController uiController, View view) {
      DrawerLayout drawer = (DrawerLayout) view;

      if (!checkAction().matches(drawer)) {
        return;
      }

      DrawerListener listener = getDrawerListener(drawer);
      IdlingDrawerListener idlingListener;
      if (listener instanceof IdlingDrawerListener) {
        idlingListener = (IdlingDrawerListener) listener;
      } else {
        idlingListener = IdlingDrawerListener.getInstance(listener);
        drawer.setDrawerListener(idlingListener);
        Espresso.registerIdlingResources(idlingListener);
      }

      performAction(uiController, drawer);
      uiController.loopMainThreadUntilIdle();

      Espresso.unregisterIdlingResources(idlingListener);
      drawer.setDrawerListener(idlingListener.parentListener);
      idlingListener.parentListener = null;
    }

    protected abstract Matcher<View> checkAction();

    protected abstract void performAction(UiController uiController, DrawerLayout view);
  }

  /**
   * @deprecated Use {@link #open()} with {@code perform} after matching a view. This method will be
   *     removed in the next release.
   */
  @Deprecated
  public static void openDrawer(int drawerLayoutId) {
    openDrawer(drawerLayoutId, GravityCompat.START);
  }

  /**
   * @deprecated Use {@link #open(int)} with {@code perform} after matching a view. This method will
   *     be removed in the next release.
   */
  @Deprecated
  public static void openDrawer(int drawerLayoutId, int gravity) {
    onView(withId(drawerLayoutId)).perform(open(gravity));
  }

  /**
   * Creates an action which opens the {@link DrawerLayout} drawer with gravity START. This method
   * blocks until the drawer is fully open. No operation if the drawer is already open.
   */
  // TODO alias to openDrawer before 3.0 and deprecate this method.
  public static ViewAction open() {
    return open(GravityCompat.START);
  }

  /**
   * Creates an action which opens the {@link DrawerLayout} drawer with the gravity. This method
   * blocks until the drawer is fully open. No operation if the drawer is already open.
   */
  // TODO alias to openDrawer before 3.0 and deprecate this method.
  public static ViewAction open(final int gravity) {
    return new DrawerAction() {
      @Override
      public String getDescription() {
        return "open drawer with gravity " + gravity;
      }

      @Override
      protected Matcher<View> checkAction() {
        return isClosed(gravity);
      }

      @Override
      protected void performAction(UiController uiController, DrawerLayout view) {
        view.openDrawer(gravity);
      }
    };
  }

  /**
   * @deprecated Use {@link #close()} with {@code perform} after matching a view. This method will
   *     be removed in the next release.
   */
  @Deprecated
  public static void closeDrawer(int drawerLayoutId) {
    closeDrawer(drawerLayoutId, GravityCompat.START);
  }

  /**
   * @deprecated Use {@link #open(int)} with {@code perform} after matching a view. This method will
   *     be removed in the next release.
   */
  @Deprecated
  public static void closeDrawer(int drawerLayoutId, int gravity) {
    onView(withId(drawerLayoutId)).perform(close(gravity));
  }

  /**
   * Creates an action which closes the {@link DrawerLayout} with gravity START. This method blocks
   * until the drawer is fully closed. No operation if the drawer is already closed.
   */
  // TODO alias to closeDrawer before 3.0 and deprecate this method.
  public static ViewAction close() {
    return close(GravityCompat.START);
  }

  /**
   * Creates an action which closes the {@link DrawerLayout} with the gravity. This method blocks
   * until the drawer is fully closed. No operation if the drawer is already closed.
   */
  // TODO alias to closeDrawer before 3.0 and deprecate this method.
  public static ViewAction close(final int gravity) {
    return new DrawerAction() {
      @Override
      public String getDescription() {
        return "close drawer with gravity " + gravity;
      }

      @Override
      protected Matcher<View> checkAction() {
        return isOpen(gravity);
      }

      @Override
      protected void performAction(UiController uiController, DrawerLayout view) {
        view.closeDrawer(gravity);
        uiController.loopMainThreadUntilIdle();
        // If still open wait some more...
        if (view.isDrawerVisible(gravity)) {
          uiController.loopMainThreadForAtLeast(300);
        }
      }
    };
  }

  /**
   * Pries the current {@link DrawerListener} loose from the cold dead hands of the given {@link
   * DrawerLayout}. Uses reflection.
   */
  @Nullable
  private static DrawerListener getDrawerListener(DrawerLayout drawer) {
    try {
      if (listenerField == null) {
        // lazy initialization of reflected field.
        listenerField = DrawerLayout.class.getDeclaredField("mListener");
        listenerField.setAccessible(true);
      }
      return (DrawerListener) listenerField.get(drawer);
    } catch (IllegalArgumentException ex) {
      // Pity we can't use Java 7 multi-catch for all of these.
      throw new PerformException.Builder().withCause(ex).build();
    } catch (IllegalAccessException ex) {
      throw new PerformException.Builder().withCause(ex).build();
    } catch (NoSuchFieldException ex) {
      throw new PerformException.Builder().withCause(ex).build();
    } catch (SecurityException ex) {
      throw new PerformException.Builder().withCause(ex).build();
    }
  }

  /**
   * Drawer listener that wraps an existing {@link DrawerListener}, and functions as an {@link
   * IdlingResource} for Espresso.
   */
  private static class IdlingDrawerListener implements DrawerListener, IdlingResource {

    private static IdlingDrawerListener instance;

    private static IdlingDrawerListener getInstance(DrawerListener parentListener) {
      if (instance == null) {
        instance = new IdlingDrawerListener();
      }
      instance.setParentListener(parentListener);
      return instance;
    }

    @Nullable private DrawerListener parentListener;
    private ResourceCallback callback;
    // Idle state is only accessible from main thread.
    private boolean idle = true;

    public void setParentListener(@Nullable DrawerListener parentListener) {
      this.parentListener = parentListener;
    }

    @Override
    public void onDrawerClosed(View drawer) {
      if (parentListener != null) {
        parentListener.onDrawerClosed(drawer);
      }
    }

    @Override
    public void onDrawerOpened(View drawer) {
      if (parentListener != null) {
        parentListener.onDrawerOpened(drawer);
      }
    }

    @Override
    public void onDrawerSlide(View drawer, float slideOffset) {
      if (parentListener != null) {
        parentListener.onDrawerSlide(drawer, slideOffset);
      }
    }

    @Override
    public void onDrawerStateChanged(int newState) {
      if (newState == DrawerLayout.STATE_IDLE) {
        idle = true;
        if (callback != null) {
          callback.onTransitionToIdle();
        }
      } else {
        idle = false;
      }
      if (parentListener != null) {
        parentListener.onDrawerStateChanged(newState);
      }
    }

    @Override
    public String getName() {
      return "IdlingDrawerListener";
    }

    @Override
    public boolean isIdleNow() {
      return idle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
      this.callback = callback;
    }
  }
}
