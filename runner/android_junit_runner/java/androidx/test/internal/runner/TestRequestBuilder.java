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

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import androidx.test.filters.RequiresDevice;
import androidx.test.filters.SdkSuppress;
import androidx.test.internal.runner.ClassPathScanner.ChainedClassNameFilter;
import androidx.test.internal.runner.ClassPathScanner.ExcludeClassNamesFilter;
import androidx.test.internal.runner.ClassPathScanner.ExcludePackageNameFilter;
import androidx.test.internal.runner.ClassPathScanner.ExternalClassNameFilter;
import androidx.test.internal.runner.ClassPathScanner.InclusivePackageNamesFilter;
import androidx.test.internal.util.AndroidRunnerParams;
import androidx.test.internal.util.Checks;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * A builder for {@link Request} that builds up tests to run, filtered on provided set of
 * restrictions.
 */
public class TestRequestBuilder {
  private static final String TAG = "TestRequestBuilder";

  // Excluded test packages
  private static final String[] DEFAULT_EXCLUDED_PACKAGES = {
    "junit",
    "org.junit",
    "org.hamcrest",
    "org.mockito", // exclude Mockito for performance and to prevent JVM related errors
    "androidx.test.internal.runner.junit3", // always skip AndroidTestSuite
    "org.jacoco", // exclude Jacoco to prevent class loading issues
    "net.bytebuddy" // exclude byte buddy to prevent Mockito 2.0 class loading issues
  };

  static final String MISSING_ARGUMENTS_MSG =
      "Must provide either classes to run, or paths to scan";
  static final String AMBIGUOUS_ARGUMENTS_MSG =
      "Ambiguous arguments: cannot provide both test package and test class(es) to run";

  private final List<String> pathsToScan = new ArrayList<>();
  private Set<String> mIncludedPackages = new HashSet<>();
  private Set<String> mExcludedPackages = new HashSet<>();
  private Set<String> mIncludedClasses = new HashSet<>();
  private Set<String> mExcludedClasses = new HashSet<>();
  private ClassAndMethodFilter mClassMethodFilter = new ClassAndMethodFilter();
  private Filter mFilter =
      new AnnotationExclusionFilter(androidx.test.filters.Suppress.class)
          .intersect(
              new AnnotationExclusionFilter(android.test.suitebuilder.annotation.Suppress.class))
          .intersect(new SdkSuppressFilter())
          .intersect(new RequiresDeviceFilter())
          .intersect(mClassMethodFilter);
  private List<Class<? extends RunnerBuilder>> customRunnerBuilderClasses = new ArrayList<>();
  private boolean mSkipExecution = false;
  private final DeviceBuild mDeviceBuild;
  private long mPerTestTimeout = 0;
  private final Instrumentation mInstr;
  private final Bundle mArgsBundle;
  private ClassLoader mClassLoader;

  /**
   * Instructs the test builder if JUnit3 suite() methods should be executed.
   *
   * <p>Currently set to false if any method filter is set, for consistency with
   * InstrumentationTestRunner.
   */
  private boolean mIgnoreSuiteMethods = false;

  /**
   * Accessor interface for retrieving device build properties.
   *
   * <p>Used so unit tests can mock calls
   */
  interface DeviceBuild {
    /** Returns the SDK API level for current device. */
    int getSdkVersionInt();

    /** Returns the hardware type of the current device. */
    String getHardware();
  }

  private static class DeviceBuildImpl implements DeviceBuild {
    @Override
    public int getSdkVersionInt() {
      return android.os.Build.VERSION.SDK_INT;
    }

    @Override
    public String getHardware() {
      return android.os.Build.HARDWARE;
    }
  }

  /** Helper parent class for {@link Filter} that allows suites to run if any child matches. */
  private abstract static class ParentFilter extends Filter {
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
  }

  /** Filter that only runs tests whose method or class has been annotated with given filter. */
  private static class AnnotationInclusionFilter extends ParentFilter {

    private final Class<? extends Annotation> mAnnotationClass;

    AnnotationInclusionFilter(Class<? extends Annotation> annotation) {
      mAnnotationClass = annotation;
    }

    /**
     * Determine if given test description matches filter.
     *
     * @param description the {@link Description} describing the test
     * @return <code>true</code> if matched
     */
    @Override
    protected boolean evaluateTest(Description description) {
      final Class<?> testClass = description.getTestClass();
      return description.getAnnotation(mAnnotationClass) != null
          || (testClass != null && testClass.isAnnotationPresent(mAnnotationClass));
    }

    protected Class<? extends Annotation> getAnnotationClass() {
      return mAnnotationClass;
    }

    /** {@inheritDoc} */
    @Override
    public String describe() {
      return String.format("annotation %s", mAnnotationClass.getName());
    }
  }

  /**
   * A filter for test sizes.
   *
   * <p>Will match if test method has given size annotation, or class does, but only if method does
   * not have any other size annotations. ie method size annotation overrides class size annotation.
   */
  private static class SizeFilter extends ParentFilter {

    private final TestSize mTestSize;

    SizeFilter(TestSize testSize) {
      mTestSize = testSize;
    }

    @Override
    public String describe() {
      return "";
    }

    @Override
    protected boolean evaluateTest(Description description) {
      // If test method is annotated with test size annotation include it
      if (mTestSize.testMethodIsAnnotatedWithTestSize(description)) {
        return true;
      } else if (mTestSize.testClassIsAnnotatedWithTestSize(description)) {
        // size annotation matched at class level. Make sure method doesn't have any other
        // size annotations
        for (Annotation a : description.getAnnotations()) {
          if (TestSize.isAnyTestSize(a.annotationType())) {
            return false;
          }
        }
        return true;
      }
      return false;
    }
  }

  /** Filter out tests whose method or class has been annotated with given filter. */
  private static class AnnotationExclusionFilter extends ParentFilter {

    private final Class<? extends Annotation> mAnnotationClass;

    AnnotationExclusionFilter(Class<? extends Annotation> annotation) {
      mAnnotationClass = annotation;
    }

    @Override
    protected boolean evaluateTest(Description description) {
      final Class<?> testClass = description.getTestClass();
      if ((testClass != null && testClass.isAnnotationPresent(mAnnotationClass))
          || (description.getAnnotation(mAnnotationClass) != null)) {
        return false;
      }
      return true;
    }

    /** {@inheritDoc} */
    @Override
    public String describe() {
      return String.format("not annotation %s", mAnnotationClass.getName());
    }
  }

  private static class ExtendedSuite extends Suite {

    static Suite createSuite(List<Runner> runners) {
      try {
        return new ExtendedSuite(runners);
      } catch (InitializationError e) {
        throw new RuntimeException(
            "Internal Error: "
                + Suite.class.getName()
                + "(Class<?>, List<Runner>) should never throw an "
                + "InitializationError when passed a null Class");
      }
    }

    ExtendedSuite(List<Runner> runners) throws InitializationError {
      super(null, runners);
    }
  }

  private class SdkSuppressFilter extends ParentFilter {

    @Override
    protected boolean evaluateTest(Description description) {
      final SdkSuppress sdkSuppress = getAnnotationForTest(description);
      if (sdkSuppress != null) {
        if (getDeviceSdkInt() >= sdkSuppress.minSdkVersion()
            && getDeviceSdkInt() <= sdkSuppress.maxSdkVersion()) {
          return true; // run the test
        }
        return false; // don't run the test
      }
      return true; // no SdkSuppress, run the test
    }

    private SdkSuppress getAnnotationForTest(Description description) {
      final SdkSuppress s = description.getAnnotation(SdkSuppress.class);
      if (s != null) {
        return s;
      }
      final Class<?> testClass = description.getTestClass();
      if (testClass != null) {
        return testClass.getAnnotation(SdkSuppress.class);
      }
      return null;
    }

    /** {@inheritDoc} */
    @Override
    public String describe() {
      return String.format("skip tests annotated with SdkSuppress if necessary");
    }
  }

  /** Class that filters out tests annotated with {@link RequiresDevice} when running on emulator */
  @VisibleForTesting
  class RequiresDeviceFilter extends AnnotationExclusionFilter {

    static final String EMULATOR_HARDWARE_GOLDFISH = "goldfish";
    static final String EMULATOR_HARDWARE_RANCHU = "ranchu";
    // TODO(b/65053549) Remove once we have a more generic solution
    static final String EMULATOR_HARDWARE_GCE = "gce_x86";

    private final Set<String> emulatorHardwareNames =
        new HashSet<>(
            Arrays.asList(
                EMULATOR_HARDWARE_GOLDFISH, EMULATOR_HARDWARE_RANCHU, EMULATOR_HARDWARE_GCE));

    RequiresDeviceFilter() {
      super(RequiresDevice.class);
    }

    @Override
    protected boolean evaluateTest(Description description) {
      if (!super.evaluateTest(description)) {
        // annotation is present - check if device is an emulator
        return !emulatorHardwareNames.contains(getDeviceHardware());
      }
      return true;
    }

    /** {@inheritDoc} */
    @Override
    public String describe() {
      return String.format("skip tests annotated with RequiresDevice if necessary");
    }
  }

  private static class ShardingFilter extends Filter {
    private final int mNumShards;
    private final int mShardIndex;

    ShardingFilter(int numShards, int shardIndex) {
      mNumShards = numShards;
      mShardIndex = shardIndex;
    }

    @Override
    public boolean shouldRun(Description description) {
      if (description.isTest()) {
        return (Math.abs(description.hashCode()) % mNumShards) == mShardIndex;
      }

      // The description is a suite, so assume that it can be run so that filtering is
      // applied to its children. If after filtering it has no children then it will be
      // automatically filtered out.
      return true;
    }

    /** {@inheritDoc} */
    @Override
    public String describe() {
      return String.format("Shard %s of %s shards", mShardIndex, mNumShards);
    }
  }

  /**
   * A {@link Request} that doesn't report an error if all tests are filtered out. Done for
   * consistency with InstrumentationTestRunner.
   */
  private static class LenientFilterRequest extends Request {
    private final Request mRequest;
    private final Filter mFilter;

    public LenientFilterRequest(Request classRequest, Filter filter) {
      mRequest = classRequest;
      mFilter = filter;
    }

    @Override
    public Runner getRunner() {
      try {
        Runner runner = mRequest.getRunner();
        mFilter.apply(runner);
        return runner;
      } catch (NoTestsRemainException e) {
        // don't treat filtering out all tests as an error
        return new BlankRunner();
      }
    }
  }

  /** A {@link Runner} that doesn't do anything */
  private static class BlankRunner extends Runner {
    @Override
    public Description getDescription() {
      return Description.createSuiteDescription("no tests found");
    }

    @Override
    public void run(RunNotifier notifier) {
      // do nothing
    }
  }

  /** A {@link Filter} to support the ability to filter out multiple class#method combinations. */
  private static class ClassAndMethodFilter extends ParentFilter {

    private Map<String, MethodFilter> mMethodFilters = new HashMap<>();

    @Override
    public boolean evaluateTest(Description description) {
      if (mMethodFilters.isEmpty()) {
        return true;
      }
      String className = description.getClassName();
      MethodFilter methodFilter = mMethodFilters.get(className);
      if (methodFilter != null) {
        return methodFilter.shouldRun(description);
      }
      // This test class was not explicitly excluded and none of it's test methods were
      // explicitly included or excluded. Should be run, return true:
      return true;
    }

    @Override
    public String describe() {
      return "Class and method filter";
    }

    public void addMethod(String className, String methodName) {
      MethodFilter methodFilter = mMethodFilters.get(className);
      if (methodFilter == null) {
        methodFilter = new MethodFilter(className);
        mMethodFilters.put(className, methodFilter);
      }
      methodFilter.addInclusionMethod(methodName);
    }

    public void removeMethod(String className, String methodName) {
      MethodFilter methodFilter = mMethodFilters.get(className);
      if (methodFilter == null) {
        methodFilter = new MethodFilter(className);
        mMethodFilters.put(className, methodFilter);
      }
      methodFilter.addExclusionMethod(methodName);
    }
  }

  /** A {@link Filter} used to filter out desired test methods from a given class */
  private static class MethodFilter extends ParentFilter {

    private final String mClassName;
    private Set<String> mIncludedMethods = new HashSet<>();
    private Set<String> mExcludedMethods = new HashSet<>();

    /**
     * Constructs a method filter for a given class
     *
     * @param className name of the class the method belongs to
     */
    public MethodFilter(String className) {
      mClassName = className;
    }

    @Override
    public String describe() {
      return "Method filter for " + mClassName + " class";
    }

    @Override
    public boolean evaluateTest(Description description) {
      String methodName = description.getMethodName();

      // The method name could be null, e.g. if the class is marked with @Ignore. In that
      // case there is no matching method to run so filter the test out.
      if (methodName == null) {
        return false;
      }

      // Parameterized tests append "[#]" at the end of the method names.
      // For instance, "getFoo" would become "getFoo[0]".
      methodName = stripParameterizedSuffix(methodName);
      if (mExcludedMethods.contains(methodName)) {
        return false;
      }
      // don't filter out descriptions with method name "initializationError", since
      // Junit will generate such descriptions in error cases, See ErrorReportingRunner
      return mIncludedMethods.isEmpty()
          || mIncludedMethods.contains(methodName)
          || methodName.equals("initializationError");
    }

    // Strips out the parameterized suffix if it exists
    private String stripParameterizedSuffix(String name) {
      Pattern suffixPattern = Pattern.compile(".+(\\[[0-9]+\\])$");
      if (suffixPattern.matcher(name).matches()) {
        name = name.substring(0, name.lastIndexOf('['));
      }
      return name;
    }

    public void addInclusionMethod(String methodName) {
      mIncludedMethods.add(methodName);
    }

    public void addExclusionMethod(String methodName) {
      mExcludedMethods.add(methodName);
    }
  }

  /**
   * Creates a TestRequestBuilder
   *
   * @param instr the {@link Instrumentation} to pass to applicable tests
   * @param bundle the {@link Bundle} to pass to applicable tests
   */
  public TestRequestBuilder(Instrumentation instr, Bundle bundle) {
    this(new DeviceBuildImpl(), instr, bundle);
  }

  /** Alternate TestRequestBuilder constructor that accepts a custom DeviceBuild */
  @VisibleForTesting
  TestRequestBuilder(DeviceBuild deviceBuildAccessor, Instrumentation instr, Bundle bundle) {
    mDeviceBuild = Checks.checkNotNull(deviceBuildAccessor);
    mInstr = Checks.checkNotNull(instr);
    mArgsBundle = Checks.checkNotNull(bundle);
  }

  /**
   * Instruct builder to scan the given paths and add all test classes found. Cannot be used in
   * conjunction with {@link #addTestClass} or {@link #addTestMethod} is used.
   *
   * @param paths the list of paths (.dex and .apk files) to scan
   */
  public TestRequestBuilder addPathsToScan(Iterable<String> paths) {
    for (String path : paths) {
      addPathToScan(path);
    }
    return this;
  }

  /**
   * Instruct builder to scan given path and add all test classes found. Cannot be used in
   * conjunction with {@link #addTestClass} or {@link #addTestMethod} is used.
   *
   * @param path a filepath to scan for test methods (.dex and .apk files)
   */
  public TestRequestBuilder addPathToScan(String path) {
    pathsToScan.add(path);
    return this;
  }

  /**
   * Set the {@link ClassLoader} to be used to load test cases.
   *
   * @param loader {@link ClassLoader} to load test cases with.
   */
  public TestRequestBuilder setClassLoader(ClassLoader loader) {
    mClassLoader = loader;
    return this;
  }

  /**
   * Instructs the test builder if JUnit3 suite() methods should be executed.
   *
   * @param ignoreSuiteMethods true to ignore all suite methods.
   */
  public TestRequestBuilder ignoreSuiteMethods(boolean ignoreSuiteMethods) {
    mIgnoreSuiteMethods = ignoreSuiteMethods;
    return this;
  }

  /**
   * Add a test class to be executed. All test methods in this class will be executed, unless a test
   * method was explicitly included or excluded.
   *
   * @param className
   */
  public TestRequestBuilder addTestClass(String className) {
    mIncludedClasses.add(className);
    return this;
  }

  /**
   * Excludes a test class. All test methods in this class will be excluded.
   *
   * @param className
   */
  public TestRequestBuilder removeTestClass(String className) {
    mExcludedClasses.add(className);
    return this;
  }

  /** Adds a test method to run. */
  public TestRequestBuilder addTestMethod(String testClassName, String testMethodName) {
    mIncludedClasses.add(testClassName);
    mClassMethodFilter.addMethod(testClassName, testMethodName);
    return this;
  }

  /** Excludes a test method from being run. */
  public TestRequestBuilder removeTestMethod(String testClassName, String testMethodName) {
    mClassMethodFilter.removeMethod(testClassName, testMethodName);
    return this;
  }

  /**
   * Run only tests within given java package. Cannot be used in conjunction with
   * addTestClass/Method.
   *
   * <p>At least one {@link #addPathToScan} also must be provided.
   *
   * @param testPackage the fully qualified java package name
   */
  public TestRequestBuilder addTestPackage(String testPackage) {
    mIncludedPackages.add(testPackage);
    return this;
  }

  /**
   * Excludes all tests within given java package. Cannot be used in conjunction with
   * addTestClass/Method.
   *
   * <p>At least one {@link #addPathToScan} also must be provided.
   *
   * @param testPackage the fully qualified java package name
   */
  public TestRequestBuilder removeTestPackage(String testPackage) {
    mExcludedPackages.add(testPackage);
    return this;
  }

  /**
   * Run only tests with given size
   *
   * @param forTestSize
   */
  public TestRequestBuilder addTestSizeFilter(TestSize forTestSize) {
    if (!TestSize.NONE.equals(forTestSize)) {
      addFilter(new SizeFilter(forTestSize));
    } else {
      Log.e(TAG, String.format("Unrecognized test size '%s'", forTestSize.getSizeQualifierName()));
    }
    return this;
  }

  /**
   * Only run tests annotated with given annotation class.
   *
   * @param annotation the full class name of annotation
   */
  public TestRequestBuilder addAnnotationInclusionFilter(String annotation) {
    Class<? extends Annotation> annotationClass = loadAnnotationClass(annotation);
    if (annotationClass != null) {
      addFilter(new AnnotationInclusionFilter(annotationClass));
    }
    return this;
  }

  /**
   * Skip tests annotated with given annotation class.
   *
   * @param notAnnotation the full class name of annotation
   */
  public TestRequestBuilder addAnnotationExclusionFilter(String notAnnotation) {
    Class<? extends Annotation> annotationClass = loadAnnotationClass(notAnnotation);
    if (annotationClass != null) {
      addFilter(new AnnotationExclusionFilter(annotationClass));
    }
    return this;
  }

  public TestRequestBuilder addShardingFilter(int numShards, int shardIndex) {
    return addFilter(new ShardingFilter(numShards, shardIndex));
  }

  public TestRequestBuilder addFilter(Filter filter) {
    mFilter = mFilter.intersect(filter);
    return this;
  }

  public TestRequestBuilder addCustomRunnerBuilderClass(
      Class<? extends RunnerBuilder> runnerBuilderClass) {
    customRunnerBuilderClasses.add(runnerBuilderClass);
    return this;
  }

  /**
   * Build a request that will generate test started and test ended events, but will skip actual
   * test execution.
   */
  public TestRequestBuilder setSkipExecution(boolean b) {
    mSkipExecution = b;
    return this;
  }

  /** Sets milliseconds timeout value applied to each test where 0 means no timeout */
  public TestRequestBuilder setPerTestTimeout(long millis) {
    mPerTestTimeout = millis;
    return this;
  }

  /** Convenience method to set builder attributes from {@link RunnerArgs} */
  public TestRequestBuilder addFromRunnerArgs(RunnerArgs runnerArgs) {
    for (RunnerArgs.TestArg test : runnerArgs.tests) {
      if (test.methodName == null) {
        addTestClass(test.testClassName);
      } else {
        addTestMethod(test.testClassName, test.methodName);
      }
    }
    for (RunnerArgs.TestArg test : runnerArgs.notTests) {
      if (test.methodName == null) {
        removeTestClass(test.testClassName);
      } else {
        removeTestMethod(test.testClassName, test.methodName);
      }
    }
    for (String pkg : runnerArgs.testPackages) {
      addTestPackage(pkg);
    }
    for (String pkg : runnerArgs.notTestPackages) {
      removeTestPackage(pkg);
    }
    if (runnerArgs.testSize != null) {
      addTestSizeFilter(TestSize.fromString(runnerArgs.testSize));
    }
    if (runnerArgs.annotation != null) {
      addAnnotationInclusionFilter(runnerArgs.annotation);
    }
    for (String notAnnotation : runnerArgs.notAnnotations) {
      addAnnotationExclusionFilter(notAnnotation);
    }
    for (Filter filter : runnerArgs.filters) {
      addFilter(filter);
    }
    if (runnerArgs.testTimeout > 0) {
      setPerTestTimeout(runnerArgs.testTimeout);
    }
    if (runnerArgs.numShards > 0
        && runnerArgs.shardIndex >= 0
        && runnerArgs.shardIndex < runnerArgs.numShards) {
      addShardingFilter(runnerArgs.numShards, runnerArgs.shardIndex);
    }
    if (runnerArgs.logOnly) {
      setSkipExecution(true);
    }
    if (runnerArgs.classLoader != null) {
      setClassLoader(runnerArgs.classLoader);
    }
    for (Class<? extends RunnerBuilder> runnerBuilderClass : runnerArgs.runnerBuilderClasses) {
      addCustomRunnerBuilderClass(runnerBuilderClass);
    }
    return this;
  }

  /**
   * Builds the {@link Request} based on provided data.
   *
   * @throws java.lang.IllegalArgumentException if provided set of data is not valid
   */
  public Request build() {
    mIncludedPackages.removeAll(mExcludedPackages);
    mIncludedClasses.removeAll(mExcludedClasses);
    validate(mIncludedClasses);

    boolean scanningPath = mIncludedClasses.isEmpty();

    // If scanning then suite methods are not supported.
    boolean ignoreSuiteMethods = mIgnoreSuiteMethods || scanningPath;

    AndroidRunnerParams runnerParams =
        new AndroidRunnerParams(mInstr, mArgsBundle, mPerTestTimeout, ignoreSuiteMethods);
    RunnerBuilder runnerBuilder = getRunnerBuilder(runnerParams, scanningPath);

    TestLoader loader = TestLoader.testLoader(mClassLoader, runnerBuilder, scanningPath);
    Collection<String> classNames;
    if (scanningPath) {
      // no class restrictions have been specified. Load all classes.
      classNames = getClassNamesFromClassPath();
    } else {
      classNames = mIncludedClasses;
    }

    List<Runner> runners = loader.getRunnersFor(classNames, scanningPath);

    Suite suite = ExtendedSuite.createSuite(runners);
    Request request = Request.runner(suite);
    return new LenientFilterRequest(request, mFilter);
  }

  /** Validate that the set of options provided to this builder are valid and not conflicting */
  private void validate(Set<String> classNames) {
    if (classNames.isEmpty() && pathsToScan.isEmpty()) {
      throw new IllegalArgumentException(MISSING_ARGUMENTS_MSG);
    }
    // TODO(b/73905202): consider failing if both test classes and scan paths are given.
    // Right now that is allowed though

    if ((!mIncludedPackages.isEmpty() || !mExcludedPackages.isEmpty()) && !classNames.isEmpty()) {
      throw new IllegalArgumentException(AMBIGUOUS_ARGUMENTS_MSG);
    }
  }

  /**
   * Get the {@link RunnerBuilder} to use to create the {@link Runner} instances.
   *
   * @param runnerParams {@link AndroidRunnerParams} that stores common runner parameters
   * @param scanningPath true if being used to build {@link Runner} from classes found while
   *     scanning the path; requires extra checks to avoid unnecessary errors.
   * @return a {@link RunnerBuilder}.
   */
  private RunnerBuilder getRunnerBuilder(AndroidRunnerParams runnerParams, boolean scanningPath) {
    RunnerBuilder builder;
    if (mSkipExecution) {
      // If all that is needed is the list of tests then replace the Runner which will
      // run the test with one that will simply fire events for each of the tests.
      builder = new AndroidLogOnlyBuilder(runnerParams, scanningPath, customRunnerBuilderClasses);
    } else {
      builder = new AndroidRunnerBuilder(runnerParams, scanningPath, customRunnerBuilderClasses);
    }
    return builder;
  }

  private Collection<String> getClassNamesFromClassPath() {
    if (pathsToScan.isEmpty()) {
      throw new IllegalStateException("neither test class to execute or class paths were provided");
    }
    Log.i(TAG, String.format("Scanning classpath to find tests in paths %s", pathsToScan));
    ClassPathScanner scanner = createClassPathScanner(pathsToScan);

    ChainedClassNameFilter filter = new ChainedClassNameFilter();
    // exclude inner classes
    filter.add(new ExternalClassNameFilter());
    for (String pkg : DEFAULT_EXCLUDED_PACKAGES) {
      // Add the test packages to the exclude list unless they were explictly included.
      if (!mIncludedPackages.contains(pkg)) {
        mExcludedPackages.add(pkg);
      }
    }
    if (!mIncludedPackages.isEmpty()) {
      filter.add(new InclusivePackageNamesFilter(mIncludedPackages));
    }
    for (String pkg : mExcludedPackages) {
      filter.add(new ExcludePackageNameFilter(pkg));
    }
    filter.add(new ExcludeClassNamesFilter(mExcludedClasses));
    try {
      return scanner.getClassPathEntries(filter);
    } catch (IOException e) {
      Log.e(TAG, "Failed to scan classes", e);
    }
    return Collections.emptyList();
  }

  /**
   * Factory method for {@link ClassPathScanner}.
   *
   * <p>Exposed so unit tests can mock.
   */
  ClassPathScanner createClassPathScanner(List<String> classPath) {
    return new ClassPathScanner(classPath);
  }

  @SuppressWarnings("unchecked")
  private Class<? extends Annotation> loadAnnotationClass(String className) {
    try {
      Class<?> clazz = Class.forName(className);
      return (Class<? extends Annotation>) clazz;
    } catch (ClassNotFoundException e) {
      Log.e(TAG, String.format("Could not find annotation class: %s", className));
    } catch (ClassCastException e) {
      Log.e(TAG, String.format("Class %s is not an annotation", className));
    }
    return null;
  }

  private int getDeviceSdkInt() {
    return mDeviceBuild.getSdkVersionInt();
  }

  private String getDeviceHardware() {
    return mDeviceBuild.getHardware();
  }
}
