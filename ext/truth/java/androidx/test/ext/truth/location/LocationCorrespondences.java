/*
 * Copyright (C) 2019 The Android Open Source Project
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
package androidx.test.ext.truth.location;

import android.location.Location;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import com.google.common.base.Objects;
import com.google.common.truth.Correspondence;

/**
 * Collection of {@link com.google.common.truth.Correspondence} helpers for asserting lists of
 * {@link Location}s.
 *
 * @see com.google.common.truth.IterableSubject#comparingElementsUsing(Correspondence)
 */
public final class LocationCorrespondences {

  public static Correspondence<Location, Location> equality() {
    return Correspondence.from(
        (actual, expected) -> {
          if (actual == expected) {
            return true;
          }
          if (actual == null || expected == null) {
            return false;
          }
          if (!Objects.equal(actual.getProvider(), expected.getProvider())) {
            return false;
          }
          if (actual.getTime() != expected.getTime()) {
            return false;
          }
          if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
            if (actual.getElapsedRealtimeNanos() != expected.getElapsedRealtimeNanos()) {
              return false;
            }
          }
          if (actual.getLatitude() != expected.getLatitude()) {
            return false;
          }
          if (actual.getLongitude() != expected.getLongitude()) {
            return false;
          }
          if (actual.getAltitude() != expected.getAltitude()) {
            return false;
          }
          if (actual.getSpeed() != expected.getSpeed()) {
            return false;
          }
          if (actual.getBearing() != expected.getBearing()) {
            return false;
          }
          if (actual.getAccuracy() != expected.getAccuracy()) {
            return false;
          }
          if (Build.VERSION.SDK_INT >= VERSION_CODES.O) {
            if (actual.getVerticalAccuracyMeters() != expected.getVerticalAccuracyMeters()) {
              return false;
            }
            if (actual.getSpeedAccuracyMetersPerSecond()
                != expected.getSpeedAccuracyMetersPerSecond()) {
              return false;
            }
            if (actual.getBearingAccuracyDegrees() != expected.getBearingAccuracyDegrees()) {
              return false;
            }
          }
          return true;
        },
        "is equal to");
  }

  public static Correspondence<Location, Location> at() {
    return Correspondence.from(
        (actual, expected) ->
            actual.getLatitude() == expected.getLatitude()
                && actual.getLongitude() == expected.getLongitude(),
        "has lat/lon at");
  }

  public static Correspondence<Location, Location> nearby(float distanceM) {
    return Correspondence.from(
        (actual, expected) -> actual.distanceTo(expected) <= distanceM, "has lat/lon near");
  }

  private LocationCorrespondences() {}
}
