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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.google.protobuf.ByteString;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

/** Tests for {@link DocumentToByteStringConverter} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public final class DocumentToByteStringConverterTest {

  private static final String HTML =
      "<!doctype html>\n"
          + "<html>\n"
          + "<head>\n"
          + "  <title>Hello Compression</title>\n"
          + "  <meta charset=\"utf-8\" />\n"
          + "  <meta http-equiv=\"Content-type\" content=\"text/html; charset=utf-8\" />\n"
          + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n"
          + "</head>\n"
          + "<body>\n"
          + "<div>\n"
          + "  <p id=\"hello\">Compression<p>\n"
          + "</div>\n"
          + "</body>\n"
          + "</html>";

  @Test
  public void documentToByteStringConversion() throws SAXException, IOException {
    DocumentToByteStringConverter documentToByteStringConverter =
        new DocumentToByteStringConverter();
    ByteString byteString =
        documentToByteStringConverter.convert(TagSoupDocumentParser.newInstance().parse(HTML));
    assertThat(byteString, notNullValue());
  }
}
