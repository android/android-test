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

package androidx.test.espresso.remote;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import androidx.test.espresso.proto.TestProtos.TestProto;
import androidx.test.espresso.remote.TestTypes.TestType;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import java.util.Arrays;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link RemoteDescriptorRegistry} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class RemoteDescriptorRegistryTest {

  private static final RemoteDescriptor REMOTE_TYPE_ARG =
      new RemoteDescriptor.Builder()
          .setInstanceType(TestType.class)
          .setRemoteType(TestTypeRemoteMessage.class)
          .setProtoType(TestProto.class)
          .setProtoBuilderType(TestProto.Builder.class)
          .setProtoParser(TestProto.parser())
          .build();

  private RemoteDescriptorRegistry typeRegistry;

  @Before
  public void buildRegistry() {
    typeRegistry = new RemoteDescriptorRegistry();
  }

  @Test
  public void registerRemoteTypeArg() {
    assertThat(typeRegistry.registerRemoteTypeArgs(Arrays.asList(REMOTE_TYPE_ARG)), is(true));
    assertThat(typeRegistry.registerRemoteTypeArgs(Arrays.asList(REMOTE_TYPE_ARG)), is(false));
  }

  @Test
  public void registerAndUnregisterRemoteTypeArg() {
    assertThat(typeRegistry.registerRemoteTypeArgs(Arrays.asList(REMOTE_TYPE_ARG)), is(true));
    typeRegistry.unregisterRemoteTypeArgs(Arrays.asList(REMOTE_TYPE_ARG));
    try {
      typeRegistry.unregisterRemoteTypeArgs(Arrays.asList(REMOTE_TYPE_ARG));
      fail();
    } catch (IllegalStateException ise) {
      // unregistering a remote arg twice throws
    }
  }

  @Test
  public void getRemoteTypeArg_ForTypeUrl() {
    typeRegistry.registerRemoteTypeArgs(Arrays.asList(REMOTE_TYPE_ARG));
    RemoteDescriptor remoteDescriptor = typeRegistry.argForRemoteTypeUrl(TestType.class.getName());
    assertThat(
        remoteDescriptor.getInstanceType(), is(CoreMatchers.<Class<?>>equalTo(TestType.class)));
  }

  @Test
  public void getRemoteTypeArg_ForTargetType() {
    typeRegistry.registerRemoteTypeArgs(Arrays.asList(REMOTE_TYPE_ARG));
    RemoteDescriptor remoteDescriptor = typeRegistry.argForInstanceType(TestType.class);
    assertThat(
        remoteDescriptor.getInstanceType(), is(CoreMatchers.<Class<?>>equalTo(TestType.class)));
  }

  @Test
  public void getRemoteTypeArg_ThatDoesNotExist_Throws() {
    try {
      typeRegistry.argForRemoteTypeUrl("Does not exist");
      fail("RemoteProtocolException expected");
    } catch (RemoteProtocolException rpe) {
      // expected
    }
  }
}
