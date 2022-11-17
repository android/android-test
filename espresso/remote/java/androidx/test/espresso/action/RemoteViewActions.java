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
package androidx.test.espresso.action;

import androidx.test.espresso.proto.action.ViewActions.AdapterDataLoaderActionProto;
import androidx.test.espresso.proto.action.ViewActions.ClickViewActionProto;
import androidx.test.espresso.proto.action.ViewActions.CloseKeyboardActionProto;
import androidx.test.espresso.proto.action.ViewActions.EditorActionProto;
import androidx.test.espresso.proto.action.ViewActions.EspressoKeyProto;
import androidx.test.espresso.proto.action.ViewActions.KeyEventActionProto;
import androidx.test.espresso.proto.action.ViewActions.PressBackActionProto;
import androidx.test.espresso.proto.action.ViewActions.ReplaceTextActionProto;
import androidx.test.espresso.proto.action.ViewActions.TranslatedCoordinatesProviderProto;
import androidx.test.espresso.proto.action.ViewActions.TypeTextActionProto;
import androidx.test.espresso.remote.FieldDescriptor;
import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptor;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import java.util.Arrays;

/**
 * Registers all supported Espresso remote {@link ViewActions} with the {@link
 * RemoteDescriptorRegistry}.
 */
public final class RemoteViewActions {

  public static void init(RemoteDescriptorRegistry remoteDescriptorRegistry) {
    remoteDescriptorRegistry.registerRemoteTypeArgs(
        Arrays.asList(
            new RemoteDescriptor.Builder()
                .setInstanceType(GeneralClickAction.class)
                .setRemoteType(GeneralClickActionRemoteMessage.class)
                .setProtoType(ClickViewActionProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(AdapterDataLoaderAction.class)
                .setRemoteType(AdapterDataLoaderActionRemoteMsg.class)
                .setProtoType(AdapterDataLoaderActionProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(CloseKeyboardAction.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(CloseKeyboardActionProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(TypeTextAction.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(TypeTextActionProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(ReplaceTextAction.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(ReplaceTextActionProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(EditorAction.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(EditorActionProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(KeyEventAction.class)
                // TODO(b/35108759): replace with remote annotation instead
                .setInstanceFieldDescriptors(
                    FieldDescriptor.of(EspressoKey.class, "espressoKey", 0))
                .setRemoteConstrTypes(EspressoKey.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(KeyEventActionProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(PressBackAction.class)
                // TODO(b/35108759): replace with remote annotation instead
                .setInstanceFieldDescriptors(
                    FieldDescriptor.of(EspressoKey.class, "espressoKey", 0))
                .setInstanceFieldDescriptors(FieldDescriptor.of(boolean.class, "conditional", 1))
                .setRemoteConstrTypes(boolean.class, EspressoKey.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(PressBackActionProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(EspressoKey.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(EspressoKeyProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(TranslatedCoordinatesProvider.class)
                .setRemoteType(TranslatedCoordinatesProviderRemoteMessage.class)
                .setProtoType(TranslatedCoordinatesProviderProto.class)
                .build()));
  }

  private RemoteViewActions() {
    // noOp instance
  }
}
