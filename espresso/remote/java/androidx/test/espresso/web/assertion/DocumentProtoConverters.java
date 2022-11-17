/*
 * Copyright (C) 2017 The Android Open Source Project
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
package androidx.test.espresso.web.assertion;

import static com.google.common.base.Preconditions.checkNotNull;

import androidx.annotation.NonNull;
import com.google.protobuf.ByteString;
import org.w3c.dom.Document;

/** Converters for converting a {@link Document} into a {@link ByteString} */
final class DocumentProtoConverters {
  /**
   * Performs a {@link Document} to {@link ByteString} conversion.
   *
   * @param document to convert to a {@link ByteString}
   * @return wrapped {@link ByteString}
   */
  static ByteString documentToByteString(@NonNull Document document) {
    return new DocumentToByteStringConverter()
        .convert(checkNotNull(document, "document cannot be null!"));
  }

  /**
   * Performs a {@link ByteString} to {@link Document} conversion.
   *
   * @param byteString to convert to a {@link Document}
   * @return unwrapped {@link Document}
   */
  static Document byteStringToDocument(ByteString byteString) {
    return new ByteStringToDocumentConverter()
        .convert(checkNotNull(byteString, "byteString cannot be null!"));
  }
}
