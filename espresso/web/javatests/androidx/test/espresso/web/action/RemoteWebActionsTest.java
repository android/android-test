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

package androidx.test.espresso.web.action;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import androidx.test.espresso.proto.TestProtos.TestAtomProto;
import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptor;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import androidx.test.espresso.remote.TestTypes.TestAtom;
import androidx.test.espresso.web.proto.action.WebActions.AtomActionProto;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Remote message transformation related test for all web actions */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class RemoteWebActionsTest {

  @Before
  public void registerWebActionsWithRegistry() {
    RemoteDescriptorRegistry remoteDescriptorRegistry = RemoteDescriptorRegistry.getInstance();
    RemoteWebActions.init(remoteDescriptorRegistry);

    // Fake test atom to aid with testing
    remoteDescriptorRegistry.registerRemoteTypeArgs(
        Arrays.asList(
            new RemoteDescriptor.Builder()
                .setInstanceType(TestAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(TestAtomProto.class)
                .build()));
  }

  @Test
  public void atomAction_transformationToProto() {
    AtomAction atomAction = new AtomAction<>(new TestAtom(), null, null);
    AtomActionProto atomActionProto = new AtomActionRemoteMessage(atomAction).toProto();
    assertThat(atomActionProto, notNullValue());
  }

  @Test
  public void atomAction_transformationFromProto() {
    AtomAction<Void> atomAction = new AtomAction<>(new TestAtom(), null, null);

    AtomActionProto atomActionProto = new AtomActionRemoteMessage(atomAction).toProto();
    AtomAction atomActionFromProto = AtomActionRemoteMessage.FROM.fromProto(atomActionProto);

    assertThat(atomActionFromProto, notNullValue());
    assertThat(atomActionFromProto, instanceOf(AtomAction.class));
  }
}
