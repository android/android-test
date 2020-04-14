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
import androidx.annotation.Nullable;
import androidx.test.ext.truth.os.BundleSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;

/** Subject for making assertions about {@link Location}s. */
public class LocationSubject extends Subject {

  public static LocationSubject assertThat(Location location) {
    return Truth.assertAbout(locations()).that(location);
  }

  public static Subject.Factory<LocationSubject, Location> locations() {
    return LocationSubject::new;
  }

  private final Location actual;

  private LocationSubject(FailureMetadata failureMetadata, @Nullable Location subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  @Override
  public void isEqualTo(@Nullable Object otherObj) {
    if (actual == null || !(otherObj instanceof Location)) {
      super.isEqualTo(otherObj);
      return;
    }
    Location other = (Location) otherObj;

    check("getProvider()").that(actual.getProvider()).isEqualTo(other.getProvider());
    check("getTime()").that(actual.getTime()).isEqualTo(other.getTime());
    if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
      check("getElapsedRealtimeNanos()")
          .that(actual.getElapsedRealtimeNanos())
          .isEqualTo(other.getElapsedRealtimeNanos());
    }
    check("getLatitude()").that(actual.getLatitude()).isEqualTo(other.getLatitude());
    check("getLongitude()").that(actual.getLongitude()).isEqualTo(other.getLongitude());
    check("getAltitude()").that(actual.getAltitude()).isEqualTo(other.getAltitude());
    check("getSpeed()").that(actual.getSpeed()).isEqualTo(other.getSpeed());
    check("getBearing()").that(actual.getBearing()).isEqualTo(other.getBearing());
    check("getAccuracy()").that(actual.getAccuracy()).isEqualTo(other.getAccuracy());
    if (Build.VERSION.SDK_INT >= VERSION_CODES.O) {
      check("getVerticalAccuracyMeters()")
          .that(actual.getVerticalAccuracyMeters())
          .isEqualTo(other.getVerticalAccuracyMeters());
      check("getSpeedAccuracyMetersPerSecond()")
          .that(actual.getSpeedAccuracyMetersPerSecond())
          .isEqualTo(other.getSpeedAccuracyMetersPerSecond());
      check("getBearingAccuracyDegrees()")
          .that(actual.getBearingAccuracyDegrees())
          .isEqualTo(other.getBearingAccuracyDegrees());
    }
  }

  /** Verifies that the location is centered on the same latitude/longitude as another location. */
  public void isAt(Location other) {
    check("getLatitude()").that(actual.getLatitude()).isEqualTo(other.getLatitude());
    check("getLongitude()").that(actual.getLongitude()).isEqualTo(other.getLongitude());
  }

  /** Verifies that the location is at most {@code distanceM} meters away from another location. */
  public void isNearby(Location other, float distanceM) {
    check("distanceTo()").that(actual.distanceTo(other)).isAtMost(distanceM);
  }

  /** Verifies that the location is at least {@code distanceM} meters away from another location. */
  public void isFaraway(Location other, float distanceM) {
    check("distanceTo()").that(actual.distanceTo(other)).isAtLeast(distanceM);
  }

  public void hasAltitude() {
    check("hasAltitude()").that(actual.hasAltitude()).isTrue();
  }

  public void hasSpeed() {
    check("hasSpeed()").that(actual.hasSpeed()).isTrue();
  }

  public void hasBearing() {
    check("hasBearing()").that(actual.hasBearing()).isTrue();
  }

  public void hasAccuracy() {
    check("hasAccuracy()").that(actual.hasAccuracy()).isTrue();
  }

  public void isMock() {
    check("isFromMockProvider()").that(actual.isFromMockProvider()).isTrue();
  }

  public void isNotMock() {
    check("isFromMockProvider()").that(actual.isFromMockProvider()).isFalse();
  }

  public final BundleSubject extras() {
    return check("getExtras()").about(BundleSubject.bundles()).that(actual.getExtras());
  }
}
