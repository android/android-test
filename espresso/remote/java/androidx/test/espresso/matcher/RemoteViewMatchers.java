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
package androidx.test.espresso.matcher;

import androidx.test.espresso.matcher.ViewMatchers.HasChildCountMatcher;
import androidx.test.espresso.matcher.ViewMatchers.HasContentDescriptionMatcher;
import androidx.test.espresso.matcher.ViewMatchers.HasDescendantMatcher;
import androidx.test.espresso.matcher.ViewMatchers.HasErrorTextMatcher;
import androidx.test.espresso.matcher.ViewMatchers.HasFocusMatcher;
import androidx.test.espresso.matcher.ViewMatchers.HasImeActionMatcher;
import androidx.test.espresso.matcher.ViewMatchers.HasLinksMatcher;
import androidx.test.espresso.matcher.ViewMatchers.HasMinimumChildCountMatcher;
import androidx.test.espresso.matcher.ViewMatchers.HasSiblingMatcher;
import androidx.test.espresso.matcher.ViewMatchers.IsAssignableFromMatcher;
import androidx.test.espresso.matcher.ViewMatchers.IsClickableMatcher;
import androidx.test.espresso.matcher.ViewMatchers.IsDescendantOfAMatcher;
import androidx.test.espresso.matcher.ViewMatchers.IsDisplayedMatcher;
import androidx.test.espresso.matcher.ViewMatchers.IsDisplayingAtLeastMatcher;
import androidx.test.espresso.matcher.ViewMatchers.IsEnabledMatcher;
import androidx.test.espresso.matcher.ViewMatchers.IsFocusableMatcher;
import androidx.test.espresso.matcher.ViewMatchers.IsJavascriptEnabledMatcher;
import androidx.test.espresso.matcher.ViewMatchers.IsRootMatcher;
import androidx.test.espresso.matcher.ViewMatchers.IsSelectedMatcher;
import androidx.test.espresso.matcher.ViewMatchers.SupportsInputMethodsMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithAlphaMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithCharSequenceMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithCheckBoxStateMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithChildMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithClassNameMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithContentDescriptionFromIdMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithContentDescriptionMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithEffectiveVisibilityMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithHintMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithIdMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithInputTypeMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithParentIndexMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithParentMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithResourceNameMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithSpinnerTextIdMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithSpinnerTextMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithTagKeyMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithTagValueMatcher;
import androidx.test.espresso.matcher.ViewMatchers.WithTextMatcher;
import androidx.test.espresso.proto.matcher.ViewMatchers.HasChildCountMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.HasContentDescriptionMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.HasDescendantMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.HasErrorTextMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.HasFocusMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.HasImeActionMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.HasLinksMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.HasMinimumChildCountMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.HasSiblingMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.IsAssignableFromMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.IsClickableMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.IsDescendantOfAMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.IsDisplayedMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.IsDisplayingAtLeastMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.IsEnabledMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.IsFocusableMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.IsJavascriptEnabledMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.IsRootMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.IsSelectedMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.SupportsInputMethodsMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithAlphaMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithCharSequenceMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithCheckBoxStateMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithChildMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithClassNameMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithContentDescriptionFromIdMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithContentDescriptionMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithEffectiveVisibilityMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithHintMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithIdMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithInputTypeMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithParentIndexMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithParentMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithResourceNameMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithSpinnerTextIdMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithSpinnerTextMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithTagKeyMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithTagValueMatcherProto;
import androidx.test.espresso.proto.matcher.ViewMatchers.WithTextMatcherProto;
import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptor;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import java.util.Arrays;

/**
 * Registers all supported Espresso remote view matchers with the {@link RemoteDescriptorRegistry}.
 */
public final class RemoteViewMatchers {

  public static void init(RemoteDescriptorRegistry remoteDescriptorRegistry) {
    remoteDescriptorRegistry.registerRemoteTypeArgs(
        Arrays.asList(
            new RemoteDescriptor.Builder()
                .setInstanceType(WithIdMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithIdMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WithResourceNameMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithResourceNameMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WithTagKeyMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithTagKeyMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WithTextMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithTextMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(IsAssignableFromMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(IsAssignableFromMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(IsDisplayedMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(IsDisplayedMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(IsDisplayingAtLeastMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(IsDisplayingAtLeastMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WithClassNameMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithClassNameMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(IsEnabledMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(IsEnabledMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(IsFocusableMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(IsFocusableMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(HasFocusMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(HasFocusMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(IsSelectedMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(IsSelectedMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(HasSiblingMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(HasSiblingMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WithContentDescriptionFromIdMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithContentDescriptionFromIdMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WithContentDescriptionMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithContentDescriptionMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WithTagValueMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithTagValueMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WithCharSequenceMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithCharSequenceMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WithHintMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithHintMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WithCheckBoxStateMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithCheckBoxStateMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(HasContentDescriptionMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(HasContentDescriptionMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(HasDescendantMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(HasDescendantMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(IsClickableMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(IsClickableMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(IsDescendantOfAMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(IsDescendantOfAMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WithEffectiveVisibilityMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithEffectiveVisibilityMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WithAlphaMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithAlphaMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WithParentMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithParentMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WithChildMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithChildMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(HasChildCountMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(HasChildCountMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(HasMinimumChildCountMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(HasMinimumChildCountMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(IsRootMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(IsRootMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(SupportsInputMethodsMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(SupportsInputMethodsMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(HasImeActionMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(HasImeActionMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(HasLinksMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(HasLinksMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WithSpinnerTextIdMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithSpinnerTextIdMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WithSpinnerTextMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithSpinnerTextMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(IsJavascriptEnabledMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(IsJavascriptEnabledMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(HasErrorTextMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(HasErrorTextMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WithInputTypeMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithInputTypeMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WithParentIndexMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithParentIndexMatcherProto.class)
                .build()));
  }

  private RemoteViewMatchers() {
    // noOp instance
  }
}
