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

import static androidx.test.espresso.remote.TestTypes.ANY_TYPE_ITERABLE;
import static androidx.test.espresso.remote.TestTypes.AN_INT;
import static androidx.test.espresso.remote.TestTypes.A_BYTE;
import static androidx.test.espresso.remote.TestTypes.A_LONG;
import static androidx.test.espresso.remote.TestTypes.A_STRING;
import static androidx.test.espresso.remote.TestTypes.A_TEST_TYPE1;
import static androidx.test.espresso.remote.TestTypes.A_TEST_TYPE2;
import static androidx.test.espresso.remote.TestTypes.A_TEST_TYPE3;
import static androidx.test.espresso.remote.TestTypes.A_TEST_TYPE4;
import static androidx.test.espresso.remote.TypeProtoConverters.typeToAny;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import android.net.Uri;
import androidx.test.espresso.proto.TestProtos.MultipleFieldClassTestProto;
import androidx.test.espresso.proto.TestProtos.NoArgClassTestProto;
import androidx.test.espresso.proto.TestProtos.ParcelableFieldClassProto;
import androidx.test.espresso.proto.TestProtos.RemoteMsgFieldAnnotatedFieldsClassProto;
import androidx.test.espresso.remote.TestTypes.MultipleFieldClass;
import androidx.test.espresso.remote.TestTypes.NoArgClass;
import androidx.test.espresso.remote.TestTypes.ParcelableFieldClass;
import androidx.test.espresso.remote.TestTypes.RemoteMsgFieldAnnotatedFieldsClass;
import androidx.test.espresso.remote.TestTypes.TestType;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.google.protobuf.MessageLite;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link RemoteMessageSerializer} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class RemoteMessageSerializerTest {

  private RemoteDescriptorRegistry remoteDescriptorRegistry;

  @Before
  public void initRegistry() {
    remoteDescriptorRegistry = RemoteDescriptorRegistryInitializer.init();
  }

  @Test
  public void serializeMultipleFieldClassToProto() {
    MultipleFieldClass multipleFieldClass =
        new MultipleFieldClass(A_BYTE, AN_INT, A_LONG, A_STRING, A_TEST_TYPE1, ANY_TYPE_ITERABLE);
    RemoteMessageSerializer remoteMsgSerializer =
        new RemoteMessageSerializer(multipleFieldClass, remoteDescriptorRegistry);
    MessageLite multipleFieldClassProto = remoteMsgSerializer.toProto();
    assertThat(multipleFieldClassProto, notNullValue());
    assertThat(multipleFieldClassProto, instanceOf(MultipleFieldClassTestProto.class));

    MultipleFieldClassTestProto testProto = (MultipleFieldClassTestProto) multipleFieldClassProto;
    assertThat(TypeProtoConverters.<Byte>byteStringToType(testProto.getAByte()), equalTo(A_BYTE));
    assertThat(
        TypeProtoConverters.<Integer>byteStringToType(testProto.getAnInt()), equalTo(AN_INT));
    assertThat(TypeProtoConverters.<Long>byteStringToType(testProto.getALong()), equalTo(A_LONG));
    assertThat(
        TypeProtoConverters.<String>byteStringToType(testProto.getAString()), equalTo(A_STRING));
    assertThat(
        TypeProtoConverters.<TestType>anyToType(
            testProto.getAnyRegisteredType(), remoteDescriptorRegistry),
        equalTo(A_TEST_TYPE1));
    assertThat(
        testProto.getAnyTypeIterableList(),
        contains(
            typeToAny(A_TEST_TYPE1, remoteDescriptorRegistry),
            typeToAny(A_TEST_TYPE2, remoteDescriptorRegistry),
            typeToAny(A_TEST_TYPE3, remoteDescriptorRegistry),
            typeToAny(A_TEST_TYPE4, remoteDescriptorRegistry)));
  }

  @Test
  public void serializeNoArgClassToProto() {
    NoArgClass noArgClass = new NoArgClass();
    RemoteMessageSerializer remoteMsgSerializer =
        new RemoteMessageSerializer(noArgClass, remoteDescriptorRegistry);
    MessageLite noArgClassProto = remoteMsgSerializer.toProto();
    assertThat(noArgClassProto, notNullValue());
    assertThat(noArgClassProto, instanceOf(NoArgClassTestProto.class));
  }

  @Test
  public void serializeToRemoteMsgAnnotatedFieldsClassToProto() {
    RemoteMsgFieldAnnotatedFieldsClass remoteMsgAnnotatedFieldsClass =
        new RemoteMsgFieldAnnotatedFieldsClass(A_STRING, A_TEST_TYPE1, ANY_TYPE_ITERABLE);
    RemoteMessageSerializer remoteMsgSerializer =
        new RemoteMessageSerializer(remoteMsgAnnotatedFieldsClass, remoteDescriptorRegistry);

    MessageLite remoteMsgAnnotatedFieldsProto = remoteMsgSerializer.toProto();
    assertThat(remoteMsgAnnotatedFieldsProto, notNullValue());
    assertThat(
        remoteMsgAnnotatedFieldsProto, instanceOf(RemoteMsgFieldAnnotatedFieldsClassProto.class));

    RemoteMsgFieldAnnotatedFieldsClassProto testProto =
        (RemoteMsgFieldAnnotatedFieldsClassProto) remoteMsgAnnotatedFieldsProto;
    assertThat(
        TypeProtoConverters.<String>byteStringToType(testProto.getAString()), equalTo(A_STRING));
    assertThat(
        TypeProtoConverters.<TestType>anyToType(
            testProto.getAnyRegisteredType(), remoteDescriptorRegistry),
        equalTo(A_TEST_TYPE1));
    assertThat(
        testProto.getAnyTypeIterableList(),
        contains(
            typeToAny(A_TEST_TYPE1, remoteDescriptorRegistry),
            typeToAny(A_TEST_TYPE2, remoteDescriptorRegistry),
            typeToAny(A_TEST_TYPE3, remoteDescriptorRegistry),
            typeToAny(A_TEST_TYPE4, remoteDescriptorRegistry)));
  }

  @Test
  public void serializeParcelableFieldClass() {
    ParcelableFieldClass parcelableFieldClass = new ParcelableFieldClass(Uri.parse("foo:bar"));
    RemoteMessageSerializer remoteMsgSerializer =
        new RemoteMessageSerializer(parcelableFieldClass, remoteDescriptorRegistry);
    MessageLite remoteParcelableFieldProto = remoteMsgSerializer.toProto();

    assertThat(remoteParcelableFieldProto, notNullValue());
    assertThat(remoteParcelableFieldProto, instanceOf(ParcelableFieldClassProto.class));
  }
}
