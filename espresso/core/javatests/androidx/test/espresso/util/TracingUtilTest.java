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

package androidx.test.espresso.util;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link TracingUtil}. */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class TracingUtilTest {
  @Test
  public void getSpanName_nullArguments() {
    // This utility method should be defensive and silently accept null arguments.
    assertThat(TracingUtil.getSpanName(null, null)).isEmpty();
    assertThat(TracingUtil.getSpanName(null, null, "")).isEmpty();
    assertThat(TracingUtil.getSpanName("prefix", null)).isEqualTo("prefix");
    assertThat(TracingUtil.getSpanName(null, "method")).isEqualTo("method");
    assertThat(TracingUtil.getSpanName(null, null, "argument")).isEqualTo("(argument)");
  }

  @Test
  public void getSpanName_separators() {
    // The seperators should correctly connect the parts
    assertThat(TracingUtil.getSpanName("prefix", "method")).isEqualTo("prefix.method");
    assertThat(TracingUtil.getSpanName(null, "method", "argument")).isEqualTo("method(argument)");
    assertThat(TracingUtil.getSpanName(null, null, "arg1", "arg2")).isEqualTo("(arg1, arg2)");
  }

  @Test
  public void getSpanName_lengthCheck() {
    // Long span name will be shorten.
    assertThat(
            TracingUtil.getSpanName(
                null,
                "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789_DELETE"))
        .isEqualTo(
            "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");
  }

  @Test
  public void getSpanName_excludeCheck() {
    // Different Sanitization on different parts of the span name.
    assertThat(TracingUtil.getSpanName("0aA._$()[]<>{} /:-\n", null)).isEqualTo("0aA._$()[] /:-");
    assertThat(TracingUtil.getSpanName(null, "0aA._$()[]<>{} /:-\n")).isEqualTo("0aA._$()[] /:-");
    assertThat(TracingUtil.getSpanName(null, null, "0aA._$()[]<>{} /:-\n"))
        .isEqualTo("(0aA._$()[] /:-)");
  }
}
