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

package androidx.test.espresso.web.webdriver;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import androidx.test.espresso.web.model.Atom;
import androidx.test.espresso.web.model.ElementReference;
import androidx.test.espresso.web.model.Evaluation;
import androidx.test.espresso.web.model.RemoteWebModelAtoms;
import androidx.test.espresso.web.model.WindowReference;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.ClearElementSimpleAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.FindElementTransformingAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.FindMultipleElementsTransformingAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.GetTextTransformingAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.SelectActiveElementTransformingAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.SelectFrameByIdOrNameTransformingAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.SelectFrameByIndexTransformingAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.WebClickSimpleAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.WebKeysSimpleAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.WebScrollIntoViewAtomProto;
import androidx.test.espresso.web.webdriver.DriverAtoms.ClearElementSimpleAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.FindElementTransformingAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.FindMultipleElementsTransformingAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.GetTextTransformingAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.SelectActiveElementTransformingAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.SelectFrameByIdOrNameTransformingAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.SelectFrameByIndexTransformingAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.WebClickSimpleAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.WebKeysSimpleAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.WebScrollIntoViewAtom;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Remote message transformation related test for all webdriver atoms */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class RemoteWebDriverAtomsTest {

  @Before
  public void registerWebdriverAtomsWithRegistry() {
    RemoteDescriptorRegistry remoteDescriptorRegistry = RemoteDescriptorRegistry.getInstance();
    RemoteWebDriverAtoms.init(remoteDescriptorRegistry);
    RemoteWebModelAtoms.init(remoteDescriptorRegistry);
  }

  @Test
  public void findElement_transformationToProto() {
    Atom<ElementReference> findElement = DriverAtoms.findElement(Locator.ID, "test");

    FindElementTransformingAtomProto findElementProto =
        (FindElementTransformingAtomProto) new GenericRemoteMessage(findElement).toProto();
    assertThat(findElementProto, notNullValue());
  }

  @Test
  public void findElement_transformationFromProto() {
    Atom<ElementReference> findElement = DriverAtoms.findElement(Locator.ID, "test");

    FindElementTransformingAtomProto findElementProto =
        (FindElementTransformingAtomProto) new GenericRemoteMessage(findElement).toProto();
    Atom<ElementReference> findElementFromProto =
        (Atom<ElementReference>) GenericRemoteMessage.FROM.fromProto(findElementProto);

    assertThat(findElementFromProto, notNullValue());
    assertThat(findElementFromProto, instanceOf(FindElementTransformingAtom.class));
  }

  @Test
  public void clearElement_transformationToProto() {
    Atom<Evaluation> clearElement = DriverAtoms.clearElement();

    ClearElementSimpleAtomProto clearElementProto =
        (ClearElementSimpleAtomProto) new GenericRemoteMessage(clearElement).toProto();
    assertThat(clearElementProto, notNullValue());
  }

  @Test
  public void clearElement_transformationFromProto() {
    Atom<Evaluation> clearElement = DriverAtoms.clearElement();

    ClearElementSimpleAtomProto clearElementProto =
        (ClearElementSimpleAtomProto) new GenericRemoteMessage(clearElement).toProto();

    Atom<ElementReference> clearElementFromProto =
        (Atom<ElementReference>) GenericRemoteMessage.FROM.fromProto(clearElementProto);

    assertThat(clearElementFromProto, notNullValue());
    assertThat(clearElementFromProto, instanceOf(ClearElementSimpleAtom.class));
  }

  @Test
  public void webKeys_transformationToProto() {
    Atom<Evaluation> webKeys = DriverAtoms.webKeys("test");

    WebKeysSimpleAtomProto webKeysSimpleAtomProto =
        (WebKeysSimpleAtomProto) new GenericRemoteMessage(webKeys).toProto();
    assertThat(webKeysSimpleAtomProto, notNullValue());
  }

  @Test
  public void webKeys_transformationFromProto() {
    Atom<Evaluation> webKeys = DriverAtoms.webKeys("test");

    WebKeysSimpleAtomProto webKeysSimpleAtomProto =
        (WebKeysSimpleAtomProto) new GenericRemoteMessage(webKeys).toProto();
    Atom<Evaluation> webKeysSimpleAtomProtoFromProto =
        (Atom<Evaluation>) GenericRemoteMessage.FROM.fromProto(webKeysSimpleAtomProto);

    assertThat(webKeysSimpleAtomProtoFromProto, notNullValue());
    assertThat(webKeysSimpleAtomProtoFromProto, instanceOf(WebKeysSimpleAtom.class));
  }

  @Test
  public void webClick_transformationToProto() {
    Atom<Evaluation> webClick = DriverAtoms.webClick();

    WebClickSimpleAtomProto webClickProto =
        (WebClickSimpleAtomProto) new GenericRemoteMessage(webClick).toProto();
    assertThat(webClickProto, notNullValue());
  }

  @Test
  public void webClick_transformationFromProto() {
    Atom<Evaluation> webClick = DriverAtoms.webClick();

    WebClickSimpleAtomProto webClickProto =
        (WebClickSimpleAtomProto) new GenericRemoteMessage(webClick).toProto();
    assertThat(webClickProto, notNullValue());

    Atom<ElementReference> webClickFromProto =
        (Atom<ElementReference>) GenericRemoteMessage.FROM.fromProto(webClickProto);

    assertThat(webClickFromProto, notNullValue());
    assertThat(webClickFromProto, instanceOf(WebClickSimpleAtom.class));
  }

  @Test
  public void getText_transformationToProto() {
    Atom<String> getText = DriverAtoms.getText();

    GetTextTransformingAtomProto getTextProto =
        (GetTextTransformingAtomProto) new GenericRemoteMessage(getText).toProto();
    assertThat(getTextProto, notNullValue());
  }

  @Test
  public void getText_transformationFromProto() {
    Atom<String> getText = DriverAtoms.getText();

    GetTextTransformingAtomProto getTextProto =
        (GetTextTransformingAtomProto) new GenericRemoteMessage(getText).toProto();

    Object getTextFromProto = GenericRemoteMessage.FROM.fromProto(getTextProto);

    assertThat(getTextFromProto, notNullValue());
    assertThat(getTextFromProto, instanceOf(GetTextTransformingAtom.class));
  }

  @Test
  public void selectActiveElement_transformationToProto() {
    Atom<ElementReference> selectActiveElement = DriverAtoms.selectActiveElement();

    SelectActiveElementTransformingAtomProto selectActiveElementTransformingAtomProto =
        (SelectActiveElementTransformingAtomProto)
            new GenericRemoteMessage(selectActiveElement).toProto();
    assertThat(selectActiveElementTransformingAtomProto, notNullValue());
  }

  @Test
  public void selectActiveElement_transformationFromProto() {
    Atom<ElementReference> selectActiveElement = DriverAtoms.selectActiveElement();

    SelectActiveElementTransformingAtomProto selectActiveElementTransformingAtomProto =
        (SelectActiveElementTransformingAtomProto)
            new GenericRemoteMessage(selectActiveElement).toProto();
    assertThat(selectActiveElementTransformingAtomProto, notNullValue());

    Atom<ElementReference> selectActiveElementTransformingAtomProtoFromProto =
        (Atom<ElementReference>)
            GenericRemoteMessage.FROM.fromProto(selectActiveElementTransformingAtomProto);

    assertThat(selectActiveElementTransformingAtomProtoFromProto, notNullValue());
    assertThat(
        selectActiveElementTransformingAtomProtoFromProto,
        instanceOf(SelectActiveElementTransformingAtom.class));
  }

  @Test
  public void selectFrameByIndex_transformationToProto() {
    Atom<WindowReference> selectFrameByIndex = DriverAtoms.selectFrameByIndex(1);

    SelectFrameByIndexTransformingAtomProto selectFrameByIndexTransformingAtomProto =
        (SelectFrameByIndexTransformingAtomProto)
            new GenericRemoteMessage(selectFrameByIndex).toProto();
    assertThat(selectFrameByIndexTransformingAtomProto, notNullValue());
  }

  @Test
  public void selectFrameByIndex_transformationFromProto() {
    Atom<WindowReference> selectFrameByIndex = DriverAtoms.selectFrameByIndex(1);

    SelectFrameByIndexTransformingAtomProto selectFrameByIndexTransformingAtomProto =
        (SelectFrameByIndexTransformingAtomProto)
            new GenericRemoteMessage(selectFrameByIndex).toProto();
    assertThat(selectFrameByIndexTransformingAtomProto, notNullValue());

    Atom<WindowReference> selectFrameByIndexTransformingAtomFromProto =
        (Atom<WindowReference>)
            GenericRemoteMessage.FROM.fromProto(selectFrameByIndexTransformingAtomProto);

    assertThat(selectFrameByIndexTransformingAtomFromProto, notNullValue());
    assertThat(
        selectFrameByIndexTransformingAtomFromProto,
        instanceOf(SelectFrameByIndexTransformingAtom.class));
  }

  @Test
  public void selectFrameByIdOrName_transformationToProto() {
    Atom<WindowReference> windowReferenceAtom = DriverAtoms.selectFrameByIdOrName("test");

    SelectFrameByIdOrNameTransformingAtomProto selectFrameByIdOrNameTransformingAtomProto =
        (SelectFrameByIdOrNameTransformingAtomProto)
            new GenericRemoteMessage(windowReferenceAtom).toProto();
    assertThat(selectFrameByIdOrNameTransformingAtomProto, notNullValue());
  }

  @Test
  public void selectFrameByIdOrName_transformationFromProto() {
    Atom<WindowReference> windowReferenceAtom = DriverAtoms.selectFrameByIdOrName("test");

    SelectFrameByIdOrNameTransformingAtomProto selectFrameByIdOrNameTransformingAtomProto =
        (SelectFrameByIdOrNameTransformingAtomProto)
            new GenericRemoteMessage(windowReferenceAtom).toProto();
    assertThat(selectFrameByIdOrNameTransformingAtomProto, notNullValue());

    Atom<WindowReference> selectFrameByIdOrNameTransformingAtomFromProto =
        (Atom<WindowReference>)
            GenericRemoteMessage.FROM.fromProto(selectFrameByIdOrNameTransformingAtomProto);

    assertThat(selectFrameByIdOrNameTransformingAtomFromProto, notNullValue());
    assertThat(
        selectFrameByIdOrNameTransformingAtomFromProto,
        instanceOf(SelectFrameByIdOrNameTransformingAtom.class));
  }

  @Test
  public void findMultipleElements_transformationToProto() {
    Atom<List<ElementReference>> multipleElements =
        DriverAtoms.findMultipleElements(Locator.ID, "test");

    FindMultipleElementsTransformingAtomProto findMultipleElementsTransformingAtomProto =
        (FindMultipleElementsTransformingAtomProto)
            new GenericRemoteMessage(multipleElements).toProto();
    assertThat(findMultipleElementsTransformingAtomProto, notNullValue());
  }

  @Test
  public void findMultipleElements_transformationFromProto() {
    Atom<List<ElementReference>> multipleElements =
        DriverAtoms.findMultipleElements(Locator.ID, "test");

    FindMultipleElementsTransformingAtomProto findMultipleElementsTransformingAtomProto =
        (FindMultipleElementsTransformingAtomProto)
            new GenericRemoteMessage(multipleElements).toProto();
    Object findMultipleElementsTransformingAtomFromProto =
        GenericRemoteMessage.FROM.fromProto(findMultipleElementsTransformingAtomProto);

    assertThat(findMultipleElementsTransformingAtomFromProto, notNullValue());
    assertThat(
        findMultipleElementsTransformingAtomFromProto,
        instanceOf(FindMultipleElementsTransformingAtom.class));
  }

  @Test
  public void webScrollIntoView_transformationToProto() {
    Atom<Boolean> webScrollIntoView = DriverAtoms.webScrollIntoView();

    WebScrollIntoViewAtomProto webScrollIntoViewAtomProto =
        (WebScrollIntoViewAtomProto) new GenericRemoteMessage(webScrollIntoView).toProto();
    assertThat(webScrollIntoViewAtomProto, notNullValue());
  }

  @Test
  public void webScrollIntoView_transformationFromProto() {
    Atom<Boolean> webScrollIntoView = DriverAtoms.webScrollIntoView();

    WebScrollIntoViewAtomProto webScrollIntoViewAtomProto =
        (WebScrollIntoViewAtomProto) new GenericRemoteMessage(webScrollIntoView).toProto();
    Object webScrollIntoViewAtomProtomFromProto =
        GenericRemoteMessage.FROM.fromProto(webScrollIntoViewAtomProto);

    assertThat(webScrollIntoViewAtomProtomFromProto, notNullValue());
    assertThat(webScrollIntoViewAtomProtomFromProto, instanceOf(WebScrollIntoViewAtom.class));
  }
}
