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

import static kotlin.collections.CollectionsKt.listOf;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import androidx.test.espresso.proto.TestProtos.GenericTestProto;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link BuilderReflector} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class BuilderReflectorTest {

  private static final String ANY_LIST_METHOD_SUFFIX = "SomeAnyList";
  private static final String ANY_VALUE_METHOD_SUFFIX = "SomeAnyValue";
  private static final String VALUE_METHOD_SUFFIX = "SomeValue";

  private static final String ANY_TYPE_VALUE_ONE = "one";
  private static final String ANY_TYPE_VALUE_TWO = "two";
  private static final String ANY_TYPE_VALUE_THREE = "three";

  private static final String ANY_TYPE_URL_ONE = "any.type.url.".concat(ANY_TYPE_VALUE_ONE);
  private static final String ANY_TYPE_URL_TWO = "any.type.url.".concat(ANY_TYPE_VALUE_TWO);
  private static final String ANY_TYPE_URL_THREE = "any.type.url.".concat(ANY_TYPE_VALUE_THREE);

  private static final String STRING_VALUE = "stringValue";

  private BuilderReflector builderReflector;

  private static Any buildAny(String anyTypeUrlOne, String payload) {
    return Any.newBuilder()
        .setTypeUrl(anyTypeUrlOne)
        .setValue(ByteString.copyFromUtf8(payload))
        .build();
  }

  @Before
  public void init() {
    builderReflector = new BuilderReflector(GenericTestProto.Builder.class, GenericTestProto.class);
    assertThat(
        builderReflector.builderInstance,
        allOf(notNullValue(), instanceOf(GenericTestProto.Builder.class)));
  }

  @Test
  public void setValuesOnBuilder() {
    Any any1 = buildAny(ANY_TYPE_URL_ONE, ANY_TYPE_VALUE_ONE);
    Any any2 = buildAny(ANY_TYPE_URL_TWO, ANY_TYPE_VALUE_TWO);
    Any any3 = buildAny(ANY_TYPE_URL_THREE, ANY_TYPE_VALUE_THREE);

    Any anyValue = any1;
    List<Any> anyList = listOf(any2, any3);
    ByteString value = TypeProtoConverters.typeToByteString(STRING_VALUE);

    Object protoMsg =
        builderReflector
            .invokeAddAllAnyList(ANY_LIST_METHOD_SUFFIX, anyList)
            .invokeSetAnyValue(ANY_VALUE_METHOD_SUFFIX, anyValue)
            .invokeSetByteStringValue(VALUE_METHOD_SUFFIX, value)
            .invokeBuild();

    assertThat(protoMsg, instanceOf(GenericTestProto.class));

    GenericTestProto genericProtoMsg = (GenericTestProto) protoMsg;

    assertThat(genericProtoMsg.getSomeAnyListCount(), is(2));
    assertThat(genericProtoMsg.getSomeAnyListList(), contains(any2, any3));
    assertThat(
        genericProtoMsg.getSomeAnyListList().get(0).getTypeUrl(), equalTo(any2.getTypeUrl()));
    assertThat(
        genericProtoMsg.getSomeAnyListList().get(1).getTypeUrl(), equalTo(any3.getTypeUrl()));
    assertThat(genericProtoMsg.getSomeAnyValue(), equalTo(any1));
    assertThat(genericProtoMsg.getSomeValue(), equalTo(value));
  }

  @Test
  public void failingToProvideArgs_invokeAddAllAnyList_Throws() {
    Object[] emptyArgs = new Object[0];
    String emptyString = "";
    try {
      builderReflector.invokeAddAllAnyList(emptyString, emptyArgs);
      fail("expected IllegalStateException");
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void failingToProvideArgs_invokeSetAnyValue_Throws() {
    Object[] emptyArgs = new Object[0];
    String emptyString = "";
    try {
      builderReflector.invokeSetAnyValue(emptyString, emptyArgs);
      fail("expected IllegalStateException");
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void failingToProvideArgs_invokeSetByteStringValue_Throws() {
    Object[] emptyArgs = new Object[0];
    String emptyString = "";
    try {
      builderReflector.invokeSetByteStringValue(emptyString, emptyArgs);
      fail("expected IllegalStateException");
    } catch (IllegalStateException expected) {
    }
  }
}
