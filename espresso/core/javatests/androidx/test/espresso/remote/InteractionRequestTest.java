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

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import androidx.test.espresso.Root;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.RemoteViewActions;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.RemoteViewAssertions;
import androidx.test.espresso.matcher.RemoteHamcrestCoreMatchers13;
import androidx.test.espresso.matcher.RemoteViewMatchers;
import androidx.test.espresso.proto.UiInteraction.InteractionRequestProto;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.google.protobuf.Any;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class InteractionRequestTest {

  private Matcher<Root> rootMatcher;
  private static final String VIEW_MATCHER_WITH_ID_NAME =
      "androidx.test.espresso.matcher.ViewMatchers$WithIdMatcher";
  private static final String VIEW_ASSERTION_MATCHES =
      "androidx.test.espresso.assertion.ViewAssertions$MatchesViewAssertion";

  private Matcher withIdMatcher;
  private ViewAction viewAction;
  private ViewAssertion viewAssertion;
  private RemoteDescriptorRegistry remoteDescriptorRegistry;

  @Before
  public void createTargetTypes() {
    remoteDescriptorRegistry = RemoteDescriptorRegistry.getInstance();
    remoteDescriptorRegistry.clear();
    RemoteDescriptorRegistryInitializer.init(remoteDescriptorRegistry);
    RemoteHamcrestCoreMatchers13.init(remoteDescriptorRegistry);
    RemoteViewMatchers.init(remoteDescriptorRegistry);
    RemoteViewActions.init(remoteDescriptorRegistry);
    RemoteViewAssertions.init(remoteDescriptorRegistry);
    rootMatcher = new StubRootMatcher();
    withIdMatcher = withId(123);
    viewAction = ViewActions.click();
    viewAssertion = matches(withIdMatcher);
  }

  @Test
  public void createInteractionRequest_WithAction_FromTypes() {
    InteractionRequest interactionRequest =
        new InteractionRequest.Builder()
            .setRootMatcher(rootMatcher)
            .setViewMatcher(withIdMatcher)
            .setViewAction(viewAction)
            .build();

    InteractionRequestProto uiInteractionProto =
        (InteractionRequestProto) interactionRequest.toProto();
    assertThat(
        uiInteractionProto.getViewMatcher().getTypeUrl(), is(equalTo(VIEW_MATCHER_WITH_ID_NAME)));
    assertThat(
        uiInteractionProto.getViewAction().getTypeUrl(),
        is(equalTo(GeneralClickAction.class.getName())));
    assertThat(uiInteractionProto.getViewAssertion().getTypeUrl(), isEmptyString());
  }

  @Test
  public void createInteractionRequest_WithAssertion_FromTypes() {
    InteractionRequest interactionRequest =
        new InteractionRequest.Builder()
            .setRootMatcher(rootMatcher)
            .setViewMatcher(withIdMatcher)
            .setViewAssertion(viewAssertion)
            .build();

    InteractionRequestProto uiInteractionProto =
        (InteractionRequestProto) interactionRequest.toProto();

    assertThat(
        uiInteractionProto.getViewMatcher().getTypeUrl(), is(equalTo(VIEW_MATCHER_WITH_ID_NAME)));
    assertThat(uiInteractionProto.getViewAction().getTypeUrl(), isEmptyString());
    assertThat(
        uiInteractionProto.getViewAssertion().getTypeUrl(), is(equalTo(VIEW_ASSERTION_MATCHES)));
  }

  @Test
  public void createInteractionRequest_WithAction_FromProto() throws ClassNotFoundException {
    Any rootMatcherAny = TypeProtoConverters.typeToAny(rootMatcher);
    Any withIdMatcherAny = TypeProtoConverters.typeToAny(withIdMatcher);
    Any viewActionAny = TypeProtoConverters.typeToAny(viewAction);

    InteractionRequestProto interactionRequestProto =
        InteractionRequestProto.newBuilder()
            .setRootMatcher(rootMatcherAny)
            .setViewMatcher(withIdMatcherAny)
            .setViewAction(viewActionAny)
            .build();

    InteractionRequest interactionRequest =
        new InteractionRequest.Builder()
            .setRequestProto(interactionRequestProto.toByteArray())
            .build();

    assertThat(interactionRequest.getRootMatcher(), notNullValue());
    assertThat(
        interactionRequest.getViewMatcher(), instanceOf(Class.forName(VIEW_MATCHER_WITH_ID_NAME)));
    assertThat(interactionRequest.getViewAction(), instanceOf(GeneralClickAction.class));
  }

  @Test
  public void createInteractionRequest_WithAssertion_FromProto() throws ClassNotFoundException {
    Any rootMatcherAny = TypeProtoConverters.typeToAny(rootMatcher);
    Any withIdMatcherAny = TypeProtoConverters.typeToAny(withIdMatcher);
    Any viewAssertAny = TypeProtoConverters.typeToAny(viewAssertion);

    InteractionRequestProto interactionRequestProto =
        InteractionRequestProto.newBuilder()
            .setRootMatcher(rootMatcherAny)
            .setViewMatcher(withIdMatcherAny)
            .setViewAssertion(viewAssertAny)
            .build();

    InteractionRequest interactionRequest =
        new InteractionRequest.Builder()
            .setRequestProto(interactionRequestProto.toByteArray())
            .build();

    assertThat(interactionRequest.getRootMatcher(), notNullValue());
    assertThat(
        interactionRequest.getViewMatcher(), instanceOf(Class.forName(VIEW_MATCHER_WITH_ID_NAME)));
    assertThat(
        interactionRequest.getViewAssertion(), instanceOf(Class.forName(VIEW_ASSERTION_MATCHES)));
  }

  @Test
  public void createInteractionRequest_WithViewActionAndViewAssertion_ThrowsISE() {
    try {
      new InteractionRequest.Builder()
          .setRootMatcher(rootMatcher)
          .setViewAction(viewAction)
          .setViewAssertion(viewAssertion)
          .setRequestProto(new byte[256])
          .build();
      fail("IllegalStateException expected!");
    } catch (IllegalStateException ise) {
      // expected
    }
  }

  @Test
  public void createInteractionRequest_WithViewMatcherAndProto_ThrowsISE() {
    try {
      new InteractionRequest.Builder()
          .setRootMatcher(rootMatcher)
          .setViewMatcher(withIdMatcher)
          .setRequestProto(new byte[256])
          .build();
      fail("IllegalStateException expected!");
    } catch (IllegalStateException ise) {
      // expected
    }
  }

  @Test
  public void createInteractionRequest_WithViewMatcherViewActionAndResultProto_ThrowsISE() {
    try {
      new InteractionRequest.Builder()
          .setRootMatcher(rootMatcher)
          .setViewMatcher(withIdMatcher)
          .setViewAction(ViewActions.click())
          .setViewAssertion(viewAssertion)
          .setRequestProto(new byte[256])
          .build();
      fail("IllegalStateException expected!");
    } catch (IllegalStateException ise) {
      // expected
    }
  }

  @Test
  public void createInteractionResponse_WithInvalidProtoByteArray() {
    try {
      new InteractionRequest.Builder()
          .setRootMatcher(rootMatcher)
          .setRequestProto(new byte[256])
          .build();
      fail("RemoteProtocolException expected!");
    } catch (RemoteProtocolException rpe) {
      // expected
    }
  }

  @Test
  public void createInteractionResponse_NoRootMatcher_ThrowsISE() {
    try {
      new InteractionRequest.Builder()
          .setViewMatcher(withIdMatcher)
          .setViewAction(ViewActions.click())
          .setViewAssertion(viewAssertion)
          .build();
      fail("IllegalStateException expected!");
    } catch (IllegalStateException ise) {
      // expected
    }
  }
}
