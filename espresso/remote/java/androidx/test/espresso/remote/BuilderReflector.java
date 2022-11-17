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

import static androidx.test.espresso.remote.ProtoUtils.capitalizeFirstChar;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import java.util.Locale;

/**
 * Reflection helper to invoke methods on a proto message Builder.
 *
 * <p>This class creates a proto message builder instance and provides methods to reflectively
 * invoke proto builder methods.
 */
final class BuilderReflector {

  private static final String NEW_BUILDER_METHOD_NAME = "newBuilder";
  private static final String BUILDER_BUILD_METHOD_NAME = "build";

  private static final String BUILDER_SET_VALUE_METHOD_FTD_NAME = "set%s";
  private static final String BUILDER_ADD_ALL_LIST_METHOD_FTD_NAME = "addAll%s";

  @VisibleForTesting final Object builderInstance;
  private final Class<?> builderType;

  /**
   * Creates a new {@link BuilderReflector}.
   *
   * @param builderType the proto Builder type
   * @param protoType the proto message type. This type must match the proto message type of the
   *     Builder. It is used to create a new instance of the proto Builder.
   */
  BuilderReflector(@NonNull Class<?> builderType, @NonNull Class<?> protoType) {
    this.builderType = checkNotNull(builderType, "builderType cannot be null");
    this.builderInstance = newBuilderInstance(checkNotNull(protoType, "protoType cannot be null"));
  }

  /**
   * Invokes a proto Builders {@value BUILDER_ADD_ALL_LIST_METHOD_FTD_NAME} method to set all values
   * of an {@link Iterable} on the Builder instance.
   *
   * @param methodSuffix method suffix to append to "addAll" to create the full method name. For
   *     instance, if your Builder method is called {@code addAllSomeSuffix()} the method prefix
   *     needs to be "SomeSuffix".
   * @param methodParams the parameters passed along to the Builder method
   * @return fluent BuilderReflector interface
   */
  public BuilderReflector invokeAddAllAnyList(String methodSuffix, Object... methodParams) {
    return invokeMethod(
        BUILDER_ADD_ALL_LIST_METHOD_FTD_NAME, methodSuffix, Iterable.class, methodParams);
  }

  /**
   * Invokes a proto Builders {@value BUILDER_SET_VALUE_METHOD_FTD_NAME} method to set an {@link
   * Any} value on the Builder instance.
   *
   * @param methodSuffix method suffix to append to a "set" method prefix to create the full method
   *     name. For instance, if your Builder method is called {@code setSomeAny()} the method prefix
   *     needs to be "SomeAny".
   * @param methodParams the parameters passed along to the Builder method
   * @return fluent BuilderReflector interface
   */
  public BuilderReflector invokeSetAnyValue(String methodSuffix, Object... methodParams) {
    return invokeMethod(BUILDER_SET_VALUE_METHOD_FTD_NAME, methodSuffix, Any.class, methodParams);
  }

  /**
   * Invokes a proto Builders {@value BUILDER_SET_VALUE_METHOD_FTD_NAME} method to set a {@link
   * ByteString} value on the Builder instance.
   *
   * @param methodSuffix method suffix to append to a "set" method prefix to create the full method
   *     name. For instance, if your Builder method is called {@code setSomeSuffix()} the method
   *     prefix needs to be "SomeSuffix".
   * @param methodParams the parameters passed along to the Builder method
   * @return fluent BuilderReflector interface
   */
  public BuilderReflector invokeSetByteStringValue(String methodSuffix, Object... methodParams) {
    return invokeMethod(
        BUILDER_SET_VALUE_METHOD_FTD_NAME, methodSuffix, ByteString.class, methodParams);
  }

  /**
   * Invokes the {@code build()} method of the proto Builder
   *
   * @return a proto message of protoType
   */
  public Object invokeBuild() {
    return new MethodInvocation(builderType, builderInstance, BUILDER_BUILD_METHOD_NAME)
        .invokeMethod();
  }

  private BuilderReflector invokeMethod(
      String methodNameTpl, String methodSuffix, Class<?> type, Object... args) {
    checkState(
        args != null && args.length > 0,
        "args set on builder %s, cannot be null or empty",
        builderType);
    new MethodInvocation(
            builderType,
            builderInstance,
            String.format(Locale.ROOT, methodNameTpl, capitalizeFirstChar(methodSuffix)),
            type)
        .invokeDeclaredMethod(args);
    return this;
  }

  private Object newBuilderInstance(Class<?> protoType) {
    return new MethodInvocation(protoType, protoType, NEW_BUILDER_METHOD_NAME).invokeMethod();
  }
}
