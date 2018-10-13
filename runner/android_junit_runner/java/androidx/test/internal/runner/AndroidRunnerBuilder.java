/*
 * Copyright (C) 2012 The Android Open Source Project
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
package androidx.test.internal.runner;

import androidx.test.internal.runner.junit3.AndroidJUnit3Builder;
import androidx.test.internal.runner.junit3.AndroidSuiteBuilder;
import androidx.test.internal.runner.junit4.AndroidAnnotatedBuilder;
import androidx.test.internal.runner.junit4.AndroidJUnit4Builder;
import androidx.test.internal.util.AndroidRunnerParams;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.internal.builders.AnnotatedBuilder;
import org.junit.internal.builders.IgnoredBuilder;
import org.junit.internal.builders.JUnit3Builder;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

/** A {@link RunnerBuilder} that can handle all types of tests. */
class AndroidRunnerBuilder extends AllDefaultPossibilitiesBuilder {

  private final AndroidJUnit3Builder androidJUnit3Builder;
  private final AndroidJUnit4Builder androidJUnit4Builder;
  private final AndroidSuiteBuilder androidSuiteBuilder;
  private final AndroidAnnotatedBuilder androidAnnotatedBuilder;

  // TODO: customize for Android ?
  private final IgnoredBuilder ignoredBuilder;

  private final List<RunnerBuilder> customRunnerBuilders;

  /** @param runnerParams {@link AndroidRunnerParams} that stores common runner parameters */
  public AndroidRunnerBuilder(AndroidRunnerParams runnerParams) {
    this(null, runnerParams, false, Collections.<Class<? extends RunnerBuilder>>emptyList());
  }

  /**
   * @param runnerParams {@link AndroidRunnerParams} that stores common runner parameters
   * @param scanningPath true if being used to build {@link Runner} from classes found while
   *     scanning the path; requires extra checks to avoid unnecessary errors.
   * @param customRunnerBuilderClasses custom {@link RunnerBuilder} classes
   */
  AndroidRunnerBuilder(
      AndroidRunnerParams runnerParams,
      boolean scanningPath,
      List<Class<? extends RunnerBuilder>> customRunnerBuilderClasses) {
    this(null, runnerParams, scanningPath, customRunnerBuilderClasses);
  }

  /**
   * Initialize.
   *
   * <p>Each class in the {@code customRunnerBuilderClasses} list must be a concrete public class
   * and must have a public no-argument constructor.
   *
   * @param suiteBuilder the top level {@link RunnerBuilder} to use to build nested classes.
   * @param runnerParams {@link AndroidRunnerParams} that stores common runner parameters
   * @param scanningPath true if being used to build {@link Runner} from classes found while
   *     scanning the path; requires extra checks to avoid unnecessary errors.
   * @param customRunnerBuilderClasses custom {@link RunnerBuilder} classes
   * @throws IllegalStateException if any of the custom {@link RunnerBuilder} classes cannot be
   *     instantiated.
   */
  AndroidRunnerBuilder(
      RunnerBuilder suiteBuilder,
      AndroidRunnerParams runnerParams,
      boolean scanningPath,
      List<Class<? extends RunnerBuilder>> customRunnerBuilderClasses) {
    super(true);
    androidJUnit3Builder = new AndroidJUnit3Builder(runnerParams, scanningPath);
    androidJUnit4Builder = new AndroidJUnit4Builder(runnerParams, scanningPath);
    androidSuiteBuilder = new AndroidSuiteBuilder(runnerParams);
    androidAnnotatedBuilder =
        new AndroidAnnotatedBuilder(suiteBuilder == null ? this : suiteBuilder, runnerParams);
    ignoredBuilder = new IgnoredBuilder();

    customRunnerBuilders = instantiateRunnerBuilders(customRunnerBuilderClasses);
  }

  /**
   * Transform the list of {@code Class<? extends RunnerBuilder>} into a list of {@code
   * RunnerBuilder} instances.
   *
   * @throws IllegalStateException if any of the classes cannot be instantiated.
   */
  private List<RunnerBuilder> instantiateRunnerBuilders(
      List<Class<? extends RunnerBuilder>> customRunnerBuilderClasses) {

    List<RunnerBuilder> runnerBuilders = new ArrayList<>();
    for (Class<? extends RunnerBuilder> customRunnerBuilderClass : customRunnerBuilderClasses) {
      try {
        RunnerBuilder runnerBuilder =
            customRunnerBuilderClass.getDeclaredConstructor().newInstance();
        runnerBuilders.add(runnerBuilder);
      } catch (InstantiationException e) {
        throw new IllegalStateException(
            "Could not create instance of "
                + customRunnerBuilderClass
                + ", make sure that it is a public concrete class with a public no-argument"
                + " constructor",
            e);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException(
            "Could not create instance of "
                + customRunnerBuilderClass
                + ", make sure that it is a public concrete class with a public no-argument"
                + " constructor",
            e);
      } catch (NoSuchMethodException e) {
        throw new IllegalStateException(
            "Could not create instance of "
                + customRunnerBuilderClass
                + ", make sure that it is a public concrete class with a public no-argument"
                + " constructor",
            e);
      } catch (InvocationTargetException e) {
        throw new IllegalStateException(
            "Could not create instance of "
                + customRunnerBuilderClass
                + ", the constructor must not throw an exception",
            e);
      }
    }
    return runnerBuilders;
  }

  @Override
  public Runner runnerForClass(Class<?> testClass) throws Throwable {
    // Try the custom RunnerBuilder instances first.
    for (RunnerBuilder customRunnerBuilder : customRunnerBuilders) {
      Runner runner = customRunnerBuilder.safeRunnerForClass(testClass);
      if (runner != null) {
        return runner;
      }
    }

    return super.runnerForClass(testClass);
  }

  @Override
  protected JUnit4Builder junit4Builder() {
    return androidJUnit4Builder;
  }

  @Override
  protected JUnit3Builder junit3Builder() {
    return androidJUnit3Builder;
  }

  @Override
  protected AnnotatedBuilder annotatedBuilder() {
    return androidAnnotatedBuilder;
  }

  @Override
  protected IgnoredBuilder ignoredBuilder() {
    return ignoredBuilder;
  }

  @Override
  protected RunnerBuilder suiteMethodBuilder() {
    return androidSuiteBuilder;
  }
}
