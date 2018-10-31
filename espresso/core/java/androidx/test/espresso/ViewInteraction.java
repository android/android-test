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

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.internal.util.LogUtil.logDebugWithProcess;
import static com.google.common.base.Preconditions.checkNotNull;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import androidx.test.espresso.action.ScrollToAction;
import androidx.test.espresso.base.InterruptableUiController;
import androidx.test.espresso.base.MainThread;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.remote.Bindable;
import androidx.test.espresso.remote.IInteractionExecutionStatus;
import androidx.test.espresso.remote.RemoteInteraction;
import androidx.test.espresso.util.HumanReadables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.StringDescription;

/**
 * Provides the primary interface for test authors to perform actions or asserts on views.
 *
 * <p>Each interaction is associated with a view identified by a view matcher. All view actions and
 * asserts are performed on the UI thread (thus ensuring sequential execution). The same goes for
 * retrieval of views (this is done to ensure that view state is "fresh" prior to execution of each
 * operation).
 */
public final class ViewInteraction {

  private static final String TAG = ViewInteraction.class.getSimpleName();

  private final InterruptableUiController uiController;
  private final ViewFinder viewFinder;
  private final Executor mainThreadExecutor;
  private volatile FailureHandler failureHandler;
  private final Matcher<View> viewMatcher;
  private final AtomicReference<Matcher<Root>> rootMatcherRef;
  private final AtomicReference<Boolean> needsActivity;
  private final RemoteInteraction remoteInteraction;
  private final ListeningExecutorService remoteExecutor;
  // test thread only
  private boolean hasRootMatcher = false;

  @Inject
  ViewInteraction(
      UiController uiController,
      ViewFinder viewFinder,
      @MainThread Executor mainThreadExecutor,
      FailureHandler failureHandler,
      Matcher<View> viewMatcher,
      AtomicReference<Matcher<Root>> rootMatcherRef,
      AtomicReference<Boolean> needsActivity,
      RemoteInteraction remoteInteraction,
      ListeningExecutorService remoteExecutor) {
    this.viewFinder = checkNotNull(viewFinder);
    this.uiController = (InterruptableUiController) checkNotNull(uiController);
    this.failureHandler = checkNotNull(failureHandler);
    this.mainThreadExecutor = checkNotNull(mainThreadExecutor);
    this.viewMatcher = checkNotNull(viewMatcher);
    this.rootMatcherRef = checkNotNull(rootMatcherRef);
    this.needsActivity = checkNotNull(needsActivity);
    this.remoteInteraction = checkNotNull(remoteInteraction);
    this.remoteExecutor = checkNotNull(remoteExecutor);
  }

  /**
   * Performs the given action(s) on the view selected by the current view matcher. If more than one
   * action is provided, actions are executed in the order provided with precondition checks running
   * prior to each action.
   *
   * @param viewActions one or more actions to execute.
   * @return this interaction for further perform/verification calls.
   */
  public ViewInteraction perform(final ViewAction... viewActions) {
    checkNotNull(viewActions);
    for (ViewAction va : viewActions) {
      SingleExecutionViewAction singleExecutionViewAction =
          new SingleExecutionViewAction(va, viewMatcher);
      desugaredPerform(singleExecutionViewAction);
    }
    return this;
  }

  private static Map<String, IBinder> getIBindersFromBindables(List<Bindable> bindables) {
    Map<String, IBinder> iBinders = new HashMap<>();
    for (Bindable bindable : bindables) {
      iBinders.put(
          checkNotNull(bindable.getId(), "Bindable id cannot be null!"),
          checkNotNull(bindable.getIBinder(), "Bindable binder cannot be null!"));
    }
    return ImmutableMap.copyOf(iBinders);
  }

  private static List<Bindable> getBindables(Object... objects) {
    List<Bindable> bindables = Lists.newArrayListWithCapacity(objects.length);
    for (Object object : objects) {
      if (object instanceof Bindable) {
        bindables.add((Bindable) object);
      }
    }
    return bindables;
  }

  private static Map<String, IBinder> getIBindersFromViewActions(ViewAction... viewActions) {
    return getIBindersFromBindables(getBindables((Object[]) viewActions));
  }

  private static Map<String, IBinder> getIBindersFromViewAssertions(
      ViewAssertion... viewAssertions) {
    return getIBindersFromBindables(getBindables((Object[]) viewAssertions));
  }

  private void desugaredPerform(final SingleExecutionViewAction va) {
    Callable<Void> performInteraction =
        new Callable<Void>() {
          @Override
          public Void call() {
            doPerform(va);
            return null;
          }
        };

    ViewAction innerViewAction = va.getInnerViewAction();

    List<ListenableFuture<Void>> interactions = new ArrayList<>();
    interactions.add(runSynchronouslyOnUiThread(performInteraction));
    if (!remoteInteraction.isRemoteProcess()) {
      // Only the original process should submit remote interactionsList;
      interactions.add(
          remoteExecutor.submit(
              remoteInteraction.createRemotePerformCallable(
                  rootMatcherRef.get(),
                  viewMatcher,
                  getIBindersFromViewActions(va, innerViewAction),
                  innerViewAction)));
    }

    waitForAndHandleInteractionResults(interactions);
  }

  /**
   * Replaces the default failure handler (@see Espresso.setFailureHandler) with a custom
   * failurehandler for this particular interaction.
   *
   * @param failureHandler a non-null failurehandler to use to report failures.
   * @return this interaction for further perform/verification calls.
   */
  public ViewInteraction withFailureHandler(FailureHandler failureHandler) {
    this.failureHandler = checkNotNull(failureHandler);
    return this;
  }

  /** Makes this ViewInteraction scoped to the root selected by the given root matcher. */
  public ViewInteraction inRoot(Matcher<Root> rootMatcher) {
    hasRootMatcher = true;
    this.rootMatcherRef.set(checkNotNull(rootMatcher));
    return this;
  }

  /** Removes the need of waiting for an Activity before performing a ViewAction/ViewAssertion */
  public ViewInteraction noActivity() {
    if (!hasRootMatcher) {
      this.rootMatcherRef.set(
          Matchers.anyOf(
              RootMatchers.DEFAULT,
              Matchers.allOf(
                  RootMatchers.hasWindowLayoutParams(), RootMatchers.isSystemAlertWindow())));
    }
    this.needsActivity.set(false);
    return this;
  }

  /**
   * Performs the given action on the view selected by the current view matcher. Should be executed
   * on the main thread.
   *
   * @param viewAction the action to execute.
   */
  private void doPerform(final SingleExecutionViewAction viewAction) {
    checkNotNull(viewAction);
    final Matcher<? extends View> constraints = checkNotNull(viewAction.getConstraints());
    uiController.loopMainThreadUntilIdle();
    View targetView = viewFinder.getView();
    Log.i(
        TAG,
        String.format(
            "Performing '%s' action on view %s", viewAction.getDescription(), viewMatcher));
    if (!constraints.matches(targetView)) {
      // TODO: update this to describeMismatch once hamcrest 1.4 is available
      StringDescription stringDescription =
          new StringDescription(
              new StringBuilder(
                  "Action will not be performed because the target view "
                      + "does not match one or more of the following constraints:\n"));
      constraints.describeTo(stringDescription);
      stringDescription
          .appendText("\nTarget view: ")
          .appendValue(HumanReadables.describe(targetView));

      if (viewAction.getInnerViewAction() instanceof ScrollToAction
          && isDescendantOfA(isAssignableFrom((AdapterView.class))).matches(targetView)) {
        stringDescription.appendText(
            "\nFurther Info: ScrollToAction on a view inside an AdapterView will not work. "
                + "Use Espresso.onData to load the view.");
      }
      throw new PerformException.Builder()
          .withActionDescription(viewAction.getDescription())
          .withViewDescription(viewMatcher.toString())
          .withCause(new RuntimeException(stringDescription.toString()))
          .build();
    } else {
      viewAction.perform(uiController, targetView);
    }
  }

  /**
   * Checks the given {@link ViewAssertion} on the the view selected by the current view matcher.
   *
   * @param viewAssert the assertion to check.
   * @return this interaction for further perform/verification calls.
   */
  public ViewInteraction check(final ViewAssertion viewAssert) {
    checkNotNull(viewAssert);

    final SingleExecutionViewAssertion singleExecutionViewAssertion =
        new SingleExecutionViewAssertion(viewAssert);

    Callable<Void> checkInteraction =
        new Callable<Void>() {
          @Override
          public Void call() {
            uiController.loopMainThreadUntilIdle();

            View targetView = null;
            NoMatchingViewException missingViewException = null;
            try {
              targetView = viewFinder.getView();
            } catch (NoMatchingViewException nsve) {
              missingViewException = nsve;
            }
            Log.i(
                TAG, String.format("Checking '%s' assertion on view %s", viewAssert, viewMatcher));
            singleExecutionViewAssertion.check(targetView, missingViewException);
            return null;
          }
        };

    List<ListenableFuture<Void>> interactions = new ArrayList<>();
    interactions.add(runSynchronouslyOnUiThread(checkInteraction));
    if (!remoteInteraction.isRemoteProcess()) {
      // Only the original process should submit remote interactionsList;
      interactions.add(
          remoteExecutor.submit(
              remoteInteraction.createRemoteCheckCallable(
                  rootMatcherRef.get(),
                  viewMatcher,
                  getIBindersFromViewAssertions(singleExecutionViewAssertion, viewAssert),
                  viewAssert)));
    }

    waitForAndHandleInteractionResults(interactions);
    return this;
  }

  private ListenableFuture<Void> runSynchronouslyOnUiThread(Callable<Void> interaction) {
    ListenableFutureTask<Void> mainThreadInteraction = ListenableFutureTask.create(interaction);
    mainThreadExecutor.execute(mainThreadInteraction);
    return mainThreadInteraction;
  }

  private void waitForAndHandleInteractionResults(List<ListenableFuture<Void>> interactions) {
    try {
      // Blocking call
      InteractionResultsHandler.gatherAnyResult(interactions);
    } catch (RuntimeException ee) {
      failureHandler.handle(ee, viewMatcher);
    } catch (Error error) {
      failureHandler.handle(error, viewMatcher);
    } finally {
      uiController.interruptEspressoTasks();
    }
  }

  private static final class SingleExecutionViewAction implements ViewAction, Bindable {

    final ViewAction viewAction;
    final Matcher<View> viewMatcher;

    // Instance of interaction execution status that should be checked before each interaction.
    // Helps with synchronization across processes.
    private IInteractionExecutionStatus actionExecutionStatus =
        new IInteractionExecutionStatus.Stub() {
          AtomicBoolean run = new AtomicBoolean(true);

          @Override
          public boolean canExecute() throws RemoteException {
            return run.getAndSet(false);
          }
        };

    private SingleExecutionViewAction(ViewAction viewAction, Matcher<View> viewMatcher) {
      this.viewAction = viewAction;
      this.viewMatcher = viewMatcher;
    }

    @Override
    public Matcher<View> getConstraints() {
      return viewAction.getConstraints();
    }

    @Override
    public String getDescription() {
      return viewAction.getDescription();
    }

    @Override
    public void perform(UiController uiController, View view) {
      try {
        if (actionExecutionStatus.canExecute()) {
          viewAction.perform(uiController, view);
        } else {
          logDebugWithProcess(
              TAG, "Attempted to execute a Single Execution Action more then once: " + viewAction);
        }
      } catch (RemoteException e) {
        throw new PerformException.Builder()
            .withActionDescription(viewAction.getDescription())
            .withViewDescription(viewMatcher.toString())
            .withCause(
                new RuntimeException("Unable to query interaction execution status", e.getCause()))
            .build();
      }
    }

    ViewAction getInnerViewAction() {
      return viewAction;
    }

    @Override
    public String getId() {
      return RemoteInteraction.BUNDLE_EXECUTION_STATUS;
    }

    @Override
    public IBinder getIBinder() {
      return actionExecutionStatus.asBinder();
    }

    @Override
    public void setIBinder(IBinder binder) {
      actionExecutionStatus = IInteractionExecutionStatus.Stub.asInterface(binder);
    }
  }

  private static final class SingleExecutionViewAssertion implements ViewAssertion, Bindable {

    final ViewAssertion viewAssertion;

    // Instance of interaction execution status that should be checked before each interaction.
    // Helps with synchronization across processes.
    private IInteractionExecutionStatus assertionExecutionStatus =
        new IInteractionExecutionStatus.Stub() {
          AtomicBoolean run = new AtomicBoolean(true);

          @Override
          public boolean canExecute() throws RemoteException {
            return run.getAndSet(false);
          }
        };

    private SingleExecutionViewAssertion(ViewAssertion viewAssertion) {
      this.viewAssertion = viewAssertion;
    }

    @Override
    public void check(View view, NoMatchingViewException noViewFoundException) {
      try {
        if (assertionExecutionStatus.canExecute()) {
          viewAssertion.check(view, noViewFoundException);
        } else {
          logDebugWithProcess(
              TAG,
              "Attempted to execute a Single Execution Assertion more then once: " + viewAssertion);
        }
      } catch (RemoteException e) {
        throw new RuntimeException("Unable to query interaction execution status", e.getCause());
      }
    }

    @Override
    public String getId() {
      return RemoteInteraction.BUNDLE_EXECUTION_STATUS;
    }

    @Override
    public IBinder getIBinder() {
      return assertionExecutionStatus.asBinder();
    }

    @Override
    public void setIBinder(IBinder binder) {
      assertionExecutionStatus = IInteractionExecutionStatus.Stub.asInterface(binder);
    }
  }
}
