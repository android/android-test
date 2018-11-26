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

import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;

/** Converters from {@link Any} proto messages to their unwrapped target types. */
public final class TypeProtoConverters {

  private static final RemoteDescriptorRegistry REGISTRY = RemoteDescriptorRegistry.getInstance();

  private TypeProtoConverters() {
    // no-op instance
  }

  /**
   * Performs an {@link Any} proto message to type T conversion.
   *
   * @param any type to unwrap into its target type T representation
   * @return unwrapped target type T
   */
  @SuppressWarnings("TypeParameterUnusedInFormals")
  public static <T> T anyToType(@NonNull Any any) {
    return anyToType(any, REGISTRY);
  }

  /**
   * Performs an {@link Any} proto message to type T conversion.
   *
   * <p>Note: By default this class will use the default {@link RemoteDescriptorRegistry} instance.
   * Passing in a custom {@link RemoteDescriptorRegistry} is usually not what you want. This method
   * should only be used if you want to allow for injection of a fake RemoteDescriptorRegistry
   * instance for tests.
   *
   * @param any type to unwrap into its target type T representation
   * @param remoteDescriptorRegistry the {@link RemoteDescriptorRegistry} fot type lookup.
   * @return unwrapped target type T
   */
  @SuppressWarnings("TypeParameterUnusedInFormals")
  static <T> T anyToType(
      @NonNull Any any, @NonNull RemoteDescriptorRegistry remoteDescriptorRegistry) {
    return new AnyToTypeConverter<T>(checkNotNull(remoteDescriptorRegistry))
        .convert(checkNotNull(any, "any cannot be null!"));
  }

  /**
   * Performs a type T to {@link Any} proto message conversion.
   *
   * @param type target type to wrap into its {@link Any} representation
   * @return {@link Any} proto message which contains the wrapped proto representation of T
   */
  public static <T> Any typeToAny(@NonNull T type) {
    return typeToAny(type, REGISTRY);
  }

  /**
   * Performs a type T to {@link Any} proto message conversion.
   *
   * <p>Note: By default this class will use the default {@link RemoteDescriptorRegistry} instance.
   * Passing in a custom {@link RemoteDescriptorRegistry} is usually not what you want. This method
   * should only be used if you want to allow for injection of a fake RemoteDescriptorRegistry
   * instance for tests.
   *
   * @param type target type to wrap into its {@link Any} representation
   * @param remoteDescriptorRegistry the {@link RemoteDescriptorRegistry} fot type lookup.
   * @return {@link Any} proto message which contains the wrapped proto representation of T
   */
  static <T> Any typeToAny(
      @NonNull T type, @NonNull RemoteDescriptorRegistry remoteDescriptorRegistry) {
    return new TypeToAnyConverter<T>(checkNotNull(remoteDescriptorRegistry))
        .convert(
            checkNotNull(
                type, "Target type: %s cannot be null!", type.getClass().getCanonicalName()));
  }

  /**
   * Performs an object to {@link ByteString} conversion.
   *
   * @param object object to convert to a {@link ByteString}
   * @return {@link ByteString} representation of the passed object
   */
  public static ByteString typeToByteString(@NonNull Object object) {
    return new TypeToByteStringConverter<>()
        .convert(checkNotNull(object, "object cannot be null!"));
  }

  /**
   * Performs a {@link ByteString} to type T conversion.
   *
   * @param byteString the {@link ByteString} to convert into T
   * @return instance of type T
   */
  @SuppressWarnings("TypeParameterUnusedInFormals")
  public static <T> T byteStringToType(@NonNull ByteString byteString) {
    return new ByteStringToTypeConverter<T>()
        .convert(checkNotNull(byteString, "byteString cannot be null!"));
  }

  /**
   * Performs {@link Parcelable} to {@link ByteString} conversion.
   *
   * @param parcelable {@link Parcelable} to convert to a {@link ByteString}
   * @return {@link ByteString} representation of the passed object
   */
  public static ByteString parcelableToByteString(@NonNull Parcelable parcelable) {
    return new ParcelableToByteStringConverter()
        .convert(checkNotNull(parcelable, "parcelable cannot be null!"));
  }

  /**
   * Performs a {@link ByteString} to {@link Parcelable} conversion.
   *
   * @param byteString the {@link ByteString} to convert to a {@link Parcelable}
   * @return instance of {@link Parcelable}
   */
  public static Parcelable byteStringToParcelable(
      @NonNull ByteString byteString, @NonNull Class<Parcelable> parcelableClass) {
    return new ByteStringToParcelableConverter(parcelableClass)
        .convert(checkNotNull(byteString, "byteString cannot be null!"));
  }
}
