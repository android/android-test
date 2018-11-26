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

import static androidx.test.internal.util.LogUtil.logDebug;
import static com.google.common.base.Preconditions.checkNotNull;

import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.test.espresso.remote.annotation.RemoteMsgConstructor;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Deserializes a proto message, into its object representation.
 *
 * <p>Types must register a serialization descriptor in the form a {@link RemoteDescriptor} with the
 * {@link RemoteDescriptorRegistry}. The descriptor must contain all the necessary information to
 * perform proto deserialization.
 *
 * <p>Deserialization steps:
 *
 * <ol>
 *   <li>Look up all serialised fields through {@link FieldDescriptor} values provided by the
 *       registered {@link RemoteDescriptor}
 *   <li/>
 *   <li>Process all {@link Iterable} getter methods on the proto message
 *   <li>Process all {@link ByteString} getter methods on the proto message
 *   <li/>
 *   <li>Process all {@link Any} getter methods on the proto message
 *   <li/>
 *   <li>Convert all {@link ByteString} and {@link Any} proto messages into their object
 *       representation.
 *   <li/>
 *   <li>Invoke the constructor of the target instance using the constructor parameters gathered
 *       during processing step
 *   <li/>
 *       <ol/>
 *
 * @see RemoteMessageSerializer
 */
final class RemoteMessageDeserializer implements EspressoRemoteMessage.From<Object, MessageLite> {
  private static final String TAG = "RemoteMsgDeserializer";

  private final RemoteDescriptorRegistry remoteDescriptorRegistry;

  /**
   * Creates a {@link RemoteMessageDeserializer}
   *
   * @param remoteDescriptorRegistry the remote descriptor registry used for type lookup
   */
  public RemoteMessageDeserializer(@NonNull RemoteDescriptorRegistry remoteDescriptorRegistry) {
    this.remoteDescriptorRegistry =
        checkNotNull(remoteDescriptorRegistry, "remoteDescriptorRegistry cannot be null!");
  }

  /** {@inheritDoc} */
  @Override
  public Object fromProto(@NonNull MessageLite messageLite) {
    checkNotNull(messageLite, "messageLite cannot be null!");
    try {
      RemoteDescriptor remoteDescriptor =
          messageLite instanceof Any
              // For Any protos get the remote through the type url
              ? remoteDescriptorRegistry.argForRemoteTypeUrl(((Any) messageLite).getTypeUrl())
              // For All other msg types get remote type descriptor for proto message "runtime" type
              : remoteDescriptorRegistry.argForMsgType(messageLite.getClass());
      return fromProtoInternal(messageLite, remoteDescriptor);
    } catch (Exception e) {
      if (e.getCause() instanceof RemoteProtocolException) {
        throw (RemoteProtocolException) e;
      }
      throw new RemoteProtocolException("Error: " + e.getMessage(), e);
    }
  }

  private Object fromProtoInternal(MessageLite messageLite, RemoteDescriptor remoteDescriptor) {
    // List that will be used to store constructor parameters. These values will later be passed
    // along to the constructor of the target object
    List<Object> constructorParams = Lists.newArrayList();
    // Create a new proto reflector to interact with a proto msg
    ProtoReflector protoReflector =
        new ProtoReflector(remoteDescriptor.getProtoType(), messageLite);

    List<FieldDescriptor> fieldDescriptorList = remoteDescriptor.getInstanceFieldDescriptorList();

    // Iterate over all the field properties to lookup the field types and names
    for (FieldDescriptor fieldDescriptor : fieldDescriptorList) {
      Object constructorParam;
      // Process Iterable types
      if (Iterable.class.isAssignableFrom(fieldDescriptor.fieldType)) {
        List<Any> iterable = protoReflector.getAnyList(fieldDescriptor.fieldName);
        constructorParam =
            Iterables.transform(
                iterable,
                new Function<MessageLite, Object>() {
                  @Override
                  public Object apply(MessageLite messageLite) {
                    if (messageLite instanceof Any) {
                      return TypeProtoConverters.anyToType(
                          (Any) messageLite, remoteDescriptorRegistry);
                    }
                    return fromProto(messageLite);
                  }
                });
      } else if (Serializable.class.isAssignableFrom(fieldDescriptor.fieldType)
          || Object.class == fieldDescriptor.fieldType
          || fieldDescriptor.fieldType.isPrimitive()) {
        // Process primitive and Serializables
        ByteString byteString = protoReflector.getByteStringValue(fieldDescriptor.fieldName);
        constructorParam = TypeProtoConverters.byteStringToType(byteString);
      } else if (Parcelable.class.isAssignableFrom(fieldDescriptor.fieldType)) {
        // Process Parcelables
        ByteString byteString = protoReflector.getByteStringValue(fieldDescriptor.fieldName);
        @SuppressWarnings("unchecked") // type is checked above in the if block
        Class<Parcelable> parcelableClass = (Class<Parcelable>) fieldDescriptor.fieldType;
        constructorParam = TypeProtoConverters.byteStringToParcelable(byteString, parcelableClass);
      } else {
        // Process Any types
        Any any = protoReflector.getAnyValue(fieldDescriptor.fieldName);
        constructorParam = TypeProtoConverters.anyToType(any, remoteDescriptorRegistry);
      }
      constructorParams.add(constructorParam);
    }

    // Reflectively create the instance and return it
    Object instance = null;
    try {
      Class<?>[] constructorTypes = new Class<?>[fieldDescriptorList.size()];
      Lists.transform(
              fieldDescriptorList,
              new Function<FieldDescriptor, Class<?>>() {
                @Override
                public Class<?> apply(FieldDescriptor fieldDescriptor) {
                  // Transform fieldDescriptorList into a new list which contains only field types
                  return fieldDescriptor.fieldType;
                }
              })
          .toArray(constructorTypes);

      instance =
          new ConstructorInvocation(
                  remoteDescriptor.getInstanceType(), RemoteMsgConstructor.class, constructorTypes)
              .invokeConstructor(constructorParams.toArray());
    } finally {
      logDebug(
          TAG,
          "proto: %s, createdInstance %s, instanceType: %s, constructorParams: [%s], "
              + "available constructors: %s",
          messageLite.getClass().getSimpleName(),
          instance,
          remoteDescriptor.getInstanceType(),
          Joiner.on(",").join(constructorParams),
          Arrays.toString(remoteDescriptor.getInstanceType().getConstructors()));
    }
    return instance;
  }
}
