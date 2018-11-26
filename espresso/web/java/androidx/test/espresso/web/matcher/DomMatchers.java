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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.test.espresso.remote.annotation.RemoteMsgConstructor;
import androidx.test.espresso.remote.annotation.RemoteMsgField;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A collection of hamcrest matchers for objects in the org.w3c.dom package (such as {@link
 * Document} and {@link Element}).
 */
public final class DomMatchers {

  private DomMatchers() {}

  /** Returns a matcher that matches Documents that have a body containing the given text. */
  public static Matcher<Document> containingTextInBody(String text) {
    checkNotNull(text);
    return withBody(withTextContent(containsString(text)));
  }

  /** Returns a matcher that matches {@link Document}s with body that matches the given matcher. */
  public static Matcher<Document> withBody(final Matcher<Element> bodyMatcher) {
    return new WithBodyMatcher(bodyMatcher);
  }

  /**
   * Returns a matcher that matches {@link Document}s that have at least one element with the given
   * id.
   */
  public static Matcher<Document> hasElementWithId(final String id) {
    return new HasElementWithIdMatcher(id);
  }

  /**
   * Matches {@link Document}s that have an {@link Element} with the given id that matches the given
   * element matcher.
   */
  public static Matcher<Document> elementById(
      final String id, final Matcher<Element> elementMatcher) {
    return new ElementByIdMatcher(id, elementMatcher);
  }

  /**
   * Returns a matcher that matches {@link Document}s that have at least one element with the given
   * xpath.
   */
  public static Matcher<Document> hasElementWithXpath(final String xpath) {
    return new HasElementWithXPathMatcher(xpath);
  }

  /**
   * Matches a XPath and validates it against the first {@link Element} that it finds in the {@link
   * NodeList}.
   */
  public static Matcher<Document> elementByXPath(
      final String xpath, final Matcher<Element> elementMatcher) {
    return new ElementByXPathMatcher(xpath, elementMatcher);
  }

  private static NodeList extractNodeListForXPath(String xpath, Document document) {
    try {
      XPath xPath = XPathFactory.newInstance().newXPath();
      XPathExpression expr = xPath.compile(xpath);
      return (NodeList) expr.evaluate(document, XPathConstants.NODESET);
    } catch (XPathExpressionException e) {
      return null;
    }
  }

  /**
   * Returns a matcher that matches {@link Element}s with the given textContent. Equivalent of
   * withTextContent(is(textContent)).
   */
  public static Matcher<Element> withTextContent(String textContent) {
    return withTextContent(is(textContent));
  }

  /**
   * Returns a matcher that matches {@link Element}s that have textContent matching the given
   * matcher.
   */
  public static Matcher<Element> withTextContent(final Matcher<String> textContentMatcher) {
    return new WithTextContentMatcher(textContentMatcher);
  }

  @VisibleForTesting
  static final class WithBodyMatcher extends TypeSafeMatcher<Document> {

    @RemoteMsgField(order = 0)
    private final Matcher<Element> bodyMatcher;

    @RemoteMsgConstructor
    WithBodyMatcher(@NonNull final Matcher<Element> bodyMatcher) {
      this.bodyMatcher = checkNotNull(bodyMatcher, "bodyMatcher cannot be null");
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("with body: ");
      bodyMatcher.describeTo(description);
    }

    @Override
    public boolean matchesSafely(Document document) {
      NodeList nodeList = document.getElementsByTagName("body");
      if (nodeList.getLength() == 0) {
        return false;
      }
      return bodyMatcher.matches(nodeList.item(0));
    }
  }

  @VisibleForTesting
  static final class HasElementWithIdMatcher extends TypeSafeMatcher<Document> {

    @RemoteMsgField(order = 0)
    private final String elementId;

    @RemoteMsgConstructor
    HasElementWithIdMatcher(final String elementId) {
      this.elementId = checkNotNull(elementId);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("has element with id: " + elementId);
    }

    @Override
    public boolean matchesSafely(Document document) {
      return document.getElementById(elementId) != null;
    }
  }

  @VisibleForTesting
  static final class ElementByIdMatcher extends TypeSafeMatcher<Document> {

    @RemoteMsgField(order = 0)
    private final String elementId;

    @RemoteMsgField(order = 1)
    private final Matcher<Element> elementMatcher;

    @RemoteMsgConstructor
    ElementByIdMatcher(final String elementId, final Matcher<Element> elementMatcher) {
      this.elementId = checkNotNull(elementId);
      this.elementMatcher = checkNotNull(elementMatcher);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(String.format("element with id '%s' matches: ", elementId));
      elementMatcher.describeTo(description);
    }

    @Override
    public boolean matchesSafely(Document document) {
      return elementMatcher.matches(document.getElementById(elementId));
    }
  }

  @VisibleForTesting
  static final class HasElementWithXPathMatcher extends TypeSafeMatcher<Document> {

    @RemoteMsgField(order = 0)
    private final String xpath;

    @RemoteMsgConstructor
    HasElementWithXPathMatcher(final String xpath) {
      this.xpath = checkNotNull(xpath);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("has element with xpath: " + xpath);
    }

    @Override
    public boolean matchesSafely(Document document) {
      NodeList nodeList = extractNodeListForXPath(xpath, document);
      return nodeList != null && nodeList.getLength() != 0;
    }
  }

  @VisibleForTesting
  static final class ElementByXPathMatcher extends TypeSafeMatcher<Document> {

    @RemoteMsgField(order = 0)
    private final String xpath;

    @RemoteMsgField(order = 1)
    private final Matcher<Element> elementMatcher;

    @RemoteMsgConstructor
    ElementByXPathMatcher(final String xpath, final Matcher<Element> elementMatcher) {
      this.xpath = checkNotNull(xpath);
      this.elementMatcher = checkNotNull(elementMatcher);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(String.format("element with xpath '%s' matches: ", xpath));
      elementMatcher.describeTo(description);
    }

    @Override
    public boolean matchesSafely(Document document) {
      NodeList nodeList = extractNodeListForXPath(xpath, document);
      if (nodeList == null || nodeList.getLength() == 0) {
        return false;
      }
      if (nodeList.getLength() > 1) {
        throw new AmbiguousElementMatcherException(xpath);
      }
      if (nodeList.item(0).getNodeType() != Node.ELEMENT_NODE) {
        return false;
      }
      Element element = (Element) nodeList.item(0);
      return elementMatcher.matches(element);
    }
  }

  @VisibleForTesting
  static final class WithTextContentMatcher extends TypeSafeMatcher<Element> {

    @RemoteMsgField(order = 0)
    private final Matcher<String> textContentMatcher;

    @RemoteMsgConstructor
    WithTextContentMatcher(@NonNull Matcher<String> textContentMatcher) {
      this.textContentMatcher =
          checkNotNull(textContentMatcher, "textContentMatcher cannot be null");
    }

    @Override
    protected boolean matchesSafely(Element element) {
      return textContentMatcher.matches(element.getTextContent());
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("with text content: ");
      textContentMatcher.describeTo(description);
    }
  }
}
