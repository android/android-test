/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.test.espresso.web.model;

import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptor;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import androidx.test.espresso.web.model.Atoms.CastOrDieAtom;
import androidx.test.espresso.web.model.Atoms.ScriptWithArgsSimpleAtom;
import androidx.test.espresso.web.proto.model.WebModelAtoms.CastOrDieAtomProto;
import androidx.test.espresso.web.proto.model.WebModelAtoms.ElementReferenceProto;
import androidx.test.espresso.web.proto.model.WebModelAtoms.ScriptWithArgsSimpleAtomProto;
import androidx.test.espresso.web.proto.model.WebModelAtoms.TransformingAtomProto;
import androidx.test.espresso.web.proto.model.WebModelAtoms.WindowReferenceProto;
import java.util.Arrays;

/** Registers all supported Espresso remote web models with the {@link RemoteDescriptorRegistry}. */
public class RemoteWebModelAtoms {
  public static void init(RemoteDescriptorRegistry remoteDescriptorRegistry) {
    remoteDescriptorRegistry.registerRemoteTypeArgs(
        Arrays.asList(
            new RemoteDescriptor.Builder()
                .setInstanceType(CastOrDieAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(CastOrDieAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(ElementReference.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(ElementReferenceProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WindowReference.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WindowReferenceProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(ScriptWithArgsSimpleAtom.class)
                .setRemoteType(ScriptWithArgsSimpleAtomRemoteMessage.class)
                .setProtoType(ScriptWithArgsSimpleAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(TransformingAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(TransformingAtomProto.class)
                .build()));
  }
}
