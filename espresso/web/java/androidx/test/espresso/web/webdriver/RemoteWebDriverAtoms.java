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

import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptor;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.ActiveElementSimpleAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.ClearElementSimpleAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.ElementReferenceListAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.FindElementSimpleAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.FindElementTransformingAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.FindElementsScriptSimpleAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.FindMultipleElementsTransformingAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.FrameByIdOrNameSimpleAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.FrameByIdOrNameWithRootSimpleAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.FrameByIndexSimpleAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.FrameByIndexWithRootSimpleAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.GetTextTransformingAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.GetVisibleTextSimpleAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.SelectActiveElementTransformingAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.SelectFrameByIdOrNameTransformingAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.SelectFrameByIndexTransformingAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.WebClickSimpleAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.WebKeysSimpleAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.WebScrollIntoViewAtomProto;
import androidx.test.espresso.web.proto.webdriver.WebWebdriverAtoms.WebScrollIntoViewSimpleAtomProto;
import androidx.test.espresso.web.webdriver.DriverAtoms.ActiveElementSimpleAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.ClearElementSimpleAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.ElementReferenceListAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.FindElementSimpleAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.FindElementTransformingAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.FindElementsScriptSimpleAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.FindMultipleElementsTransformingAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.FrameByIdOrNameSimpleAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.FrameByIdOrNameWithRootSimpleAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.FrameByIndexSimpleAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.FrameByIndexWithRootSimpleAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.GetTextTransformingAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.GetVisibleTextSimpleAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.SelectActiveElementTransformingAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.SelectFrameByIdOrNameTransformingAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.SelectFrameByIndexTransformingAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.WebClickSimpleAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.WebKeysSimpleAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.WebScrollIntoViewAtom;
import androidx.test.espresso.web.webdriver.DriverAtoms.WebScrollIntoViewSimpleAtom;
import java.util.Arrays;

/** Registers all supported Espresso remote web driver with the {@link RemoteDescriptorRegistry}. */
public final class RemoteWebDriverAtoms {
  public static void init(RemoteDescriptorRegistry remoteDescriptorRegistry) {
    remoteDescriptorRegistry.registerRemoteTypeArgs(
        Arrays.asList(
            new RemoteDescriptor.Builder()
                .setInstanceType(FindElementSimpleAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(FindElementSimpleAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(FindElementTransformingAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(FindElementTransformingAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(ClearElementSimpleAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(ClearElementSimpleAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WebKeysSimpleAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WebKeysSimpleAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WebClickSimpleAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WebClickSimpleAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(GetTextTransformingAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(GetTextTransformingAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(GetVisibleTextSimpleAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(GetVisibleTextSimpleAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(ActiveElementSimpleAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(ActiveElementSimpleAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(SelectActiveElementTransformingAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(SelectActiveElementTransformingAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(FrameByIndexSimpleAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(FrameByIndexSimpleAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(FrameByIndexWithRootSimpleAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(FrameByIndexWithRootSimpleAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(SelectFrameByIndexTransformingAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(SelectFrameByIndexTransformingAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(FrameByIdOrNameSimpleAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(FrameByIdOrNameSimpleAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(FrameByIdOrNameWithRootSimpleAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(FrameByIdOrNameWithRootSimpleAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(SelectFrameByIdOrNameTransformingAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(SelectFrameByIdOrNameTransformingAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(FindElementsScriptSimpleAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(FindElementsScriptSimpleAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(FindMultipleElementsTransformingAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(FindMultipleElementsTransformingAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(ElementReferenceListAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(ElementReferenceListAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WebScrollIntoViewSimpleAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WebScrollIntoViewSimpleAtomProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WebScrollIntoViewAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WebScrollIntoViewAtomProto.class)
                .build()));
  }
}
