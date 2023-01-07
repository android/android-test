/*
 * Copyright (C) 2022 The Android Open Source Project
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

import androidx.annotation.NonNull;
import androidx.test.platform.tracing.Tracer;
import java.util.ArrayList;
import java.util.List;
import kotlin.text.StringsKt;

/**
 * A test utility {@link Tracer} implementation used during tests to capture trace spans.
 *
 * <p>Usage:
 *
 * <pre>
 *   TestTracer tracer = new TestTracer();
 *   Tracing.getInstance().registerTracer(tracer);
 *   ... perform test
 *   assertThat(tracer.getActions()).containsExactly("span1", "span2").inOrder();
 *   Tracing.getInstance().unregisterTracer(tracer);
 * </pre>
 */
public class TestTracer implements Tracer {
  private final List<String> spans = new ArrayList<>();

  public List<String> getSpans() {
    return spans;
  }

  /**
   * Test implementation should override this method to adjust the span names to remove any test
   * variance. See example of usage in ViewInteractionTest.
   */
  @NonNull
  public String rewriteSpanName(@NonNull String spanName) {
    return spanName;
  }

  @NonNull
  @Override
  public Span beginSpan(@NonNull String name) {
    name = rewriteSpanName(name);
    spans.add("beginSpan: " + name);
    return new TestUtilTracerSpan(name, 0);
  }

  class TestUtilTracerSpan implements Span {
    private final String spanName;
    private final int level;

    public TestUtilTracerSpan(@NonNull String spanName, int level) {
      this.spanName = spanName;
      this.level = level;
    }

    @NonNull
    @Override
    public Span beginChildSpan(@NonNull String name) {
      name = rewriteSpanName(name);
      spans.add(StringsKt.repeat("| ", level) + "+ childSpan: " + name);
      return new TestUtilTracerSpan(name, level + 1);
    }

    @Override
    public void close() {
      spans.add(StringsKt.repeat("| ", level) + "+-endSpan: " + spanName);
    }
  }
}
