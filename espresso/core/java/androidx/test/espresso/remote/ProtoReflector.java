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

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import java.util.List;
import java.util.Locale;

/** Reflection helper to invoke methods on a proto message. */
final class ProtoReflector {

  private static final String PROTO_MSG_GET_ALL_LIST_METHOD_NAME_TPL = "get%sList";
  private static final String PROTO_MSG_GET_VALUE_METHOD_NAME_TPL = "get%s";

  private final Class<?> protoType;
  private final MessageLite proto;

  /**
   * Creates a new {@link BuilderReflector}.
   *
   * @param protoType the proto message type
   * @param proto instance of a proto message. The runtime class of this instance must match the
   *     {@link #protoType}.
   */
  public ProtoReflector(Class<?> protoType, MessageLite proto) {
    this.protoType = protoType;
    this.proto = proto;
  }

  /**
   * Invokes and returns the {@link Any} list of a proto instances {@value
   * PROTO_MSG_GET_ALL_LIST_METHOD_NAME_TPL} method.
   *
   * @param methodSuffix the method suffix to substitute "get<suffix>List" to create the full method
   *     name. For instance, if the proto method is called {@code getSomeSuffixList()} the method
   *     prefix needs to be "SomeSuffix".
   * @return a list of {@link Any} objects
   */
  @SuppressWarnings("unchecked")
  public List<Any> getAnyList(String methodSuffix) {
    return invokeMethod(proto, PROTO_MSG_GET_ALL_LIST_METHOD_NAME_TPL, methodSuffix, List.class);
  }

  /**
   * Invokes and returns the {@link Any} value of a proto instances {@value
   * PROTO_MSG_GET_VALUE_METHOD_NAME_TPL} method.
   *
   * @param methodSuffix the method suffix to append "get" to create the full method name. For
   *     instance, if the proto method is called {@code getSomeSuffix()} the method prefix needs to
   *     be "SomeSuffix".
   * @return the {@link Any} object
   */
  public Any getAnyValue(String methodSuffix) {
    return invokeMethod(proto, PROTO_MSG_GET_VALUE_METHOD_NAME_TPL, methodSuffix, Any.class);
  }

  /**
   * Invokes and returns the {@link ByteString} value of a proto instances {@value
   * PROTO_MSG_GET_VALUE_METHOD_NAME_TPL} method.
   *
   * @param methodSuffix the method suffix to append "get" to create the full method name. For
   *     instance, if the proto method is called {@code getSomeSuffix()} the method prefix weeds to
   *     be "SomeSuffix".
   * @return the {@link ByteString} object
   */
  public ByteString getByteStringValue(String methodSuffix) {
    return invokeMethod(proto, PROTO_MSG_GET_VALUE_METHOD_NAME_TPL, methodSuffix, ByteString.class);
  }

  private <T> T invokeMethod(
      MessageLite messageLite, String methodNameTpl, String methodSuffix, Class<T> clazz) {
    return clazz.cast(
        new MethodInvocation(
                protoType,
                messageLite,
                String.format(Locale.ROOT, methodNameTpl, capitalizeFirstChar(methodSuffix)))
            .invokeDeclaredMethod());
  }
}
