/*
 * Copyright (C) 2016 The Android Open Source Project
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
package androidx.test.espresso.assertion;

import androidx.test.espresso.assertion.LayoutAssertions.NoOverlapsViewAssertion;
import androidx.test.espresso.assertion.ViewAssertions.DoesNotExistViewAssertion;
import androidx.test.espresso.assertion.ViewAssertions.MatchesViewAssertion;
import androidx.test.espresso.assertion.ViewAssertions.SelectedDescendantsMatchViewAssertion;
import androidx.test.espresso.proto.assertion.ViewAssertions.DoesNotExistViewAssertionProto;
import androidx.test.espresso.proto.assertion.ViewAssertions.MatchesViewAssertionProto;
import androidx.test.espresso.proto.assertion.ViewAssertions.NoOverlapsViewAssertionProto;
import androidx.test.espresso.proto.assertion.ViewAssertions.SelectedDescendantsMatchViewAssertionProto;
import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptor;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import java.util.Arrays;

/**
 * Registers all supported Espresso remote {@link ViewAssertions} with the {@link
 * RemoteDescriptorRegistry}.
 */
public final class RemoteViewAssertions {

  private RemoteViewAssertions() {
    // noOp instance
  }

  public static void init(RemoteDescriptorRegistry remoteDescriptorRegistry) {
    remoteDescriptorRegistry.registerRemoteTypeArgs(
        Arrays.asList(
            new RemoteDescriptor.Builder()
                .setInstanceType(MatchesViewAssertion.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(MatchesViewAssertionProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(DoesNotExistViewAssertion.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(DoesNotExistViewAssertionProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(SelectedDescendantsMatchViewAssertion.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(SelectedDescendantsMatchViewAssertionProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(NoOverlapsViewAssertion.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(NoOverlapsViewAssertionProto.class)
                .build()));
  }
}
