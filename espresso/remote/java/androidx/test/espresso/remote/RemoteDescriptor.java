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

import static androidx.test.internal.util.Checks.checkArgument;
import static androidx.test.internal.util.Checks.checkNotNull;
import static androidx.test.internal.util.Checks.checkState;
import static kotlin.collections.CollectionsKt.listOf;
import static kotlin.collections.CollectionsKt.mutableListOf;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.espresso.remote.annotation.RemoteMsgField;
import androidx.test.espresso.util.StringJoinerKt;
import com.google.protobuf.Parser;
import java.util.List;
import java.util.Locale;

/**
 * Descriptor object containing all the required information to serialize and deserialize a type to
 * and from a proto. Needs to be registered with an {@link RemoteDescriptorRegistry}.
 */
public final class RemoteDescriptor {
  private static final String TAG = "RemoteDescriptor";

  private final Class<?> instanceType;
  private final List<FieldDescriptor> instanceFieldDescriptorList;

  private final Class<?> remoteType;
  private final List<Class<?>> remoteConstrTypes;

  private final Class<?> protoType;
  private final Class<?> protoBuilderType;
  private final Parser<?> protoParser;

  private RemoteDescriptor(
      @NonNull Class<?> instanceType,
      @NonNull List<FieldDescriptor> instanceFieldDescriptorList,
      @NonNull Class<?> remoteType,
      @NonNull List<Class<?>> remoteConstrTypes,
      @NonNull Class<?> protoType,
      @NonNull Class<?> protoBuilderType,
      @NonNull Parser<?> protoParser) {
    this.instanceType = instanceType;
    this.instanceFieldDescriptorList = instanceFieldDescriptorList;
    this.remoteType = remoteType;
    this.remoteConstrTypes = remoteConstrTypes;
    this.protoType = protoType;
    this.protoBuilderType = protoBuilderType;
    this.protoParser = protoParser;
  }

  private RemoteDescriptor(Builder builder) {
    this(
        builder.instanceType,
        builder.instanceFieldDescriptorList,
        builder.remoteType,
        builder.remoteConstrTypes,
        builder.protoType,
        builder.protoBuilderType,
        builder.protoParser);
  }

  /** @return the instance type */
  public Class<?> getInstanceType() {
    return instanceType;
  }

  /** @return list of field properties which identify target field, in declared order. */
  public List<FieldDescriptor> getInstanceFieldDescriptorList() {
    return instanceFieldDescriptorList;
  }

  /** @return the remote message type */
  public Class<?> getRemoteType() {
    return remoteType;
  }

  /**
   * @return list of types which identify the constructor's formal parameter types, in declared
   *     order.
   */
  public Class<?>[] getRemoteConstrTypes() {
    Class<?>[] asVarArgs = new Class<?>[remoteConstrTypes.size()];
    return remoteConstrTypes.toArray(asVarArgs);
  }

  /** @return the proto message type */
  public Class<?> getProtoType() {
    return protoType;
  }

  /** @return the proto message builder class for proto message type */
  public Class<?> getProtoBuilderClass() {
    return protoBuilderType;
  }

  /** @return the proto parser to parse the proto message type */
  public Parser<?> getProtoParser() {
    return protoParser;
  }

  /** @return the class name for instance type */
  public String getInstanceTypeName() {
    return instanceType.getName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (null == o || getClass() != o.getClass()) {
      return false;
    }

    RemoteDescriptor that = (RemoteDescriptor) o;

    if (!instanceType.equals(that.instanceType)) {
      return false;
    }
    if (!remoteType.equals(that.remoteType)) {
      return false;
    }
    if (!protoType.equals(that.protoType)) {
      return false;
    }
    if (!protoBuilderType.equals(that.protoBuilderType)) {
      return false;
    }
    return protoParser.equals(that.protoParser);
  }

  @Override
  public int hashCode() {
    int result = instanceType.hashCode();
    result = 31 * result + remoteType.hashCode();
    result = 31 * result + protoType.hashCode();
    result = 31 * result + protoBuilderType.hashCode();
    result = 31 * result + protoParser.hashCode();
    return result;
  }

  /** Builder for {@link RemoteDescriptor} */
  public static final class Builder {
    private static final String GENERIC_REMOTE_MESSAGE_CLS =
        "androidx.test.espresso.remote.GenericRemoteMessage";

    private Class<?> instanceType;
    private List<FieldDescriptor> instanceFieldDescriptorList = mutableListOf();

    private Class<?> remoteType;
    private List<Class<?>> remoteConstrTypes;

    private Class<?> protoType;
    private Class<?> protoBuilderType;
    private Parser<?> protoParser;

    public Builder() {
      // no-op constructor
    }

    private static List<FieldDescriptor> getFieldDescriptorsFromAnnotation(
        Class<?> instanceType, List<FieldDescriptor> originalFieldDescriptors) {
      // Check for any annotated fields
      List<FieldDescriptor> annotatedFieldList =
          FieldDescriptor.getFieldDescriptorsFromAnnotation(instanceType, RemoteMsgField.class);

      // Annotated fields take precedence over any registered field descriptors
      if (!annotatedFieldList.isEmpty()) {
        if (!originalFieldDescriptors.isEmpty()) {
          Log.w(
              TAG,
              String.format(
                  Locale.ROOT,
                  "RemoteMsgField field annotations found for type: %s. Ignoring"
                      + "field descriptors: %s, registered with RemoteDescriptorRegistry",
                  instanceType,
                  StringJoinerKt.joinToString(originalFieldDescriptors, ",")));
        }
        // return annotated field descriptors
        return annotatedFieldList;
      } else {
        // no annotated field descriptors return original descriptors registered with
        // RemoteDescriptorRegistry
        return originalFieldDescriptors;
      }
    }

    /**
     * Sets the instance type for associated with this {@link RemoteDescriptor}.
     *
     * <p>The instance type represents the class that will be converted to and from a proto.
     *
     * @param instanceType
     * @return fluent builder interface
     */
    public Builder setInstanceType(@NonNull Class<?> instanceType) {
      this.instanceType = instanceType;
      return this;
    }

    /**
     * Sets the {@link FieldDescriptor}s associated with this {@link RemoteDescriptor}.
     *
     * <p>The field descriptor order, must match the {@code instanceType}s declared constructor
     * parameter order.
     *
     * <p>Note: Any field descriptors passed to this method will be overwritten by field descriptors
     * annotated with {@link RemoteMsgField}.
     *
     * @param fieldDescriptors
     * @return fluent builder interface
     */
    public Builder setInstanceFieldDescriptors(@Nullable FieldDescriptor... fieldDescriptors) {
      this.instanceFieldDescriptorList = listOf(fieldDescriptors);
      return this;
    }

    /**
     * Sets the type of the {@link EspressoRemoteMessage} associated with this {@link
     * RemoteDescriptor}.
     *
     * @param remoteType the remote message class
     * @return fluent builder interface
     */
    public Builder setRemoteType(@NonNull Class<?> remoteType) {
      this.remoteType = remoteType;
      return this;
    }

    /**
     * Sets the remote constructor types of the {@link EspressoRemoteMessage} associated with this
     * {@link RemoteDescriptor}.
     *
     * <p>The types passed to this method will be used to reflectively infer the remote message
     * constructor.
     *
     * <p>By default the {@code instanceType} is used as remote message constructor. Only set custom
     * remote constructor types, when the remote message constructor takes a superclass or
     * interface, implemented by the instance type. Don't call this method when using {@link
     * androidx.test.espresso.remote.GenericRemoteMessage}.
     *
     * @param remoteConstrTypes
     * @return fluent builder interface
     */
    public Builder setRemoteConstrTypes(@Nullable Class<?>... remoteConstrTypes) {
      this.remoteConstrTypes = listOf(remoteConstrTypes);
      return this;
    }

    /**
     * Sets the type of the proto message associated with this {@link RemoteDescriptor}.
     *
     * @param protoType the proto message class
     * @return fluent builder interface
     */
    public Builder setProtoType(@NonNull Class<?> protoType) {
      this.protoType = protoType;
      return this;
    }

    /**
     * Sets the type of the proto message builder associated with this {@link RemoteDescriptor}.
     *
     * <p>By default the proto builder will be created by this class. Only call this method if a
     * custom builder is required.
     *
     * @param protoBuilderType the proto message builder class
     * @return fluent builder interface
     */
    public Builder setProtoBuilderType(@NonNull Class<?> protoBuilderType) {
      this.protoBuilderType = protoBuilderType;
      return this;
    }

    /**
     * Sets the type of the proto message parser associated with this {@link RemoteDescriptor}.
     *
     * <p>By default the proto parser is inferred from the {@code protoType}. Only call this method
     * if a custom parser is required.
     *
     * @param protoParser the proto parser
     * @return fluent builder interface
     */
    public Builder setProtoParser(@NonNull Parser<?> protoParser) {
      this.protoParser = protoParser;
      return this;
    }

    /** Builds a {@link RemoteDescriptor} from the builder properties set. */
    public RemoteDescriptor build() {
      checkNotNull(
          instanceType,
          "instanceType cannot be null! Use Builder.setInstanceType(Class<?> to set)");
      checkNotNull(
          instanceFieldDescriptorList,
          "instanceFieldDescriptorList cannot be null! Use "
              + "Builder.setInstanceFieldDescriptors(FieldDescriptor...) to set");
      checkNotNull(
          remoteType, "remoteType cannot be null! Use Builder.setRemoteType(Class<?> to set");
      // Create field descriptor list from field annotations
      instanceFieldDescriptorList =
          getFieldDescriptorsFromAnnotation(instanceType, instanceFieldDescriptorList);

      // Most remote message constructors will use the instance type as constructor param type
      if (null == remoteConstrTypes) {
        remoteConstrTypes = listOf(instanceType);
      }

      // GenericRemoteMessage constructor param type is always Object.class
      try {
        if (remoteType.isAssignableFrom(Class.forName(GENERIC_REMOTE_MESSAGE_CLS))) {
          remoteConstrTypes = listOf(Object.class);
        }
      } catch (ClassNotFoundException cnfe) {
        throw new IllegalStateException(
            String.format(
                Locale.ROOT, "Could not load class for name: %s", GENERIC_REMOTE_MESSAGE_CLS),
            cnfe);
      }

      checkArgument(protoType != null, "protoType is a mandatory field!");
      if (null == protoBuilderType) {
        try {
          protoBuilderType = Class.forName(protoType.getName().concat("$Builder"));
        } catch (ClassNotFoundException e) {
          throw new IllegalArgumentException(
              "Proto Builder type was not set. Attempt to load class with Class.forName() also "
                  + "failed!");
        }
      }

      if (null == protoParser) {
        // if proto parser not set infer it from the proto type
        protoParser = (Parser<?>) new MethodInvocation(protoType, null, "parser").invokeMethod();
        checkState(
            protoParser != null,
            "protoParser could not be inferred from proto type! Use "
                + "Builder.setProtoParser(Parser<?>) to set");
      }
      return new RemoteDescriptor(this);
    }
  }
}
