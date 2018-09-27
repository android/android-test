/*
 * Copyright (C) 2017 The Android Open Source Project
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
package androidx.test.espresso.action;

import android.view.View;

/**
 * Translates a {@link GeneralLocation} by the given x and y distances. The distances are given in
 * term of the view's size. 1.0 means to translate by an amount equivalent to the view's length.
 *
 * <p>This class is a wrapper around {@link GeneralLocation#translate(CoordinatesProvider, float,
 * float)}, which enables serialization and deserialization of custom {@link CoordinatesProvider}s.
 */
final class TranslatedCoordinatesProvider implements CoordinatesProvider {

  final CoordinatesProvider coordinatesProvider;
  final float dx;
  final float dy;

  /**
   * Creates an instance of {@link TranslatedCoordinatesProvider}
   *
   * @param coordinatesProvider the {@link GeneralLocation} to translate
   * @param dx the distance in x direction
   * @param dy the distance in y direction
   */
  public TranslatedCoordinatesProvider(
      CoordinatesProvider coordinatesProvider, float dx, float dy) {
    this.coordinatesProvider = coordinatesProvider;
    this.dx = dx;
    this.dy = dy;
  }

  /** {@inheritDoc} */
  @Override
  public float[] calculateCoordinates(View view) {
    float[] xy = coordinatesProvider.calculateCoordinates(view);
    xy[0] += dx * view.getWidth();
    xy[1] += dy * view.getHeight();
    return xy;
  }
}
