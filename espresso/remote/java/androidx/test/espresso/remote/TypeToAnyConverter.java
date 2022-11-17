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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import androidx.annotation.NonNull;
import androidx.test.espresso.remote.EspressoRemoteMessage.To;
import com.google.protobuf.Any;
import com.google.protobuf.MessageLite;

/** Converts a type T into its {@link Any} proto message representation. */
final class TypeToAnyConverter<T> implements Converter<T, Any> {
  private static final String TAG = "TypeToAnyConverter";

  private final RemoteDescriptorRegistry remoteDescriptorRegistry;

  TypeToAnyConverter(@NonNull RemoteDescriptorRegistry remoteDescriptorRegistry) {
    this.remoteDescriptorRegistry =
        checkNotNull(remoteDescriptorRegistry, "remoteDescriptorRegistry cannot be null!");
  }

  @Override
  public Any convert(@NonNull T instance) {
    checkNotNull(instance, "instance cannot be null!");
    RemoteDescriptor remoteDescriptor =
        remoteDescriptorRegistry.argForInstanceType(instance.getClass());
    MessageLite remoteProto = createProtoMsgForInstanceType(instance, remoteDescriptor);
    return Any.newBuilder()
        .setTypeUrl(remoteDescriptor.getInstanceTypeName())
        .setValue(remoteProto.toByteString())
        .build();
  }

  @SuppressWarnings("unchecked") // safe covariant cast, missing type arguments for generic class
  private MessageLite createProtoMsgForInstanceType(T instance, RemoteDescriptor remoteDescriptor) {
    Class<?> remoteType = remoteDescriptor.getRemoteType();
    To<MessageLite> remoteMessage =
        (To<MessageLite>)
            new ConstructorInvocation(remoteType, null, remoteDescriptor.getRemoteConstrTypes())
                .invokeConstructor(instance);
    MessageLite remoteProtoMsg = remoteMessage.toProto();
    checkState(remoteDescriptor.getProtoType() == remoteProtoMsg.getClass());
    return remoteProtoMsg;
  }
}
