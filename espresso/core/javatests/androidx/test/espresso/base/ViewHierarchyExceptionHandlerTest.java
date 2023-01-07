/*
 * Copyright (C) 2021 The Android Open Source Project
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
package androidx.test.espresso.base;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.test.espresso.AmbiguousViewMatcherException;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.io.PlatformTestStorage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import kotlin.Pair;
import kotlin.collections.MapsKt;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(AndroidJUnit4.class)
public class ViewHierarchyExceptionHandlerTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  private final AtomicInteger failureCount = new AtomicInteger();
  private ViewHierarchyExceptionHandler<NoMatchingViewException> noMatchingViewExceptionHandler;
  private ViewHierarchyExceptionHandler<AmbiguousViewMatcherException>
      ambiguousViewMatcherExceptionHandler;
  private Matcher<View> alwaysFalseMatcher;
  @Mock private PlatformTestStorage testStorage;

  @Before
  public void setUp() throws IOException {
    when(testStorage.openOutputFile("view-hierarchy-1.txt"))
        .thenReturn(new ByteArrayOutputStream());
    noMatchingViewExceptionHandler =
        new ViewHierarchyExceptionHandler<>(
            testStorage,
            failureCount,
            NoMatchingViewException.class,
            DefaultFailureHandler.getNoMatchingViewExceptionTruncater());
    ambiguousViewMatcherExceptionHandler =
        new ViewHierarchyExceptionHandler<>(
            testStorage,
            failureCount,
            AmbiguousViewMatcherException.class,
            DefaultFailureHandler.getAmbiguousViewMatcherExceptionTruncater());
    alwaysFalseMatcher =
        new BaseMatcher<View>() {
          @Override
          public boolean matches(Object o) {
            return false;
          }

          @Override
          public void describeTo(Description description) {
            description.appendText("A view matcher");
          }
        };
  }

  @Test
  public void handle_noMatchingViewException() throws IOException {
    NoMatchingViewException exceptionUnderTest =
        new NoMatchingViewException.Builder()
            .withViewMatcher(alwaysFalseMatcher)
            .withRootView(new TextView(getInstrumentation().getTargetContext()))
            .build();
    String expectedMsg =
        "\n\nView Hierarchy:\n"
            + "+>TextView{id=-1, visibility=VISIBLE, width=0, height=0, has-focus=false,"
            + " has-focusable=false, has-window-focus=false, is-clickable=false, is-enabled=true,"
            + " is-focused=false, is-focusable=false, is-layout-requested=true, is-selected=false,"
            + " layout-params=null, tag=null, root-is-layout-requested=true,"
            + " has-input-connection=false, x=0.0, y=0.0, text=, input-type=0, ime-target=false,"
            + " has-links=false} ";

    failureCount.incrementAndGet();
    NoMatchingViewException thrown =
        assertThrows(
            NoMatchingViewException.class,
            () -> noMatchingViewExceptionHandler.handle(exceptionUnderTest, alwaysFalseMatcher));

    assertThat(thrown)
        .hasMessageThat()
        .contains("No views in hierarchy found matching: A view matcher" + expectedMsg);
    verify(testStorage).openOutputFile(eq("view-hierarchy-1.txt"));
  }

  @Test
  public void handle_nonNoMatchingViewException() {
    // No-op. No exception should be thrown.
    noMatchingViewExceptionHandler.handle(new Throwable("A random error"), alwaysFalseMatcher);
  }

  @Test
  public void handle_ambiguousViewMatcherException() throws IOException {
    ViewGroup layoutUnderTest = new RelativeLayout(getInstrumentation().getContext());
    View child1 = new TextView(getInstrumentation().getContext());
    child1.setId(1);
    View child2 = new TextView(getInstrumentation().getContext());
    child2.setId(2);
    layoutUnderTest.addView(child1);
    layoutUnderTest.addView(child2);

    AmbiguousViewMatcherException exceptionUnderTest =
        new AmbiguousViewMatcherException.Builder()
            .withViewMatcher(alwaysFalseMatcher)
            .withRootView(layoutUnderTest)
            .withView1(layoutUnderTest)
            .withView2(child1)
            .withOtherAmbiguousViews(child2)
            .build();

    String expectedMsg =
        "'A view matcher' matches 3 views in the hierarchy:\n"
            + "- [1] RelativeLayout{id=-1, visibility=VISIBLE, width=0, height=0, has-focus=false,"
            + " has-focusable=false, has-window-focus=false, is-clickable=false, is-enabled=true,"
            + " is-focused=false, is-focusable=false, is-layout-requested=true, is-selected=false,"
            + " layout-params=null, tag=null, root-is-layout-requested=true,"
            + " has-input-connection=false, x=0.0, y=0.0, child-count=2}\n"
            + "- [2] TextView{id=1, visibility=VISIBLE, width=0, height=0, has-focus=false,"
            + " has-focusable=false, has-window-focus=false, is-clickable=false, is-enabled=true,"
            + " is-focused=false, is-focusable=false, is-layout-requested=true, is-selected=false,"
            + " layout-params=android.widget.RelativeLayout$LayoutParams@YYYYYY, tag=null,"
            + " root-is-layout-requested=true, has-input-connection=false, x=0.0, y=0.0, text=,"
            + " input-type=0, ime-target=false, has-links=false}\n"
            + "- [3] TextView{id=2, visibility=VISIBLE, width=0, height=0, has-focus=false,"
            + " has-focusable=false, has-window-focus=false, is-clickable=false, is-enabled=true,"
            + " is-focused=false, is-focusable=false, is-layout-requested=true, is-selected=false,"
            + " layout-params=android.widget.RelativeLayout$LayoutParams@YYYYYY, tag=null,"
            + " root-is-layout-requested=true, has-input-connection=false, x=0.0, y=0.0, text=,"
            + " input-type=0, ime-target=false, has-links=false}\n"
            + "Problem views are marked with '****MATCHES****' below.\n\n"
            + "View Hierarchy:\n"
            + "+>RelativeLayout{id=-1, visibility=VISIBLE, width=0, height=0, has-focus=false,"
            + " has-focusable=false, has-window-focus=false, is-clickable=false, is-enabled=true,"
            + " is-focused=false, is-focusable=false, is-layout-requested=true, is-selected=false,"
            + " layout-params=null, tag=null, root-is-layout-requested=true,"
            + " has-input-connection=false, x=0.0, y=0.0, child-count=2} ****MATCHES****\n"
            + "|\n"
            + "+->TextView{id=1, visibility=VISIBLE, width=0, height=0, has-focus=false,"
            + " has-focusable=false, has-window-focus=false, is-clickable=false, is-enabled=true,"
            + " is-focused=false, is-focusable=false, is-layout-requested=true, is-selected=false,"
            + " layout-params=android.widget.RelativeLayout$LayoutParams@YYYYYY, tag=null,"
            + " root-is-layout-requested=true, has-input-connection=false, x=0.0, y=0.0, text=,"
            + " input-type=0, ime-target=false, has-links=false} ****MATCHES****\n"
            + "|\n"
            + "+->TextView{id=2, visibility=VISIBLE, width=0, height=0, has-focus=false,"
            + " has-focusable=false, has-window-focus=false, is-clickable=false, is-enabled=true,"
            + " is-focused=false, is-focusable=false, is-layout-requested=true, is-selected=false,"
            + " layout-params=android.widget.RelativeLayout$LayoutParams@YYYYYY, tag=null,"
            + " root-is-layout-requested=true, has-input-connection=false, x=0.0, y=0.0, text=,"
            + " input-type=0, ime-target=false, has-links=false} ****MATCHES****";

    failureCount.incrementAndGet();
    AmbiguousViewMatcherException thrown =
        assertThrows(
            AmbiguousViewMatcherException.class,
            () ->
                ambiguousViewMatcherExceptionHandler.handle(
                    exceptionUnderTest, alwaysFalseMatcher));

    assertThat(thrown).hasMessageThat().contains(expectedMsg);

    verify(testStorage).openOutputFile(eq("view-hierarchy-1.txt"));
  }

  @Test
  public void handle_ambiguousViewMatcherException_withTruncatedMessage() throws IOException {
    ViewGroup layoutUnderTest = new RelativeLayout(getInstrumentation().getContext());
    View child1 = new TextView(getInstrumentation().getContext());
    child1.setId(1);
    View child2 = new TextView(getInstrumentation().getContext());
    child2.setId(2);
    layoutUnderTest.addView(child1);
    layoutUnderTest.addView(child2);

    AmbiguousViewMatcherException exceptionUnderTest =
        new AmbiguousViewMatcherException.Builder()
            .withViewMatcher(alwaysFalseMatcher)
            .withRootView(layoutUnderTest)
            .withView1(layoutUnderTest)
            .withView2(child1)
            .withOtherAmbiguousViews(child2)
            .build();

    Map<String, String> inputArgs = MapsKt.mapOf(new Pair<>("view_hierarchy_char_limit", "1772"));
    when(testStorage.getInputArgs()).thenReturn(inputArgs);
    doAnswer(invocation -> inputArgs.get(invocation.getArgument(0)))
        .when(testStorage)
        .getInputArg(anyString());

    String expectedMsg =
        "'A view matcher' matches 3 views in the hierarchy:\n"
            + "- [1] RelativeLayout{id=-1, visibility=VISIBLE, width=0, height=0, has-focus=false,"
            + " has-focusable=false, has-window-focus=false, is-clickable=false, is-enabled=true,"
            + " is-focused=false, is-focusable=false, is-layout-requested=true, is-selected=false,"
            + " layout-params=null, tag=null, root-is-layout-requested=true,"
            + " has-input-connection=false, x=0.0, y=0.0, child-count=2}\n"
            + "- [2] TextView{id=1, visibility=VISIBLE, width=0, height=0, has-focus=false,"
            + " has-focusable=false, has-window-focus=false, is-clickable=false, is-enabled=true,"
            + " is-focused=false, is-focusable=false, is-layout-requested=true, is-selected=false,"
            + " layout-params=android.widget.RelativeLayout$LayoutParams@YYYYYY, tag=null,"
            + " root-is-layout-requested=true, has-input-connection=false, x=0.0, y=0.0, text=,"
            + " input-type=0, ime-target=false, has-links=false}\n"
            + "- [3] TextView{id=2, visibility=VISIBLE, width=0, height=0, has-focus=false,"
            + " has-focusable=false, has-window-focus=false, is-clickable=false, is-enabled=true,"
            + " is-focused=false, is-focusable=false, is-layout-requested=true, is-selected=false,"
            + " layout-params=android.widget.RelativeLayout$LayoutParams@YYYYYY, tag=null,"
            + " root-is-layout-requested=true, has-input-connection=false, x=0.0, y=0.0, text=,"
            + " input-type=0, ime-target=false, has-links=false}\n"
            + "Problem views are marked with '****MATCHES****' below.\n\n"
            + "View Hierarchy:\n"
            + "+>RelativeLayout{id=-1, visibility=VISIBLE, width=0, height=0, has-focus=false,"
            + " has-focusable=false, has-window-focus=false, is-clickable=false, is-enabled=true,"
            + " is-focused=false, is-focusable=false, is-layout-requested=true, is-selected=false,"
            + " layout-params=null, tag=null, root-is-layout-requested=true,"
            + " has-input-connection=false, x=0.0, y=0.0, child-count=2} ****MATCHES****"
            + " [truncated]";

    failureCount.incrementAndGet();
    AmbiguousViewMatcherException thrown =
        assertThrows(
            AmbiguousViewMatcherException.class,
            () ->
                ambiguousViewMatcherExceptionHandler.handle(
                    exceptionUnderTest, alwaysFalseMatcher));

    assertThat(thrown).hasMessageThat().contains(expectedMsg);

    assertThat(thrown)
        .hasMessageThat()
        .containsMatch(
            "The complete view hierarchy is available in artifact file 'view-hierarchy-1.txt'.");

    verify(testStorage).openOutputFile(eq("view-hierarchy-1.txt"));
  }

  @Test
  public void handle_nonAmbiguousViewMatcherException() {
    // No-op. No exception should be thrown.
    ambiguousViewMatcherExceptionHandler.handle(
        new Throwable("A random error"), alwaysFalseMatcher);
  }
}
