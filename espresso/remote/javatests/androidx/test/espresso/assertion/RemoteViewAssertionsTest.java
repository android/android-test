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

package androidx.test.espresso.assertion;

import static androidx.test.espresso.assertion.LayoutAssertions.noOverlaps;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.assertion.ViewAssertions.selectedDescendantsMatch;
import static androidx.test.espresso.matcher.ViewMatchers.hasContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import android.view.View;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.assertion.LayoutAssertions.NoOverlapsViewAssertion;
import androidx.test.espresso.assertion.ViewAssertions.DoesNotExistViewAssertion;
import androidx.test.espresso.assertion.ViewAssertions.MatchesViewAssertion;
import androidx.test.espresso.assertion.ViewAssertions.SelectedDescendantsMatchViewAssertion;
import androidx.test.espresso.matcher.RemoteHamcrestCoreMatchers13;
import androidx.test.espresso.matcher.RemoteViewMatchers;
import androidx.test.espresso.proto.assertion.ViewAssertions.DoesNotExistViewAssertionProto;
import androidx.test.espresso.proto.assertion.ViewAssertions.MatchesViewAssertionProto;
import androidx.test.espresso.proto.assertion.ViewAssertions.NoOverlapsViewAssertionProto;
import androidx.test.espresso.proto.assertion.ViewAssertions.SelectedDescendantsMatchViewAssertionProto;
import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Remote message transformation related test for all assertions under {@link ViewAssertion} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class RemoteViewAssertionsTest {

  private RemoteDescriptorRegistry descriptorRegistry;

  @Before
  public void registerMatcherWithRegistry() {
    descriptorRegistry = RemoteDescriptorRegistry.getInstance();
    RemoteHamcrestCoreMatchers13.init(descriptorRegistry);
    RemoteViewAssertions.init(descriptorRegistry);
    RemoteViewMatchers.init(descriptorRegistry);
  }

  @Test
  public void matches_transformationToProto() {
    View view = new View(getInstrumentation().getContext());
    ViewAssertion viewAssertion = matches(withId(view.getId()));

    MatchesViewAssertionProto viewAssertionProto =
        (MatchesViewAssertionProto) new GenericRemoteMessage(viewAssertion).toProto();

    assertThat(viewAssertionProto, notNullValue());
  }

  @Test
  public void matches_transformationFromProto() {
    View view = new View(getInstrumentation().getContext());
    ViewAssertion viewAssertion = matches(withId(view.getId()));

    MatchesViewAssertionProto viewAssertionProto =
        (MatchesViewAssertionProto) new GenericRemoteMessage(viewAssertion).toProto();

    ((MatchesViewAssertion) GenericRemoteMessage.FROM.fromProto(viewAssertionProto))
        .viewMatcher.matches(view);
  }

  @Test
  public void doesNotExist_transformationToProto() {
    ViewAssertion viewAssertion = doesNotExist();

    DoesNotExistViewAssertionProto viewAssertionProto =
        (DoesNotExistViewAssertionProto) new GenericRemoteMessage(viewAssertion).toProto();

    assertThat(viewAssertionProto, notNullValue());
  }

  @Test
  public void doesNotExist_transformationFromProto() {
    ViewAssertion viewAssertion = doesNotExist();

    DoesNotExistViewAssertionProto viewAssertionProto =
        (DoesNotExistViewAssertionProto) new GenericRemoteMessage(viewAssertion).toProto();
    ViewAssertion viewAssertionFromProto =
        (ViewAssertion) GenericRemoteMessage.FROM.fromProto(viewAssertionProto);

    assertThat(viewAssertionFromProto, instanceOf(DoesNotExistViewAssertion.class));
  }

  @Test
  public void selectedDescendantsMatch_transformationToProto() {
    ViewAssertion viewAssertion =
        selectedDescendantsMatch(withText("no content description"), hasContentDescription());

    SelectedDescendantsMatchViewAssertionProto viewAssertionProto =
        (SelectedDescendantsMatchViewAssertionProto)
            new GenericRemoteMessage(viewAssertion).toProto();

    assertThat(viewAssertionProto, notNullValue());
  }

  @Test
  public void selectedDescendantsMatch_transformationFromProto() {
    ViewAssertion viewAssertion =
        selectedDescendantsMatch(withText("no content description"), hasContentDescription());

    SelectedDescendantsMatchViewAssertionProto viewAssertionProto =
        (SelectedDescendantsMatchViewAssertionProto)
            new GenericRemoteMessage(viewAssertion).toProto();
    ViewAssertion viewAssertionFromProto =
        (ViewAssertion) GenericRemoteMessage.FROM.fromProto(viewAssertionProto);

    assertThat(viewAssertionFromProto, instanceOf(SelectedDescendantsMatchViewAssertion.class));
  }

  @Test
  public void noOverlaps_transformationToProto() {
    ViewAssertion viewAssertion = noOverlaps();

    NoOverlapsViewAssertionProto viewAssertionProto =
        (NoOverlapsViewAssertionProto) new GenericRemoteMessage(viewAssertion).toProto();

    assertThat(viewAssertionProto, notNullValue());
  }

  @Test
  public void noOverlaps_transformationFromProto() {
    ViewAssertion viewAssertion = noOverlaps();

    NoOverlapsViewAssertionProto viewAssertionProto =
        (NoOverlapsViewAssertionProto) new GenericRemoteMessage(viewAssertion).toProto();
    ViewAssertion viewAssertionFromProto =
        (ViewAssertion) GenericRemoteMessage.FROM.fromProto(viewAssertionProto);

    assertThat(viewAssertionFromProto, instanceOf(NoOverlapsViewAssertion.class));
  }
}
