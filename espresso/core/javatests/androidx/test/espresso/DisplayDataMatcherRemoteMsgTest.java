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

package androidx.test.espresso;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import android.view.View;
import android.widget.AdapterView;
import androidx.test.espresso.DataInteraction.DisplayDataMatcher;
import androidx.test.espresso.action.AdapterDataLoaderAction;
import androidx.test.espresso.action.AdapterViewProtocol;
import androidx.test.espresso.action.AdapterViewProtocols;
import androidx.test.espresso.action.RemoteViewActions;
import androidx.test.espresso.matcher.RemoteHamcrestCoreMatchers13;
import androidx.test.espresso.matcher.RemoteViewMatchers;
import androidx.test.espresso.proto.matcher.ViewMatchers.DisplayDataMatcherProto;
import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link DisplayDataMatcher}
 *
 * <p>Note: This integration test only tests the "to proto" conversion. "From proto" conversion
 * requires injection of {@code noOpDataLoaderFunction}, which cannot performed through {@link
 * androidx.test.espresso.remote.RemoteMessageSerializer}. Therefore full integration can
 * only be tested in a full end-to-end scenario. See {@link
 * com.google.android.apps.common.testing.ui.multiprocess.testapp.DefaultProcessActivityTest#verifyAssertingOnDataInRemoteProcessIsSuccessful()}.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class DisplayDataMatcherRemoteMsgTest {

  private static final String TEXT_VIEW_TEXT = "Late";

  private static DisplayDataMatcherProto toProto() {
    Matcher<View> adapterMatcher = isAssignableFrom(AdapterView.class);
    Matcher<String> stringMatcher = allOf(instanceOf(String.class), equalTo(TEXT_VIEW_TEXT));
    AdapterViewProtocol adapterViewProtocol = AdapterViewProtocols.standardProtocol();
    AdapterDataLoaderAction adapterDataLoaderAction =
        new AdapterDataLoaderAction(stringMatcher, (Integer) null, adapterViewProtocol);

    DisplayDataMatcher displayDataMatcher =
        new DisplayDataMatcher(
            adapterMatcher,
            stringMatcher,
            adapterViewProtocol,
            adapterDataLoaderAction,
            adapterDataLoaderAction1 -> null);
    return (DisplayDataMatcherProto) new GenericRemoteMessage(displayDataMatcher).toProto();
  }

  @Before
  public void initRegistry() {
    RemoteHamcrestCoreMatchers13.init(RemoteDescriptorRegistry.getInstance());
    RemoteViewMatchers.init(RemoteDescriptorRegistry.getInstance());
    RemoteViewActions.init(RemoteDescriptorRegistry.getInstance());
    DataInteractionRemote.init(RemoteDescriptorRegistry.getInstance());
  }

  @Test
  public void transformationToProto() {
    assertThat(toProto(), notNullValue());
  }
}
