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

import androidx.annotation.NonNull;
import com.google.protobuf.ByteString;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/** Converts a {@link ByteString} into a type T. */
final class ByteStringToTypeConverter<T> implements Converter<ByteString, T> {

  /**
   * Converts a {@link ByteString} into a type T.
   *
   * @param byteString the {@link ByteString} encoding the value T
   * @return an instance of type T
   */
  @Override
  @SuppressWarnings("unchecked") // safe covariant cast
  public T convert(@NonNull ByteString byteString) {
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteString.toByteArray());
    ObjectInputStream objectInputStream = null;
    try {
      objectInputStream = new ObjectInputStream(byteArrayInputStream);
      return (T) objectInputStream.readObject();
    } catch (IOException ioe) {
      throw new RemoteProtocolException("Cannot read ByteString into object", ioe);
    } catch (ClassNotFoundException cnfe) {
      throw new RemoteProtocolException("Cannot find ByteString to object", cnfe);
    } catch (ClassCastException cce) {
      throw new RemoteProtocolException("Cannot cast ByteString to object", cce);
    } finally {
      try {
        if (objectInputStream != null) {
          objectInputStream.close();
        }
      } catch (IOException ex) {
        // ignore close exception
      }
    }
  }
}
