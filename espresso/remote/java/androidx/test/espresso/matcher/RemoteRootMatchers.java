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

import androidx.test.espresso.matcher.RootMatchers.HasWindowFocus;
import androidx.test.espresso.matcher.RootMatchers.HasWindowLayoutParams;
import androidx.test.espresso.matcher.RootMatchers.IsDialog;
import androidx.test.espresso.matcher.RootMatchers.IsFocusable;
import androidx.test.espresso.matcher.RootMatchers.IsPlatformPopup;
import androidx.test.espresso.matcher.RootMatchers.IsSubwindowOfCurrentActivity;
import androidx.test.espresso.matcher.RootMatchers.IsSystemAlertWindow;
import androidx.test.espresso.matcher.RootMatchers.IsTouchable;
import androidx.test.espresso.matcher.RootMatchers.WithDecorView;
import androidx.test.espresso.proto.matcher.RootMatchers.HasWindowFocusProto;
import androidx.test.espresso.proto.matcher.RootMatchers.HasWindowLayoutParamsProto;
import androidx.test.espresso.proto.matcher.RootMatchers.IsDialogProto;
import androidx.test.espresso.proto.matcher.RootMatchers.IsFocusableProto;
import androidx.test.espresso.proto.matcher.RootMatchers.IsPlatformPopupProto;
import androidx.test.espresso.proto.matcher.RootMatchers.IsSubwindowOfCurrentActivityProto;
import androidx.test.espresso.proto.matcher.RootMatchers.IsSystemAlertWindowProto;
import androidx.test.espresso.proto.matcher.RootMatchers.IsTouchableProto;
import androidx.test.espresso.proto.matcher.RootMatchers.WithDecorViewProto;
import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptor;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import java.util.Arrays;

/** Registers all Espresso remote root matchers with the {@link RemoteDescriptorRegistry}. */
public final class RemoteRootMatchers {

  private RemoteRootMatchers() {
    // noOp instance
  }

  public static void init(RemoteDescriptorRegistry remoteDescriptorRegistry) {
    remoteDescriptorRegistry.registerRemoteTypeArgs(
        Arrays.asList(
            new RemoteDescriptor.Builder()
                .setInstanceType(IsFocusable.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(IsFocusableProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(IsTouchable.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(IsTouchableProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(IsDialog.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(IsDialogProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(IsSystemAlertWindow.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(IsSystemAlertWindowProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(IsPlatformPopup.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(IsPlatformPopupProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(WithDecorView.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(WithDecorViewProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(HasWindowFocus.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(HasWindowFocusProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(HasWindowLayoutParams.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(HasWindowLayoutParamsProto.class)
                .build(),
            new RemoteDescriptor.Builder()
                .setInstanceType(IsSubwindowOfCurrentActivity.class)
                .setRemoteType(GenericRemoteMessage.class)
                .setProtoType(IsSubwindowOfCurrentActivityProto.class)
                .build()));
  }
}
