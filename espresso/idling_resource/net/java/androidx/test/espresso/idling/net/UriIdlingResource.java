/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Lice`nse is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso.idling.net;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.VisibleForTesting;
import android.util.Log;
import androidx.test.espresso.IdlingResource;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * An implementation of {@link IdlingResource} useful for monitoring idleness of network traffic.
 *
 * <p>This is similar to {@link androidx.test.espresso.contrib.CountingIdlingResource}, with
 * the additional idleness constraint that the counter must be 0 for a set period of time before the
 * resource becomes idle.
 *
 * <p>A network timeout is required to be reasonably sure that the webview has finished loading.
 * Imagine the case where each response that comes back causes another request to be made until
 * loading is complete. The counter will go from 0->1->0->1->0->1..., but we don't want to report
 * the webview as idle each time this happens.
 *
 * <p><b>This API is currently in beta.</b>
 */
public class UriIdlingResource implements IdlingResource {

  private static final String TAG = "UriIdlingResource";
  private final String resourceName;
  private final long timeoutMs;
  private final boolean debug;

  // Read and modified from multiple threads
  private final AtomicInteger counter = new AtomicInteger(0);
  private final CopyOnWriteArrayList<Pattern> ignoredRegexes = new CopyOnWriteArrayList<>();
  private final AtomicBoolean idle = new AtomicBoolean(true);
  private final Runnable transitionToIdle;
  private volatile ResourceCallback resourceCallback;
  private final HandlerIntf handler;

  public UriIdlingResource(String resourceName, long timeoutMs) {
    this(resourceName, timeoutMs, false, new DefaultHandler(new Handler(Looper.getMainLooper())));
  }

  @VisibleForTesting
  UriIdlingResource(String resourceName, long timeoutMs, boolean debug, HandlerIntf handler) {
    if (timeoutMs <= 0) {
      throw new IllegalArgumentException("timeoutMs has to be greater than 0");
    }
    this.resourceName = resourceName;
    this.timeoutMs = timeoutMs;
    this.debug = debug;
    this.handler = handler;

    transitionToIdle =
        new Runnable() {
          @Override
          public void run() {
            idle.set(true);
            if (resourceCallback != null) {
              resourceCallback.onTransitionToIdle();
            }
          }
        };
  }

  @Override
  public String getName() {
    return resourceName;
  }

  @Override
  public boolean isIdleNow() {
    return idle.get();
  }

  @Override
  public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
    this.resourceCallback = resourceCallback;
  }

  /**
   * Add a regex pattern to the ignore list.
   *
   * <p>All request URIs are checked against all patterns, and matches are ignored for the purposes
   * of detecting when the webview is idle.
   *
   * <p>Ignored patterns can only be added when the webview is idle.
   */
  public void ignoreUri(Pattern pattern) {
    if (!isIdleNow()) {
      Log.e(TAG, "Ignored patterns can only be added when the resource is idle.");
    } else {
      ignoredRegexes.add(pattern);
    }
  }

  /**
   * Called when a request is made.
   *
   * <p>If the URI is not blacklisted the idle counter is incremented.
   */
  public void beginLoad(String uri) {
    if (uriIsIgnored(uri)) {
      return;
    }
    idle.set(false);
    long count = counter.getAndIncrement();
    if (count == 0) {
      handler.removeCallbacks(transitionToIdle);
    }
    if (debug) {
      Log.i(TAG, "Resource " + resourceName + " counter increased to " + (count + 1));
    }
  }

  /**
   * Called when a request is completed (ie the response is returned).
   *
   * <p>If the URI is not blacklisted the idle counter is decremented. Once the idle counter reaches
   * 0, the idle update thread will set the resource as idle after the appropriate timeout.
   */
  public void endLoad(String uri) {
    if (uriIsIgnored(uri)) {
      return;
    }
    int count = counter.decrementAndGet();
    if (count < 0) {
      throw new IllegalStateException("Counter has been corrupted! Count=" + count);
    } else if (count == 0) {
      handler.postDelayed(transitionToIdle, timeoutMs);
    }
    if (debug) {
      Log.i(TAG, "Resource " + resourceName + " counter decreased to " + count);
    }
  }

  private boolean uriIsIgnored(String uri) {
    for (Pattern pattern : ignoredRegexes) {
      if (pattern.matcher(uri).matches()) {
        Log.i(TAG, "Resource " + resourceName + " ignored URI: <" + uri + ">");
        return true;
      }
    }
    return false;
  }

  @VisibleForTesting
  void forceIdleTransition() {
    transitionToIdle.run();
  }

  /**
   * Wraps a Handler object.
   *
   * <p>Mock this for testing purposes.
   */
  public static interface HandlerIntf {
    public void postDelayed(Runnable runnable, long millis);

    public void removeCallbacks(Runnable runnable);
  }

  private static final class DefaultHandler implements HandlerIntf {
    private final Handler handler;

    public DefaultHandler(Handler handler) {
      this.handler = handler;
    }

    @Override
    public void postDelayed(Runnable runnable, long millis) {
      handler.postDelayed(runnable, millis);
    }

    @Override
    public void removeCallbacks(Runnable runnable) {
      handler.removeCallbacks(runnable);
    }
  }
}
