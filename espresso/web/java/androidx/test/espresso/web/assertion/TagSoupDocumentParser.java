/*
 * Copyright (C) 2015 The Android Open Source Project
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
 */

package androidx.test.espresso.web.assertion;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.StringBufferInputStream;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/** Uses TagSoup to parse html into Documents. */
// Note: preferring this minimalistic approach to a full implementation of
// javax.xml.parsers.DocumentBuilder.java because we don't need document building capabilities and
// other parsing features.
public final class TagSoupDocumentParser {
  private static final ThreadLocal<TransformerFactory> transformerFactory =
      new ThreadLocal<TransformerFactory>() {
        @Override
        protected TransformerFactory initialValue() {
          return TransformerFactory.newInstance();
        }
      };

  private final Parser parser;

  private TagSoupDocumentParser() throws SAXNotRecognizedException, SAXNotSupportedException {
    parser = new Parser();
    // We do xpath evaluations which are not namespace aware. So make the parser
    // not use any namespaces.
    parser.setFeature(Parser.namespacesFeature, false);
  }

  // VisibleForTesting
  public static TagSoupDocumentParser newInstance()
      throws SAXNotRecognizedException, SAXNotSupportedException {
    return new TagSoupDocumentParser();
  }

  /** Parses the given html into an {@link Document}. */
  // VisibleForTesting
  public Document parse(String html) throws SAXException, IOException {
    checkNotNull(html);
    SAXSource in = new SAXSource(parser, new InputSource(new StringBufferInputStream(html)));
    DOMResult out = new DOMResult();
    try {
      transformerFactory.get().newTransformer().transform(in, out);
    } catch (TransformerException e) {
      throw new SAXException(e);
    }
    return (Document) out.getNode();
  }
}
