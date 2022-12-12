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
 */

package androidx.test.espresso.action;

import static com.google.common.truth.Truth.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import androidx.test.espresso.matcher.RemoteHamcrestCoreMatchers13;
import androidx.test.espresso.proto.action.ViewActions.AdapterDataLoaderActionProto;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import androidx.test.espresso.remote.TypeProtoConverters;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link AdapterDataLoaderActionRemoteMsg} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class AdapterDataLoaderActionRemoteMsgTest {
  private static final Integer EXPECTED_INT = 5;
  private static final String EXPECTED_STRING = "Cortado";

  private static AdapterDataLoaderActionProto toProto(Integer expectedInt, String expectedString) {
    Matcher<String> stringMatcher = allOf(instanceOf(String.class), is(expectedString));
    AdapterDataLoaderAction adapterDataLoaderAction =
        new AdapterDataLoaderAction(
            stringMatcher, expectedInt, AdapterViewProtocols.standardProtocol());

    return new AdapterDataLoaderActionRemoteMsg(adapterDataLoaderAction).toProto();
  }

  @Before
  public void initRegistry() {
    RemoteHamcrestCoreMatchers13.init(RemoteDescriptorRegistry.getInstance());
    RemoteViewActions.init(RemoteDescriptorRegistry.getInstance());
  }

  @Test
  public void transformationToProto() {
    AdapterDataLoaderActionProto adapterDataLoaderActionProto =
        toProto(EXPECTED_INT, EXPECTED_STRING);

    assertThat(adapterDataLoaderActionProto.getAtPosition()).isEqualTo(EXPECTED_INT);
    assertThat(adapterDataLoaderActionProto.getDataToLoadMatcher()).isNotNull();
    assertThat(
            TypeProtoConverters.<Class<? extends AdapterViewProtocol>>byteStringToType(
                adapterDataLoaderActionProto.getAdapterViewProtocolClass()))
        .isAssignableTo(AdapterViewProtocol.class);
  }

  @Test
  public void transformationToProto_noPosition_SetsNoProtoConstant() {
    AdapterDataLoaderActionProto adapterDataLoaderActionProto = toProto(null, EXPECTED_STRING);

    assertThat(adapterDataLoaderActionProto.getAtPosition())
        .isEqualTo(AdapterDataLoaderActionRemoteMsg.NO_POSITION_SET);
    assertThat(adapterDataLoaderActionProto.getDataToLoadMatcher()).isNotNull();
  }

  @Test
  public void transformationFromProto() {
    AdapterDataLoaderActionProto adapterDataLoaderActionProto =
        toProto(EXPECTED_INT, EXPECTED_STRING);

    AdapterDataLoaderAction adapterDataLoaderAction =
        AdapterDataLoaderActionRemoteMsg.FROM.fromProto(adapterDataLoaderActionProto);

    assertThat(adapterDataLoaderAction.atPosition).isEqualTo(EXPECTED_INT);
    MatcherAssert.assertThat(
        EXPECTED_STRING, (Matcher<? super String>) adapterDataLoaderAction.dataToLoadMatcher);
    assertThat(adapterDataLoaderAction.adapterViewProtocol).isNotNull();
  }

  @Test
  public void transformationFromProto_noPosition_SetsOptionalAbsent() {
    AdapterDataLoaderActionProto adapterDataLoaderActionProto = toProto(null, EXPECTED_STRING);

    AdapterDataLoaderAction adapterDataLoaderAction =
        AdapterDataLoaderActionRemoteMsg.FROM.fromProto(adapterDataLoaderActionProto);

    assertThat(adapterDataLoaderAction.atPosition).isNull();
  }
}
