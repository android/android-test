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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import androidx.test.espresso.proto.TestProtos.GenericTestProto;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ProtoReflector} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class ProtoReflectorTest {

  private static final String ANY_TYPE_VALUE_ONE = "one";
  private static final String ANY_TYPE_VALUE_TWO = "two";
  private static final String ANY_TYPE_VALUE_THREE = "three";

  private static final String ANY_TYPE_URL_ONE = "any.type.url.".concat(ANY_TYPE_VALUE_ONE);
  private static final String ANY_TYPE_URL_TWO = "any.type.url.".concat(ANY_TYPE_VALUE_TWO);
  private static final String ANY_TYPE_URL_THREE = "any.type.url.".concat(ANY_TYPE_VALUE_THREE);

  private static final String STRING_VALUE_ONE = "stringValueOne";

  private static Any buildAny(String anyTypeUrlOne, String payload) {
    return Any.newBuilder()
        .setTypeUrl(anyTypeUrlOne)
        .setValue(ByteString.copyFromUtf8(payload))
        .build();
  }

  @Test
  public void reflectivelyReadProto() {
    Any any1 = buildAny(ANY_TYPE_URL_ONE, ANY_TYPE_VALUE_ONE);
    Any any2 = buildAny(ANY_TYPE_URL_TWO, ANY_TYPE_VALUE_TWO);
    Any any3 = buildAny(ANY_TYPE_URL_THREE, ANY_TYPE_VALUE_THREE);

    Any anyValue1 = any1;
    Iterable anyList1 = listOf(any2, any3);
    ByteString value1 = TypeProtoConverters.typeToByteString(STRING_VALUE_ONE);

    ProtoReflector protoReflector =
        new ProtoReflector(
            GenericTestProto.class,
            GenericTestProto.newBuilder()
                .addAllSomeAnyList(anyList1)
                .setSomeAnyValue(anyValue1)
                .setSomeValue(value1)
                .build());

    assertThat(protoReflector.getAnyList("SomeAnyList"), contains(any2, any3));
    assertThat(protoReflector.getAnyValue("SomeAnyValue"), equalTo(anyValue1));
    assertThat(protoReflector.getByteStringValue("SomeValue"), equalTo(value1));
  }
}
