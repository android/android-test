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
package androidx.test.espresso.matcher;

import androidx.test.espresso.proto.matcher13.HamcrestMatchersv13.AllOfProto;
import androidx.test.espresso.proto.matcher13.HamcrestMatchersv13.AnyOfProto;
import androidx.test.espresso.proto.matcher13.HamcrestMatchersv13.IsEqualProto;
import androidx.test.espresso.proto.matcher13.HamcrestMatchersv13.IsInstanceOfProto;
import androidx.test.espresso.proto.matcher13.HamcrestMatchersv13.IsNotProto;
import androidx.test.espresso.proto.matcher13.HamcrestMatchersv13.IsNullProto;
import androidx.test.espresso.proto.matcher13.HamcrestMatchersv13.IsProto;
import androidx.test.espresso.proto.matcher13.HamcrestMatchersv13.StringContainsProto;
import androidx.test.espresso.remote.FieldDescriptor;
import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptor;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import java.util.Arrays;
import org.hamcrest.Matcher;
import org.hamcrest.core.AllOf;
import org.hamcrest.core.AnyOf;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.hamcrest.core.StringContains;

/** Registers all supported hamcrest v1.3 {@link RemoteDescriptor}s. */
public final class RemoteHamcrestCoreMatchers13 {
  public static void init(RemoteDescriptorRegistry remoteDescriptorRegistry) {
    remoteDescriptorRegistry.registerRemoteTypeArgs(
        Arrays.asList(
            new RemoteDescriptor.Builder()
                .setInstanceType(IsEqual.class)
                .setInstanceFieldDescriptors(FieldDescriptor.of(Object.class, "expectedValue", 0))
                .setRemoteType(GenericRemoteMessage.class)
                .setRemoteConstrTypes(Object.class)
                .setProtoType(IsEqualProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(Is.class)
                .setInstanceFieldDescriptors(FieldDescriptor.of(Matcher.class, "matcher", 0))
                .setRemoteType(GenericRemoteMessage.class)
                .setRemoteConstrTypes(Matcher.class)
                .setProtoType(IsProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(AnyOf.class)
                .setInstanceFieldDescriptors(FieldDescriptor.of(Iterable.class, "matchers", 0))
                .setRemoteType(GenericRemoteMessage.class)
                .setRemoteConstrTypes(Iterable.class)
                .setProtoType(AnyOfProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(AllOf.class)
                .setInstanceFieldDescriptors(FieldDescriptor.of(Iterable.class, "matchers", 0))
                .setRemoteType(GenericRemoteMessage.class)
                .setRemoteConstrTypes(Iterable.class)
                .setProtoType(AllOfProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(IsInstanceOf.class)
                .setInstanceFieldDescriptors(FieldDescriptor.of(Class.class, "expectedClass", 0))
                .setRemoteType(GenericRemoteMessage.class)
                .setRemoteConstrTypes(Class.class)
                .setProtoType(IsInstanceOfProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(IsNull.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setRemoteConstrTypes(Class.class)
                .setProtoType(IsNullProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(IsNot.class)
                .setInstanceFieldDescriptors(FieldDescriptor.of(Matcher.class, "matcher", 0))
                .setRemoteType(GenericRemoteMessage.class)
                .setRemoteConstrTypes(Class.class)
                .setProtoType(IsNotProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(StringContains.class)
                .setInstanceFieldDescriptors(FieldDescriptor.of(String.class, "substring", 0))
                .setRemoteType(GenericRemoteMessage.class)
                .setRemoteConstrTypes(String.class)
                .setProtoType(StringContainsProto.class)
                .build()));
  }

  private RemoteHamcrestCoreMatchers13() {
    // noOp instance
  }
}
