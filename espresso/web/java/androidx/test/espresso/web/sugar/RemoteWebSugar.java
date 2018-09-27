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

package androidx.test.espresso.web.sugar;

import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptor;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import androidx.test.espresso.web.proto.sugar.WebSugar.ExceptionPropagatorProto;
import androidx.test.espresso.web.sugar.Web.WebInteraction.ExceptionPropagator;
import java.util.Arrays;

/** Registers all supported Espresso remote web sugar with the {@link RemoteDescriptorRegistry}. */
public final class RemoteWebSugar {

  public static void init(RemoteDescriptorRegistry remoteDescriptorRegistry) {
    remoteDescriptorRegistry.registerRemoteTypeArgs(
        Arrays.asList(
            new RemoteDescriptor.Builder()
                .setInstanceType(ExceptionPropagator.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(ExceptionPropagatorProto.class)
                .build()));
  }
}
