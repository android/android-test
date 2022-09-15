/*
 * Copyright (C) 2018 The Android Open Source Project
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
package androidx.test.ext.junit.runners;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.view.View;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.platform.io.PlatformTestStorage;
import androidx.test.services.storage.TestStorage;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultDescriptor;
import com.google.android.apps.common.testing.accessibility.framework.integrations.espresso.AccessibilityValidator;
import com.google.android.apps.common.testing.accessibility.framework.integrations.internal.rules.CollectingListener;
import com.google.android.apps.common.testing.accessibility.framework.integrations.internal.rules.UncheckedIOException;
import com.google.android.apps.common.testing.accessibility.framework.integrations.reporting.ProtoWriter;
import com.google.android.apps.common.testing.accessibility.framework.integrations.reporting.StepResult;
import com.google.android.apps.common.testing.accessibility.framework.integrations.reporting.TestCaseResult;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

/**
 * A JUnit4 runner for Android tests.
 *
 * <p>This runner offers several features on top of the standard JUnit4 runner,
 *
 * <ul>
 *   <li>Supports running on Robolectric. This implementation will delegate to RobolectricTestRunner
 *       if test is running in Robolectric enviroment. A custom runner can be provided by specifying
 *       the full class name in a 'android.junit.runner' system property.
 *   <li>Supports a per-test timeout - specfied via a 'timeout_msec' {@link
 *       androidx.test.runner.AndroidJUnitRunner} argument.
 *   <li>Supports running tests on the application's UI Thread, for tests annotated with {@link
 *       androidx.test.annotation.UiThreadTest}.
 * </ul>
 *
 * <p>Usage {@code @RunWith(AndroidJUnit4.class)}
 */
public final class AndroidJUnit4 extends Runner implements Filterable, Sortable {
  private static final String TAG = "AndroidJUnit4";
  private static final String TEST_SERVICES_PACKAGE_NAME =
      "com.google.android.apps.common.testing.services";

  private final Runner delegate;
  private final ProtoWriter protoWriter = new ProtoWriter();
  private boolean checksWereRunFromViewAssertion = false;
  private boolean assertionAdded = false;
  private final CollectingListener collectingListener =
      new CollectingListener()
          .setResultDescriptor(
              new AccessibilityCheckResultDescriptor() {
                @Override
                public String describeView(View view) {
                  return HumanReadables.describe(view);
                }
              });
  private final Logger logger = Logger.getLogger(AndroidJUnit4.class.getSimpleName());
  private final AccessibilityValidator accessibilityValidator =
      new AccessibilityValidator()
          .setResultDescriptor(
              new AccessibilityCheckResultDescriptor() {
                @Override
                public String describeView(View view) {
                  return HumanReadables.describe(view);
                }
              })
          .setRunChecksFromRootView(true)
          .setSaveImages(false)
          .setThrowExceptionFor(null)
          .setCaptureScreenshots(false)
          .addCheckListener(collectingListener);
  private PlatformTestStorage testStorage = null;
  private final ViewAssertion accessibilityCheckAssertion =
      new ViewAssertion() {
        @Override
        public void check(View view, NoMatchingViewException noViewFoundException) {
          if (noViewFoundException != null) {
            logger.log(
                Level.SEVERE,
                String.format(
                    "'accessibility checks could not be performed because view '%s' was not"
                        + "found.\n",
                    noViewFoundException.getViewMatcherDescription()));
            throw noViewFoundException;
          }

          if ((view == null) || (accessibilityValidator == null)) {
            return;
          }

          StrictMode.ThreadPolicy originalPolicy = StrictMode.allowThreadDiskWrites();
          try {
            accessibilityValidator.check(view);
            checksWereRunFromViewAssertion = true;

            if (testStorage == null) {
              try {
                testStorage =
                    new TestStorage(view.getContext().getApplicationContext().getContentResolver());
              } catch (Throwable e) {
                testStorage = null;
              }
            }

          } finally {
            StrictMode.setThreadPolicy(originalPolicy);
          }
        }
      };

  public AndroidJUnit4(Class<?> klass) throws InitializationError {
    delegate = loadRunner(klass);
  }

  private static String getRunnerClassName() {
    String runnerClassName = System.getProperty("android.junit.runner", null);
    if (runnerClassName == null) {
      if (!System.getProperty("java.runtime.name").toLowerCase().contains("android")
          && hasClass("org.robolectric.RobolectricTestRunner")) {
        return "org.robolectric.RobolectricTestRunner";
      } else {
        return "androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner";
      }
    }
    return runnerClassName;
  }

  private static boolean hasClass(String className) {
    try {
      return Class.forName(className) != null;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  private static Runner loadRunner(Class<?> testClass) throws InitializationError {
    String runnerClassName = getRunnerClassName();
    return loadRunner(testClass, runnerClassName);
  }

  @SuppressWarnings("unchecked")
  private static Runner loadRunner(Class<?> testClass, String runnerClassName)
      throws InitializationError {
    Class<? extends Runner> runnerClass = null;
    try {
      runnerClass = (Class<? extends Runner>) Class.forName(runnerClassName);
    } catch (ClassNotFoundException e) {
      throwInitializationError(
          String.format(
              "Delegate runner %s for AndroidJUnit4 could not be found.\n", runnerClassName),
          e);
    }
    Constructor<? extends Runner> constructor = null;
    try {
      constructor = runnerClass.getConstructor(Class.class);
    } catch (NoSuchMethodException e) {
      throwInitializationError(
          String.format(
              "Delegate runner %s for AndroidJUnit4 requires a public constructor that takes a"
                  + " Class<?>.\n",
              runnerClassName),
          e);
    }
    try {
      return constructor.newInstance(testClass);
    } catch (IllegalAccessException e) {
      throwInitializationError(
          String.format("Illegal constructor access for test runner %s\n", runnerClassName), e);
    } catch (InstantiationException e) {
      throwInitializationError(
          String.format("Failed to instantiate test runner %s\n", runnerClassName), e);
    } catch (InvocationTargetException e) {
      String details = getInitializationErrorDetails(e, testClass);
      throwInitializationError(
          String.format("Failed to instantiate test runner %s\n%s\n", runnerClass, details), e);
    }
    throw new IllegalStateException("Should never reach here");
  }

  private static void throwInitializationError(String details, Throwable cause)
      throws InitializationError {
    throw new InitializationError(new RuntimeException(details, cause));
  }

  private static String getInitializationErrorDetails(Throwable throwable, Class<?> testClass) {
    StringBuilder innerCause = new StringBuilder();
    final Throwable cause = throwable.getCause();
    if (cause == null) {
      return "";
    }
    final Class<? extends Throwable> causeClass = cause.getClass();
    if (causeClass == InitializationError.class) {
      final InitializationError initializationError = (InitializationError) cause;
      final List<Throwable> testClassProblemList = initializationError.getCauses();
      innerCause.append(
          String.format(
              "Test class %s is malformed. (%s problems):\n",
              testClass, testClassProblemList.size()));
      for (Throwable testClassProblem : testClassProblemList) {
        innerCause.append(testClassProblem).append("\n");
      }
    }
    return innerCause.toString();
  }

  @Override
  public Description getDescription() {
    return delegate.getDescription();
  }

  @Override
  public void run(RunNotifier runNotifier) {
    try {
      logger.log(Level.INFO, ">>> Build.FINGERPRINT = " + Build.FINGERPRINT);
    } catch (Throwable e) {
      logger.log(Level.INFO, ">>> Build.FINGERPRINT is not available");
    }
    logger.log(Level.INFO, ">> dislayName = " + delegate.getDescription().getDisplayName());
    logger.log(Level.INFO, ">> class name = " + delegate.getDescription().getClassName());
    logger.log(
        Level.INFO,
        ">> test class simple Name = " + delegate.getDescription().getTestClass().getSimpleName());

    String runnerClassName = getRunnerClassName();
    Context appContext = null;
    ContentResolver contentResolver = null;
    try {
      Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
      contentResolver = context.getContentResolver();

      boolean isRunningGoogle3Test = isPackageInstalled(context, TEST_SERVICES_PACKAGE_NAME);
      logger.log(Level.INFO, ">> isGoogle3Test = " + isRunningGoogle3Test);
    } catch (RuntimeException e) {
      contentResolver = null;
      logger.log(Level.INFO, ">> Could not get content resolver");
    }

    if ("androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner".equals(runnerClassName)
        && !assertionAdded
        && !isRobolectric()
        && !delegate.getDescription().getDisplayName().toLowerCase().contains("robolectric")
        && !delegate.getDescription().getClassName().toLowerCase().contains("robolectric")) {
      logger.log(Level.INFO, ">> Add a11y checks global assertion");

      ViewActions.addGlobalAssertion("Accessibility Checks", accessibilityCheckAssertion);
      assertionAdded = true;
    }
    delegate.run(runNotifier);
    if (assertionAdded) {
      logger.log(Level.INFO, ">> Remove a11y checks global assertion");

      ViewActions.removeGlobalAssertion(accessibilityCheckAssertion);
      assertionAdded = false;
      if (testStorage != null) {
        writeFindingsToProtoFile("a11y_results.pb", getTestIdentifier(delegate.getDescription()));
      }
    }
  }

  @Override
  public void filter(Filter filter) throws NoTestsRemainException {
    ((Filterable) delegate).filter(filter);
  }

  @Override
  public void sort(Sorter sorter) {
    ((Sortable) delegate).sort(sorter);
  }

  private String getTestIdentifier(Description description) {
    return String.format(
        Locale.ENGLISH,
        "%s.%s",
        description.getTestClass().getSimpleName(),
        description.getMethodName());
  }

  private void writeFindingsToProtoFile(String filename, String testCase) {
    ImmutableList<StepResult> stepResults = collectingListener.getStepResults();
    if (!hasFindings(stepResults)) {
      // Don't write a file if there are no results.
      return;
    }
    try (OutputStream out = getTestOutputStream(filename)) {
      protoWriter.write(out, getTestCaseResult(stepResults, testCase));
    } catch (IOException e) {
      throw new UncheckedIOException(String.format("Failed to write proto file: %s ", filename), e);
    }
  }

  private TestCaseResult getTestCaseResult(ImmutableList<StepResult> stepResults, String testCase) {
    return new TestCaseResult(testCase, stepResults);
  }

  private OutputStream getTestOutputStream(String outputPath) throws IOException {
    return this.testStorage.openOutputFile(outputPath);
  }

  private static boolean hasFindings(List<StepResult> results) {
    return FluentIterable.from(results)
        .anyMatch(stepResult -> !stepResult.getCheckResults().isEmpty());
  }

  private static boolean isRobolectric() {
    try {
      return "robolectric".equals(Build.FINGERPRINT);
    } catch (Throwable e) {
      return false;
    }
  }

  private static boolean isEspresso() {
    try {
      return "espresso".equals(Build.FINGERPRINT);
    } catch (Throwable e) {
      return false;
    }
  }

  private static boolean isPackageInstalled(Context context, String packageName) {
    try {
      context.getPackageManager().getPackageInfo(packageName, 0);
      return true;
    } catch (Throwable e) {
      return false;
    }
  }
}
