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
package androidx.test.espresso.web.assertion;

import androidx.annotation.NonNull;
import androidx.test.espresso.remote.Converter;
import com.google.protobuf.ByteString;
import java.io.IOException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;

/** Converts a type {@link ByteString} into its {@link Document} representation. */
final class ByteStringToDocumentConverter implements Converter<ByteString, Document> {

  private static String decompressDocument(byte[] bytes) throws IOException {
    return new String(CompressorDecompressor.decompress(bytes));
  }

  /**
   * Convert an object of type {@link ByteString} into its {@link Document} representation.
   *
   * @param byteString to convert into a {@link Document}
   */
  @Override
  public Document convert(@NonNull ByteString byteString) {
    try {
      return TagSoupDocumentParser.newInstance()
          .parse(decompressDocument(byteString.toByteArray()));
    } catch (IOException ioe) {
      throw new RuntimeException("Parsing document from ByteString failed!", ioe);
    } catch (SAXNotSupportedException snse) {
      throw new RuntimeException("Parsing document from ByteString failed!", snse);
    } catch (SAXException se) {
      throw new RuntimeException("Parsing document from ByteString failed!", se);
    }
  }
}
