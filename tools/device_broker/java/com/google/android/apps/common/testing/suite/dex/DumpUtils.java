/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.apps.common.testing.suite.dex;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Throwables.throwIfUnchecked;

import com.google.android.apps.common.testing.proto.TestInfo;
import com.google.android.apps.common.testing.proto.TestInfo.AnnotationPb;
import com.google.android.apps.common.testing.proto.TestInfo.AnnotationValuePb;
import com.google.android.apps.common.testing.proto.TestInfo.InfoPb;
import com.google.android.apps.common.testing.proto.TestInfo.TestSuitePb;
import com.google.android.apps.common.testing.suite.dex.DexClassData.MethodData;
import com.google.android.apps.common.testing.suite.dex.DexClassData.Visibility;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Extracts test information from the output of dexdump into a @link(TestInfo).
 *
 * Background:
 * Initially we created test suites from the output of am instrument,
 * this had the unfortunate drawback of newing each test class to get its
 * test methods. This caused suite listing to take ~1 to 2 mins. To get around
 * it we wrote our own InstrumentationTestRunner that listed suites without newing
 * the individual test cases and it was much faster. We added some richer annotation handling
 * like SDKSuppress, and the testsuite was written to a protobuf file instead of being parsed
 * from am instrument's stdout. (Aside: am instrument std out parsing would fail once the
 * number of test cases generated an output greater then about 40kb).
 *
 * All was well and good until we ended up having dex conflicts with the guava libraries
 * in the apk under test vs. the GoogleInstrumentationTestRunner library. There was no one
 * fix (because fixing it would break other projects which it was working for). Therefore all
 * guava deps were removed. Subsequently the same type of issue popped up with protobuf
 * dependencies. This library sharing problem is a huge issue in android (and needs to be
 * addressed seperately). In the interim we decided to discover test cases without needing
 * to be in the apk under test's jvm at all. We do this by parsing the output of dexdump on
 * the apk and generating the test suite structure.
 *
 */
public final class DumpUtils {

  private static final String J3_TEST_PREFIX = "test";

  private static final String DEFAULT_ANNOTATION_CLASS = "dalvik.annotation.AnnotationDefault";

  private static final Function<AnnotationPb, String> EXTRACT_ANNOTATION_CLASS_NAME =
    new Function<AnnotationPb, String>() {
        @Override
        public String apply(AnnotationPb anno) {
          return anno.getClassName();
        }
    };

  private static final Function<MethodData, String> EXTRACT_METHOD_NAME =
    new Function<MethodData, String>() {
        @Override
        public String apply(MethodData method) {
          return method.getMethodName();
        }
    };

  private static final Predicate<Future<?>> COMPUTATION_COMPLETE =
      new Predicate<Future<?>>() {
        @Override
        public boolean apply(Future<?> future) {
          return future.isDone();
        }
      };

  private static final Predicate<Optional<?>> OPTION_PRESENT =
      new Predicate<Optional<?>>() {
        @Override
        public boolean apply(Optional<?> optionIn) {
          return optionIn.isPresent();
        }
      };


  private static final Predicate<MethodData> COMMON_TEST_METHOD_REQUIREMENTS =
      new Predicate<MethodData>() {

        @Override
        public boolean apply(MethodData methodData) {
          return
              !methodData.hasArguments() &&
              !methodData.hasReturnType() &&
              Visibility.PUBLIC.equals(methodData.getVisibility());
        }
      };

  private static final Predicate<MethodData> IS_J4_TEST_METHOD =
      Predicates.and(COMMON_TEST_METHOD_REQUIREMENTS, new Predicate<MethodData>() {
        @Override
        public boolean apply(MethodData methodData) {
          for (AnnotationPb anno : methodData.getAnnotations()) {
            if (anno.getClassName().equals("org.junit.Test")) {
              return true;
            }
          }
          return false;
        }
      });

  private static final Predicate<MethodData> IS_J3_TEST_METHOD =
      Predicates.and(COMMON_TEST_METHOD_REQUIREMENTS, new Predicate<MethodData>() {

        @Override
        public boolean apply(MethodData methodData) {
          return
              !methodData.hasArguments() &&
              !methodData.hasReturnType() &&
              Visibility.PUBLIC.equals(methodData.getVisibility()) &&
              methodData.getMethodName().startsWith(J3_TEST_PREFIX);
        }
      });

  private static final Predicate<AnnotationPb> HAS_RUN_WITH_ANNOTATION =
      new Predicate<AnnotationPb>() {

        @Override
        public boolean apply(AnnotationPb annoPb) {
          return "org.junit.runner.RunWith".equals(annoPb.getClassName());
        }
      };

  private static class MethodToTestInfoConvertor implements Function<MethodData, InfoPb> {
    private final DexClassData concreteClass;
    private final List<AnnotationPb> classAnnotations;
    private final boolean isUiTest;
    public MethodToTestInfoConvertor(DexClassData concreteClass,
        List<AnnotationPb> classAnnotations,
        boolean isUiTest) {
      this.concreteClass = checkNotNull(concreteClass);
      this.classAnnotations = checkNotNull(classAnnotations);
      this.isUiTest = isUiTest;
      checkState(!concreteClass.isAbstract(), "A test class must be concrete! %s", concreteClass);
    }

    @Override
    public InfoPb apply(MethodData method) {
      InfoPb.Builder builder = InfoPb.newBuilder()
          .setTestClass(concreteClass.getClassName())
          .setTestPackage(concreteClass.getPackageName())
          .setTestMethod(method.getMethodName())
          .setIsUiTest(isUiTest)
          .addAllClassAnnotation(classAnnotations)
          .addAllMethodAnnotation(method.getAnnotations());
      return builder.build();
    }
  }

  private static class MergeWithDefaultAnnotations implements Function<InfoPb, InfoPb> {
    private final Map<String, AnnotationPb> defaultAnnotations;
    public MergeWithDefaultAnnotations(Map<String, AnnotationPb> defaultAnnotations) {
      this.defaultAnnotations = checkNotNull(defaultAnnotations);
    }

    @Override
    public InfoPb apply(InfoPb in) {
      List<AnnotationPb> classAnnotations =
          FluentIterable
              .from(in.getClassAnnotationList())
              .transform(new Function<AnnotationPb, AnnotationPb>() {
                @Override
                public AnnotationPb apply(AnnotationPb annotation) {
                  return mergeWithDefaults(annotation, defaultAnnotations);
                }
              })
              .toList();
      List<AnnotationPb> methodAnnotations =
          FluentIterable
              .from(in.getMethodAnnotationList())
              .transform(new Function<AnnotationPb, AnnotationPb>() {
                @Override
                public AnnotationPb apply(AnnotationPb annotation) {
                  return mergeWithDefaults(annotation, defaultAnnotations);
                }
              })
              .toList();
      return in.toBuilder()
          .clearClassAnnotation()
          .clearMethodAnnotation()
          .addAllClassAnnotation(classAnnotations)
          .addAllMethodAnnotation(methodAnnotations)
          .build();
    }
  }

  private static class TestRelatedClassData {
    private final boolean descendsFromJUnit3TestCase;

    private TestRelatedClassData(boolean descendsFromJUnit3TestCase) {
      this.descendsFromJUnit3TestCase = descendsFromJUnit3TestCase;
    }

    private boolean isUiTest = false;
    private Optional<AnnotationPb> defaultAnnotationData = Optional.absent();
    private Optional<List<MethodData>> aggregatedTestMethods = Optional.absent();
    private Optional<List<AnnotationPb>> aggregatedClassAnnotations = Optional.absent();
    private Optional<List<InfoPb>> executableTests = Optional.absent();
  }

  /**
   * Consumes several DexClassData and presents them as a unique by class iterator.
   *
   * <p> Android instrumentation tests have a classpath of the test apk's classes and then the app
   * under test classes. If there are duplicate definitions - the first one wins.
   *
   * <p> Obviously based on this order matters in the construction of this iterator.
   */
  private static class ClassDeduplicatingIterator extends AbstractIterator<DexClassData> {
    private final Iterator<DexClassData> combinedIterator;
    private final Set<String> definedClassNames = Sets.newHashSet();

    @Override
    protected DexClassData computeNext() {
      while (combinedIterator.hasNext()) {
        DexClassData maybeNext = combinedIterator.next();
        if (definedClassNames.add(maybeNext.getFullClassName())) {
          return maybeNext;
        }
      }
      return endOfData();
    }

    ImmutableSet<String> encounteredClassNames() {
      return ImmutableSet.copyOf(definedClassNames);
    }

    private ClassDeduplicatingIterator(Iterator<DexClassData> combinedIterator) {
      this.combinedIterator = checkNotNull(combinedIterator);
    }
  }

  // Shim for the Pair data structure for OSS release
  public static class Pair<A, B> {
    public final A first;
    public final B second;
    public static <A, B> Pair<A, B> of(A first, B second) {
      return new Pair<>(first, second);
    }
    public A getFirst() {
      return first;
    }
    public B getSecond() {
      return second;
    }
    Pair(A a, B b) {
      this.first = a;
      this.second = b;
    }
  }
  // */

  /**
   * Converts an input stream containing the output of dexdump into TestSuitePb and a set of all
   * classes contained in the apk.
   *
   * <p>DexDump is expected to be ran with the following flags: -a cms -l xml_private
   *
   * <p>This forces it to generate annotations placed at the class level, method level, and all
   * system annotations. It also makes it list non-public methods and classes. All this info is
   * required for proper dumping.
   */
  public static Pair<TestSuitePb, ImmutableSet<String>> parseDexDump(InputStream... xmlIns) {

    Iterator<DexClassData> combinedIterator = baseTestClasses();
    for (InputStream xmlIn : xmlIns) {
      combinedIterator = Iterators.concat(combinedIterator, new DexDumpIterator(xmlIn));
    }
    ClassDeduplicatingIterator dexIterator = new ClassDeduplicatingIterator(combinedIterator);
    TestSuitePb suite = discoverTests(dexIterator);
    return Pair.of(suite, dexIterator.encounteredClassNames());
  }


  public static TestSuitePb discoverTests(Iterator<DexClassData> dexIterator) {
    Map<String, SettableFuture<Optional<TestRelatedClassData>>> classNameToTestData =
        Maps.newHashMap();

    SettableFuture<Optional<TestRelatedClassData>> javaLangObject = SettableFuture.create();
    javaLangObject.set(Optional.<TestRelatedClassData>absent());

    classNameToTestData.put("java.lang.Object", javaLangObject);

    while (dexIterator.hasNext()) {
      DexClassData data = dexIterator.next();
      SettableFuture<Optional<TestRelatedClassData>> myTestData = classNameToTestData.get(
          data.getFullClassName());
      if (null == myTestData) {
        // no one has depended on us as a parent class yet. but they might!
        myTestData = SettableFuture.create();
        classNameToTestData.put(data.getFullClassName(), myTestData);
      }
      checkState(!myTestData.isDone(), "Impossible! I'm responsible for creating this! %s",
          data.getFullClassName());
      if (hasAnnotationDefaultData(data)) {
        // this must be an annotation class - and it has a default value.
        // we'll need this to properly populate TestInfo proto buffers.
        TestRelatedClassData annoData = new TestRelatedClassData(false);
        annoData.defaultAnnotationData = Optional.of(extractAnnotationDefault(data));
        myTestData.set(Optional.of(annoData));
      } else if (data.getFullClassName().equals("junit.framework.TestCase")) {
        // our base case! the class everyone must extend to be a j3 class.

        TestRelatedClassData junit3TestData = new TestRelatedClassData(true);

        // obviously has no test methods and is not executable in and of itself,
        // however this will trigger its decendants listeners.
        // this is provably not a junit descendant (eg: extends something in java).
        myTestData.set(Optional.of(junit3TestData));
      } else {
        // We need to determine whether we're a decendant of junit3 test case.
        // we do that by listening to our parent's future to see if it has
        // any TestRelatedClassData.

        SettableFuture<Optional<TestRelatedClassData>> parentTestData = classNameToTestData.get(
            data.getExtendsClass());
        if (null == parentTestData) {
          parentTestData = SettableFuture.create();
          classNameToTestData.put(data.getExtendsClass(), parentTestData);
        }
        parentTestData.addListener(new OnParentTestRelatedDataListener(data, myTestData,
            parentTestData), MoreExecutors.directExecutor());
      }
    }

    // Lets get all the test related data that we discovered

    List<TestRelatedClassData> testData =
        FluentIterable.from(classNameToTestData.values())
            .filter(COMPUTATION_COMPLETE)
            .transform(
                new Function<
                    Future<Optional<TestRelatedClassData>>, Optional<TestRelatedClassData>>() {
                  @Override
                  public Optional<TestRelatedClassData> apply(
                      Future<Optional<TestRelatedClassData>> futureIn) {
                    try {
                      return futureIn.get();
                    } catch (InterruptedException ie) {
                      throw new RuntimeException(ie);
                    } catch (ExecutionException ee) {
                      throwIfUnchecked(ee.getCause());
                      throw new RuntimeException(ee.getCause());
                    }
                  }
                })
            .filter(OPTION_PRESENT)
            .transform(DumpUtils.<TestRelatedClassData>removeOptionality())
            .toList();

    // Build a map of annotations to their defaults

    Map<String, AnnotationPb> defaultAnnotations =
        FluentIterable
            .from(testData)
            .transform(new Function<TestRelatedClassData, Optional<AnnotationPb>>() {
              @Override
              public Optional<AnnotationPb> apply(TestRelatedClassData dataIn) {
                return dataIn.defaultAnnotationData;
              }
            })
            .filter(OPTION_PRESENT)
            .transform(DumpUtils.<AnnotationPb>removeOptionality())
            .uniqueIndex(EXTRACT_ANNOTATION_CLASS_NAME);


    // Get all the tests (and apply annotation defaults where needed)
    List<InfoPb> allExecutableTests =
        FluentIterable
            .from(testData)
            .transform(new Function<TestRelatedClassData, Optional<List<InfoPb>>>() {
              @Override
              public Optional<List<InfoPb>> apply(TestRelatedClassData dataIn) {
                return dataIn.executableTests;
              }
            })
            .filter(OPTION_PRESENT)
            .transformAndConcat(DumpUtils.<List<InfoPb>>removeOptionality())
            .transform(new MergeWithDefaultAnnotations(defaultAnnotations))
            .toList();

    return TestSuitePb.newBuilder().addAllInfo(allExecutableTests).build();
  }

  private static boolean hasJUnit4TestData(DexClassData classData) {
    boolean hasJ4Methods = FluentIterable.from(classData.getMethods()).anyMatch(IS_J4_TEST_METHOD);
    boolean hasRunWith =
        FluentIterable.from(classData.getAnnotations()).anyMatch(HAS_RUN_WITH_ANNOTATION);
    return hasJ4Methods || hasRunWith;
  }


  private static class OnParentTestRelatedDataListener implements Runnable {
    private DexClassData classData;
    private SettableFuture<Optional<TestRelatedClassData>> myFutureTestData;
    private SettableFuture<Optional<TestRelatedClassData>> myParentFutureTestData;
    public OnParentTestRelatedDataListener(DexClassData classData,
        SettableFuture<Optional<TestRelatedClassData>> myFutureTestData,
        SettableFuture<Optional<TestRelatedClassData>> myParentFutureTestData) {

      this.classData = checkNotNull(classData);
      this.myFutureTestData = checkNotNull(myFutureTestData);
      this.myParentFutureTestData = checkNotNull(myParentFutureTestData);
    }

    @Override
    public void run() {
      checkState(null != classData, "run twice?");
      Optional<TestRelatedClassData> myParentData = null;
      try {
        myParentData = myParentFutureTestData.get();
      } catch (InterruptedException ie) {
        throw new RuntimeException(ie);
      } catch (ExecutionException ee) {
        throwIfUnchecked(ee.getCause());
        throw new RuntimeException(ee.getCause());
      }
      if (myParentData.isPresent() && myParentData.get().defaultAnnotationData.isPresent()) {
        // parent is default annotation data.
        myFutureTestData.set(Optional.<TestRelatedClassData>absent());
        return;
      }

      if (!myParentData.isPresent() && !hasJUnit4TestData(classData)) {
        // nope - not a j4 test, not a j3 test.
        myFutureTestData.set(Optional.<TestRelatedClassData>absent());
        return;
      }
      TestRelatedClassData parentData = myParentData.or(new TestRelatedClassData(false));
      TestRelatedClassData myTestRelatedData =
          new TestRelatedClassData(parentData.descendsFromJUnit3TestCase);
      if (classData.isUiTest() || parentData.isUiTest) {
        myTestRelatedData.isUiTest = true;
      }

      List<AnnotationPb> myAggregatedClassAnnotations =
          Lists.newArrayList(classData.getAnnotations());
      Set<String> myAnnotationClassNames = Sets.newHashSet();
      for (AnnotationPb anno : myAggregatedClassAnnotations) {
        myAnnotationClassNames.add(anno.getClassName());
      }
      if (parentData.aggregatedClassAnnotations.isPresent()) {
        for (AnnotationPb parentAnno : parentData.aggregatedClassAnnotations.get()) {
          if (!myAnnotationClassNames.contains(parentAnno.getClassName())) {
            myAggregatedClassAnnotations.add(parentAnno);
          }
        }
      }

      myTestRelatedData.aggregatedClassAnnotations = Optional.of(myAggregatedClassAnnotations);

      Map<String, MethodData> myAggregatedTestMethods = Maps.newHashMap();
      myAggregatedTestMethods.putAll(
          FluentIterable.from(classData.getMethods())
              .filter(Predicates.or(IS_J3_TEST_METHOD, IS_J4_TEST_METHOD))
              .uniqueIndex(EXTRACT_METHOD_NAME));

      if (parentData.aggregatedTestMethods.isPresent()) {
        // merge the parent test methods to the sub class test methods
        // append any annotation present on the parent method to our method.

        for (MethodData parentMeth : parentData.aggregatedTestMethods.get()) {
          MethodData myOverride = myAggregatedTestMethods.get(parentMeth.getMethodName());
          if (null == myOverride) {
            myAggregatedTestMethods.put(parentMeth.getMethodName(), parentMeth);
          } else {
            Set<String> myOverrideAnnotationNames = Sets.newHashSet();
            for (AnnotationPb myOverrideAnnotation : myOverride.getAnnotations()) {
              myOverrideAnnotationNames.add(myOverrideAnnotation.getClassName());
            }
            List<AnnotationPb> myAggregatedAnnotations =
                Lists.newArrayList(myOverride.getAnnotations());
            for (AnnotationPb parentAnno : parentMeth.getAnnotations()) {
              if (!myOverrideAnnotationNames.contains(parentAnno.getClassName())) {
                myAggregatedAnnotations.add(parentAnno);
              }
            }
            if (myAggregatedAnnotations.size() != myOverride.getAnnotations().size()) {
              MethodData.MethodBuilder builder = myOverride.toBuilder().clearAnnotations();
              for (AnnotationPb anno : myAggregatedAnnotations) {
                builder.addAnnotation(anno);
              }
              myOverride = builder.build();
              myAggregatedTestMethods.put(myOverride.getMethodName(), myOverride);
            }
          }
        }
      }
      myTestRelatedData.aggregatedTestMethods =
          Optional.<List<MethodData>>of(Lists.newArrayList(myAggregatedTestMethods.values()));

      if (!classData.isAbstract()) {
        myTestRelatedData.executableTests =
            Optional.<List<InfoPb>>of(
                FluentIterable.from(myTestRelatedData.aggregatedTestMethods.get())
                    .filter(runnerFilter(myTestRelatedData))
                    .transform(
                        new MethodToTestInfoConvertor(
                            classData,
                            myTestRelatedData.aggregatedClassAnnotations.get(),
                            myTestRelatedData.isUiTest))
                    .toList());
      }
      classData = null;
      myFutureTestData.set(Optional.<TestRelatedClassData>of(myTestRelatedData));
    }
  }

  private static Predicate<MethodData> runnerFilter(TestRelatedClassData testData) {
    boolean hasRunWith =
        FluentIterable.from(testData.aggregatedClassAnnotations.get())
            .anyMatch(HAS_RUN_WITH_ANNOTATION);
    if (hasRunWith) {
      return IS_J4_TEST_METHOD;
    }
    if (testData.descendsFromJUnit3TestCase) {
      return IS_J3_TEST_METHOD;
    }
    return Predicates.<MethodData>alwaysFalse();
  }

  private static Iterator<DexClassData> baseTestClasses() {
    // these classes actually live in android.test.runner.jar which is dynamically linked in
    // on the device.
    // I'd rather do this dynamically some time in the future.

    DexClassData.Builder androidBuilder = DexClassData.builder()
        .setIsAbstract(true) // technically false but we dont actually want to run these classes
        .setPackageName("android.test")
        .setVisibility(Visibility.PUBLIC.toString());

    MethodData.MethodBuilder methodBuilder = MethodData.builder()
        .setIsAbstract(false)
        .setHasArguments(false)
        .setHasReturnType(false)
        .setVisibility(Visibility.PUBLIC.toString());

    List<DexClassData> baseClasses = Lists.newArrayList(
        DexClassData.builder()
            .setClassName("TestCase")
            .setVisibility(Visibility.PUBLIC.toString())
            .setExtendsClass("java.lang.Object")
            .setPackageName("junit.framework")
            .setIsAbstract(true)
            .build(),
        androidBuilder
            .setClassName("AndroidTestCase")
            .setExtendsClass("junit.framework.TestCase")
            .clearMethods()
            .addMethod(
                methodBuilder
                    .setMethodName("testAndroidTestCaseSetupProperly")
                    .build().toBuilder()
                    .addAnnotation(AnnotationPb.newBuilder()
                      // Technically it appears as a android.test.suitebuilder.annotation.Suppress
                      // but only in the API 24 sdk, so we have to translate it to a min/max
                      // suppression using the ATSL filter.
                      .setClassName("androidx.test.filters.SdkSuppress")
                      .addAnnotationValue(AnnotationValuePb.newBuilder()
                        .setFieldName("maxSdkVersion")
                        .addFieldValue("23") // its INCLUSIVE - this is the last valid api level.
                        .setFieldType(TestInfo.Type.INTEGER)
                        .setIsArray(false)
                        .build())
                      .build())
                    .build())
            .build(),
        androidBuilder
            .setClassName("ApplicationTestCase")
            .setExtendsClass("android.test.AndroidTestCase")
            .clearMethods()
            .addMethod(
                methodBuilder
                    .setMethodName("testApplicationTestCaseSetUpProperly")
                    .build())
            .build(),
        androidBuilder
            .setClassName("LoaderTestCase")
            .setExtendsClass("android.test.AndroidTestCase")
            .clearMethods()
            .build(),
        androidBuilder
            .setClassName("ProviderTestCase2")
            .setExtendsClass("android.test.AndroidTestCase")
            .clearMethods()
            .build(),
        androidBuilder
            .setClassName("ServiceTestCase")
            .setExtendsClass("android.test.AndroidTestCase")
            .clearMethods()
            .addMethod(
                methodBuilder
                    .setMethodName("testServiceTestCaseSetUpProperly")
                    .build())
            .build(),
        androidBuilder
            .setClassName("InstrumentationTestCase")
            .setExtendsClass("junit.framework.TestCase")
            .clearMethods()
            .build(),
        androidBuilder
            .setClassName("ActivityTestCase")
            .setExtendsClass("android.test.InstrumentationTestCase")
            .clearMethods()
            .build(),
        androidBuilder
            .setClassName("ActivityInstrumentationTestCase")
            .setExtendsClass("android.test.ActivityTestCase")
            .clearMethods()
            .setIsUiTest(true)
            .addMethod(
                methodBuilder
                    .setMethodName("testActivityTestCaseSetUpProperly")
                    .build())
            .build(),
        androidBuilder
            .setClassName("ActivityInstrumentationTestCase2")
            .setExtendsClass("android.test.ActivityTestCase")
            .setIsUiTest(true)
            .clearMethods()
            .build(),
        androidBuilder
            .setIsUiTest(false)
            .setClassName("ActivityUnitTestCase")
            .setExtendsClass("android.test.ActivityTestCase")
            .clearMethods()
            .build(),
        androidBuilder
            .setClassName("ProviderTestCase")
            .setExtendsClass("android.test.InstrumentationTestCase")
            .clearMethods()
            .build(),
        androidBuilder
            .setClassName("SingleLaunchActivityTestCase")
            .setExtendsClass("android.test.InstrumentationTestCase")
            .clearMethods()
            .addMethod(
                methodBuilder
                    .setMethodName("testActivityTestCaseSetUpProperly")
                    .build())
            .build(),
        androidBuilder
            .setClassName("SyncBaseInstrumentation")
            .setExtendsClass("android.test.InstrumentationTestCase")
            .clearMethods()
            .build());

    return baseClasses.iterator();

  }

  private static final boolean hasAnnotationDefaultData(DexClassData data) {
    for (AnnotationPb annotation : data.getAnnotations()) {
      if (annotation.getClassName().equals(DEFAULT_ANNOTATION_CLASS)) {
        return true;
      }
    }
    return false;
  }

  private static final AnnotationPb extractAnnotationDefault(DexClassData data) {
    for (AnnotationPb annotation : data.getAnnotations()) {
      if (annotation.getClassName().equals(DEFAULT_ANNOTATION_CLASS)) {
        return annotation.getAnnotationValue(0).getFieldAnnotationValue(0);
      }
    }
    throw new IllegalStateException("no default annotation in " + data);
  }

  private static <T> Function<Optional<T>, T> removeOptionality() {
    return new Function<Optional<T>, T>() {
      @Override
      public T apply(Optional<T> optionIn) {
        return optionIn.get();
      }
    };
  }

  private static AnnotationPb mergeWithDefaults(AnnotationPb annotation,
      Map<String, AnnotationPb> defaultValues) {
    AnnotationPb defaultAnno = defaultValues.get(annotation.getClassName());
    if (null == defaultAnno) {
      return annotation;
    }
    Set<String> explicitValues = Sets.newHashSet();
    for (AnnotationValuePb value : annotation.getAnnotationValueList()) {
      explicitValues.add(value.getFieldName());
    }

    AnnotationPb.Builder newAnno = annotation.toBuilder();
    for (AnnotationValuePb defaultValue : defaultAnno.getAnnotationValueList()) {
      if (!explicitValues.contains(defaultValue.getFieldName())) {
        newAnno.addAnnotationValue(defaultValue);
      }
    }
    return newAnno.build();
  }

  private DumpUtils() { }
}
