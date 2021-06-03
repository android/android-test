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

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static androidx.test.ext.truth.location.LocationSubject.assertThat;
import static com.google.common.truth.ExpectFailure.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import android.location.Location;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LocationSubjectTest {

  @Test
  public void isEqualTo() {
    Location location = new Location(GPS_PROVIDER);
    location.setLatitude(1);
    location.setLongitude(-1);
    location.setTime(2);

    Location other = new Location(location);

    assertThat(location).isEqualTo(other);

    assertThat((Location) null).isEqualTo(null);
  }

  @Test
  public void isEqualTo_notEqual() {
    Location location = new Location(GPS_PROVIDER);
    location.setLatitude(1);
    location.setLongitude(-1);
    location.setTime(2);

    Location other = new Location(GPS_PROVIDER);
    other.setLatitude(1);
    other.setLongitude(-1);
    other.setTime(3);

    try {
      assertThat(location).isEqualTo(other);
      fail("Should have thrown");
    } catch (AssertionError e) {
      // isEqualTo only provides detailed info below S
      if (VERSION.SDK_INT <= VERSION_CODES.R) {
        assertThat(e).factValue("expected").isEqualTo("3");
        assertThat(e).factValue("but was").isEqualTo("2");
      }
    }

    try {
      assertThat(location).isEqualTo(null);
      fail("Should have thrown");
    } catch (AssertionError e) {
      assertThat(e).factValue("expected").isEqualTo("null");
      assertThat(e).factValue("but was").isEqualTo(location.toString());
    }
  }

  @Test
  public void isAt() {
    Location location = new Location(GPS_PROVIDER);
    location.setLatitude(1);
    location.setLongitude(-1);

    Location other = new Location(GPS_PROVIDER);
    other.setLatitude(1);
    other.setLongitude(-1);

    assertThat(location).isAt(other);
  }

  @Test
  public void isAt_notAt() {
    Location location = new Location(GPS_PROVIDER);
    location.setLatitude(1);
    location.setLongitude(-1);

    Location other = new Location(GPS_PROVIDER);
    other.setLatitude(1);
    other.setLongitude(-2);

    try {
      assertThat(location).isAt(other);
      fail();
    } catch (AssertionError e) {
      assertThat(e).factValue("expected").isEqualTo("-2.0");
      assertThat(e).factValue("but was").isEqualTo("-1.0");
    }
  }

  @Test
  public void isNearby() {
    Location location = new Location(GPS_PROVIDER);
    location.setLatitude(1);
    location.setLongitude(-1);

    Location other = new Location(GPS_PROVIDER);
    other.setLatitude(1.1);
    other.setLongitude(-1.1);

    assertThat(location).isNearby(other, 100000);
  }

  @Test
  public void isNearby_notNearby() {
    Location location = new Location(GPS_PROVIDER);
    location.setLatitude(1);
    location.setLongitude(-1);

    Location other = new Location(GPS_PROVIDER);
    other.setLatitude(1.1);
    other.setLongitude(-1.1);

    try {
      assertThat(location).isNearby(other, 10);
      fail();
    } catch (AssertionError e) {
      assertThat(e).factValue("expected to be at most").isEqualTo("10.0");
      assertThat(e).factValue("but was").isEqualTo("15689.056");
    }
  }

  @Test
  public void isFaraway() {
    Location location = new Location(GPS_PROVIDER);
    location.setLatitude(1);
    location.setLongitude(-1);

    Location other = new Location(GPS_PROVIDER);
    other.setLatitude(1.1);
    other.setLongitude(-1.1);

    assertThat(location).isFaraway(other, 10);
  }

  @Test
  public void isFaraway_notFaraway() {
    Location location = new Location(GPS_PROVIDER);
    location.setLatitude(1);
    location.setLongitude(-1);

    Location other = new Location(GPS_PROVIDER);
    other.setLatitude(1.1);
    other.setLongitude(-1.1);

    try {
      assertThat(location).isFaraway(other, 100000);
      fail();
    } catch (AssertionError e) {
      assertThat(e).factValue("expected to be at least").isEqualTo("100000.0");
      assertThat(e).factValue("but was").isEqualTo("15689.056");
    }
  }

  @Test
  public void distanceTo() {
    Location location = new Location(GPS_PROVIDER);
    location.setLatitude(1);
    location.setLongitude(1);

    assertThat(location).distanceTo(2, 2).isWithin(1).of(156876.16f);
  }

  @Test
  public void bearingTo() {
    Location location = new Location(GPS_PROVIDER);
    location.setLatitude(1);
    location.setLongitude(1);

    assertThat(location).bearingTo(2, 2).isWithin(.1f).of(45.170467f);
  }

  @Test
  public void time() {
    Location location = new Location(GPS_PROVIDER);
    location.setTime(1000);

    assertThat(location).time().isEqualTo(1000);
  }

  @Test
  public void elapsedRealtime() {
    assumeTrue(VERSION.SDK_INT > VERSION_CODES.JELLY_BEAN_MR1);

    Location location = new Location(GPS_PROVIDER);
    location.setElapsedRealtimeNanos(100000000);

    assertThat(location).elapsedRealtimeNanos().isEqualTo(100000000);
    assertThat(location).elapsedRealtimeMillis().isEqualTo(100);
  }

  @Test
  public void hasProvider() {
    Location location = new Location(GPS_PROVIDER);

    assertThat(location).hasProvider(GPS_PROVIDER);
    assertThat(location).doesNotHaveProvider(NETWORK_PROVIDER);
  }

  @Test
  public void altitude() {
    Location location = new Location(GPS_PROVIDER);
    location.setAltitude(1);

    assertThat(location).hasAltitude();
    assertThat(location).altitude().isEqualTo(1);
  }

  @Test
  public void speed() {
    Location location = new Location(GPS_PROVIDER);
    location.setSpeed(1f);

    assertThat(location).hasSpeed();
    assertThat(location).speed().isEqualTo(1);
  }

  @Test
  public void bearing() {
    Location location = new Location(GPS_PROVIDER);
    location.setBearing(1f);

    assertThat(location).hasBearing();
    assertThat(location).bearing().isEqualTo(1);
  }

  @Test
  public void accuracy() {
    Location location = new Location(GPS_PROVIDER);
    location.setAccuracy(1f);

    assertThat(location).hasAccuracy();
    assertThat(location).accuracy().isEqualTo(1);
  }

  @Test
  public void verticalAccuracy() {
    assumeTrue(VERSION.SDK_INT > VERSION_CODES.O);

    Location location = new Location(GPS_PROVIDER);
    location.setVerticalAccuracyMeters(1f);

    assertThat(location).hasVerticalAccuracy();
    assertThat(location).verticalAccuracy().isEqualTo(1);
  }

  @Test
  public void extras() {
    Bundle bundle = new Bundle();
    bundle.putInt("extra", 2);

    Location location = new Location(GPS_PROVIDER);
    location.setExtras(bundle);

    assertThat(location).extras().containsKey("extra");
    assertThat(location).extras().integer("extra").isEqualTo(2);
  }
}
