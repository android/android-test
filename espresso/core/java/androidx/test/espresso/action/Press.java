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

package androidx.test.espresso.action;

/** Returns different touch target sizes. */
public enum Press implements PrecisionDescriber {
  PINPOINT {
    @Override
    public float[] describePrecision() {
      float[] pinpoint = {1f, 1f};
      return pinpoint;
    }
  },
  FINGER {
    // average width of the index finger is 16 – 20 mm.
    @Override
    public float[] describePrecision() {
      float finger[] = {16f, 16f};
      return finger;
    }
  },
  // average width of an adult thumb is 25 mm (1 inch).
  THUMB {
    @Override
    public float[] describePrecision() {
      float thumb[] = {25f, 25f};
      return thumb;
    }
  }
}
