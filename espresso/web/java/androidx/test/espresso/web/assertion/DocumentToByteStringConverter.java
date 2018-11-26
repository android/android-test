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

import android.os.Binder;
import androidx.annotation.NonNull;
import androidx.test.espresso.remote.Converter;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.io.StringWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 * Converts a type {@link Document} into its {@link ByteString} representation.
 *
 * <p>This class uses {@link
 * androidx.test.espresso.web.assertion.CompressorDecompressor.GZIPCompressor} to reduce the
 * size of the document as much as possible to avoid hitting the {@link Binder} transaction limit.
 */
final class DocumentToByteStringConverter implements Converter<Document, ByteString> {
  private static byte[] compressDocument(String document) throws IOException {
    return CompressorDecompressor.compress(document.getBytes());
  }

  /**
   * Convert an object of type {@link Document} into its {@link ByteString} representation.
   *
   * @param document to convert into a {@link ByteString}
   */
  @Override
  public ByteString convert(@NonNull Document document) {
    try {
      DOMSource docSource = new DOMSource(document);
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      StringWriter writer = new StringWriter();
      StreamResult streamer = new StreamResult(writer);
      transformer.transform(docSource, streamer);
      return ByteString.copyFrom(compressDocument(writer.toString()));
    } catch (TransformerException te) {
      throw new RuntimeException("Could not convert!!!", te);
    } catch (IOException ioe) {
      throw new RuntimeException("Could not convert!!!", ioe);
    }
  }
}
