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

import static androidx.test.espresso.remote.ProtoUtils.getFilteredFieldList;
import static androidx.test.internal.util.Checks.checkNotNull;
import static androidx.test.internal.util.Checks.checkState;
import static androidx.test.internal.util.LogUtil.logDebug;

import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import kotlin.collections.CollectionsKt;

/**
 * Serializes an arbitrary object into its proto message representation.
 *
 * <p>Types must register a serialization descriptor in the form a {@link RemoteDescriptor} with the
 * {@link RemoteDescriptorRegistry}. The descriptor must contain all the necessary information to
 * perform proto serialization.
 *
 * <p>Primitive and {@link Serializable} types are declared inside the instance class are supported
 * out of the box and do not need to be registered with {@link RemoteDescriptorRegistry}. This
 * Serializer also offers support for {@link Iterable}s. Objects that resides inside an {@link
 * Iterable}, are converted to an {@link Any} proto message and therefore must be registered with
 * {@link RemoteDescriptorRegistry}.
 *
 * <p>Serialization steps:
 *
 * <ol>
 *   <li>Filter declared instance fields based on {@link FieldDescriptor} values provided by the
 *       registered {@link RemoteDescriptor}
 *   <li>Create a proto message Builder using {@code builderType}
 *   <li>Process all {@link Iterable}s and set the values on the proto message Builder
 *   <li>Process all primitives and {@link Serializable} objects
 *   <li>Convert all types into {@link ByteString}s and set their values on the proto message
 *       Builder
 *   <li>Process all other types that are registered with {@link RemoteDescriptorRegistry}
 *   <li>Creates a proto message by calling the proto message Builders {@code build} method
 *       <ol/>
 */
final class RemoteMessageSerializer implements EspressoRemoteMessage.To<MessageLite> {
  private static final String TAG = "RemoteMsgSerializer";

  private final RemoteDescriptorRegistry remoteDescriptorRegistry;
  private final Object instance;
  private final List<FieldDescriptor> fieldDescriptorList;
  private final Class<?> builderType;
  private final Class<?> protoType;

  /**
   * Creates a {@link RemoteMessageSerializer}
   *
   * @param instance the instance to serialize into a proto
   * @param remoteDescriptorRegistry the remote descriptor registry used for type lookup
   */
  public RemoteMessageSerializer(
      @NonNull Object instance, @NonNull RemoteDescriptorRegistry remoteDescriptorRegistry) {
    RemoteDescriptor remoteDescriptor =
        checkNotNull(remoteDescriptorRegistry, "remoteDescriptorRegistry cannot be null!")
            .argForInstanceType(instance.getClass());
    this.remoteDescriptorRegistry = remoteDescriptorRegistry;
    this.instance = checkNotNull(instance, "instance cannot be null!");
    this.fieldDescriptorList = remoteDescriptor.getInstanceFieldDescriptorList();
    this.builderType = remoteDescriptor.getProtoBuilderClass();
    this.protoType = remoteDescriptor.getProtoType();
  }

  /** {@inheritDoc} */
  @Override
  public MessageLite toProto() {
    return toProtoInternal();
  }

  private MessageLite toProtoInternal() {
    List<Field> targetFields = null;
    try {
      // Filter a class declared fields based on field descriptor names
      targetFields =
          getFilteredFieldList(
              instance.getClass(),
              CollectionsKt.map(
                  fieldDescriptorList,
                  fieldDescriptor -> {
                    // Transform fieldDescriptorList into a new list which contains field names
                    return fieldDescriptor.fieldName;
                  }));
      return createProtoFromTargetFields(targetFields, instance);
    } catch (Exception e) {
      if ((e instanceof RemoteProtocolException)) {
        throw (RemoteProtocolException) e;
      }
      throw new RemoteProtocolException("Error", e);
    } finally {
      logDebug(
          TAG,
          "instance type: %s, protoType: %s, declaredFields %s total: %s, "
              + "targetFields %s total: %s",
          instance.getClass(),
          protoType,
          instance.getClass().getDeclaredFields().length,
          Arrays.toString(instance.getClass().getDeclaredFields()),
          targetFields.size(),
          targetFields);
    }
  }

  private MessageLite createProtoFromTargetFields(List<Field> instanceTargetFields, Object instance)
      throws IllegalAccessException {
    // Create a new builder reflector to interact with a proto builder
    BuilderReflector builderReflector = new BuilderReflector(builderType, protoType);

    // Iterate through all the filtered instance fields
    for (Field targetField : instanceTargetFields) {
      targetField.setAccessible(true);
      String targetFieldName = targetField.getName();
      Object fieldValue = targetField.get(instance);

      checkState(
          fieldValue != null,
          "Serialization of field %s.%s failed, field was null!",
          targetField.getDeclaringClass(),
          targetFieldName);

      if (fieldValue instanceof Iterable) {
        // Process any fields of type Iterable. Iterate through all the values, create proto msgs
        // for each value and add the list on the proto builder.
        List<MessageLite> messageLites = new ArrayList<>();
        Iterator<?> iterator = ((Iterable) fieldValue).iterator();
        while (iterator.hasNext()) {
          messageLites.add(
              TypeProtoConverters.typeToAny(iterator.next(), remoteDescriptorRegistry));
        }
        builderReflector.invokeAddAllAnyList(targetFieldName, messageLites);
      } else if (fieldValue instanceof Serializable) {
        // Process any Serializable objects
        builderReflector.invokeSetByteStringValue(
            targetFieldName, TypeProtoConverters.typeToByteString(fieldValue));
      } else if (fieldValue instanceof Parcelable) {
        // Process any Parcelable objects
        builderReflector.invokeSetByteStringValue(
            targetFieldName, TypeProtoConverters.parcelableToByteString((Parcelable) fieldValue));
      } else if (remoteDescriptorRegistry.hasArgForInstanceType(fieldValue.getClass())) {
        // Third check if type is a registered type that needs to be serialised into an Any type
        builderReflector.invokeSetAnyValue(
            targetFieldName, TypeProtoConverters.typeToAny(fieldValue, remoteDescriptorRegistry));
      } else {
        throw new RemoteProtocolException(
            String.format(
                Locale.ROOT,
                "Target field: %s#%s cannot be serialised "
                    + "into a proto. Supported target fields can be of type: Any, Serializable or "
                    + "an Iterable<Any/Serializable>",
                fieldValue.getClass().getName(),
                targetFieldName));
      }
    }
    return (MessageLite) builderReflector.invokeBuild();
  }
}
