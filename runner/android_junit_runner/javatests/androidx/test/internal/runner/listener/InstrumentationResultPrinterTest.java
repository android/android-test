/*
 * Copyright (C) 2014 The Android Open Source Project
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

package androidx.test.internal.runner.listener;

import static androidx.test.internal.runner.listener.InstrumentationResultPrinter.REPORT_KEY_STACK;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import android.os.Bundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Computer;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class InstrumentationResultPrinterTest {

  /** Ensure the correct result code along with a stack trace was sent back to instrumentation */
  @Test
  public void testExecutionFlowDuringProcessCrash() throws Exception {
    int[] resultCode = new int[1];
    Bundle[] resultBundle = new Bundle[1];

    InstrumentationResultPrinter intrResultPrinter =
        new InstrumentationResultPrinter() {
          @Override
          public void sendStatus(int code, Bundle bundle) {
            resultCode[0] = code;
            resultBundle[0] = bundle;
          }
        };

    // fake start test execution
    intrResultPrinter.testStarted(Description.EMPTY);
    Assert.assertEquals(InstrumentationResultPrinter.REPORT_VALUE_RESULT_START, resultCode[0]);
    Assert.assertFalse(resultBundle[0].containsKey(REPORT_KEY_STACK));

    // define and report a RunTimeException
    Throwable e = new RuntimeException();
    intrResultPrinter.reportProcessCrash(e);

    // ensure the correct result code along with a stack trace was sent back to instrumentation
    Assert.assertEquals(InstrumentationResultPrinter.REPORT_VALUE_RESULT_FAILURE, resultCode[0]);
    assertTrue(resultBundle[0].containsKey(REPORT_KEY_STACK));
  }

  private static class TestInstrumentationResultPrinter extends InstrumentationResultPrinter {
    final List<String> resultsLog = new ArrayList<>();

    @Override
    public void sendStatus(int code, Bundle bundle) {
      resultsLog.add(
          String.format(
              "code=%s, name=%s#%s",
              code,
              bundle.getString(REPORT_KEY_NAME_CLASS),
              bundle.getString(REPORT_KEY_NAME_TEST)));
    }
  }

  public static class ThrowingTest {
    @BeforeClass
    public static void setUpClass() {
      throw new RuntimeException();
    }

    @AfterClass
    public static void tearDownClass() {
      throw new RuntimeException();
    }

    @Test
    public void emptyTest() {}
  }

  @Test
  public void verifyBeforeClassExceptionsReported() throws Exception {
    JUnitCore core = new JUnitCore();
    var intrResultPrinter = new TestInstrumentationResultPrinter();
    core.addListener(intrResultPrinter);
    Request testRequest = Request.classes(new Computer(), new Class<?>[] {ThrowingTest.class});
    core.run(testRequest);

    String className = ThrowingTest.class.getName();
    assertEquals(
        List.of(
            "code=1, name=" + className + "#null",
            "code=-2, name=" + className + "#null",
            "code=1, name=" + className + "#null",
            "code=-2, name=" + className + "#null"),
        intrResultPrinter.resultsLog);
  }

  public static class AssumptionFailureInClassTest {
    @BeforeClass
    public static void setUpClass() {
      assumeTrue(false);
    }

    @Test
    public void emptyTest() {}
  }

  @Test
  public void verifyBeforeClassAssumptionsReported() throws Exception {
    JUnitCore core = new JUnitCore();
    var intrResultPrinter = new TestInstrumentationResultPrinter();
    core.addListener(intrResultPrinter);
    Request testRequest = Request.classes(new Computer(), AssumptionFailureInClassTest.class);
    core.run(testRequest);

    String className = AssumptionFailureInClassTest.class.getName();
    assertThat(intrResultPrinter.resultsLog)
        .containsExactly(
            "code=1, name=" + className + "#null", "code=-4, name=" + className + "#null")
        .inOrder();
  }

  public static final class IgnoredMethodTest {
    @Ignore("Flaky test")
    @Test
    public void ignoredTest() {}
  }

  @Ignore("Class ignore reason")
  public static final class IgnoredClassTest {
    @Test
    public void ignoredTest() {}
  }

  public static final class IgnoredNoReasonMethodTest {
    @Ignore
    @Test
    public void ignoredTest() {}
  }

  @Ignore
  public static final class IgnoredNoReasonClassTest {
    @Test
    public void ignoredTest() {}
  }

  @Ignore("Class ignore reason")
  public static final class IgnoredMethodInIgnoredClassTest {
    @Ignore("Method ignore reason")
    @Test
    public void ignoredWithMethodReason() {}

    @Ignore
    @Test
    public void ignoredWithoutMethodReason() {}
  }

  @Test
  public void testIgnored_populatesStackWithIgnoreReason() throws Exception {
    List<String> stacks = new ArrayList<>();
    InstrumentationResultPrinter intrResultPrinter =
        new InstrumentationResultPrinter() {
          @Override
          public void sendStatus(int code, Bundle bundle) {
            if (code == REPORT_VALUE_RESULT_IGNORED) {
              stacks.add(bundle.getString(REPORT_KEY_STACK));
            }
          }
        };
    JUnitCore core = new JUnitCore();
    core.addListener(intrResultPrinter);

    core.run(Request.classes(new Computer(), IgnoredMethodTest.class));

    assertWithMessage("ignored test stacks").that(stacks).containsExactly("Flaky test");
  }

  @Test
  public void testIgnored_classLevelIgnore_populatesStackWithIgnoreReason() throws Exception {
    List<String> stacks = new ArrayList<>();
    InstrumentationResultPrinter intrResultPrinter =
        new InstrumentationResultPrinter() {
          @Override
          public void sendStatus(int code, Bundle bundle) {
            if (code == REPORT_VALUE_RESULT_IGNORED) {
              stacks.add(bundle.getString(REPORT_KEY_STACK));
            }
          }
        };
    JUnitCore core = new JUnitCore();
    core.addListener(intrResultPrinter);

    core.run(Request.classes(new Computer(), IgnoredClassTest.class));

    assertWithMessage("ignored test stacks").that(stacks).containsExactly("Class ignore reason");
  }

  @Test
  public void testIgnored_withoutReason_doesNotPopulateStack() throws Exception {
    List<Bundle> ignoredBundles = new ArrayList<>();
    InstrumentationResultPrinter intrResultPrinter =
        new InstrumentationResultPrinter() {
          @Override
          public void sendStatus(int code, Bundle bundle) {
            if (code == REPORT_VALUE_RESULT_IGNORED) {
              ignoredBundles.add(new Bundle(bundle));
            }
          }
        };
    JUnitCore core = new JUnitCore();
    core.addListener(intrResultPrinter);

    core.run(Request.classes(new Computer(), IgnoredNoReasonMethodTest.class));

    assertWithMessage("ignored test bundles").that(ignoredBundles).hasSize(1);
    assertWithMessage("bundle keys for ignored method without reason")
        .that(ignoredBundles.get(0).keySet())
        .doesNotContain(REPORT_KEY_STACK);
  }

  @Test
  public void testIgnored_classLevelIgnoreWithoutReason_doesNotPopulateStack() throws Exception {
    List<Bundle> ignoredBundles = new ArrayList<>();
    InstrumentationResultPrinter intrResultPrinter =
        new InstrumentationResultPrinter() {
          @Override
          public void sendStatus(int code, Bundle bundle) {
            if (code == REPORT_VALUE_RESULT_IGNORED) {
              ignoredBundles.add(new Bundle(bundle));
            }
          }
        };
    JUnitCore core = new JUnitCore();
    core.addListener(intrResultPrinter);

    core.run(Request.classes(new Computer(), IgnoredNoReasonClassTest.class));

    assertWithMessage("ignored test bundles").that(ignoredBundles).hasSize(1);
    assertWithMessage("bundle keys for ignored class without reason")
        .that(ignoredBundles.get(0).keySet())
        .doesNotContain(REPORT_KEY_STACK);
  }

  @Test
  public void testIgnored_methodAndClassIgnore_prefersMethodReasonAndFallsBackToClassReason()
      throws Exception {
    List<String> stacks = new ArrayList<>();
    InstrumentationResultPrinter intrResultPrinter =
        new InstrumentationResultPrinter() {
          @Override
          public void sendStatus(int code, Bundle bundle) {
            if (code == REPORT_VALUE_RESULT_IGNORED) {
              stacks.add(bundle.getString(REPORT_KEY_STACK));
            }
          }
        };
    Description methodWithReason =
        Description.createTestDescription(
            IgnoredMethodInIgnoredClassTest.class,
            "ignoredWithMethodReason",
            IgnoredMethodInIgnoredClassTest.class
                .getMethod("ignoredWithMethodReason")
                .getAnnotations());
    Description methodWithoutReason =
        Description.createTestDescription(
            IgnoredMethodInIgnoredClassTest.class,
            "ignoredWithoutMethodReason",
            IgnoredMethodInIgnoredClassTest.class
                .getMethod("ignoredWithoutMethodReason")
                .getAnnotations());

    intrResultPrinter.testIgnored(methodWithReason);
    intrResultPrinter.testIgnored(methodWithoutReason);

    assertWithMessage("ignored test stacks")
        .that(stacks)
        .containsExactly("Method ignore reason", "Class ignore reason")
        .inOrder();
  }
}
