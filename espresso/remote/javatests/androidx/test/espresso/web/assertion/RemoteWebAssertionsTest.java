/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso.web.assertion;

import static androidx.test.espresso.web.webdriver.DriverAtoms.getText;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import androidx.test.espresso.matcher.RemoteHamcrestCoreMatchers13;
import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import androidx.test.espresso.web.assertion.WebAssertion.CheckResultWebAssertion;
import androidx.test.espresso.web.assertion.WebViewAssertions.DocumentParserAtom;
import androidx.test.espresso.web.assertion.WebViewAssertions.ResultCheckingWebAssertion;
import androidx.test.espresso.web.assertion.WebViewAssertions.ResultDescriber;
import androidx.test.espresso.web.assertion.WebViewAssertions.ToStringResultDescriber;
import androidx.test.espresso.web.assertion.WebViewAssertions.WebContentResultDescriber;
import androidx.test.espresso.web.model.Atom;
import androidx.test.espresso.web.model.RemoteWebModelAtoms;
import androidx.test.espresso.web.proto.assertion.WebAssertions.CheckResultAssertionProto;
import androidx.test.espresso.web.proto.assertion.WebAssertions.DocumentParserAtomProto;
import androidx.test.espresso.web.proto.assertion.WebAssertions.ToStringResultDescriberProto;
import androidx.test.espresso.web.proto.assertion.WebAssertions.WebContentResultDescriberProto;
import androidx.test.espresso.web.webdriver.RemoteWebDriverAtoms;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Remote message transformation related test for all web actions */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class RemoteWebAssertionsTest {

  private static final Object SERIALISABLE_RESULT = "hello";
  private static final Matcher<String> ANY_RESULT = containsString(SERIALISABLE_RESULT.toString());

  private static WebAssertion<String> createResultCheckingWebAssertion() {
    Atom<String> stringAtom = getText();
    Matcher<String> stringMatcher = containsString(SERIALISABLE_RESULT.toString());
    ResultDescriber<Object> resultDescriber = new ToStringResultDescriber();
    return new ResultCheckingWebAssertion<>(stringAtom, stringMatcher, resultDescriber);
  }

  private static CheckResultAssertionProto checkAssertion_transformationToProto(
      CheckResultWebAssertion webAssertion) {
    CheckResultAssertionProto checkResultWebAssertionProto =
        (CheckResultAssertionProto)
            new CheckResultWebAssertionRemoteMessage(webAssertion).toProto();
    assertThat(checkResultWebAssertionProto, notNullValue());
    return checkResultWebAssertionProto;
  }

  private static void checkAssertion_transformationFromProto(CheckResultWebAssertion webAssertion) {
    CheckResultAssertionProto checkResultAssertionProto =
        checkAssertion_transformationToProto(webAssertion);
    Object checkResultWebAssertionFromProto =
        CheckResultWebAssertionRemoteMessage.FROM.fromProto(checkResultAssertionProto);

    assertThat(checkResultWebAssertionFromProto, notNullValue());
    assertThat(checkResultWebAssertionFromProto, instanceOf(CheckResultWebAssertion.class));
  }

  @Before
  public void registerWebActionsWithRegistry() {
    RemoteDescriptorRegistry descriptorRegistry = RemoteDescriptorRegistry.getInstance();
    RemoteWebDriverAtoms.init(descriptorRegistry);
    RemoteWebModelAtoms.init(descriptorRegistry);
    RemoteHamcrestCoreMatchers13.init(descriptorRegistry);
    RemoteWebViewAssertions.init(descriptorRegistry);
  }

  @Test
  public void checkResultAssertion_serializableResult_transformationToProto() {
    WebAssertion<String> resultCheckingWebAssertion = createResultCheckingWebAssertion();
    CheckResultWebAssertion checkResultWebAssertion =
        new CheckResultWebAssertion(SERIALISABLE_RESULT, resultCheckingWebAssertion);
    checkAssertion_transformationToProto(checkResultWebAssertion);
  }

  @Test
  public void checkAssertion_serializableResult_transformationFromProto() {
    WebAssertion<String> resultCheckingWebAssertion = createResultCheckingWebAssertion();
    CheckResultWebAssertion checkResultWebAssertion =
        new CheckResultWebAssertion(SERIALISABLE_RESULT, resultCheckingWebAssertion);
    checkAssertion_transformationFromProto(checkResultWebAssertion);
  }

  @Test
  public void checkAssertion_anyResult_transformationFromProto() {
    WebAssertion<String> resultCheckingWebAssertion = createResultCheckingWebAssertion();
    CheckResultWebAssertion checkResultWebAssertion =
        new CheckResultWebAssertion(ANY_RESULT, resultCheckingWebAssertion);
    checkAssertion_transformationFromProto(checkResultWebAssertion);
  }

  @Test
  public void checkResultAssertion_anyResult_transformationToProto() {
    WebAssertion<String> resultCheckingWebAssertion = createResultCheckingWebAssertion();
    CheckResultWebAssertion checkResultWebAssertion =
        new CheckResultWebAssertion(ANY_RESULT, resultCheckingWebAssertion);
    checkAssertion_transformationToProto(checkResultWebAssertion);
  }

  @Test
  public void toStringResultDescriber_transformationToProto() {
    ToStringResultDescriber toStringResultDescriber = new ToStringResultDescriber();
    ToStringResultDescriberProto toStringResultDescriberProto =
        (ToStringResultDescriberProto) new GenericRemoteMessage(toStringResultDescriber).toProto();

    assertThat(toStringResultDescriberProto, notNullValue());
  }

  @Test
  public void toStringResultDescriber_transformationFromProto() {
    ToStringResultDescriber toStringResultDescriber = new ToStringResultDescriber();
    ToStringResultDescriberProto toStringResultDescriberProto =
        (ToStringResultDescriberProto) new GenericRemoteMessage(toStringResultDescriber).toProto();

    Object toStringResultDescriberFromProto =
        GenericRemoteMessage.FROM.fromProto(toStringResultDescriberProto);

    assertThat(toStringResultDescriberFromProto, notNullValue());
    assertThat(toStringResultDescriberFromProto, instanceOf(ToStringResultDescriber.class));
  }

  @Test
  public void webContentResultDescriber_transformationToProto() {
    WebContentResultDescriber webContentResultDescriber = new WebContentResultDescriber();
    WebContentResultDescriberProto toStringResultDescriberProto =
        (WebContentResultDescriberProto)
            new GenericRemoteMessage(webContentResultDescriber).toProto();

    assertThat(toStringResultDescriberProto, notNullValue());
  }

  @Test
  public void webContentResultDescriber_transformationFromProto() {
    WebContentResultDescriber webContentResultDescriber = new WebContentResultDescriber();
    WebContentResultDescriberProto webContentResultDescriberProto =
        (WebContentResultDescriberProto)
            new GenericRemoteMessage(webContentResultDescriber).toProto();

    Object webContentResultDescriberFromProto =
        GenericRemoteMessage.FROM.fromProto(webContentResultDescriberProto);

    assertThat(webContentResultDescriberFromProto, notNullValue());
    assertThat(webContentResultDescriberFromProto, instanceOf(WebContentResultDescriber.class));
  }

  @Test
  public void documentParserAtom_transformationToProto() {
    DocumentParserAtom documentParserAtom = new DocumentParserAtom();
    DocumentParserAtomProto documentParserAtomProto =
        (DocumentParserAtomProto) new GenericRemoteMessage(documentParserAtom).toProto();

    assertThat(documentParserAtomProto, notNullValue());
  }

  @Test
  public void documentParserAtom_transformationFromProto() {
    DocumentParserAtom documentParserAtom = new DocumentParserAtom();
    DocumentParserAtomProto documentParserAtomProto =
        (DocumentParserAtomProto) new GenericRemoteMessage(documentParserAtom).toProto();

    Object documentParserAtomFromProto =
        GenericRemoteMessage.FROM.fromProto(documentParserAtomProto);

    assertThat(documentParserAtomFromProto, notNullValue());
    assertThat(documentParserAtomFromProto, instanceOf(DocumentParserAtom.class));
  }
}
