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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class MethodInvocationTest {

  @Before
  public void invalidateConstructorCache() {
    MethodInvocation.invalidateCache();
  }

  @Test
  public void noParamDeclaredMethodInvocation() {
    MethodInvocation methodInvocation =
        new MethodInvocation(
            MethodInvocationType.class, new MethodInvocationType(), "noParamMethod");
    assertThat(methodInvocation.invokeDeclaredMethod(), equalTo(MethodInvocationType.FIELD_VALUE1));
  }

  @Test
  public void singleParamDeclaredMethodInvocation() {
    MethodInvocation methodInvocation =
        new MethodInvocation(
            MethodInvocationType.class, new MethodInvocationType(), "paramMethod", Object.class);
    assertThat(
        methodInvocation.invokeDeclaredMethod(MethodInvocationType.PARAM_VALUE),
        equalTo(MethodInvocationType.PARAM_VALUE));
  }

  @Test
  public void multipleParamDeclaredMethodInvocation() {
    MethodInvocation methodInvocation =
        new MethodInvocation(
            MethodInvocationType.class,
            new MethodInvocationType(),
            "paramMethod",
            Object.class,
            Object.class);
    boolean returnValue =
        (boolean)
            methodInvocation.invokeDeclaredMethod(
                MethodInvocationType.PARAM_VALUE, MethodInvocationType.PARAM_VALUE);
    assertThat(returnValue, is(true));
  }

  @Test
  public void invokeMethodCallsMethodOnSuperClass() {
    MethodInvocation methodInvocation =
        new MethodInvocation(MethodInvocationType.class, new MethodInvocationType(), "superMethod");
    assertThat(methodInvocation.invokeMethod(), equalTo(MethodInvocationType.SUPER_FIELD_VALUE1));
  }

  @Test
  public void privateMethodsAreMadeAccessibleBeforeInvocation() {
    MethodInvocation methodInvocation =
        new MethodInvocation(
            MethodInvocationType.class, new MethodInvocationType(), "privateMethod");
    assertThat(methodInvocation.invokeDeclaredMethod(), equalTo(MethodInvocationType.FIELD_VALUE1));
  }

  @Test
  public void voidMethodInvocation() {
    MethodInvocation methodInvocation =
        new MethodInvocation(MethodInvocationType.class, new MethodInvocationType(), "voidMethod");
    assertThat(methodInvocation.invokeDeclaredMethod(), nullValue());
  }

  private static final class MethodInvocationType extends MethodInvocationSuperType {
    static final Object FIELD_VALUE1 = "field1";
    static final Object PARAM_VALUE = "param";

    private final Object field1 = FIELD_VALUE1;

    Object noParamMethod() {
      return field1;
    }

    void voidMethod() {
      // no-op
    }

    Object paramMethod(Object param1) {
      return param1;
    }

    // Overloaded
    boolean paramMethod(Object param1, Object param2) {
      return param1.equals(param2);
    }

    @SuppressWarnings("unused") // used reflectively
    private Object privateMethod() {
      return field1;
    }
  }

  private static class MethodInvocationSuperType {
    static final Object SUPER_FIELD_VALUE1 = "superField1";

    public Object superMethod() {
      return SUPER_FIELD_VALUE1;
    }
  }
}
