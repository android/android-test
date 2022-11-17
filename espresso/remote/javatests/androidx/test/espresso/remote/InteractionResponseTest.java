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

import static androidx.test.espresso.remote.InteractionResponse.RemoteError.REMOTE_ESPRESSO_ERROR_CODE;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import androidx.test.espresso.remote.InteractionResponse.RemoteError;
import androidx.test.espresso.remote.InteractionResponse.Status;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link InteractionResponseTest} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class InteractionResponseTest {

  @Test
  public void createInteractionResponse_WithStatusOk() {
    InteractionResponse response = new InteractionResponse.Builder().setStatus(Status.Ok).build();
    assertThat(response.getStatus(), equalTo(Status.Ok));
    assertThat(response.hasRemoteError(), is(false));
  }

  @Test
  public void createInteractionResponse_WithInvalidProtoByteArrayThrowsRPE() {
    try {
      new InteractionResponse.Builder().setResultProto(new byte[256]).build();
      fail("RemoteProtocolException expected!");
    } catch (RemoteProtocolException rpe) {
      // expected
    }
  }

  @Test
  public void createInteractionResponse_WithStatusAndInteractionResultProto_ThrowsRPE() {
    try {
      new InteractionResponse.Builder().setStatus(Status.Ok).setResultProto(new byte[256]).build();
      fail("RemoteProtocolException expected!");
    } catch (RemoteProtocolException ise) {
      // expected
    }
  }

  @Test
  public void createInteractionResponse_WithRemoteErrorAndProto_ThrowsRPE() {
    try {
      new InteractionResponse.Builder()
          .setRemoteError(new RemoteError(REMOTE_ESPRESSO_ERROR_CODE, "description"))
          .setResultProto(new byte[256])
          .build();
      fail("RemoteProtocolException expected!");
    } catch (RemoteProtocolException ise) {
      // expected
    }
  }

  @Test
  public void createInteractionResponse_WithStatusAndRemoteErrorAndResultProto_ThrowsRPE() {
    try {
      new InteractionResponse.Builder()
          .setStatus(Status.Ok)
          .setRemoteError(new RemoteError(REMOTE_ESPRESSO_ERROR_CODE, "description"))
          .setResultProto(new byte[256])
          .build();
      fail("RemoteProtocolException expected!");
    } catch (RemoteProtocolException ise) {
      // expected
    }
  }

  @Test
  public void remoteError_GetWellKnownFormattedErrorFromCodeAndDetailedError() {
    String detailedError = "some detailed error";
    String errorCodeAsString = String.valueOf(REMOTE_ESPRESSO_ERROR_CODE);
    String remoteError =
        RemoteError.getWellKnownFormattedErrorDescription(
            REMOTE_ESPRESSO_ERROR_CODE, detailedError);
    assertThat(
        remoteError, allOf(containsString(detailedError), containsString(errorCodeAsString)));
  }
}
