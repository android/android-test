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

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import androidx.test.espresso.proto.TestProtos.RemoteMsgFieldAnnotatedFieldsClassProto;
import androidx.test.espresso.proto.TestProtos.TestProto;
import androidx.test.espresso.remote.RemoteDescriptor.Builder;
import androidx.test.espresso.remote.TestTypes.RemoteMsgFieldAnnotatedFieldsClass;
import androidx.test.espresso.remote.TestTypes.TestType;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.google.protobuf.Parser;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link RemoteDescriptor} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class RemoteDescriptorTest {

  private static final FieldDescriptor FIELD_DESCRIPTOR_TEST_TYPE1 =
      FieldDescriptor.of(String.class, "hello", 0);
  private static final FieldDescriptor FIELD_DESCRIPTOR_TEST_TYPE2 =
      FieldDescriptor.of(String.class, "hello1", 1);

  private static final FieldDescriptor FIELD_DESCRIPTOR_FIELD_DESCR_ANNOTATED1 =
      FieldDescriptor.of(String.class, "aString", 0);
  private static final FieldDescriptor FIELD_DESCRIPTOR_FIELD_DESCR_ANNOTATED2 =
      FieldDescriptor.of(TestType.class, "anyRegisteredType", 1);
  private static final FieldDescriptor FIELD_DESCRIPTOR_FIELD_DESCR_ANNOTATED3 =
      FieldDescriptor.of(Iterable.class, "anyTypeIterable", 2);

  @Test
  public void setFieldPropertyList_setsFieldPropertiesOnBuilder() {
    RemoteDescriptor remoteDescriptor =
        new Builder()
            .setInstanceType(TestType.class)
            .setInstanceFieldDescriptors(FIELD_DESCRIPTOR_TEST_TYPE1, FIELD_DESCRIPTOR_TEST_TYPE2)
            .setRemoteType(GenericRemoteMessage.class)
            .setProtoType(TestProto.class)
            .setProtoParser(TestProto.parser())
            .build();

    assertThat(remoteDescriptor.getInstanceFieldDescriptorList().size(), equalTo(2));
    assertThat(
        remoteDescriptor.getInstanceFieldDescriptorList(),
        contains(FIELD_DESCRIPTOR_TEST_TYPE1, FIELD_DESCRIPTOR_TEST_TYPE2));
  }

  @Test
  public void setNoFieldPropertyList_usesEmptyList() {
    RemoteDescriptor remoteDescriptor =
        new Builder()
            .setInstanceType(TestType.class)
            .setRemoteType(GenericRemoteMessage.class)
            .setProtoType(TestProto.class)
            .setProtoParser(TestProto.parser())
            .build();

    assertThat(remoteDescriptor.getInstanceFieldDescriptorList().size(), equalTo(0));
  }

  @Test
  public void remoteMsgFieldAnnotations_takePrecedenceOverSetFieldDescriptors() {
    RemoteDescriptor remoteDescriptor =
        new Builder()
            .setInstanceType(RemoteMsgFieldAnnotatedFieldsClass.class)
            // Set some fake field descriptors to see if they are overridden by annotated fields
            // declared in RemoteMsgFieldAnnotatedFieldsClass.class
            .setInstanceFieldDescriptors(FIELD_DESCRIPTOR_TEST_TYPE1, FIELD_DESCRIPTOR_TEST_TYPE2)
            .setRemoteType(GenericRemoteMessage.class)
            .setProtoType(RemoteMsgFieldAnnotatedFieldsClassProto.class)
            .setProtoParser(RemoteMsgFieldAnnotatedFieldsClassProto.parser())
            .build();

    assertThat(remoteDescriptor.getInstanceFieldDescriptorList().size(), equalTo(3));
    assertThat(
        remoteDescriptor.getInstanceFieldDescriptorList(),
        contains(
            FIELD_DESCRIPTOR_FIELD_DESCR_ANNOTATED1,
            FIELD_DESCRIPTOR_FIELD_DESCR_ANNOTATED2,
            FIELD_DESCRIPTOR_FIELD_DESCR_ANNOTATED3));
  }

  @Test
  public void fieldPropertyAnnotations_takePrecedenceOverDefaultEmptyFDsList() {
    RemoteDescriptor remoteDescriptor =
        new Builder()
            .setInstanceType(RemoteMsgFieldAnnotatedFieldsClass.class)
            // Set some fake field descriptors to see if they are overridden by annotated fields
            // declared in RemoteMsgFieldAnnotatedFieldsClass.class
            .setInstanceFieldDescriptors(FIELD_DESCRIPTOR_TEST_TYPE1, FIELD_DESCRIPTOR_TEST_TYPE2)
            .setRemoteType(GenericRemoteMessage.class)
            .setProtoType(RemoteMsgFieldAnnotatedFieldsClassProto.class)
            .setProtoParser(RemoteMsgFieldAnnotatedFieldsClassProto.parser())
            .build();

    assertThat(remoteDescriptor.getInstanceFieldDescriptorList().size(), equalTo(3));
    assertThat(
        remoteDescriptor.getInstanceFieldDescriptorList(),
        contains(
            FIELD_DESCRIPTOR_FIELD_DESCR_ANNOTATED1,
            FIELD_DESCRIPTOR_FIELD_DESCR_ANNOTATED2,
            FIELD_DESCRIPTOR_FIELD_DESCR_ANNOTATED3));
  }

  @Test
  public void remoteConstructorTypes_UsingGenericRemoteMessageUsesObjectType() {
    RemoteDescriptor remoteDescriptor =
        new Builder()
            .setInstanceType(RemoteMsgFieldAnnotatedFieldsClass.class)
            .setRemoteType(GenericRemoteMessage.class)
            .setProtoType(RemoteMsgFieldAnnotatedFieldsClassProto.class)
            .setProtoParser(RemoteMsgFieldAnnotatedFieldsClassProto.parser())
            .build();

    Class<?>[] remoteConstrTypes = {Object.class};
    assertThat(remoteDescriptor.getRemoteConstrTypes().length, equalTo(1));
    assertThat(remoteDescriptor.getRemoteConstrTypes(), arrayContaining(remoteConstrTypes));
  }

  @Test
  public void remoteConstructorTypes_NotSetFallsBackToInstanceType() {
    RemoteDescriptor remoteDescriptor =
        new Builder()
            .setInstanceType(TestType.class)
            .setRemoteType(TestTypeRemoteMessage.class)
            .setProtoType(TestProto.class)
            .setProtoParser(TestProto.parser())
            .build();

    Class<?>[] remoteConstrTypes = {TestType.class};
    assertThat(remoteDescriptor.getRemoteConstrTypes().length, equalTo(1));
    assertThat(remoteDescriptor.getRemoteConstrTypes(), arrayContaining(remoteConstrTypes));
  }

  @Test
  public void remoteParserType_InferredFromProtoMsgType() {
    RemoteDescriptor remoteDescriptor =
        new Builder()
            .setInstanceType(TestType.class)
            .setRemoteType(TestTypeRemoteMessage.class)
            .setProtoType(TestProto.class)
            .build();

    assertThat(remoteDescriptor.getProtoParser(), allOf(notNullValue(), isA(Parser.class)));
  }
}
