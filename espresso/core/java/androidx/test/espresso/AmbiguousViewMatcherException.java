/*
 * Copyright (C) 2014 The Android Open Source Project
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

import static androidx.test.internal.util.Checks.checkNotNull;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.internal.platform.util.TestOutputEmitter;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.hamcrest.Matcher;

/**
 * An exception which indicates that a {@code Matcher<View>} matched multiple views in the hierarchy
 * when only one view was expected. It should be called only from the main thread.
 *
 * <p>Contains details about the matcher and the current view hierarchy to aid in debugging.
 *
 * <p>Since this is usually an unrecoverable error this exception is a runtime exception.
 *
 * <p>References to the view and failing matcher are purposefully not included in the state of this
 * object - since it will most likely be created on the UI thread and thrown on the instrumentation
 * thread, it would be invalid to touch the view on the instrumentation thread. Also the view
 * hierarchy may have changed since exception creation (leading to more confusion).
 */
public final class AmbiguousViewMatcherException extends RuntimeException
    implements RootViewException {

  private Matcher<? super View> viewMatcher;
  private View rootView;
  private View view1;
  private View view2;
  private View[] others;

  private AmbiguousViewMatcherException(String description) {
    super(description);
    TestOutputEmitter.dumpThreadStates("ThreadState-AmbiguousViewMatcherException.txt");
  }

  private AmbiguousViewMatcherException(Builder builder) {
    this(getErrorMessage(builder));
    this.viewMatcher = builder.viewMatcher;
    this.rootView = builder.rootView;
    this.view1 = builder.view1;
    this.view2 = builder.view2;
    this.others = builder.others;
  }

  private static String getErrorMessage(Builder builder) {
    String errorMessage = "";
    if (builder.includeViewHierarchy) {
      List<View> ambiguousViews = new ArrayList<>();
      ambiguousViews.add(builder.view1);
      ambiguousViews.add(builder.view2);
      Collections.addAll(ambiguousViews, builder.others);

      StringBuilder viewsAsText = new StringBuilder();
      int numViews = ambiguousViews.size();
      for (int i = 0; i < numViews; i++) {
        if (i < 5) {
          viewsAsText.append(
              String.format(
                  Locale.ROOT,
                  "\n- [%d] %s",
                  i + 1,
                  HumanReadables.describe(ambiguousViews.get(i))));
        } else {
          viewsAsText.append(
              String.format(Locale.ROOT, "\n- [truncated, listing 5 out of %d views].", numViews));
          break;
        }
      }

      errorMessage =
          HumanReadables.getViewHierarchyErrorMessage(
              builder.rootView,
              ambiguousViews,
              String.format(
                  Locale.ROOT,
                  "'%s' matches %d views in the hierarchy:%s",
                  builder.viewMatcher,
                  numViews,
                  viewsAsText),
              "****MATCHES****",
              builder.maxMsgLen);

      if (builder.viewHierarchyFile != null) {
        errorMessage +=
            String.format(
                "\nThe complete view hierarchy is available in artifact file '%s'.",
                builder.viewHierarchyFile);
      }
    } else {
      errorMessage =
          String.format(
              Locale.ROOT, "Multiple ambiguous views found for matcher %s", builder.viewMatcher);
    }

    return errorMessage;
  }

  /** Returns the root view where this exception is thrown. */
  @Override
  public View getRootView() {
    return rootView;
  }

  /** Builder for {@link AmbiguousViewMatcherException}. */
  public static class Builder {
    private Matcher<? super View> viewMatcher;
    private View rootView;
    private View view1;
    private View view2;
    private View[] others;
    private boolean includeViewHierarchy = true;
    private int maxMsgLen = Integer.MAX_VALUE;
    private String viewHierarchyFile = null;

    public Builder from(AmbiguousViewMatcherException exception) {
      this.viewMatcher = exception.viewMatcher;
      this.rootView = exception.rootView;
      this.view1 = exception.view1;
      this.view2 = exception.view2;
      this.others = exception.others;
      return this;
    }

    public Builder withViewMatcher(Matcher<? super View> viewMatcher) {
      this.viewMatcher = viewMatcher;
      return this;
    }

    public Builder withRootView(View rootView) {
      this.rootView = rootView;
      return this;
    }

    public Builder withView1(View view1) {
      this.view1 = view1;
      return this;
    }

    public Builder withView2(View view2) {
      this.view2 = view2;
      return this;
    }

    public Builder withOtherAmbiguousViews(View... others) {
      this.others = others;
      return this;
    }

    public Builder includeViewHierarchy(boolean includeViewHierarchy) {
      this.includeViewHierarchy = includeViewHierarchy;
      return this;
    }

    @CanIgnoreReturnValue
    @NonNull
    public Builder withMaxMsgLen(int maxMsgLen) {
      this.maxMsgLen = maxMsgLen;
      return this;
    }

    @CanIgnoreReturnValue
    @NonNull
    public Builder withViewHierarchyFile(@Nullable String viewHierarchyFile) {
      this.viewHierarchyFile = viewHierarchyFile;
      return this;
    }

    public AmbiguousViewMatcherException build() {
      checkNotNull(viewMatcher);
      checkNotNull(rootView);
      checkNotNull(view1);
      checkNotNull(view2);
      checkNotNull(others);
      return new AmbiguousViewMatcherException(this);
    }
  }
}
