/*
 * Copyright (C) 2019 The Android Open Source Project
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

package androidx.test.orchestrator.callback;

import static androidx.test.orchestrator.callback.BundleConverter.getBundleFromTestRunEvent;
import static androidx.test.orchestrator.listeners.OrchestrationListenerManager.KEY_TEST_EVENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import android.os.Bundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.orchestrator.junit.ParcelableDescription;
import androidx.test.orchestrator.junit.ParcelableFailure;
import androidx.test.orchestrator.junit.ParcelableResult;
import androidx.test.services.events.Annotation;
import androidx.test.services.events.AnnotationValue;
import androidx.test.services.events.Failure;
import androidx.test.services.events.TestCase;
import androidx.test.services.events.client.TestEventClientException;
import androidx.test.services.events.run.TestAssumptionFailureEvent;
import androidx.test.services.events.run.TestFailureEvent;
import androidx.test.services.events.run.TestFinishedEvent;
import androidx.test.services.events.run.TestIgnoredEvent;
import androidx.test.services.events.run.TestRunFinishedEvent;
import androidx.test.services.events.run.TestRunStartedEvent;
import androidx.test.services.events.run.TestStartedEvent;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link BundleConverter}. */
@RunWith(AndroidJUnit4.class)
public class BundleConverterTest {

  private static final String KEY_DESCRIPTION = "description";
  private static final String KEY_FAILURE = "failure";
  private static final String KEY_RESULT = "result";
  private TestCase testCase;
  private Failure failure;

  @Before
  public void before() {
    List<Annotation> classAnnotations =
        getAnnotationWithValue("RunWith", "runner", "AndroidJUnit", "class");
    List<Annotation> methodAnnotations =
        getAnnotationWithValue("TestSize", "size", "SMALL", "enum");
    testCase = new TestCase("foo.SampleTest", "test_method", methodAnnotations, classAnnotations);
    failure = new Failure("Something broke", "IllegalStateException", "at line 10", testCase);
  }

  @Test
  public void testAssumptionFailureEvent() throws TestEventClientException {
    TestAssumptionFailureEvent event = new TestAssumptionFailureEvent(testCase, failure);

    Bundle bundle = getBundleFromTestRunEvent(event);
    assertThat(bundle.getString(KEY_TEST_EVENT), is("TEST_ASSUMPTION_FAILURE"));

    ParcelableFailure result = bundle.getParcelable(KEY_FAILURE);
    assertThat(result.getTrace(), is("at line 10\n"));
    assertThat(result.getDescription(), is(test("foo.SampleTest", "test_method")));
  }

  @Test
  public void testFailureEvent() throws TestEventClientException {
    TestFailureEvent event = new TestFailureEvent(testCase, failure);

    Bundle bundle = getBundleFromTestRunEvent(event);
    assertThat(bundle.getString(KEY_TEST_EVENT), is("TEST_FAILURE"));

    ParcelableFailure result = bundle.getParcelable(KEY_FAILURE);
    assertThat(result.getTrace(), is("at line 10\n"));
    assertThat(result.getDescription(), is(test("foo.SampleTest", "test_method")));
  }

  @Test
  public void testFinishedEvent() throws TestEventClientException {
    TestFinishedEvent event = new TestFinishedEvent(testCase);

    Bundle bundle = getBundleFromTestRunEvent(event);
    assertThat(bundle.getString(KEY_TEST_EVENT), is("TEST_FINISHED"));

    ParcelableDescription result = bundle.getParcelable(KEY_DESCRIPTION);
    assertThat(result, is(test("foo.SampleTest", "test_method")));
  }

  @Test
  public void testIgnoredEvent() throws TestEventClientException {
    TestIgnoredEvent event = new TestIgnoredEvent(testCase);

    Bundle bundle = getBundleFromTestRunEvent(event);
    assertThat(bundle.getString(KEY_TEST_EVENT), is("TEST_IGNORED"));

    ParcelableDescription result = bundle.getParcelable(KEY_DESCRIPTION);
    assertThat(result, is(test("foo.SampleTest", "test_method")));
  }

  @Test
  public void testRunFinishedEvent() throws TestEventClientException {
    List<Failure> failures = new ArrayList<>();
    failures.add(failure);
    TestRunFinishedEvent event = new TestRunFinishedEvent(1, 2, 3, failures);

    Bundle bundle = getBundleFromTestRunEvent(event);
    assertThat(bundle.getString(KEY_TEST_EVENT), is("TEST_RUN_FINISHED"));

    ParcelableResult result = bundle.getParcelable(KEY_RESULT);
    assertThat(result.getFailureCount(), is(1));
    assertThat(result.getFailures().get(0).getTrace(), is("at line 10\n"));
  }

  @Test
  public void testRunStartedEvent() throws TestEventClientException {
    TestRunStartedEvent event = new TestRunStartedEvent(testCase);

    Bundle bundle = getBundleFromTestRunEvent(event);
    assertThat(bundle.getString(KEY_TEST_EVENT), is("TEST_RUN_STARTED"));

    ParcelableDescription result = bundle.getParcelable(KEY_DESCRIPTION);
    assertThat(result, is(test("foo.SampleTest", "test_method")));
  }

  @Test
  public void testStartedEvent() throws TestEventClientException {
    TestStartedEvent event = new TestStartedEvent(testCase);

    Bundle bundle = getBundleFromTestRunEvent(event);
    assertThat(bundle.getString(KEY_TEST_EVENT), is("TEST_STARTED"));

    ParcelableDescription result = bundle.getParcelable(KEY_DESCRIPTION);
    assertThat(result, is(test("foo.SampleTest", "test_method")));
  }

  private static List<Annotation> getAnnotationWithValue(
      String annotationName, String fieldName, String fieldValue, String fieldType) {
    List<String> value = new ArrayList<>();
    value.add(fieldValue);
    List<AnnotationValue> annotationValues = new ArrayList<>();
    annotationValues.add(new AnnotationValue(fieldName, value, fieldType));
    List<Annotation> annotations = new ArrayList<>();
    annotations.add(new Annotation(annotationName, annotationValues));
    return annotations;
  }

  private static Matcher<ParcelableDescription> test(String className, String methodName) {
    return new ParcelableDescriptionMatcher(className, methodName);
  }

  private static class ParcelableDescriptionMatcher extends BaseMatcher<ParcelableDescription> {

    private final String className;
    private final String methodName;

    private ParcelableDescriptionMatcher(String className, String methodName) {
      this.className = className;
      this.methodName = methodName;
    }

    @Override
    public boolean matches(Object item) {
      ParcelableDescription other = (ParcelableDescription) item;
      return className.equals(other.getClassName()) && methodName.equals(other.getMethodName());
    }

    @Override
    public void describeTo(Description description) {
      description.appendValue(className + "#" + methodName);
    }
  }
}
