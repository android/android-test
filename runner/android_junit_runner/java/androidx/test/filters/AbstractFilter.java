/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.test.filters;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

/** Helper parent class for {@link Filter} that allows suites to run if any child matches. */
public abstract class AbstractFilter extends Filter {
  /** {@inheritDoc} */
  @Override
  public boolean shouldRun(Description description) {
    if (description.isTest()) {
      return evaluateTest(description);
    }
    // this is a suite, explicitly check if any children should run
    for (Description each : description.getChildren()) {
      if (shouldRun(each)) {
        return true;
      }
    }
    // no children to run, filter this out
    return false;
  }

  /**
   * Determine if given test description matches filter.
   *
   * @param description the {@link Description} describing the test
   * @return <code>true</code> if matched
   */
  protected abstract boolean evaluateTest(Description description);

  /**
   * Get a list of method annotations that are annotated with @CustomFilter with this class as the
   * filter class.
   *
   * @param description the {@link Description} describing the test
   * @return a list of annotations on methods that are handled by this filter
   */
  protected List<Annotation> getMethodAnnotations(Description description) {
    ArrayList<Annotation> testAnnotations = new ArrayList<>();
    for (Annotation annotation : description.getAnnotations()) {
      CustomFilter customFilterAnnotation =
          annotation.annotationType().getAnnotation(CustomFilter.class);
      if (customFilterAnnotation != null && customFilterAnnotation.filterClass().isInstance(this)) {
        testAnnotations.add(annotation);
      }
    }
    return testAnnotations;
  }

  /**
   * Get a list of class annotations that are annotated with @CustomFilter with this class as the
   * filter class.
   *
   * @param description the {@link Description} describing the test
   * @return a list of annotations on the test class that are handled by this filter
   */
  protected List<Annotation> getClassAnnotations(Description description) {
    ArrayList<Annotation> testAnnotations = new ArrayList<>();
    for (Annotation c : description.getTestClass().getAnnotations()) {
      CustomFilter customFilterAnnotation = c.annotationType().getAnnotation(CustomFilter.class);
      if (customFilterAnnotation != null && customFilterAnnotation.filterClass().isInstance(this)) {
        testAnnotations.add(c);
      }
    }
    return testAnnotations;
  }
}
