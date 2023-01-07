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

import static kotlin.collections.CollectionsKt.listOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import androidx.test.espresso.web.model.Atoms.ScriptWithArgsSimpleAtom;
import androidx.test.espresso.web.proto.model.WebModelAtoms.ScriptWithArgsSimpleAtomProto;
import androidx.test.espresso.web.proto.model.WebModelAtoms.TransformingAtomProto;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Remote message transformation related test for all {@link Atoms} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class RemoteAtomsTest {

  private static final String SCRIPT = "return document.title;";
  private static final Object ARG1 = "arg1";
  private static final Object ARG2 = "arg1";
  private static final List<Object> ARGS = listOf(ARG1, ARG2);

  private static TransformingAtomProto transformingAtom_transformationToProto(Atom atom) {
    TransformingAtomProto atomProto =
        (TransformingAtomProto) new GenericRemoteMessage(atom).toProto();
    assertThat(atomProto, notNullValue());
    return atomProto;
  }

  private static TransformingAtom transformingAtom_transformationFromProto(Atom atom) {
    TransformingAtomProto atomProto = transformingAtom_transformationToProto(atom);
    Object objectFromProto = GenericRemoteMessage.FROM.fromProto(atomProto);

    assertThat(objectFromProto, notNullValue());
    assertThat(objectFromProto, Matchers.instanceOf(TransformingAtom.class));

    TransformingAtom transformingAtomFromProto = (TransformingAtom) objectFromProto;
    assertThat(atomProto, notNullValue());
    return transformingAtomFromProto;
  }

  @Before
  public void registerWebModelsWithRegistry() {
    RemoteDescriptorRegistry remoteDescriptorRegistry = RemoteDescriptorRegistry.getInstance();
    RemoteWebModelAtoms.init(remoteDescriptorRegistry);
  }

  @Test
  public void scriptWithArgs_transformationToProto() {
    ScriptWithArgsSimpleAtomProto scriptWithArgsProto =
        new ScriptWithArgsSimpleAtomRemoteMessage(
                (ScriptWithArgsSimpleAtom) Atoms.scriptWithArgs(SCRIPT, ARGS))
            .toProto();
    assertThat(scriptWithArgsProto, notNullValue());

    assertThat(scriptWithArgsProto.getScript(), equalTo(SCRIPT));
    assertThat(scriptWithArgsProto.getArgsList(), hasSize(2));
  }

  @Test
  public void scriptWithArgs_transformationFromProto() {
    ScriptWithArgsSimpleAtomProto scriptWithArgsProto =
        new ScriptWithArgsSimpleAtomRemoteMessage(
                (ScriptWithArgsSimpleAtom) Atoms.scriptWithArgs(SCRIPT, ARGS))
            .toProto();

    Object objectFromProto =
        ScriptWithArgsSimpleAtomRemoteMessage.FROM.fromProto(scriptWithArgsProto);

    assertThat(objectFromProto, notNullValue());
    assertThat(objectFromProto, instanceOf(ScriptWithArgsSimpleAtom.class));

    ScriptWithArgsSimpleAtom scriptWithArgsFromProto = (ScriptWithArgsSimpleAtom) objectFromProto;
    assertThat(scriptWithArgsFromProto.getScript(), equalTo(SCRIPT));
    assertThat(scriptWithArgsFromProto.getNonContextualArguments(), hasItems(ARG1, ARG2));
  }

  @Test
  public void scriptWithArgs_emptyArgs_transformationToProto() {
    ScriptWithArgsSimpleAtomProto scriptWithArgsProto =
        new ScriptWithArgsSimpleAtomRemoteMessage(
                (ScriptWithArgsSimpleAtom) Atoms.scriptWithArgs(SCRIPT, listOf()))
            .toProto();
    assertThat(scriptWithArgsProto, notNullValue());

    assertThat(scriptWithArgsProto.getScript(), equalTo(SCRIPT));
    assertThat(scriptWithArgsProto.getArgsList(), hasSize(0));
  }

  @Test
  public void scriptWithArgs_emptyArgs_transformationFromProto() {
    ScriptWithArgsSimpleAtomProto scriptWithArgsProto =
        new ScriptWithArgsSimpleAtomRemoteMessage(
                (ScriptWithArgsSimpleAtom) Atoms.scriptWithArgs(SCRIPT, listOf()))
            .toProto();

    Object objectFromProto =
        ScriptWithArgsSimpleAtomRemoteMessage.FROM.fromProto(scriptWithArgsProto);

    assertThat(objectFromProto, notNullValue());
    assertThat(objectFromProto, instanceOf(ScriptWithArgsSimpleAtom.class));

    ScriptWithArgsSimpleAtom scriptWithArgsFromProto = (ScriptWithArgsSimpleAtom) objectFromProto;
    assertThat(scriptWithArgsFromProto.getScript(), equalTo(SCRIPT));
    assertThat(scriptWithArgsFromProto.getNonContextualArguments(), hasSize(0));
  }

  @Test
  public void getTitle_transformationToProto() {
    transformingAtom_transformationFromProto(Atoms.getTitle());
  }

  @Test
  public void getTitle_transformationFromProto() {
    TransformingAtom getTitleAtomFromProto =
        transformingAtom_transformationFromProto(Atoms.getTitle());
    assertThat(
        getTitleAtomFromProto.getScript(), equalTo("function getTitle() {return document.title;}"));
  }

  @Test
  public void getCurrentUrl_transformationToProto() {
    transformingAtom_transformationFromProto(Atoms.getCurrentUrl());
  }

  @Test
  public void getCurrentUrl_transformationFromProto() {
    TransformingAtom getCurrentUrlAtomProto =
        transformingAtom_transformationFromProto(Atoms.getCurrentUrl());
    assertThat(
        getCurrentUrlAtomProto.getScript(),
        equalTo("function getCurrentUrl() {return document.location.href;}"));
  }
}
