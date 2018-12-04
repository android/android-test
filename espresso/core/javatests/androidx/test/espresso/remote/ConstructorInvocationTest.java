/*
 * Copyright (C) 2016 The Android Open Source Project
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
 *
 */
package androidx.test.espresso.remote;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import androidx.test.espresso.remote.TestTypes.TestAnnotation;
import androidx.test.espresso.remote.annotation.RemoteMsgConstructor;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ConstructorInvocationTest {

  private static final Object VALUE_1 = "value1";
  private static final Object VALUE_2 = "value2";

  private static final Object CONSTRUCTOR1 = "constructor1";
  private static final Object CONSTRUCTOR2 = "constructor2";

  @Before
  public void invalidateConstructorCache() {
    ConstructorInvocation.invalidateCache();
  }

  @Test
  public void invokeNoArgConstructor_ReturnsInstance() {
    ConstructorInvocation constructorInvocation =
        new ConstructorInvocation(NoArgConstructorClass.class, null);
    assertThat(constructorInvocation.invokeConstructor(), instanceOf(NoArgConstructorClass.class));
  }

  @Test
  public void invokeSingleArgConstructor_ReturnsInstance() {
    ConstructorInvocation constructorInvocation =
        new ConstructorInvocation(SingleArgConstructorClass.class, null, Object.class);
    assertThat(
        constructorInvocation.invokeConstructor(VALUE_1),
        instanceOf(SingleArgConstructorClass.class));
    SingleArgConstructorClass instance =
        (SingleArgConstructorClass) constructorInvocation.invokeConstructor(VALUE_1);
    assertThat(instance.arg1, equalTo(VALUE_1));
  }

  @Test
  public void invokeMultipleArgConstructor_ReturnsInstance() {
    ConstructorInvocation constructorInvocation =
        new ConstructorInvocation(
            MultipleArgConstructorClass.class, null, Object.class, Object.class);
    assertThat(
        constructorInvocation.invokeConstructor(VALUE_1, VALUE_2),
        instanceOf(MultipleArgConstructorClass.class));
    MultipleArgConstructorClass instance =
        (MultipleArgConstructorClass) constructorInvocation.invokeConstructor(VALUE_1, VALUE_2);
    assertThat(instance.arg1, equalTo(VALUE_1));
    assertThat(instance.arg2, equalTo(VALUE_2));
  }

  @Test
  public void invokeOverloadedConstructor_ReturnsInstance() {
    OverloadedConstructorClass instance;

    ConstructorInvocation firstConstructor =
        new ConstructorInvocation(OverloadedConstructorClass.class, null, Object.class);
    assertThat(
        firstConstructor.invokeConstructor(VALUE_1), instanceOf(OverloadedConstructorClass.class));
    instance = (OverloadedConstructorClass) firstConstructor.invokeConstructor(VALUE_1);
    assertThat(instance.arg1, equalTo(VALUE_1));
    assertThat(instance.arg2, equalTo(VALUE_2));

    ConstructorInvocation secondConstructor =
        new ConstructorInvocation(
            OverloadedConstructorClass.class, null, Object.class, Object.class);
    assertThat(
        secondConstructor.invokeConstructor(VALUE_1, VALUE_2),
        instanceOf(OverloadedConstructorClass.class));
    instance = (OverloadedConstructorClass) secondConstructor.invokeConstructor(VALUE_1, VALUE_2);
    assertThat(instance.arg1, equalTo(VALUE_1));
    assertThat(instance.arg2, equalTo(VALUE_2));
  }

  @Test
  public void annotatedConstructor_ReturnsInstance() {
    ConstructorInvocation constructorInvocation =
        new ConstructorInvocation(AnnotatedConstructorClass.class, RemoteMsgConstructor.class);
    Object instance = constructorInvocation.invokeConstructor(VALUE_1);
    assertThat(instance, instanceOf(AnnotatedConstructorClass.class));
    AnnotatedConstructorClass annotatedConstructorClass =
        (AnnotatedConstructorClass) constructorInvocation.invokeConstructor(VALUE_1);
    assertThat(annotatedConstructorClass.result, equalTo(CONSTRUCTOR1));
  }

  @Test
  public void annotatedConstructorNotFound_FallsBackToVarArgsLookup() {
    ConstructorInvocation constructorInvocation =
        new ConstructorInvocation(
            AnnotatedConstructorClass.class,
            TestAnnotation.class, /* unknown annotation class */
            Object.class,
            Object.class);
    Object instance = constructorInvocation.invokeConstructor(VALUE_1, VALUE_2);
    assertThat(instance, instanceOf(AnnotatedConstructorClass.class));
    AnnotatedConstructorClass annotatedConstructorClass =
        (AnnotatedConstructorClass) constructorInvocation.invokeConstructor(VALUE_1, VALUE_2);
    assertThat(annotatedConstructorClass.result, equalTo(CONSTRUCTOR2));
  }

  private static class NoArgConstructorClass {
    public NoArgConstructorClass() {}
  }

  private static class SingleArgConstructorClass {
    final Object arg1;

    public SingleArgConstructorClass(Object arg1) {
      this.arg1 = arg1;
    }
  }

  private static class MultipleArgConstructorClass {
    final Object arg1;
    final Object arg2;

    public MultipleArgConstructorClass(Object arg1, Object arg2) {
      this.arg1 = arg1;
      this.arg2 = arg2;
    }
  }

  private static class OverloadedConstructorClass {
    final Object arg1;
    final Object arg2;

    public OverloadedConstructorClass(Object arg1) {
      this(arg1, VALUE_2);
    }

    public OverloadedConstructorClass(Object arg1, Object arg2) {
      this.arg1 = arg1;
      this.arg2 = arg2;
    }
  }

  private static class AnnotatedConstructorClass {
    final Object result;

    @RemoteMsgConstructor
    public AnnotatedConstructorClass(Object arg1) {
      this.result = CONSTRUCTOR1;
    }

    public AnnotatedConstructorClass(Object arg1, Object arg2) {
      this.result = CONSTRUCTOR2;
    }
  }
}
