/*
 * Copyright (C) 2016 The Android Open Source Project
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

package androidx.test.espresso.matcher;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import androidx.test.espresso.proto.matcher13.HamcrestMatchersv13.AllOfProto;
import androidx.test.espresso.proto.matcher13.HamcrestMatchersv13.AnyOfProto;
import androidx.test.espresso.proto.matcher13.HamcrestMatchersv13.IsEqualProto;
import androidx.test.espresso.proto.matcher13.HamcrestMatchersv13.IsInstanceOfProto;
import androidx.test.espresso.proto.matcher13.HamcrestMatchersv13.IsNotProto;
import androidx.test.espresso.proto.matcher13.HamcrestMatchersv13.IsNullProto;
import androidx.test.espresso.proto.matcher13.HamcrestMatchersv13.IsProto;
import androidx.test.espresso.proto.matcher13.HamcrestMatchersv13.StringContainsProto;
import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.google.common.collect.Lists;
import org.hamcrest.Matcher;
import org.hamcrest.core.AllOf;
import org.hamcrest.core.AnyOf;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Remote message transformation related test for all matchers under {@link
 * RemoteHamcrestCoreMatchers13}
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class RemoteHamcrestCoreMatcher13Test {

  @Before
  public void registerMatcherWithRegistry() {
    RemoteHamcrestCoreMatchers13.init(RemoteDescriptorRegistry.getInstance());
  }

  @Test
  public void isEqual_transformationToProto() {
    IsEqual<Integer> isEqual = new IsEqual<>(5);
    GenericRemoteMessage isEqualRemoteMessage = new GenericRemoteMessage(isEqual);
    IsEqualProto isEqualProto = (IsEqualProto) isEqualRemoteMessage.toProto();

    assertThat(isEqualProto.getExpectedValue(), notNullValue());
  }

  @Test
  public void isEqual_transformationFromProto() {
    IsEqual isEqual = new IsEqual(5);
    GenericRemoteMessage isEqualRemoteMessage = new GenericRemoteMessage(isEqual);
    IsEqualProto isEqualProto = (IsEqualProto) isEqualRemoteMessage.toProto();
    IsEqual isEqualFromProto = (IsEqual) GenericRemoteMessage.FROM.fromProto(isEqualProto);

    assertThat(5, isEqualFromProto);
  }

  @Test
  public void is_transformationToProto() {
    IsEqual<Integer> nestedMatcher = new IsEqual<>(5);
    Is<Integer> isMatcher = new Is<>(nestedMatcher);
    GenericRemoteMessage isMatcherRemoteMessage = new GenericRemoteMessage(isMatcher);
    IsProto isMatcherProto = (IsProto) isMatcherRemoteMessage.toProto();

    assertThat(isMatcherProto.getMatcher(), notNullValue());
  }

  @Test
  public void is_transformationFromProto() {
    IsEqual<Integer> nestedMatcher = new IsEqual<>(5);
    Is<Integer> isMatcher = new Is<>(nestedMatcher);

    GenericRemoteMessage isMatcherRemoteMessage = new GenericRemoteMessage(isMatcher);
    IsProto isMatcherProto = (IsProto) isMatcherRemoteMessage.toProto();
    Is isMatcherFromProto = (Is) GenericRemoteMessage.FROM.fromProto(isMatcherProto);

    assertThat(5, isMatcherFromProto);
  }

  @Test
  public void anyOf_transformationToProto() {
    Matcher isEqualInteger = new IsEqual<>(5);
    Matcher isEqualInteger2 = new IsEqual<>(3);

    AnyOf<Matcher> anyOfMatcher =
        new AnyOf<Matcher>(Lists.newArrayList(isEqualInteger, isEqualInteger2));
    GenericRemoteMessage anyOfMatcherRemoteMessage = new GenericRemoteMessage(anyOfMatcher);
    AnyOfProto anyOfMatcherMatcherProto = (AnyOfProto) anyOfMatcherRemoteMessage.toProto();

    assertThat(anyOfMatcherMatcherProto.getMatchersCount(), equalTo(2));
    assertThat(anyOfMatcherMatcherProto.getMatchersList(), notNullValue());
  }

  @Test
  public void anyOf_transformationFromProto() {
    Matcher isEqualInteger = new IsEqual<>(5);
    Matcher isEqualInteger2 = new IsEqual<>(3);

    AnyOf<Matcher> anyOfMatcher =
        new AnyOf<Matcher>(Lists.newArrayList(isEqualInteger, isEqualInteger2));
    GenericRemoteMessage anyOfMatcherRemoteMessage = new GenericRemoteMessage(anyOfMatcher);
    AnyOfProto anyOfMatcherMatcherProto = (AnyOfProto) anyOfMatcherRemoteMessage.toProto();
    Matcher<Integer> anyOfMatcherFromProto =
        (Matcher<Integer>) GenericRemoteMessage.FROM.fromProto(anyOfMatcherMatcherProto);

    assertThat(5, anyOfMatcherFromProto);
    assertThat(3, anyOfMatcherFromProto);
  }

  @Test
  public void allOf_transformationToProto() {
    Matcher isEqualInteger = new IsEqual<>(5);
    Matcher isEqualInteger2 = new IsEqual<>(5);

    AllOf<Matcher> allOfMatcher =
        new AllOf<Matcher>(Lists.newArrayList(isEqualInteger, isEqualInteger2));
    GenericRemoteMessage allOfMatcherRemoteMessage = new GenericRemoteMessage(allOfMatcher);
    AllOfProto allOfMatcherMatcherProto = (AllOfProto) allOfMatcherRemoteMessage.toProto();

    assertThat(allOfMatcherMatcherProto.getMatchersCount(), equalTo(2));
    assertThat(allOfMatcherMatcherProto.getMatchersList(), notNullValue());
  }

  @Test
  public void allOf_transformationFromProto() {
    Matcher<Integer> isEqualInteger = equalTo(5);
    Matcher<Integer> isEqualInteger2 = equalTo(5);

    Matcher<Integer> allOfMatcher = allOf(isEqualInteger, isEqualInteger2);
    GenericRemoteMessage allOfMatcherRemoteMessage = new GenericRemoteMessage(allOfMatcher);
    AllOfProto allOfMatcherMatcherProto = (AllOfProto) allOfMatcherRemoteMessage.toProto();
    AllOf<Matcher> allOfMatcherFromProto =
        (AllOf<Matcher>) GenericRemoteMessage.FROM.fromProto(allOfMatcherMatcherProto);
    assertThat(allOfMatcherFromProto.matches(5), is(true));
  }

  @Test
  public void isInstanceOf_transformationToProto() {
    IsInstanceOf isInstanceOfMatcher = new IsInstanceOf(String.class);
    GenericRemoteMessage isInstanceOfMatcherRemoteMsg =
        new GenericRemoteMessage(isInstanceOfMatcher);
    IsInstanceOfProto isInstanceOfMatcherProto =
        (IsInstanceOfProto) isInstanceOfMatcherRemoteMsg.toProto();
    assertThat(isInstanceOfMatcherProto.getExpectedClass(), notNullValue());
  }

  @Test
  public void isInstanceOf_transformationFromProto() {
    String expected = "macchiato";

    IsInstanceOf isInstanceOfMatcher = new IsInstanceOf(String.class);
    GenericRemoteMessage isInstanceOfMatcherRemoteMsg =
        new GenericRemoteMessage(isInstanceOfMatcher);
    IsInstanceOfProto isInstanceOfMatcherProto =
        (IsInstanceOfProto) isInstanceOfMatcherRemoteMsg.toProto();
    IsInstanceOf isInstanceOfMatcherFromProto =
        (IsInstanceOf) GenericRemoteMessage.FROM.fromProto(isInstanceOfMatcherProto);

    assertThat(expected, isInstanceOfMatcherFromProto);
  }

  @Test
  public void isNull_transformationToProto() {
    IsNull isNotNull = new IsNull();
    GenericRemoteMessage isNullMatcherRemoteMsg = new GenericRemoteMessage(isNotNull);
    IsNullProto isNullMatcherProto = (IsNullProto) isNullMatcherRemoteMsg.toProto();

    assertThat(isNullMatcherProto, notNullValue());
  }

  @Test
  public void isNull_transformationFromProto() {
    IsNull isNullMatcher = new IsNull();
    GenericRemoteMessage isNullMatcherRemoteMsg = new GenericRemoteMessage(isNullMatcher);
    IsNullProto isNullMatcherProto = (IsNullProto) isNullMatcherRemoteMsg.toProto();
    IsNull isNullMatcherFromProto =
        (IsNull) GenericRemoteMessage.FROM.fromProto(isNullMatcherProto);

    assertThat(null, isNullMatcherFromProto);
  }

  @Test
  public void isNot_transformationToProto() {
    IsNot isNotMatcher = new IsNot<Object>(is("foo"));

    GenericRemoteMessage isNotMatcherRemoteMsg = new GenericRemoteMessage(isNotMatcher);
    IsNotProto isNotMatcherProto = (IsNotProto) isNotMatcherRemoteMsg.toProto();

    assertThat(isNotMatcherProto, notNullValue());
  }

  @Test
  public void isNot_transformationFromProto() {
    String expected = "test";

    IsNot isNotMatcher = new IsNot<Object>(is(expected));
    GenericRemoteMessage isNotMatcherRemoteMsg = new GenericRemoteMessage(isNotMatcher);
    IsNotProto isNotMatcherProto = (IsNotProto) isNotMatcherRemoteMsg.toProto();
    IsNot isNotMatcherFromProto = (IsNot) GenericRemoteMessage.FROM.fromProto(isNotMatcherProto);

    assertThat(is(expected), isNotMatcherFromProto);
  }

  @Test
  public void stringContains_transformationToProto() {
    StringContains stringContainsMatcher = new StringContains("hello");

    GenericRemoteMessage stringContainsRemoteMsg = new GenericRemoteMessage(stringContainsMatcher);
    StringContainsProto stringContainsProto =
        (StringContainsProto) stringContainsRemoteMsg.toProto();

    assertThat(stringContainsProto, notNullValue());
  }

  @Test
  public void stringContains_transformationFromProto() {
    StringContains stringContainsMatcher = new StringContains("hello");

    GenericRemoteMessage stringContainsRemoteMsg = new GenericRemoteMessage(stringContainsMatcher);
    StringContainsProto stringContainsProto =
        (StringContainsProto) stringContainsRemoteMsg.toProto();

    StringContains stringContainsFromProto =
        (StringContains) GenericRemoteMessage.FROM.fromProto(stringContainsProto);

    assertThat("hello", stringContainsFromProto);
  }
}
