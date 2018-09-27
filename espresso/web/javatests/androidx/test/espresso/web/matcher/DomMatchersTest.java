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

package androidx.test.espresso.web.matcher;

import static androidx.test.espresso.web.matcher.DomMatchers.containingTextInBody;
import static androidx.test.espresso.web.matcher.DomMatchers.elementById;
import static androidx.test.espresso.web.matcher.DomMatchers.elementByXPath;
import static androidx.test.espresso.web.matcher.DomMatchers.hasElementWithId;
import static androidx.test.espresso.web.matcher.DomMatchers.hasElementWithXpath;
import static androidx.test.espresso.web.matcher.DomMatchers.withBody;
import static androidx.test.espresso.web.matcher.DomMatchers.withTextContent;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import android.test.suitebuilder.annotation.SmallTest;
import androidx.test.espresso.web.assertion.TagSoupDocumentParser;
import java.io.StringBufferInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import junit.framework.TestCase;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Test case for {@link DomMatchers}. */
@SmallTest
public class DomMatchersTest extends TestCase {

  private static final String HTML =
      "<html>"
          + "<script>document.was_clicked = false</script>"
          + "<body>"
          + "<button id='one' onclick='document.was_clicked = true'>"
          + "click here</button>"
          + "<span id='dup'></span>"
          + "<span id='dup'></span>"
          + "<span id='test'>Test</span>"
          + "</body>"
          + "</html>";

  private static final String HTML_NO_BODY =
      "<html>" + "<script>document.was_clicked = false</script>" + "</html>";

  private Document document;

  @Override
  public void setUp() throws Exception {
    document = TagSoupDocumentParser.newInstance().parse(HTML);
  }

  public void testContainingTextInBody() {
    assertTrue(containingTextInBody("click here").matches(document));
    assertFalse(containingTextInBody("garbage").matches(document));
  }

  public void testWithBody_NormalDocument() {
    Matcher<Element> hasAttributes =
        new TypeSafeMatcher<Element>() {
          @Override
          public void describeTo(Description description) {
            description.appendText("has attributes");
          }

          @Override
          public boolean matchesSafely(Element element) {
            return element.hasAttributes();
          }
        };

    assertTrue(withBody(not(hasAttributes)).matches(document));
  }

  public void testWithBody_DocumentWithNoBody() throws Exception {
    Document documentWithNoBody =
        DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(new StringBufferInputStream(HTML_NO_BODY));

    Matcher<Element> alwaysMatch =
        new TypeSafeMatcher<Element>() {
          @Override
          public void describeTo(Description description) {
            description.appendText("always a match");
          }

          @Override
          public boolean matchesSafely(Element element) {
            return true;
          }
        };
    assertFalse(withBody(alwaysMatch).matches(documentWithNoBody));
  }

  public void testHasElementWithId() {
    assertTrue(hasElementWithId("one").matches(document));
    assertTrue(hasElementWithId("dup").matches(document));
    assertFalse(hasElementWithId("notthere").matches(document));
  }

  public void testHasElementWithXPath() {
    assertTrue(hasElementWithXpath("/html/body/button").matches(document));
    assertFalse(hasElementWithXpath("/html/body/bogus").matches(document));
  }

  public void testWithXPath() {
    assertTrue(
        elementByXPath("/html/body/button", withTextContent("click here")).matches(document));
    assertTrue(elementByXPath("/html/body/span[3]", withTextContent("Test")).matches(document));
    assertFalse(
        elementByXPath("/html/body/some_bogus_path", withTextContent("click here"))
            .matches(document));
  }

  public void testWithMultipleMatchesForXPath() {
    try {
      assertTrue(
          elementByXPath("/html/body/span", withTextContent("click here")).matches(document));
      fail("Should have thrown AmbigousElementMatcherException");
    } catch (AmbiguousElementMatcherException e) {
      // expected.
    }
  }

  public void testElementById() {
    assertTrue(elementById("one", withTextContent("click here")).matches(document));
    assertFalse(elementById("one", withTextContent("click hereee")).matches(document));
    assertFalse(elementById("not there", withTextContent("click here")).matches(document));
  }

  public void testWithTextContent() {
    Element e = (Element) document.getElementsByTagName("button").item(0);
    assertTrue(withTextContent(is("click here")).matches(e));
    assertFalse(withTextContent(is("garbage")).matches(e));
  }
}
