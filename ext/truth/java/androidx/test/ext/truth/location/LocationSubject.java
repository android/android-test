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

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import android.location.Location;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.Nullable;
import androidx.test.ext.truth.os.BundleSubject;
import com.google.common.truth.DoubleSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.FloatSubject;
import com.google.common.truth.LongSubject;
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

  private LocationSubject(FailureMetadata failureMetadata, Location subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  @Override
  public void isEqualTo(@Nullable Object otherObj) {
    if (VERSION.SDK_INT >= VERSION_CODES.S) {
      // from android S+, Location.equals() is well defined
      super.isEqualTo(otherObj);
      return;
    }
    if (actual == null || !(otherObj instanceof Location)) {
      super.isEqualTo(otherObj);
      return;
    }
    Location other = (Location) otherObj;

    check("getProvider()").that(actual.getProvider()).isEqualTo(other.getProvider());
    check("getTime()").that(actual.getTime()).isEqualTo(other.getTime());
    if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
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
    if (VERSION.SDK_INT >= VERSION_CODES.O) {
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

  /** Verifies that the location is at the same latitude/longitude as another location. */
  public void isAt(Location other) {
    isAt(other.getLatitude(), other.getLongitude());
  }

  public void isAt(double latitude, double longitude) {
    check("getLatitude()").that(actual.getLatitude()).isEqualTo(latitude);
    check("getLongitude()").that(actual.getLongitude()).isEqualTo(longitude);
  }

  public void isNotAt(Location other) {
    isNotAt(other.getLatitude(), other.getLongitude());
  }

  public void isNotAt(double latitude, double longitude) {
    check("getLatitude()").that(actual.getLatitude()).isNotEqualTo(latitude);
    check("getLongitude()").that(actual.getLongitude()).isNotEqualTo(longitude);
  }

  public FloatSubject distanceTo(double latitude, double longitude) {
    Location location = new Location("");
    location.setLatitude(latitude);
    location.setLongitude(longitude);
    return distanceTo(location);
  }

  public FloatSubject distanceTo(Location location) {
    return check("distanceTo(" + location.getLatitude() + ", " + location.getLongitude() + ")")
        .that(actual.distanceTo(location));
  }

  /** Verifies that the location is at most {@code distanceM} meters away from another location. */
  public void isNearby(Location other, float distanceM) {
    distanceTo(other).isAtMost(distanceM);
  }

  /** Verifies that the location is at least {@code distanceM} meters away from another location. */
  public void isFaraway(Location other, float distanceM) {
    distanceTo(other).isAtLeast(distanceM);
  }

  public FloatSubject bearingTo(double latitude, double longitude) {
    Location location = new Location("");
    location.setLatitude(latitude);
    location.setLongitude(longitude);
    return bearingTo(location);
  }

  public FloatSubject bearingTo(Location location) {
    return check("bearingTo(" + location.getLatitude() + ", " + location.getLongitude() + ")")
        .that(actual.bearingTo(location));
  }

  public LongSubject time() {
    return check("getTime()").that(actual.getTime());
  }

  public LongSubject elapsedRealtimeNanos() {
    return check("getElapsedRealtimeNanos()").that(actual.getElapsedRealtimeNanos());
  }

  public LongSubject elapsedRealtimeMillis() {
    return check("getElapsedRealtimeMillis()")
        .that(NANOSECONDS.toMillis(actual.getElapsedRealtimeNanos()));
  }

  public void hasProvider(String provider) {
    check("getProvider()").that(actual.getProvider()).isEqualTo(provider);
  }

  public void doesNotHaveProvider(String provider) {
    check("getProvider()").that(actual.getProvider()).isNotEqualTo(provider);
  }

  public void hasAltitude() {
    check("hasAltitude()").that(actual.hasAltitude()).isTrue();
  }

  public DoubleSubject altitude() {
    return check("getAltitude()").that(actual.getAltitude());
  }

  public void hasSpeed() {
    check("hasSpeed()").that(actual.hasSpeed()).isTrue();
  }

  public FloatSubject speed() {
    return check("getSpeed()").that(actual.getSpeed());
  }

  public void hasSpeedAccuracy() {
    check("hasSpeedAccuracy()").that(actual.hasSpeedAccuracy()).isTrue();
  }

  public FloatSubject speedAccuracy() {
    return check("getSpeedAccuracyMetersPerSecond()")
        .that(actual.getSpeedAccuracyMetersPerSecond());
  }

  public void hasBearing() {
    check("hasBearing()").that(actual.hasBearing()).isTrue();
  }

  public FloatSubject bearing() {
    return check("getBearing()").that(actual.getBearing());
  }

  public void hasBearingAccuracy() {
    check("hasBearingAccuracy()").that(actual.hasBearingAccuracy()).isTrue();
  }

  public FloatSubject bearingAccuracy() {
    return check("getBearingAccuracyDegrees()").that(actual.getBearingAccuracyDegrees());
  }

  public void hasAccuracy() {
    check("hasAccuracy()").that(actual.hasAccuracy()).isTrue();
  }

  public FloatSubject accuracy() {
    return check("getAccuracy()").that(actual.getAccuracy());
  }

  public void hasVerticalAccuracy() {
    check("hasVerticalAccuracy()").that(actual.hasVerticalAccuracy()).isTrue();
  }

  public FloatSubject verticalAccuracy() {
    return check("getVerticalAccuracyMeters()").that(actual.getVerticalAccuracyMeters());
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
