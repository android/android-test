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

import androidx.annotation.NonNull;
import androidx.test.espresso.remote.EspressoRemoteMessage.From;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import java.lang.reflect.Field;
import java.util.Locale;

/**
 * Unwraps an {@link Any} proto message to it's target type T, where T must implement the {@link
 * EspressoRemoteMessage.From} interface.
 *
 * @param <T> the target type T
 */
final class AnyToTypeConverter<T> implements Converter<Any, T> {

  private final RemoteDescriptorRegistry remoteDescriptorRegistry;

  AnyToTypeConverter(@NonNull RemoteDescriptorRegistry remoteDescriptorRegistry) {
    this.remoteDescriptorRegistry =
        checkNotNull(remoteDescriptorRegistry, "remoteDescriptorRegistry cannot be null!");
  }

  @Override
  public T convert(@NonNull Any any) {
    checkNotNull(any, "any cannot be null!");
    T targetType;
    try {
      Class<?> targetClass = createRemoteTargetClassFromAny(any);
      Field fromField = accessFromField(targetClass);
      From<T, MessageLite> remoteMessageFromField = createRemoteMessageFromField(fromField);
      targetType = createTargetTypeFromRemoteMessage(remoteMessageFromField, any);
    } catch (IllegalAccessException iae) {
      throw new RemoteProtocolException("Cannot unwrap target type instance from any proto: ", iae);
    } catch (RuntimeException re) {
      throw new RemoteProtocolException(
          String.format(
              Locale.ROOT,
              "Something went wrong during any conversion " + "for type url: %s",
              any.getTypeUrl()),
          re);
    }
    return targetType;
  }

  private T createTargetTypeFromRemoteMessage(
      From<T, MessageLite> remoteMessageFromField, Any anyMessage) {
    MessageLite remoteMessage = anyToProto(anyMessage);
    // Create an instance of our remote view Matcher from the remoteMatcherMessage
    return remoteMessageFromField.fromProto(remoteMessage);
  }

  @SuppressWarnings("unchecked") // safe covariant cast
  private EspressoRemoteMessage.From<T, MessageLite> createRemoteMessageFromField(Field fromField)
      throws IllegalAccessException {
    try {
      return EspressoRemoteMessage.From.class.cast(fromField.get(null));
    } catch (ClassCastException cce) {
      throw new RemoteProtocolException(
          "Cannot unwrap target type from any proto: Cannot cast"
              + "'private static FROM' field to the EspressoRemoteMessage.From interface.");
    }
  }

  private Field accessFromField(Class<?> remoteClass) throws IllegalAccessException {
    Field fromField;
    try {
      fromField = remoteClass.getDeclaredField("FROM");
    } catch (NoSuchFieldException nsfe) {
      throw new RemoteProtocolException(
          "Cannot unwrap target type from any proto: "
              + remoteClass.getSimpleName()
              + " does not declare a "
              + "'private static FROM' fromViewMatcherField implementing the"
              + "EspressoRemoteMessage.From interface. Please ensure that such a"
              + "fromViewMatcherField exists!",
          nsfe);
    }
    fromField.setAccessible(true);

    if (null == fromField /* should never happen */ || !fromField.isAccessible()) {
      throw new IllegalAccessException(
          "Failed to make EspressoRemoteMessage.From FROM field accessible");
    }
    return fromField;
  }

  private Class<?> createRemoteTargetClassFromAny(Any any) {
    return remoteDescriptorRegistry.argForRemoteTypeUrl(any.getTypeUrl()).getRemoteType();
  }

  private <T extends MessageLite> T anyToProto(Any any, Class<T> expectedType, Parser<?> parser)
      throws InvalidProtocolBufferException {
    checkNotNull(expectedType, "expectedType cannot be null!");
    checkNotNull(parser, "parser cannot be null!");
    Object proto = parser.parseFrom(any.getValue());
    if (!expectedType.isInstance(proto)) {
      RemoteProtocolException rpe =
          new RemoteProtocolException(
              String.format(
                  Locale.ROOT,
                  "Got type: %s, but expected type: %s instead",
                  proto.getClass().getName(),
                  expectedType.getName()));
      throw rpe;
    }
    return expectedType.cast(proto);
  }

  @SuppressWarnings("TypeParameterUnusedInFormals") // addressed in anyToProto(Any, Class,// Parser)
  private <T extends MessageLite> T anyToProto(Any any) {
    RemoteDescriptor remoteDescriptor =
        remoteDescriptorRegistry.argForRemoteTypeUrl(any.getTypeUrl());

    try {
      @SuppressWarnings("unchecked") // safe covariant cast
      Class<T> messageType = (Class<T>) remoteDescriptor.getProtoType();
      return anyToProto(any, messageType, remoteDescriptor.getProtoParser());
    } catch (ClassCastException cce) {
      throw new RemoteProtocolException("Message cannot be casted to type T", cce);
    } catch (InvalidProtocolBufferException ipbe) {
      throw new RemoteProtocolException(
          "Invalid Protocol buffer for any type url: " + any.getTypeUrl(), ipbe);
    }
  }
}
