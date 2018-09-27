/*
 * Copyright (C) 2016 The Android Open Source Project
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
 *
 */

package androidx.test.espresso.remote;

import android.os.IBinder;
import android.view.View;
import androidx.test.espresso.Root;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
import java.util.Map;
import java.util.concurrent.Callable;
import org.hamcrest.Matcher;

/**
 * Facilitates communication between other Espresso instance that may be running in different
 * processes.
 */
public interface RemoteInteraction {
  static final String BUNDLE_EXECUTION_STATUS = "executionStatus";

  /** @return {@code true} if the current Espresso instance running in a remote process. */
  boolean isRemoteProcess();

  /**
   * Creates a callable to run Espresso check interaction on remote processes
   *
   * <p>The caller is expected to schedule the task to run.
   *
   * @param rootMatcher the root matcher to use.
   * @param viewMatcher the view matcher to use.
   * @param iBinders a list of binders to pass along to the remote process instance
   * @param viewAssert the assertion to check.
   * @return a {@link Callable} that will perform the check pending completion of the task.
   */
  Callable<Void> createRemoteCheckCallable(
      Matcher<Root> rootMatcher,
      Matcher<View> viewMatcher,
      Map<String, IBinder> iBinders,
      ViewAssertion viewAssert);

  /**
   * Creates a callable to run a perform interaction on remote processes.
   *
   * <p>If there no remote Espresso currently running in a timely manner the interaction will not be
   * executed and a {@link NoRemoteEspressoInstanceException} will be thrown.
   *
   * @param rootMatcher the root matcher to use.
   * @param viewMatcher the view matcher to use.
   * @param viewActions one or more actions to execute.
   * @param iBinders a list of binders to pass along to the remote process instance
   * @return a {@link Callable} that performs the action.
   */
  Callable<Void> createRemotePerformCallable(
      Matcher<Root> rootMatcher,
      Matcher<View> viewMatcher,
      Map<String, IBinder> iBinders,
      ViewAction... viewActions);
}
