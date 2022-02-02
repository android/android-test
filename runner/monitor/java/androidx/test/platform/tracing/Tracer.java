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

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.test.annotation.ExperimentalTestApi;
import com.google.errorprone.annotations.MustBeClosed;
import java.io.Closeable;

/**
 * {@link Tracer} provides an API implemented by wrappers around common tracing libraries, such as
 * AndroidX Tracing. Tracer wrappers should be registered with {@link Tracing#registerTracer}.
 *
 * <p>A trace tracks the progression of various actions during testing. Each action is wrapped in a
 * "span". A span starts when the {@link #beginSpan(String)} method is called and ends when the
 * {@link Span#close()} method is called.
 *
 * <p>Traces are recursive. Within a specific span, nested spans are created by invoking {@link
 * Span#beginChildSpan(String)} and end when a corresponding {@link Span#close()} method is called.
 * Nested spans must be entirely constrained within their parent span.
 *
 * <p>The {@link Span} interface implements {@link Closeable} to encourage its use in a try-resource
 * block and ensure that the {@link Span#close()} method is always properly called. Example of
 * usage:
 *
 * <pre>
 *   class MyCustomTracingLibraryTracer implements Tracer {}
 *   Tracer tracer = new MyCustomTracingLibraryTracer();
 *   Tracing.getInstance().registerTracer(tracer);
 *   try(Span span = Tracing.getInstance().beginSpan("Some Action")) {
 *     ...action to be traced...
 *   }
 * </pre>
 */
@ExperimentalTestApi
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) // used by core and espresso
public interface Tracer {

  /**
   * Starts a new time span to track progression of some action.
   *
   * <p>The implementation must return a {@link Span} object which the caller must use later to
   * close and end the span.
   *
   * @param name A name describing the action performed.
   * @return A new span object that the caller must invoke later to close the span.
   */
  @NonNull
  @MustBeClosed
  Span beginSpan(@NonNull String name);

  /**
   * Span is a helper object denoting an ongoing time span and providing a way for the caller to
   * close and end the span to record the end of the action being traced.
   *
   * <p>The Span interface implements {@link Closeable} to encourage its use in a try-resource block
   * and ensure that the {@link #close()} method is always properly called.
   */
  @ExperimentalTestApi
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) // used by core and espresso
  interface Span extends Closeable {
    /**
     * Starts a new time span nested in a parent span, to track progression of some inner action.
     *
     * <p>The implementation must return a {@link Span} object which the caller must use later to
     * close and end the span.
     *
     * <p>Multiple child spans can be created for the same parent span. It is expected that the
     * child spans do not overlap.
     *
     * @param name A name describing the inner action performed.
     * @return A new span object that the caller must invoke later to close the span.
     */
    @NonNull
    @MustBeClosed
    Span beginChildSpan(@NonNull String name);

    /**
     * Closes and ends this span.
     *
     * <p>Callers must not interact any further with this span.
     */
    @Override
    void close();
  }
}
