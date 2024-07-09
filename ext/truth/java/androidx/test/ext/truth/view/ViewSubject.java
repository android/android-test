/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.test.ext.truth.view;

import androidx.annotation.Nullable;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;

/** {@link Subject} for {@link View}. */
public final class ViewSubject extends Subject {

  public static ViewSubject assertThat(View view) {
    return Truth.assertAbout(views()).that(view);
  }

  public static Subject.Factory<ViewSubject, View> views() {
    return ViewSubject::new;
  }

  private final View actual;

  private ViewSubject(FailureMetadata failureMetadata, @Nullable View view) {
    super(failureMetadata, view);
    this.actual = view;
  }

  // public void hasText(String text) {
  //   check("getAction()").that(view).isEqualTo(action);
  // }

}
