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
import com.google.protobuf.ByteString;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Locale;

/** Converts a type T into its {@link ByteString} representation. */
final class TypeToByteStringConverter<T> implements Converter<T, ByteString> {
  /**
   * Convert an object of type T into its {@link ByteString} representation.
   *
   * @param object to convert into a {@link ByteString}
   */
  @Override
  public ByteString convert(@NonNull T object) {
    checkNotNull(object, "object cannot be null!");
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ObjectOutputStream objectOutputStream = null;
    try {
      objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
      objectOutputStream.writeObject(object);
      return ByteString.copyFrom(byteArrayOutputStream.toByteArray());
    } catch (IOException ioe) {
      throw new RemoteProtocolException(
          String.format(
              Locale.ROOT,
              "Cannot write object of type: %s to ByteStream",
              object.getClass().getSimpleName()),
          ioe);
    } finally {
      try {
        if (objectOutputStream != null) {
          objectOutputStream.close();
        }
      } catch (IOException ex) {
        // ignore close exception
      }
    }
  }
}
