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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.tracing.Tracer.Span;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TracingTest {

  private Tracing tracing;

  @Before
  public void setUp() throws Exception {
    tracing = Tracing.getInstance();
  }

  @Test
  public void registerTracer() {
    MyTracer tracer1 = new MyTracer();
    MyTracer tracer2 = new MyTracer();

    try (Span span0 = tracing.beginSpan("span0")) {
      assertThat(tracer1.actions).isEmpty();
      assertThat(tracer2.actions).isEmpty();

      tracing.registerTracer(tracer1);
      tracing.registerTracer(tracer1); // Duplicated tracer reference is ignored.

      try (Span span1 = tracing.beginSpan("span1")) {
        assertThat(tracer1.actions).containsExactly("beginSpan: span1");
        assertThat(tracer2.actions).isEmpty();

        tracing.registerTracer(tracer2);
        try (Span span2 = tracing.beginSpan("span2")) {
          assertThat(tracer1.actions)
              .containsExactly("beginSpan: span1", "beginSpan: span2")
              .inOrder();
          assertThat(tracer2.actions).containsExactly("beginSpan: span2");
        }
      }
    }
  }

  @Test
  public void registrerTracer_nullTracerFails() {
    NullPointerException thrown =
        assertThrows(NullPointerException.class, () -> tracing.registerTracer(null));
    assertThat(thrown).hasMessageThat().contains("Tracer cannot be null");
  }

  @Test
  public void unregisterTracer() {
    MyTracer tracer1 = new MyTracer();
    MyTracer tracer2 = new MyTracer();

    tracing.registerTracer(tracer1);
    tracing.registerTracer(tracer2);
    try (Span span0 = tracing.beginSpan("span0")) {
      assertThat(tracer1.actions).containsExactly("beginSpan: span0");
      assertThat(tracer2.actions).containsExactly("beginSpan: span0");

      tracing.unregisterTracer(tracer1);
      tracing.unregisterTracer(tracer1); // Already unregistered tracer reference is ignored.
      tracing.unregisterTracer(null); // Null reference is ignored.

      try (Span span1 = tracing.beginSpan("span1")) {
        assertThat(tracer1.actions).containsExactly("beginSpan: span0");
        assertThat(tracer2.actions)
            .containsExactly("beginSpan: span0", "beginSpan: span1")
            .inOrder();

        tracing.unregisterTracer(tracer2);
        try (Span span2 = tracing.beginSpan("span2")) {
          assertThat(tracer1.actions).containsExactly("beginSpan: span0");
          assertThat(tracer2.actions)
              .containsExactly("beginSpan: span0", "beginSpan: span1")
              .inOrder();
        }
      }
    }
  }

  @Test
  public void beingChildSpan() {
    MyTracer tracer1 = new MyTracer();
    tracing.registerTracer(tracer1);
    try (Span span0 = tracing.beginSpan("span0")) {
      assertThat(tracer1.actions).containsExactly("beginSpan: span0");

      try (Span span01 = span0.beginChildSpan("span01")) {
        try (Span span011 = span01.beginChildSpan("span011")) {
          /* no-op */
        }
        try (Span span012 = span01.beginChildSpan("span012")) {
          /* no-op */
        }
      }
      try (Span span02 = span0.beginChildSpan("span02")) {
        /* no-op */
      }
    }
    try (Span span1 = tracing.beginSpan("span1")) {
      /* no-op */
    }

    assertThat(tracer1.actions)
        .containsExactly(
            "beginSpan: span0",
            "+ childSpan: span01",
            "| + childSpan: span011",
            "| | +-endSpan: span011",
            "| + childSpan: span012",
            "| | +-endSpan: span012",
            "| +-endSpan: span01",
            "+ childSpan: span02",
            "| +-endSpan: span02",
            "+-endSpan: span0",
            "beginSpan: span1",
            "+-endSpan: span1")
        .inOrder();
  }

  @Test
  public void beingChildSpan_andUnregisterTracer() {
    MyTracer tracer1 = new MyTracer();
    tracing.registerTracer(tracer1);
    try (Span span0 = tracing.beginSpan("span0")) {
      assertThat(tracer1.actions).containsExactly("beginSpan: span0");

      try (Span span01 = span0.beginChildSpan("span01")) {
        try (Span span011 = span01.beginChildSpan("span011")) {
          /* no-op */
        }
        // Unregister tracer here... Any following beginSpan and beginChildSpan calls are not
        // passed to the tracer: span012, span02, and span1 are dropped.
        // However in-flights spans (like span0 and span01) are still closed.
        tracing.unregisterTracer(tracer1);
        try (Span span012 = span01.beginChildSpan("span012")) {
          /* no-op */
        }
      }
      try (Span span02 = span0.beginChildSpan("span02")) {
        /* no-op */
      }
    }
    try (Span span1 = tracing.beginSpan("span1")) {
      /* no-op */
    }

    assertThat(tracer1.actions)
        .containsExactly(
            "beginSpan: span0",
            "+ childSpan: span01",
            "| + childSpan: span011",
            "| | +-endSpan: span011",
            "| +-endSpan: span01",
            "+-endSpan: span0")
        .inOrder();
  }

  static class MyTracer implements Tracer {
    private final List<String> actions = new ArrayList<>();

    @Override
    public Span beginSpan(String name) {
      actions.add("beginSpan: " + name);
      return new MyTracerSpan(actions, name, 0);
    }
  }

  static class MyTracerSpan implements Span {
    private final List<String> actions;
    private final String spanName;
    private final int level;

    public MyTracerSpan(List<String> actions, String spanName, int level) {
      this.actions = actions;
      this.spanName = spanName;
      this.level = level;
    }

    @Override
    public Span beginChildSpan(String name) {
      actions.add(Strings.repeat("| ", level) + "+ childSpan: " + name);
      return new MyTracerSpan(actions, name, level + 1);
    }

    @Override
    public void close() {
      actions.add(Strings.repeat("| ", level) + "+-endSpan: " + spanName);
    }
  }
}
