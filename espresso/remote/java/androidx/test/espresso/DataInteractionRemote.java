/*
 * Copyright (C) 2014 The Android Open Source Project
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
 */

package androidx.test.espresso;

import android.util.Log;
import androidx.test.espresso.DataInteraction.DisplayDataMatcher;
import androidx.test.espresso.proto.matcher.ViewMatchers.DisplayDataMatcherProto;
import androidx.test.espresso.remote.RemoteDescriptor;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import java.util.Arrays;

/**
 * Registers all supported DataInteraction remote features with the {@link RemoteDescriptorRegistry}
 */
public final class DataInteractionRemote {
  private static final String TAG = "DIRemote";

  private DataInteractionRemote() {}

  /**
   * Registers this matcher with the {@link RemoteDescriptorRegistry}.
   *
   * <p>Note: This is an internal method, do not call from test code!
   *
   * @param remoteDescriptorRegistry the remoteDescriptorRegistry passed in from espresso remote
   */
  public static void init(RemoteDescriptorRegistry remoteDescriptorRegistry) {
    try {
      remoteDescriptorRegistry.registerRemoteTypeArgs(
          Arrays.asList(
              new RemoteDescriptor.Builder()
                  .setInstanceType(DisplayDataMatcher.class)
                  .setRemoteType(
                      Class.forName(
                          "androidx.test.espresso."
                              + "remote.GenericRemoteMessage") /* Avoid dep on proto utils */)
                  .setProtoType(DisplayDataMatcherProto.class)
                  .build()));
    } catch (ClassNotFoundException cnfe) {
      Log.w(TAG, "Cannot register DisplayData matcher", cnfe);
    }
  }
}
