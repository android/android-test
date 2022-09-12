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

import static com.google.common.truth.Truth.assertAbout;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import android.location.Location;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.Nullable;
import androidx.core.location.LocationCompat;
import androidx.test.ext.truth.os.BundleSubject;
import com.google.common.truth.DoubleSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.FloatSubject;
import com.google.common.truth.LongSubject;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Subject;

/** Subject for making assertions about {@link Location}s. */
public class LocationSubject extends Subject {

  public static LocationSubject assertThat(Location location) {
    return assertAbout(locations()).that(location);
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

    provider().isEqualTo(other.getProvider());
    isAt(other.getLatitude(), other.getLongitude());
    time().isEqualTo(other.getTime());
    if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
      elapsedRealtimeNanos().isEqualTo(other.getElapsedRealtimeNanos());
    }
    if (VERSION.SDK_INT >= VERSION_CODES.Q) {
      if (other.hasElapsedRealtimeUncertaintyNanos()
          || actual.hasElapsedRealtimeUncertaintyNanos()) {
        check("hasElapsedRealtimeUncertaintyNanos()")
            .that(actual.hasElapsedRealtimeUncertaintyNanos())
            .isEqualTo(other.hasElapsedRealtimeUncertaintyNanos());
        elapsedRealtimeUncertaintyNanos().isEqualTo(other.getElapsedRealtimeUncertaintyNanos());
      }
    }
    if (other.hasAltitude() || actual.hasAltitude()) {
      check("hasAltitude()").that(actual.hasAltitude()).isEqualTo(other.hasAltitude());
      altitude().isEqualTo(other.getAltitude());
    }
    if (other.hasSpeed() || actual.hasSpeed()) {
      check("hasSpeed()").that(actual.hasSpeed()).isEqualTo(other.hasSpeed());
      speed().isEqualTo(other.getSpeed());
    }
    if (other.hasBearing() || actual.hasBearing()) {
      check("hasBearing()").that(actual.hasBearing()).isEqualTo(other.hasBearing());
      bearing().isEqualTo(other.getBearing());
    }
    if (other.hasAccuracy() || actual.hasAccuracy()) {
      check("hasAccuracy()").that(actual.hasAccuracy()).isEqualTo(other.hasAccuracy());
      accuracy().isEqualTo(other.getAccuracy());
    }
    if (LocationCompat.hasVerticalAccuracy(other) || LocationCompat.hasVerticalAccuracy(actual)) {
      check("hasVerticalAccuracy()")
          .that(LocationCompat.hasVerticalAccuracy(actual))
          .isEqualTo(LocationCompat.hasVerticalAccuracy(other));
      verticalAccuracy().isEqualTo(LocationCompat.getVerticalAccuracyMeters(other));
    }
    if (LocationCompat.hasSpeedAccuracy(other) || LocationCompat.hasSpeedAccuracy(actual)) {
      check("hasSpeedAccuracy()")
          .that(LocationCompat.hasSpeedAccuracy(actual))
          .isEqualTo(LocationCompat.hasSpeedAccuracy(other));
      speedAccuracy().isEqualTo(LocationCompat.getSpeedAccuracyMetersPerSecond(other));
    }
    if (LocationCompat.hasBearingAccuracy(other) || LocationCompat.hasBearingAccuracy(actual)) {
      check("hasBearingAccuracy()")
          .that(LocationCompat.hasBearingAccuracy(actual))
          .isEqualTo(LocationCompat.hasBearingAccuracy(other));
      bearingAccuracy().isEqualTo(LocationCompat.getBearingAccuracyDegrees(other));
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

  /**
   * Verifies that the location is at most {@code distanceM} meters away from another location.
   *
   * @deprecated Prefer to use {@link #distanceTo(Location)} for clarity.
   */
  @Deprecated
  public void isNearby(Location other, float distanceM) {
    distanceTo(other).isAtMost(distanceM);
  }

  /**
   * Verifies that the location is at least {@code distanceM} meters away from another location.
   *
   * @deprecated Prefer to use {@link #distanceTo(Location)} for clarity.
   */
  @Deprecated
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

  public void hasElapsedRealtimeUncertaintyNanos() {
    check("hasElapsedRealtimeUncertaintyNanos()")
        .that(actual.hasElapsedRealtimeUncertaintyNanos())
        .isTrue();
  }

  public DoubleSubject elapsedRealtimeUncertaintyNanos() {
    return check("getElapsedRealtimeUncertaintyNanos()")
        .that(actual.getElapsedRealtimeUncertaintyNanos());
  }

  public StringSubject provider() {
    return check("getProvider()").that(actual.getProvider());
  }

  public void hasProvider(String provider) {
    provider().isEqualTo(provider);
  }

  public void doesNotHaveProvider(String provider) {
    provider().isNotEqualTo(provider);
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
    check("hasSpeedAccuracy()").that(LocationCompat.hasSpeedAccuracy(actual)).isTrue();
  }

  public FloatSubject speedAccuracy() {
    return check("getSpeedAccuracyMetersPerSecond()")
        .that(LocationCompat.getSpeedAccuracyMetersPerSecond(actual));
  }

  public void hasBearing() {
    check("hasBearing()").that(actual.hasBearing()).isTrue();
  }

  public FloatSubject bearing() {
    return check("getBearing()").that(actual.getBearing());
  }

  public void hasBearingAccuracy() {
    check("hasBearingAccuracy()").that(LocationCompat.hasBearingAccuracy(actual)).isTrue();
  }

  public FloatSubject bearingAccuracy() {
    return check("getBearingAccuracyDegrees()")
        .that(LocationCompat.getBearingAccuracyDegrees(actual));
  }

  public void hasAccuracy() {
    check("hasAccuracy()").that(actual.hasAccuracy()).isTrue();
  }

  public FloatSubject accuracy() {
    return check("getAccuracy()").that(actual.getAccuracy());
  }

  public void hasVerticalAccuracy() {
    check("hasVerticalAccuracy()").that(LocationCompat.hasVerticalAccuracy(actual)).isTrue();
  }

  public FloatSubject verticalAccuracy() {
    return check("getVerticalAccuracyMeters()")
        .that(LocationCompat.getVerticalAccuracyMeters(actual));
  }

  public void isMock() {
    check("isMock()").that(LocationCompat.isMock(actual)).isTrue();
  }

  public void isNotMock() {
    check("isMock()").that(LocationCompat.isMock(actual)).isFalse();
  }

  public final BundleSubject extras() {
    return check("getExtras()").about(BundleSubject.bundles()).that(actual.getExtras());
  }
}
