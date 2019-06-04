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
import android.view.MotionEvent.PointerProperties;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;

/** {@link Subject} for {@link PointerProperties} */
public final class PointerPropertiesSubject extends Subject {

  public static PointerPropertiesSubject assertThat(PointerProperties other) {
    return Truth.assertAbout(pointerProperties()).that(other);
  }

  public static Subject.Factory<PointerPropertiesSubject, PointerProperties> pointerProperties() {
    return PointerPropertiesSubject::new;
  }

  private final PointerProperties actual;

  private PointerPropertiesSubject(
      FailureMetadata failureMetadata, @Nullable PointerProperties pointerProperties) {
    super(failureMetadata, pointerProperties);
    this.actual = pointerProperties;
  }

  public void hasId(int id) {
    check("id").that(actual.id).isEqualTo(id);
  }

  public void hasToolType(int toolType) {
    check("toolType").that(actual.toolType).isEqualTo(toolType);
  }

  public void isEqualTo(PointerProperties other) {
    check("id").that(actual.id).isEqualTo(other.id);
    check("toolType").that(actual.toolType).isEqualTo(other.toolType);
  }
}
