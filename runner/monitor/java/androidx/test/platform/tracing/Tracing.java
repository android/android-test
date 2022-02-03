/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.platform.tracing;

import static androidx.test.internal.util.Checks.checkNotNull;

import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.test.annotation.ExperimentalTestApi;
import androidx.test.platform.tracing.Tracer.Span;
import com.google.errorprone.annotations.MustBeClosed;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link Tracing} is a singleton to interact with registered {@link Tracer} implementations.
 *
 * <p>Support for actual tracing libraries is done by implementing wrappers following the {@link
 * Tracer} interface. The actual {@link Tracer} implementations must be registered using {@link
 * Tracing#registerTracer(Tracer)}.
 *
 * <p>The {@link Tracing} singleton is also an entry point used to create the root span for any
 * tests by invoking the {@link #beginSpan(String)} method. The returned {@link Span} object must be
 * properly closed by invoking the {@link Span#close()} method or wrapping them in a try-resource
 * block.
 */
@ExperimentalTestApi
public final class Tracing {
  private static final String TAG = Tracing.class.getSimpleName();
  private static final Tracing singleton = new Tracing();
  private final List<Tracer> tracers = Collections.synchronizedList(new ArrayList<>());

  private Tracing() {
    // The Android Tracing API only exists starting with JB MR2 (API 18).
    if (Build.VERSION.SDK_INT >= 18) {
      registerTracer(new AndroidXTracer());
    }
  }

  /**
   * Static getter for external access to the singleton. <br>
   * Preferred method for internal Espresso is getting this singleton via dagger injection.
   */
  @NonNull
  public static Tracing getInstance() {
    return singleton;
  }

  /**
   * Registers a new tracer.
   *
   * <p>Once a tracer is registered, it starts being invoked for new root {@link #beginSpan(String)}
   * calls. Existing in-flight spans do not invoke the new tracer.
   *
   * @param tracer A non-null Tracer object. A previously registered instance is ignored.
   */
  public void registerTracer(@NonNull Tracer tracer) {
    checkNotNull(tracer, "Tracer cannot be null.");
    if (tracers.contains(tracer)) {
      Log.w(TAG, "Tracer already present: " + tracer.getClass());
    } else {
      Log.i(TAG, "Tracer added: " + tracer.getClass());
      tracers.add(tracer);
    }
  }

  /**
   * Unregisters a tracer.
   *
   * <p>Once a tracer is unregistered, it will stop being invoked for any new root {@link
   * #beginSpan(String)} calls made or any new {@link Span#beginChildSpan(String)}. However the
   * tracer is still be called for any in-flight spans being closed.
   *
   * @param tracer A Tracer object. A null reference or non-registered instance is ignored.
   */
  public void unregisterTracer(Tracer tracer) {
    tracers.remove(tracer);
    Log.i(TAG, "Tracer removed: " + (tracer == null ? null : tracer.getClass()));
  }

  /**
   * Returns a new Span as a managed resource in a try{} block. {@link Span#close()} is
   * automatically called when the resource is released.
   *
   * @param name A non-null name describing this span.
   */
  @NonNull
  @MustBeClosed
  public Span beginSpan(@NonNull String name) {
    checkNotNull(name);
    Map<Tracer, Span> spans;
    synchronized (tracers) {
      spans = new HashMap<>(tracers.size());
      for (Tracer tracer : tracers) {
        spans.put(tracer, createUnmanagedSpan(tracer, name));
      }
    }
    return new TracerSpan(spans);
  }

  /**
   * TracerSpan is an internal implementation class of a parent span.
   *
   * <p>Each Tracer creates its own Span instance wrapping a specific trace event; TracerSpan keeps
   * track of which Tracer creates which Span. <br>
   * When creating child spans, only tracers involved in the original parent span are called if they
   * are still registered.
   */
  class TracerSpan implements Span {
    private final Map<Tracer, Span> spans;

    private TracerSpan(@NonNull Map<Tracer, Span> spans) {
      this.spans = spans;
    }

    /**
     * Creates a child span for any tracer that participated in the parent span if that tracer is
     * still registered.
     */
    @NonNull
    @Override
    public Span beginChildSpan(@NonNull String name) {
      checkNotNull(name);
      Map<Tracer, Span> childSpans;
      synchronized (tracers) {
        childSpans = new HashMap<>(tracers.size());
        for (Tracer tracer : tracers) {
          Span parentSpan = spans.get(tracer);
          if (parentSpan != null) {
            childSpans.put(tracer, createUnmanagedChildSpan(parentSpan, name));
          }
        }
      }

      return new TracerSpan(childSpans);
    }

    /** All spans are closed, even if the tracer has been unregistered in between. */
    @Override
    public void close() {
      for (Span span : spans.values()) {
        span.close();
      }
    }
  }

  /**
   * Internal method to create a Span that does not enforce try-resource usage. Caller <em>must</em>
   * manually call {@link Span#close()} later on the resource.
   */
  @SuppressWarnings("MustBeClosedChecker")
  private static Span createUnmanagedSpan(Tracer tracer, String name) {
    return tracer.beginSpan(name);
  }

  /**
   * Internal method to create a Span that does not enforce try-resource usage. Caller <em>must</em>
   * manually call {@link Span#close()} later on the resource.
   */
  @SuppressWarnings("MustBeClosedChecker")
  private static Span createUnmanagedChildSpan(Span parentSpan, String name) {
    return parentSpan.beginChildSpan(name);
  }
}
