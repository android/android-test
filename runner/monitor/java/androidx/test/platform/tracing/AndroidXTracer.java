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

import androidx.tracing.Trace;
import java.util.ArrayDeque;

/**
 * AndroidX Tracing wrapper implementation of the Tracer API.
 *
 * <p>The AndroidX tracer is always enabled. A default instance is created and registered in {@link
 * Tracing}.
 *
 * @see <a
 *     href="https://developer.android.com/reference/androidx/tracing/Trace">androidx.tracing.Trace</a>
 */
class AndroidXTracer implements Tracer {

  @Override
  public Span beginSpan(String name) {
    Trace.beginSection(name);
    return new AndroidXTracerSpan();
  }

  private static class AndroidXTracerSpan implements Span {
    private final ArrayDeque<AndroidXTracerSpan> nestedSpans = new ArrayDeque<>();

    @Override
    public Span beginChildSpan(String name) {
      Trace.beginSection(name);

      AndroidXTracerSpan span = new AndroidXTracerSpan();
      nestedSpans.add(span);
      return span;
    }

    @Override
    public void close() {
      AndroidXTracerSpan span;
      while ((span = nestedSpans.pollLast()) != null) {
        span.close();
      }

      Trace.endSection();
    }
  }
}
