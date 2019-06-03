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

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.google.common.base.Throwables.throwIfUnchecked;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import android.os.IBinder;
import android.view.View;
import androidx.test.espresso.base.InterruptableUiController;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.remote.Bindable;
import androidx.test.espresso.remote.NoRemoteEspressoInstanceException;
import androidx.test.espresso.remote.RemoteInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.internal.platform.os.ControlledLooper;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.hamcrest.MockitoHamcrest;

/** Unit tests for {@link ViewInteraction}. */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class ViewInteractionTest {
  @Mock private ViewFinder mockViewFinder;
  @Mock private ViewAssertion mockAssertion;
  @Mock private ViewAction mockAction;
  @Mock private InterruptableUiController mockUiController;
  @Mock private RemoteInteraction mockRemoteInteraction;
  @Mock private IBinder iBinderMock;
  @Mock private Bindable bindableMock;
  @Mock private ControlledLooper mockControlledLooper;

  private FailureHandler failureHandler;
  private Executor testExecutor = MoreExecutors.directExecutor();
  private ActivityLifecycleMonitor realLifecycleMonitor;
  private ViewInteraction testInteraction;
  private View rootView;
  private View targetView;
  private Matcher<View> viewMatcher;
  private Matcher<View> actionConstraint;
  private AtomicReference<Matcher<Root>> rootMatcherRef;
  private AtomicReference<Boolean> needsActivity;


  private static Callable<Void> createSuccessfulListenableFutureStub() {
    return new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        return null;
      }
    };
  }

  @Before
  public void setUp() throws Exception {
    initMocks(this);
    realLifecycleMonitor = ActivityLifecycleMonitorRegistry.getInstance();
    rootView = new View(getInstrumentation().getContext());
    targetView = new View(getInstrumentation().getContext());
    viewMatcher = is(targetView);
    actionConstraint = notNullValue(View.class);
    rootMatcherRef = new AtomicReference<>(RootMatchers.DEFAULT);
    needsActivity = new AtomicReference<>(false);
    when(mockAction.getDescription()).thenReturn("A Mock!");
    failureHandler =
        new FailureHandler() {
          @Override
          public void handle(Throwable error, Matcher<View> viewMatcher) {
            throwIfUnchecked(error);
            throw new RuntimeException(error);
          }
        };
    when(mockRemoteInteraction.isRemoteProcess()).thenReturn(true);
  }

  @After
  public void tearDown() throws Exception {
    ActivityLifecycleMonitorRegistry.registerInstance(realLifecycleMonitor);
  }

  @Test
  public void verifyPerformViewViolatesConstraints() {
    actionConstraint = not(viewMatcher);
    when(mockViewFinder.getView()).thenReturn(targetView);
    initWithViewInteraction();
    try {
      testInteraction.perform(mockAction);
      fail("should propagate constraint violation!");
    } catch (RuntimeException re) {
      if (!PerformException.class.isAssignableFrom(re.getClass())) {
        throw re;
      }
    }
  }

  @Test
  public void verifyPerformPropagatesException() {
    RuntimeException exceptionToRaise = new RuntimeException();
    when(mockViewFinder.getView()).thenReturn(targetView);
    doThrow(exceptionToRaise).when(mockAction).perform(mockUiController, targetView);
    initWithViewInteraction();
    try {
      testInteraction.perform(mockAction);
      fail("Should propagate exception stored in view operation!");
    } catch (RuntimeException re) {
      verify(mockAction).perform(mockUiController, targetView);
      assertThat(exceptionToRaise, is(re));
    }
  }

  @Test
  public void verifyCheckPropagatesException() {
    RuntimeException exceptionToRaise = new RuntimeException("testCheckPropagatesException");
    when(mockViewFinder.getView()).thenReturn(targetView);
    doThrow(exceptionToRaise).when(mockAssertion).check(targetView, null);

    initWithViewInteraction();
    try {
      testInteraction.check(mockAssertion);
      fail("Should propagate exception stored in view operation!");
    } catch (RuntimeException re) {
      verify(mockAssertion).check(targetView, null);
      assertThat(re, is(exceptionToRaise));
    }
  }

  @Test
  public void verifyPerformTwiceUpdatesPreviouslyMatched() {
    View firstView = new View(getInstrumentation().getContext());
    View secondView = new View(getInstrumentation().getContext());
    initWithViewInteraction();
    when(mockViewFinder.getView()).thenReturn(firstView);
    testInteraction.perform(mockAction);
    verify(mockAction).perform(mockUiController, firstView);

    when(mockViewFinder.getView()).thenReturn(secondView);
    testInteraction.perform(mockAction);
    verify(mockAction).perform(mockUiController, secondView);

    testInteraction.check(mockAssertion);
    verify(mockAssertion).check(secondView, null);
  }

  @Test
  public void verifyPerformAndCheck() {
    when(mockViewFinder.getView()).thenReturn(targetView);
    initWithViewInteraction();
    testInteraction.perform(mockAction);
    verify(mockAction).perform(mockUiController, targetView);

    testInteraction.check(mockAssertion);
    verify(mockAssertion).check(targetView, null);
  }

  @Test
  public void verifyCheck() {
    when(mockViewFinder.getView()).thenReturn(targetView);
    initWithViewInteraction();
    testInteraction.check(mockAssertion);
    verify(mockAssertion).check(targetView, null);
  }

  @Test
  public void verifyInRootUpdatesRef() {
    initWithViewInteraction();
    Matcher<Root> testMatcher = nullValue(Root.class);
    testInteraction.inRoot(testMatcher);
    assertEquals(testMatcher, rootMatcherRef.get());
  }

  @Test
  public void verifyInRoot_NullHandling() {
    initWithViewInteraction();
    try {
      testInteraction.inRoot(null);
      fail("should throw");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void verifyCheck_ViewCannotBeFound() {
    NoMatchingViewException noViewException =
        new NoMatchingViewException.Builder()
            .withViewMatcher(viewMatcher)
            .withRootView(rootView)
            .build();

    when(mockViewFinder.getView()).thenThrow(noViewException);
    initWithViewInteraction();
    testInteraction.check(mockAssertion);
    verify(mockAssertion).check(null, noViewException);
  }

  @Test
  public void verifyFailureHandler() {
    RuntimeException exceptionToRaise = new RuntimeException("testFailureHandler");
    when(mockViewFinder.getView()).thenReturn(targetView);
    doThrow(exceptionToRaise).when(mockAction).perform(mockUiController, targetView);
    initWithViewInteraction();
    FailureHandler customFailureHandler = Mockito.mock(FailureHandler.class);
    testInteraction.withFailureHandler(customFailureHandler).perform(mockAction);
    verify(mockAction).perform(mockUiController, targetView);
    verify(customFailureHandler).handle(exceptionToRaise, viewMatcher);
  }

  @Test
  public void verifySuccessfulCheckWithFailingRemoteInteraction() {
    Callable<Void> failingRemoteInteraction =
        new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            throw new NoRemoteEspressoInstanceException("No remote instances available");
          }
        };

    when(mockViewFinder.getView()).thenReturn(targetView);
    // enable remote interaction
    when(mockRemoteInteraction.isRemoteProcess()).thenReturn(false);
    // noinspection unchecked
    when(mockRemoteInteraction.createRemoteCheckCallable(
            any(Matcher.class),
            any(Matcher.class),
            MockitoHamcrest.argThat(
                allOf(
                    hasEntry(
                        equalTo(RemoteInteraction.BUNDLE_EXECUTION_STATUS),
                        instanceOf(IBinder.class)))),
            any(ViewAssertion.class)))
        .thenReturn(failingRemoteInteraction);
    initWithViewInteraction();
    testInteraction.check(mockAssertion);
    verify(mockAssertion).check(targetView, null);
  }

  @Test
  public void verifyFailingCheckWithFailingRemoteInteractionPropagatesError() {
    NoActivityResumedException noActivityResumed =
        new NoActivityResumedException(
            "No activities in stage RESUMED. Did you forget to launch the activity. "
                + "(test.getActivity() or similar)?");

    Callable<Void> failingRemoteInteraction =
        new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            throw new NoRemoteEspressoInstanceException("No remote instances available");
          }
        };

    // fail local interaction in order to force wait for the remote interaction to finish
    when(mockViewFinder.getView()).thenThrow(noActivityResumed);
    // enable remote interaction
    when(mockRemoteInteraction.isRemoteProcess()).thenReturn(false);
    // noinspection unchecked
    when(mockRemoteInteraction.createRemoteCheckCallable(
            any(Matcher.class),
            any(Matcher.class),
            MockitoHamcrest.argThat(
                allOf(
                    hasEntry(
                        equalTo(RemoteInteraction.BUNDLE_EXECUTION_STATUS),
                        instanceOf(IBinder.class)))),
            any(ViewAssertion.class)))
        .thenReturn(failingRemoteInteraction);

    initWithViewInteraction();
    try {
      testInteraction.check(mockAssertion);
      fail("expected NoRemoteEspressoInstanceException");
    } catch (NoActivityResumedException e) {
      // ensure local NoActivityResumedException takes precedence over
      // remote NoRemoteEspressoInstanceException
      assertThat(e, is(noActivityResumed));
      verify(mockRemoteInteraction).isRemoteProcess();
      // noinspection unchecked
      verify(mockRemoteInteraction)
          .createRemoteCheckCallable(
              any(Matcher.class),
              any(Matcher.class),
              MockitoHamcrest.argThat(
                  allOf(
                      hasEntry(
                          equalTo(RemoteInteraction.BUNDLE_EXECUTION_STATUS),
                          instanceOf(IBinder.class)))),
              any(ViewAssertion.class));
    }
  }

  @Test
  public void verifyFailingCheckWithSuccessfulRemoteInteraction() {
    initWithRunCheckWithSuccessfulRemoteInteraction();
    initWithViewInteraction();

    testInteraction.check(mockAssertion);
    verify(mockRemoteInteraction).isRemoteProcess();
    // noinspection unchecked
    verify(mockRemoteInteraction)
        .createRemoteCheckCallable(
            any(Matcher.class),
            any(Matcher.class),
            MockitoHamcrest.argThat(
                allOf(
                    hasEntry(
                        equalTo(RemoteInteraction.BUNDLE_EXECUTION_STATUS),
                        instanceOf(IBinder.class)))),
            any(ViewAssertion.class));
  }

  @Test
  public void verifyPerformWithMultipleIBinders() {
    initWithRunPerformWithSuccessfulRemoteInteraction();
    initWithViewInteraction();

    String bindableViewActionId = BindableViewAction.class.getSimpleName();
    when(bindableMock.getId()).thenReturn(bindableViewActionId);
    when(bindableMock.getIBinder()).thenReturn(iBinderMock);

    ViewAction bindableViewAction = new BindableViewAction(mockAction, bindableMock);

    testInteraction.perform(bindableViewAction);
    // noinspection unchecked
    verify(mockRemoteInteraction)
        .createRemotePerformCallable(
            any(Matcher.class),
            any(Matcher.class),
            MockitoHamcrest.argThat(
                allOf(
                    hasEntry(
                        equalTo(RemoteInteraction.BUNDLE_EXECUTION_STATUS),
                        instanceOf(IBinder.class)),
                    hasEntry(equalTo(bindableViewActionId), instanceOf(IBinder.class)))),
            any(ViewAction.class));

    verify(bindableMock).getId();
    verify(bindableMock).getIBinder();
  }

  @Test
  public void verifyCheckWithMultipleIBinders() {
    initWithRunCheckWithSuccessfulRemoteInteraction();
    initWithViewInteraction();

    String bindableViewAssertionId = "test";
    when(bindableMock.getId()).thenReturn(bindableViewAssertionId);
    when(bindableMock.getIBinder()).thenReturn(iBinderMock);

    BindableViewAssertion bindableViewAssertion =
        new BindableViewAssertion(mockAssertion, bindableMock);

    testInteraction.check(bindableViewAssertion);
    // noinspection unchecked
    verify(mockRemoteInteraction)
        .createRemoteCheckCallable(
            any(Matcher.class),
            any(Matcher.class),
            MockitoHamcrest.argThat(
                allOf(
                    hasEntry(
                        equalTo(RemoteInteraction.BUNDLE_EXECUTION_STATUS),
                        instanceOf(IBinder.class)),
                    hasEntry(equalTo(bindableViewAssertionId), instanceOf(IBinder.class)))),
            any(ViewAssertion.class));

    verify(bindableMock).getId();
    verify(bindableMock).getIBinder();
  }

  private void initWithViewInteraction() {
    when(mockAction.getConstraints()).thenReturn(actionConstraint);

    testInteraction =
        new ViewInteraction(
            mockUiController,
            mockViewFinder,
            testExecutor,
            failureHandler,
            viewMatcher,
            rootMatcherRef,
            needsActivity,
            mockRemoteInteraction,
            MoreExecutors.listeningDecorator(
                new ThreadPoolExecutor(
                    0 /*corePoolSize*/,
                    5 /*maximumPoolSize*/,
                    10,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    new ThreadFactoryBuilder().setNameFormat("Espresso Remote #%d").build())),
            mockControlledLooper);
  }

  private void initWithRunPerformWithSuccessfulRemoteInteraction() {
    initWithRemoteInteraction();
    when(mockRemoteInteraction.createRemotePerformCallable(
            any(Matcher.class), any(Matcher.class), anyMap(), any(ViewAction.class)))
        .thenReturn(createSuccessfulListenableFutureStub());
  }

  private void initWithRunCheckWithSuccessfulRemoteInteraction() {
    initWithRemoteInteraction();
    when(mockRemoteInteraction.createRemoteCheckCallable(
            any(Matcher.class), any(Matcher.class), anyMap(), any(ViewAssertion.class)))
        .thenReturn(createSuccessfulListenableFutureStub());
  }

  private void initWithRemoteInteraction() {
    // fail local interaction in order to force wait for the remote interaction to finish
    when(mockViewFinder.getView())
        .thenThrow(
            new NoActivityResumedException(
                "No activities in stage RESUMED. Did you forget to launch the activity. "
                    + "(test.getActivity() or similar)?"));

    // enable remote interaction
    when(mockRemoteInteraction.isRemoteProcess()).thenReturn(false);
  }

  private static final class BindableViewAction implements ViewAction, Bindable {

    private final ViewAction viewActionMock;
    private final Bindable bindableMock;

    BindableViewAction(ViewAction viewActionMock, Bindable bindableMock) {
      this.viewActionMock = viewActionMock;
      this.bindableMock = bindableMock;
    }

    @Override
    public Matcher<View> getConstraints() {
      return viewActionMock.getConstraints();
    }

    @Override
    public String getDescription() {
      return viewActionMock.getDescription();
    }

    @Override
    public void perform(UiController uiController, View view) {
      viewActionMock.perform(uiController, view);
    }

    @Override
    public String getId() {
      return bindableMock.getId();
    }

    @Override
    public IBinder getIBinder() {
      return bindableMock.getIBinder();
    }

    @Override
    public void setIBinder(IBinder binder) {
      this.bindableMock.setIBinder(binder);
    }
  }

  private static final class BindableViewAssertion implements ViewAssertion, Bindable {

    private final ViewAssertion viewAssertionMock;
    private final Bindable bindableMock;

    BindableViewAssertion(ViewAssertion viewAssertionMock, Bindable bindableMock) {
      this.viewAssertionMock = viewAssertionMock;
      this.bindableMock = bindableMock;
    }

    @Override
    public String getId() {
      return bindableMock.getId();
    }

    @Override
    public IBinder getIBinder() {
      return bindableMock.getIBinder();
    }

    @Override
    public void setIBinder(IBinder binder) {
      this.bindableMock.setIBinder(binder);
    }

    @Override
    public void check(View view, NoMatchingViewException noViewFoundException) {
      viewAssertionMock.check(view, noViewFoundException);
    }
  }
}
