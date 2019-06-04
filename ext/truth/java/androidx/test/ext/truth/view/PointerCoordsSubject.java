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
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.FloatSubject;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;

/** {@link Subject} for {@link PointerProperties} */
public final class PointerCoordsSubject extends Subject {

  public static PointerCoordsSubject assertThat(PointerCoords other) {
    return Truth.assertAbout(pointerCoords()).that(other);
  }

  public static Subject.Factory<PointerCoordsSubject, PointerCoords> pointerCoords() {
    return PointerCoordsSubject::new;
  }

  private final PointerCoords actual;

  private PointerCoordsSubject(
      FailureMetadata failureMetadata, @Nullable PointerCoords pointerProperties) {
    super(failureMetadata, pointerProperties);
    this.actual = pointerProperties;
  }

  public FloatSubject x() {
    return check("x").that(actual.x);
  }

  public FloatSubject y() {
    return check("y").that(actual.y);
  }

  public FloatSubject orientation() {
    return check("orientation").that(actual.orientation);
  }

  public FloatSubject pressure() {
    return check("pressure").that(actual.pressure);
  }

  public FloatSubject size() {
    return check("size").that(actual.size);
  }

  public FloatSubject toolMajor() {
    return check("toolMajor").that(actual.toolMajor);
  }

  public FloatSubject toolMinor() {
    return check("toolMinor").that(actual.toolMinor);
  }

  public FloatSubject touchMinor() {
    return check("touchMinor").that(actual.touchMinor);
  }

  public FloatSubject touchMajor() {
    return check("touchMajor").that(actual.touchMajor);
  }

  public FloatSubject axisValue(int axis) {
    return check("getAxisValue(%s)", axis).that(actual.getAxisValue(axis));
  }
}
