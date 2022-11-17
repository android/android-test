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

import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptor;
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
import java.util.Arrays;

/**
 * Registers all supported Espresso web {@link DomMatchers} with the {@link
 * RemoteDescriptorRegistry}.
 */
public class RemoteWebMatchers {

  public static void init(RemoteDescriptorRegistry remoteDescriptorRegistry) {
    remoteDescriptorRegistry.registerRemoteTypeArgs(
        Arrays.asList(
            new RemoteDescriptor.Builder()
                .setInstanceType(WithBodyMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithBodyMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(HasElementWithIdMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(HasElementWithIdMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(ElementByIdMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(ElementByIdMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(HasElementWithXPathMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(HasElementWithXPathMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(ElementByXPathMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(ElementByXPathMatcherProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WithTextContentMatcher.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithTextContentMatcherProto.class)
                .build()));
  }
}
