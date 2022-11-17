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

import static junit.framework.Assert.assertTrue;

import android.net.Uri;
import androidx.test.espresso.proto.TestProtos.MultipleFieldClassTestProto;
import androidx.test.espresso.proto.TestProtos.NoArgClassTestProto;
import androidx.test.espresso.proto.TestProtos.ParcelableFieldClassProto;
import androidx.test.espresso.proto.TestProtos.RemoteMsgFieldAnnotatedFieldsClassProto;
import androidx.test.espresso.proto.TestProtos.StubRootMatcherProto;
import androidx.test.espresso.proto.TestProtos.TestProto;
import androidx.test.espresso.remote.TestTypes.ParcelableFieldClass;
import androidx.test.espresso.remote.TestTypes.RemoteMsgFieldAnnotatedFieldsClass;
import androidx.test.espresso.remote.TestTypes.TestType;
import java.util.Arrays;

/** Initialises the {@link RemoteDescriptorRegistry} with test protos used in test code. */
class RemoteDescriptorRegistryInitializer {
  static RemoteDescriptorRegistry init() {
    return init(new RemoteDescriptorRegistry());
  }

  static RemoteDescriptorRegistry init(RemoteDescriptorRegistry remoteDescriptorRegistry) {
    assertTrue(
        remoteDescriptorRegistry.registerRemoteTypeArgs(
            Arrays.asList(
                new RemoteDescriptor.Builder()
                    .setInstanceType(TestType.class)
                    .setRemoteType(TestTypeRemoteMessage.class)
                    .setProtoType(TestProto.class)
                    .setProtoBuilderType(TestProto.Builder.class)
                    .setProtoParser(TestProto.parser())
                    .build(),
                new RemoteDescriptor.Builder()
                    .setInstanceType(TestTypes.MultipleFieldClass.class)
                    .setInstanceFieldDescriptors(
                        FieldDescriptor.of(byte.class, "aByte", 0),
                        FieldDescriptor.of(int.class, "anInt", 1),
                        FieldDescriptor.of(long.class, "aLong", 2),
                        FieldDescriptor.of(String.class, "aString", 3),
                        FieldDescriptor.of(TestType.class, "anyRegisteredType", 4),
                        FieldDescriptor.of(Iterable.class, "anyTypeIterable", 5))
                    .setRemoteType(GenericRemoteMessage.class)
                    .setProtoType(MultipleFieldClassTestProto.class)
                    .setProtoBuilderType(MultipleFieldClassTestProto.Builder.class)
                    .setProtoParser(MultipleFieldClassTestProto.parser())
                    .build(),
                new RemoteDescriptor.Builder()
                    .setInstanceType(TestTypes.NoArgClass.class)
                    .setRemoteType(GenericRemoteMessage.class)
                    .setProtoType(NoArgClassTestProto.class)
                    .setProtoBuilderType(NoArgClassTestProto.Builder.class)
                    .setProtoParser(NoArgClassTestProto.parser())
                    .build(),
                new RemoteDescriptor.Builder()
                    .setInstanceType(RemoteMsgFieldAnnotatedFieldsClass.class)
                    .setRemoteType(GenericRemoteMessage.class)
                    .setProtoType(RemoteMsgFieldAnnotatedFieldsClassProto.class)
                    .setProtoBuilderType(RemoteMsgFieldAnnotatedFieldsClassProto.Builder.class)
                    .setProtoParser(RemoteMsgFieldAnnotatedFieldsClassProto.parser())
                    .build(),
                new RemoteDescriptor.Builder()
                    .setInstanceType(StubRootMatcher.class)
                    .setRemoteType(GenericRemoteMessage.class)
                    .setProtoType(StubRootMatcherProto.class)
                    .build(),
                new RemoteDescriptor.Builder()
                    .setInstanceType(ParcelableFieldClass.class)
                    .setInstanceFieldDescriptors(FieldDescriptor.of(Uri.class, "uri", 0))
                    .setRemoteType(GenericRemoteMessage.class)
                    .setProtoType(ParcelableFieldClassProto.class)
                    .build())));
    return remoteDescriptorRegistry;
  }
}
