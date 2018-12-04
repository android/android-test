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

package androidx.test.espresso.web.model;

import static androidx.test.espresso.web.model.Atoms.castOrDie;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import androidx.test.espresso.web.model.Atoms.CastOrDieAtom;
import androidx.test.espresso.web.proto.model.WebModelAtoms.CastOrDieAtomProto;
import androidx.test.espresso.web.proto.model.WebModelAtoms.ElementReferenceProto;
import androidx.test.espresso.web.proto.model.WebModelAtoms.WindowReferenceProto;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Remote message transformation related test for all web models */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class RemoteWebModelAtomsTest {

  @Before
  public void registerWebModelsWithRegistry() {
    RemoteDescriptorRegistry remoteDescriptorRegistry = RemoteDescriptorRegistry.getInstance();
    RemoteWebModelAtoms.init(remoteDescriptorRegistry);
  }

  @Test
  public void castOrDie_transformationToProto() {
    TransformingAtom.Transformer<Evaluation, String> castOrDie = castOrDie(String.class);

    CastOrDieAtomProto castOrDieAtomProto =
        (CastOrDieAtomProto) new GenericRemoteMessage(castOrDie).toProto();
    assertThat(castOrDieAtomProto, notNullValue());
  }

  @Test
  public void castOrDie_transformationFromProto() {
    TransformingAtom.Transformer<Evaluation, String> castOrDie = castOrDie(String.class);

    CastOrDieAtomProto castOrDieAtomProto =
        (CastOrDieAtomProto) new GenericRemoteMessage(castOrDie).toProto();
    CastOrDieAtom castOrDieAtomFromProto =
        (CastOrDieAtom) GenericRemoteMessage.FROM.fromProto(castOrDieAtomProto);

    assertThat(castOrDieAtomFromProto, notNullValue());
    assertThat(castOrDieAtomFromProto, instanceOf(CastOrDieAtom.class));
  }

  @Test
  public void elementReference_transformationToProto() {
    ElementReference elementReference = new ElementReference("Hellp proto");

    ElementReferenceProto elementReferenceProto =
        (ElementReferenceProto) new GenericRemoteMessage(elementReference).toProto();
    assertThat(elementReferenceProto, notNullValue());
  }

  @Test
  public void elementReference_transformationFromProto() {
    ElementReference elementReference = new ElementReference("Hellp proto");

    ElementReferenceProto elementReferenceProto =
        (ElementReferenceProto) new GenericRemoteMessage(elementReference).toProto();

    ElementReference elementReferenceFromProto =
        (ElementReference) GenericRemoteMessage.FROM.fromProto(elementReferenceProto);

    assertThat(elementReferenceFromProto, notNullValue());
    assertThat(elementReferenceFromProto, instanceOf(ElementReference.class));
  }

  @Test
  public void windowReference_transformationToProto() {
    WindowReference windowReference = new WindowReference("Hellp proto");

    WindowReferenceProto windowReferenceProto =
        (WindowReferenceProto) new GenericRemoteMessage(windowReference).toProto();
    assertThat(windowReferenceProto, notNullValue());
  }

  @Test
  public void windowReference_transformationFromProto() {
    WindowReference windowReference = new WindowReference("Hellp proto");

    WindowReferenceProto windowReferenceProto =
        (WindowReferenceProto) new GenericRemoteMessage(windowReference).toProto();

    Object windowReferenceFromProto = GenericRemoteMessage.FROM.fromProto(windowReferenceProto);

    assertThat(windowReferenceFromProto, notNullValue());
    assertThat(windowReferenceFromProto, instanceOf(WindowReference.class));
  }
}
