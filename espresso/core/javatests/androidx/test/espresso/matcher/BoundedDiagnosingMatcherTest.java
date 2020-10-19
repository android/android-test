package androidx.test.espresso.matcher;

import static androidx.test.espresso.matcher.MatcherTestUtils.getDescription;
import static androidx.test.espresso.matcher.MatcherTestUtils.getMismatchDescription;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.hamcrest.Description;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link BoundedDiagnosingMatcher} */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class BoundedDiagnosingMatcherTest {

  @Test
  public void superClassOnly_description() {
    assertThat(
        getDescription(buildMatcher(ClassA.class)), is("an instance of " + ClassA.class.getName()));
  }

  @Test
  public void superClassOnly_matchesExactType() {
    assertTrue(buildMatcher(ClassA.class).matches(new ClassA()));
  }

  @Test
  public void superClassOnly_matchesSuperType() {
    // ClassA is a BaseTestClass.
    assertTrue(buildMatcher(BaseTestClass.class).matches(new ClassA()));
  }

  @Test
  public void superClassOnly_doesNotMatchNonSuperType() {
    // ClassA is not a ClassB.
    assertFalse(buildMatcher(ClassB.class).matches(new ClassA()));
  }

  @Test
  public void superClassOnly_doesNotMatchNonSuperType_describeMismatch() {
    assertThat(
        getMismatchDescription(buildMatcher(ClassB.class), new ClassA()),
        is("<" + ClassA.class.getSimpleName() + "> is a " + ClassA.class.getName()));
  }

  @Test
  public void superClassAndInterfaces_description() {
    assertThat(
        getDescription(buildMatcher(ClassA.class, TestInterface1.class)),
        is(
            "(an instance of "
                + ClassA.class.getName()
                + " and an instance of "
                + TestInterface1.class.getName()
                + ")"));
  }

  @Test
  public void superClassAndInterface_matchesExactTypes() {
    assertTrue(buildMatcher(ClassB.class, TestInterface1.class).matches(new ClassB()));
  }

  @Test
  public void superClassAndInterface_matchesOnlyBaseClass() {
    assertTrue(buildMatcher(BaseTestClass.class).matches(new ClassB()));
  }

  @Test
  public void superClassAndInterface_matchesOnlyInterface() {
    assertTrue(buildMatcher(Object.class, TestInterface1.class).matches(new ClassB()));
  }

  @Test
  public void superClassAndInterface_failsMatchOnBaseClass() {
    assertFalse(buildMatcher(ClassA.class, TestInterface1.class).matches(new ClassB()));
  }

  @Test
  public void superClassAndInterface_failsMatchOnBaseClass_describeMismatch() {
    assertThat(
        getMismatchDescription(buildMatcher(ClassA.class, TestInterface1.class), new ClassB()),
        is(
            "an instance of "
                + ClassA.class.getName()
                + " <ClassB> is a "
                + ClassB.class.getName()));
  }

  @Test
  public void superClassAndInterface_failsMatchOnInterface() {
    assertFalse(buildMatcher(ClassB.class, TestInterface2.class).matches(new ClassB()));
  }

  @Test
  public void superClassAndInterface_failsMatchOnInterface_describeMismatch() {
    assertThat(
        getMismatchDescription(buildMatcher(ClassB.class, TestInterface2.class), new ClassB()),
        is(
            "an instance of "
                + TestInterface2.class.getName()
                + " <ClassB> is a "
                + ClassB.class.getName()));
  }

  private static class BaseTestClass {
    @Override
    public String toString() {
      return this.getClass().getSimpleName();
    }
  }

  private interface TestInterface1 {}

  private interface TestInterface2 {}

  private static class ClassA extends BaseTestClass {}

  private static class ClassB extends BaseTestClass implements TestInterface1 {}

  private static <S, T extends S> BoundedDiagnosingMatcher<S, T> buildMatcher(
      Class<? extends S> clazz) {
    return new BoundedDiagnosingMatcher<S, T>(clazz) {
      @Override
      protected boolean matchesSafely(T item, Description mismatchDescription) {
        return true;
      }

      @Override
      protected void describeMoreTo(Description description) {}
    };
  }

  private static <S, T extends S> BoundedDiagnosingMatcher<S, T> buildMatcher(
      Class<? extends S> clazz, Class<?> interfaceType1, Class<?>... otherInterfaces) {
    return new BoundedDiagnosingMatcher<S, T>(clazz, interfaceType1, otherInterfaces) {
      @Override
      protected boolean matchesSafely(T item, Description mismatchDescription) {
        return true;
      }

      @Override
      protected void describeMoreTo(Description description) {}
    };
  }
}
