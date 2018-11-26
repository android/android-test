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

package androidx.test.espresso.web.assertion;

import static com.google.common.base.Preconditions.checkNotNull;

import androidx.annotation.VisibleForTesting;
import android.view.View;
import android.webkit.WebView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.remote.annotation.RemoteMsgConstructor;
import androidx.test.espresso.remote.annotation.RemoteMsgField;
import androidx.test.espresso.web.model.Atom;

/**
 * Similar to a {@link ViewAssertion}, a {@link WebAssertion} allows users to check the results of
 * an atom evaluated against the provided {@link WebView}.
 *
 * @param <E> The type the specific {@link Atom} returns.
 */
public abstract class WebAssertion<E> {

  @RemoteMsgField(order = 0)
  private final Atom<E> atom;

  @RemoteMsgConstructor
  public WebAssertion(Atom<E> atom) {
    this.atom = checkNotNull(atom);
  }

  public final Atom<E> getAtom() {
    return atom;
  }

  public final ViewAssertion toViewAssertion(final E result) {
    return new CheckResultWebAssertion<>(checkNotNull(result), this);
  }

  /**
   * Extension point to validate a view and atom result on the main thread.
   *
   * @param view the WebView that the Atom was evaluated on.
   * @param result the result of atom evaluation.
   */
  protected abstract void checkResult(WebView view, E result);

  @VisibleForTesting
  static final class CheckResultWebAssertion<T> implements ViewAssertion {
    final T result;
    final WebAssertion<T> webAssertion;

    CheckResultWebAssertion(T result, WebAssertion<T> webAssertion) {
      this.result = result;
      this.webAssertion = webAssertion;
    }

    @Override
    public void check(View view, NoMatchingViewException noViewFoundException) {
      if (null == view) {
        throw noViewFoundException;
      } else if (!(view instanceof WebView)) {
        throw new RuntimeException(view + ": is not a WebView!");
      } else {
        webAssertion.checkResult((WebView) view, result);
      }
    }
  }
}
