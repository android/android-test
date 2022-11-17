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
package androidx.test.espresso.web.matcher;

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

import androidx.test.espresso.matcher.RemoteHamcrestCoreMatchers13;
import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import androidx.test.espresso.web.matcher.DomMatchers.ElementByIdMatcher;
import androidx.test.espresso.web.matcher.DomMatchers.ElementByXPathMatcher;
import androidx.test.espresso.web.matcher.DomMatchers.HasElementWithIdMatcher;
import androidx.test.espresso.web.matcher.DomMatchers.HasElementWithXPathMatcher;
import androidx.test.espresso.web.matcher.DomMatchers.WithBodyMatcher;
import androidx.test.espresso.web.matcher.DomMatchers.WithTextContentMatcher;
import androidx.test.espresso.web.proto.matcher.RemoteWebMatchers.ElementByIdMatcherProto;
import androidx.test.espresso.web.proto.matcher.RemoteWebMatchers.ElementByXPathMatcherProto;
import androidx.test.espresso.web.proto.matcher.RemoteWebMatchers.HasElementWithIdMatcherProto;
import androidx.test.espresso.web.proto.matcher.RemoteWebMatchers.HasElementWithXPathMatcherProto;
import androidx.test.espresso.web.proto.matcher.RemoteWebMatchers.WithBodyMatcherProto;
import androidx.test.espresso.web.proto.matcher.RemoteWebMatchers.WithTextContentMatcherProto;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Remote message transformation related test for all {@link DomMatchers} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class RemoteWebMatchersTest {

  private static final String TEXT_CONTENT = "Latte";
  private static final String ELEMENT_ID = "Macchiato";
  private static final String XPATH = "/beverage/espresso/*";

  @Before
  public void registerWebActionsWithRegistry() {
    RemoteDescriptorRegistry descriptorRegistry = RemoteDescriptorRegistry.getInstance();
    RemoteHamcrestCoreMatchers13.init(descriptorRegistry);
    RemoteWebMatchers.init(descriptorRegistry);
  }

  @Test
  public void withBody_transformationToProto() {
    Matcher<Element> withTextContentMatcher = DomMatchers.withTextContent(TEXT_CONTENT);
    Matcher<Document> withBodyMatcher = DomMatchers.withBody(withTextContentMatcher);

    WithBodyMatcherProto withBodyMatcherProto =
        (WithBodyMatcherProto) new GenericRemoteMessage(withBodyMatcher).toProto();
    assertThat(withBodyMatcherProto, notNullValue());
  }

  @Test
  public void withBody_transformationFromProto() {
    Matcher<Element> withTextContentMatcher = DomMatchers.withTextContent(TEXT_CONTENT);
    Matcher<Document> withBodyMatcher = DomMatchers.withBody(withTextContentMatcher);

    WithBodyMatcherProto withBodyMatcherProto =
        (WithBodyMatcherProto) new GenericRemoteMessage(withBodyMatcher).toProto();

    Object objectFromProto = GenericRemoteMessage.FROM.fromProto(withBodyMatcherProto);

    assertThat(objectFromProto, notNullValue());
    assertThat(objectFromProto, instanceOf(WithBodyMatcher.class));
  }

  @Test
  public void hasElementWithId_transformationToProto() {
    Matcher<Document> hasElementWithIdMatcher = DomMatchers.hasElementWithId(ELEMENT_ID);

    HasElementWithIdMatcherProto hasElementWithIdMatcherProto =
        (HasElementWithIdMatcherProto) new GenericRemoteMessage(hasElementWithIdMatcher).toProto();
    assertThat(hasElementWithIdMatcherProto, notNullValue());
  }

  @Test
  public void hasElementWithId_transformationFromProto() {
    Matcher<Document> hasElementWithIdMatcher = DomMatchers.hasElementWithId(ELEMENT_ID);

    HasElementWithIdMatcherProto hasElementWithIdMatcherProto =
        (HasElementWithIdMatcherProto) new GenericRemoteMessage(hasElementWithIdMatcher).toProto();

    Object objectFromProto = GenericRemoteMessage.FROM.fromProto(hasElementWithIdMatcherProto);

    assertThat(objectFromProto, notNullValue());
    assertThat(objectFromProto, instanceOf(HasElementWithIdMatcher.class));
  }

  @Test
  public void elementById_transformationToProto() {
    Matcher<Element> withTextContentMatcher = DomMatchers.withTextContent(TEXT_CONTENT);
    Matcher<Document> elementByIdMatcher =
        DomMatchers.elementById(ELEMENT_ID, withTextContentMatcher);

    ElementByIdMatcherProto elementByIdMatcherProto =
        (ElementByIdMatcherProto) new GenericRemoteMessage(elementByIdMatcher).toProto();
    assertThat(elementByIdMatcherProto, notNullValue());
  }

  @Test
  public void elementById_transformationFromProto() {
    Matcher<Element> withTextContentMatcher = DomMatchers.withTextContent(TEXT_CONTENT);
    Matcher<Document> elementByIdMatcher =
        DomMatchers.elementById(ELEMENT_ID, withTextContentMatcher);

    ElementByIdMatcherProto elementByIdMatcherProto =
        (ElementByIdMatcherProto) new GenericRemoteMessage(elementByIdMatcher).toProto();

    Object objectFromProto = GenericRemoteMessage.FROM.fromProto(elementByIdMatcherProto);

    assertThat(objectFromProto, notNullValue());
    assertThat(objectFromProto, instanceOf(ElementByIdMatcher.class));
  }

  @Test
  public void hasElementWithXPath_transformationToProto() {
    Matcher<Document> hasElementWithXPathMatcher = DomMatchers.hasElementWithXpath(XPATH);

    HasElementWithXPathMatcherProto hasElementWithXPathMatcherProto =
        (HasElementWithXPathMatcherProto)
            new GenericRemoteMessage(hasElementWithXPathMatcher).toProto();
    assertThat(hasElementWithXPathMatcherProto, notNullValue());
  }

  @Test
  public void hasElementWithXPath_transformationFromProto() {
    Matcher<Document> hasElementWithXPathMatcher = DomMatchers.hasElementWithXpath(XPATH);

    HasElementWithXPathMatcherProto hasElementWithXPathMatcherProto =
        (HasElementWithXPathMatcherProto)
            new GenericRemoteMessage(hasElementWithXPathMatcher).toProto();

    Object objectFromProto = GenericRemoteMessage.FROM.fromProto(hasElementWithXPathMatcherProto);

    assertThat(objectFromProto, notNullValue());
    assertThat(objectFromProto, instanceOf(HasElementWithXPathMatcher.class));
  }

  @Test
  public void elementByXPath_transformationToProto() {
    Matcher<Element> withTextContentMatcher = DomMatchers.withTextContent(TEXT_CONTENT);
    Matcher<Document> elementByXPathMatcher =
        DomMatchers.elementByXPath(XPATH, withTextContentMatcher);

    ElementByXPathMatcherProto elementByXPathMatcherProto =
        (ElementByXPathMatcherProto) new GenericRemoteMessage(elementByXPathMatcher).toProto();
    assertThat(elementByXPathMatcherProto, notNullValue());
  }

  @Test
  public void elementByXPath_transformationFromProto() {
    Matcher<Element> withTextContentMatcher = DomMatchers.withTextContent(TEXT_CONTENT);
    Matcher<Document> elementByXPathMatcher =
        DomMatchers.elementByXPath(XPATH, withTextContentMatcher);

    ElementByXPathMatcherProto elementByXPathMatcherProto =
        (ElementByXPathMatcherProto) new GenericRemoteMessage(elementByXPathMatcher).toProto();

    Object objectFromProto = GenericRemoteMessage.FROM.fromProto(elementByXPathMatcherProto);

    assertThat(objectFromProto, notNullValue());
    assertThat(objectFromProto, instanceOf(ElementByXPathMatcher.class));
  }

  @Test
  public void withTextContent_transformationToProto() {
    Matcher<Element> withTextContentMatcher = DomMatchers.withTextContent(TEXT_CONTENT);

    WithTextContentMatcherProto withTextContentMatcherProto =
        (WithTextContentMatcherProto) new GenericRemoteMessage(withTextContentMatcher).toProto();
    assertThat(withTextContentMatcherProto, notNullValue());
  }

  @Test
  public void withTextContent_transformationFromProto() {
    Matcher<Element> withTextContentMatcher = DomMatchers.withTextContent(TEXT_CONTENT);

    WithTextContentMatcherProto withTextContentMatcherProto =
        (WithTextContentMatcherProto) new GenericRemoteMessage(withTextContentMatcher).toProto();

    Object objectFromProto = GenericRemoteMessage.FROM.fromProto(withTextContentMatcherProto);

    assertThat(objectFromProto, notNullValue());
    assertThat(objectFromProto, instanceOf(WithTextContentMatcher.class));
  }
}
