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

import static androidx.test.espresso.remote.TestTypes.AN_INT;
import static androidx.test.espresso.remote.TestTypes.A_BYTE;
import static androidx.test.espresso.remote.TestTypes.A_LONG;
import static androidx.test.espresso.remote.TestTypes.A_STRING;
import static androidx.test.espresso.remote.TestTypes.A_TEST_TYPE1;
import static androidx.test.espresso.remote.TestTypes.A_TEST_TYPE2;
import static androidx.test.espresso.remote.TestTypes.A_TEST_TYPE3;
import static androidx.test.espresso.remote.TestTypes.A_TEST_TYPE4;
import static androidx.test.espresso.remote.TypeProtoConverters.typeToAny;
import static androidx.test.espresso.remote.TypeProtoConverters.typeToByteString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import android.net.Uri;
import androidx.test.espresso.proto.TestProtos.MultipleFieldClassTestProto;
import androidx.test.espresso.proto.TestProtos.NoArgClassTestProto;
import androidx.test.espresso.proto.TestProtos.RemoteMsgFieldAnnotatedFieldsClassProto;
import androidx.test.espresso.remote.TestTypes.MultipleFieldClass;
import androidx.test.espresso.remote.TestTypes.NoArgClass;
import androidx.test.espresso.remote.TestTypes.ParcelableFieldClass;
import androidx.test.espresso.remote.TestTypes.RemoteMsgFieldAnnotatedFieldsClass;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.google.protobuf.MessageLite;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link RemoteMessageDeserializer} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class RemoteMessageDeserializerTest {

  private RemoteDescriptorRegistry remoteDescriptorRegistry;
  private RemoteMessageDeserializer remoteMessageDeserializer;

  private static MultipleFieldClassTestProto buildMultipleFieldClassTestProto(
      RemoteDescriptorRegistry remoteDescriptorRegistry) {
    return MultipleFieldClassTestProto.newBuilder()
        .setAByte(typeToByteString(A_BYTE))
        .setAnInt(typeToByteString(AN_INT))
        .setALong(typeToByteString(A_LONG))
        .setAString(typeToByteString(A_STRING))
        .setAnyRegisteredType(typeToAny(A_TEST_TYPE1, remoteDescriptorRegistry))
        .addAllAnyTypeIterable(
            Arrays.asList(
                typeToAny(A_TEST_TYPE1, remoteDescriptorRegistry),
                typeToAny(A_TEST_TYPE2, remoteDescriptorRegistry),
                typeToAny(A_TEST_TYPE3, remoteDescriptorRegistry),
                typeToAny(A_TEST_TYPE4, remoteDescriptorRegistry)))
        .build();
  }

  private static NoArgClassTestProto buildNoArgClassTestProto() {
    return NoArgClassTestProto.getDefaultInstance();
  }

  private static RemoteMsgFieldAnnotatedFieldsClassProto buildAnnotatedClassTestProto(
      RemoteDescriptorRegistry remoteDescriptorRegistry) {
    return RemoteMsgFieldAnnotatedFieldsClassProto.newBuilder()
        .setAString(typeToByteString(A_STRING))
        .setAnyRegisteredType(typeToAny(A_TEST_TYPE1, remoteDescriptorRegistry))
        .addAllAnyTypeIterable(
            Arrays.asList(
                typeToAny(A_TEST_TYPE1, remoteDescriptorRegistry),
                typeToAny(A_TEST_TYPE2, remoteDescriptorRegistry),
                typeToAny(A_TEST_TYPE3, remoteDescriptorRegistry),
                typeToAny(A_TEST_TYPE4, remoteDescriptorRegistry)))
        .build();
  }

  @Before
  public void initRegistry() {
    remoteDescriptorRegistry = RemoteDescriptorRegistryInitializer.init();
    remoteMessageDeserializer = new RemoteMessageDeserializer(remoteDescriptorRegistry);
  }

  @Test
  public void deserializeMultipleFieldClassFromProto() {
    MultipleFieldClassTestProto multipleFieldClassTestProto =
        buildMultipleFieldClassTestProto(remoteDescriptorRegistry);
    Object instance = remoteMessageDeserializer.fromProto(multipleFieldClassTestProto);

    assertThat(instance, notNullValue());
    assertThat(instance, instanceOf(MultipleFieldClass.class));

    MultipleFieldClass multipleFieldClassInstance = (MultipleFieldClass) instance;
    assertThat(multipleFieldClassInstance.aByte, equalTo(A_BYTE));
    assertThat(multipleFieldClassInstance.anInt, equalTo(AN_INT));
    assertThat(multipleFieldClassInstance.aLong, equalTo(A_LONG));
    assertThat(multipleFieldClassInstance.aString, equalTo(A_STRING));
  }

  @Test
  public void deserializeNoArgClassToProto() {
    NoArgClassTestProto noArgClassTestProto = buildNoArgClassTestProto();
    Object instance = remoteMessageDeserializer.fromProto(noArgClassTestProto);

    assertThat(instance, notNullValue());
    assertThat(instance, instanceOf(NoArgClass.class));
  }

  @Test
  public void deserializeAnnotatedClassToProto() {
    RemoteMsgFieldAnnotatedFieldsClassProto annotatedFieldsClassProto =
        buildAnnotatedClassTestProto(remoteDescriptorRegistry);
    Object instance = remoteMessageDeserializer.fromProto(annotatedFieldsClassProto);

    assertThat(instance, notNullValue());
    assertThat(instance, instanceOf(RemoteMsgFieldAnnotatedFieldsClass.class));

    RemoteMsgFieldAnnotatedFieldsClass annotatedFieldsClass =
        (RemoteMsgFieldAnnotatedFieldsClass) instance;
    assertThat(annotatedFieldsClass.aString, equalTo(A_STRING));
    assertThat(annotatedFieldsClass.anyRegisteredType, equalTo(A_TEST_TYPE1));
    assertThat(
        annotatedFieldsClass.anyTypeIterable,
        contains(A_TEST_TYPE1, A_TEST_TYPE2, A_TEST_TYPE3, A_TEST_TYPE4));
  }

  @Test
  public void deserializeParcelableFieldClass() {
    ParcelableFieldClass parcelableFieldClass = new ParcelableFieldClass(Uri.parse("foo:bar"));
    RemoteMessageSerializer remoteMsgSerializer =
        new RemoteMessageSerializer(parcelableFieldClass, remoteDescriptorRegistry);
    MessageLite remoteParcelableFieldProto = remoteMsgSerializer.toProto();

    Object instance = remoteMessageDeserializer.fromProto(remoteParcelableFieldProto);

    assertThat(instance, notNullValue());
    assertThat(instance, instanceOf(ParcelableFieldClass.class));

    parcelableFieldClass = (ParcelableFieldClass) instance;
    assertThat(parcelableFieldClass.uri, equalTo(Uri.parse("foo:bar")));
  }
}
