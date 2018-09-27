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

import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptor;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import androidx.test.espresso.web.assertion.WebAssertion.CheckResultWebAssertion;
import androidx.test.espresso.web.assertion.WebViewAssertions.DocumentParserAtom;
import androidx.test.espresso.web.assertion.WebViewAssertions.ResultCheckingWebAssertion;
import androidx.test.espresso.web.assertion.WebViewAssertions.ToStringResultDescriber;
import androidx.test.espresso.web.assertion.WebViewAssertions.WebContentResultDescriber;
import androidx.test.espresso.web.proto.assertion.WebAssertions.CheckResultAssertionProto;
import androidx.test.espresso.web.proto.assertion.WebAssertions.DocumentParserAtomProto;
import androidx.test.espresso.web.proto.assertion.WebAssertions.ResultCheckingWebAssertionProto;
import androidx.test.espresso.web.proto.assertion.WebAssertions.ToStringResultDescriberProto;
import androidx.test.espresso.web.proto.assertion.WebAssertions.WebContentResultDescriberProto;
import java.util.Arrays;

/**
 * Registers all supported Espresso remote web assertions with the {@link RemoteDescriptorRegistry}.
 */
public class RemoteWebViewAssertions {
  public static void init(RemoteDescriptorRegistry remoteDescriptorRegistry) {
    remoteDescriptorRegistry.registerRemoteTypeArgs(
        Arrays.asList(
            new RemoteDescriptor.Builder()
                .setInstanceType(CheckResultWebAssertion.class)
                .setRemoteType(CheckResultWebAssertionRemoteMessage.class)
                .setProtoType(CheckResultAssertionProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(ResultCheckingWebAssertion.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(ResultCheckingWebAssertionProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(ToStringResultDescriber.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(ToStringResultDescriberProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WebContentResultDescriber.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WebContentResultDescriberProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(DocumentParserAtom.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(DocumentParserAtomProto.class)
                .build()));
  }
}
