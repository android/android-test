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

package androidx.test.espresso.web.action;

import static androidx.test.espresso.matcher.ViewMatchers.isJavascriptEnabled;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.util.concurrent.Futures.transform;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.remote.Bindable;
import androidx.test.espresso.web.model.Atom;
import androidx.test.espresso.web.model.ElementReference;
import androidx.test.espresso.web.model.Evaluation;
import androidx.test.espresso.web.model.WindowReference;
import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;
import org.hamcrest.Matcher;

/**
 * A ViewAction which causes the provided Atom to be evaluated within a webview.
 *
 * <p>It is not recommended to use AtomAction directly.
 *
 * <p>Instead {@see androidx.test.espresso.web.sugar.Web} for examples of how to interact
 * with a WebView's content through Atoms.
 *
 * <p>If you must use AtomAction directly, take care to remember that they are Stateful (unlike most
 * ViewActions) and the caller must call {@link #get()} to ensure that the action has completed.
 *
 * @param <E> The type the specific Atom returns.
 */
public final class AtomAction<E> implements ViewAction, Bindable {
  private static final String TAG = "AtomAction";
  private static final String ID = TAG;
  private static final String EVALUATION_ERROR_KEY = "evaluation_error_key";
  private final SettableFuture<Evaluation> futureEval = SettableFuture.create();
  final Atom<E> atom;
  @Nullable final WindowReference window;
  @Nullable final ElementReference element;

  private IAtomActionResultPropagator atomActionResultPropagator =
      new IAtomActionResultPropagator.Stub() {

        @Override
        public void setResult(Evaluation evaluation) throws RemoteException {
          futureEval.set(evaluation);
        }

        @Override
        public void setError(Bundle bundle) throws RemoteException {
          Throwable evalError = (Throwable) bundle.getSerializable(EVALUATION_ERROR_KEY);
          futureEval.setException(evalError);
        }
      };

  /**
   * Creates an AtomAction.
   *
   * @param atom the atom to execute
   * @param window (optional/nullable) the window context to execute on.
   * @param element (optional/nullable) the element to execute on.
   */
  public AtomAction(
      Atom<E> atom, @Nullable WindowReference window, @Nullable ElementReference element) {
    this.atom = checkNotNull(atom);
    this.window = window;
    this.element = element;
  }

  @Override
  public Matcher<View> getConstraints() {
    return isJavascriptEnabled();
  }

  @Override
  public String getDescription() {
    return String.format("Evaluate Atom: %s in window: %s with element: %s", atom, window, element);
  }

  @Override
  public void perform(UiController controller, View view) {
    WebView webView = (WebView) view;
    if (Build.VERSION.SDK_INT >= 23 && !webView.isHardwareAccelerated()) {
      throw new PerformException.Builder()
          .withViewDescription(webView.toString())
          .withCause(
              new RuntimeException("Hardware acceleration is not supported on current device"))
          .build();
    }
    List<Object> arguments = checkNotNull(atom.getArguments(element));
    String script = checkNotNull(atom.getScript());
    final ListenableFuture<Evaluation> localEval =
        JavascriptEvaluation.evaluate(webView, script, arguments, window);
    if (null != window && Build.VERSION.SDK_INT == 19) {
      Log.w(
          TAG,
          "WARNING: KitKat does not report when an iframe is loading new content. "
              + "If you are interacting with content within an iframe and that content is changing "
              + "(eg: you have just pressed a submit button). Espresso will not be able to block "
              + "you until the new content has loaded (which it can do on all other API levels). "
              + "You will need to have some custom polling / synchronization with the iframe in "
              + "that case.");
    }

    localEval.addListener(
        new Runnable() {
          @Override
          public void run() {
            try {
              atomActionResultPropagator.setResult(localEval.get());
            } catch (ExecutionException ee) {
              reportException(ee.getCause());
            } catch (InterruptedException ie) {
              reportException(ie);
            } catch (RemoteException re) {
              reportException(re);
            }
          }
        },
        MoreExecutors.directExecutor());
  }

  private void reportException(Throwable throwable) {
    Bundle errorBundle = new Bundle();
    errorBundle.putSerializable(EVALUATION_ERROR_KEY, throwable);
    try {
      atomActionResultPropagator.setError(errorBundle);
    } catch (RemoteException re) {
      Log.e(TAG, "Cannot report error to result propagator", re);
    }
  }

  /**
   * Return a Future, which will be set and transformed from futureEval. Espresso's public API
   * cannot have guava types in its method signatures, so return Future instead of ListenableFuture
   * or SettableFuture.
   */
  public Future<E> getFuture() {
    return transform(
        futureEval,
        new Function<Evaluation, E>() {
          @Override
          public E apply(Evaluation e) {
            return atom.transform(e);
          }
        },
        directExecutor());
  }

  /** Blocks until the atom has completed execution. */
  public E get() throws ExecutionException, InterruptedException {
    checkState(Looper.myLooper() != Looper.getMainLooper(), "On main thread!");
    return getFuture().get();
  }

  /** Blocks until the atom has completed execution with a configurable timeout. */
  public E get(long val, TimeUnit unit)
      throws ExecutionException, InterruptedException, TimeoutException {
    checkState(Looper.myLooper() != Looper.getMainLooper(), "On main thread!");
    return getFuture().get(val, unit);
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public IBinder getIBinder() {
    return atomActionResultPropagator.asBinder();
  }

  @Override
  public void setIBinder(IBinder binder) {
    atomActionResultPropagator = IAtomActionResultPropagator.Stub.asInterface(binder);
  }
}
