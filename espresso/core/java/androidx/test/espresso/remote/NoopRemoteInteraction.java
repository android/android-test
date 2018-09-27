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

/** Noop RemoteInteraction object */
public class NoopRemoteInteraction implements RemoteInteraction {

  @Override
  public boolean isRemoteProcess() {
    return false;
  }

  @Override
  public Callable<Void> createRemoteCheckCallable(
      Matcher<Root> rootMatcher,
      Matcher<View> viewMatcher,
      Map<String, IBinder> iBinders,
      ViewAssertion viewAssertion) {
    return new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        throw new NoRemoteEspressoInstanceException("No remote instances available");
      }
    };
  }

  @Override
  public Callable<Void> createRemotePerformCallable(
      Matcher<Root> rootMatcher,
      Matcher<View> viewMatcher,
      Map<String, IBinder> iBinders,
      ViewAction... viewActions) {
    return new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        throw new NoRemoteEspressoInstanceException("No remote instances available");
      }
    };
  }
}
