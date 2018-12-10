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

package androidx.test.ui.app;

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.ui.app.LongListMatchers.withItemContent;
import static androidx.test.ui.app.LongListMatchers.withItemSize;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;

import android.content.Intent;
import android.test.ActivityUnitTestCase;

/**
 * UnitTests for LongListMatchers matcher factory.
 */
public final class LongListMatchersTest extends ActivityUnitTestCase<LongListActivity> {

  public LongListMatchersTest() {
    super(LongListActivity.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    // AndroidJUnitRunner does not call Looper.prepare anymore. Hence Instrumentation thread is no
    // longer a Looper! To not run into an exception, we need to ensure that the Handler is created
    // on the Ui thread.
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override
      public void run() {
        startActivity(new Intent(getInstrumentation().getTargetContext(), LongListActivity.class),
            null, null);
      }
    });
  }

  public void testWithContent() {
    assertThat(getActivity().makeItem(54), withItemContent("item: 54"));
    assertThat(getActivity().makeItem(54), withItemContent(endsWith("54")));
    assertFalse(withItemContent("hello world").matches(getActivity().makeItem(54)));
  }

  @SuppressWarnings("unchecked")
  public void testWithItemSize() {
    assertThat(getActivity().makeItem(54), withItemSize(8));
    assertThat(getActivity().makeItem(54), withItemSize(anyOf(equalTo(8), equalTo(7))));
    assertFalse(withItemSize(7).matches(getActivity().makeItem(54)));
  }
}
