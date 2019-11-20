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

import static androidx.test.ext.truth.location.LocationSubject.assertThat;
import static com.google.common.truth.ExpectFailure.assertThat;
import static org.junit.Assert.fail;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LocationSubjectTest {

  @Test
  public void isEqualTo() {
    Location location = new Location(LocationManager.GPS_PROVIDER);
    location.setLatitude(1);
    location.setLongitude(-1);
    location.setTime(2);

    Location other = new Location(location);

    assertThat(location).isEqualTo(other);

    assertThat((Location) null).isEqualTo(null);
  }

  @Test
  public void isEqualTo_notEqual() {
    Location location = new Location(LocationManager.GPS_PROVIDER);
    location.setLatitude(1);
    location.setLongitude(-1);
    location.setTime(2);

    Location other = new Location(LocationManager.GPS_PROVIDER);
    other.setLatitude(1);
    other.setLongitude(-1);
    other.setTime(3);

    try {
      assertThat(location).isEqualTo(other);
      fail("Should have thrown");
    } catch (AssertionError e) {
      assertThat(e).factValue("expected").isEqualTo("3");
      assertThat(e).factValue("but was").isEqualTo("2");
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
    Location location = new Location(LocationManager.GPS_PROVIDER);
    location.setLatitude(1);
    location.setLongitude(-1);

    Location other = new Location(LocationManager.GPS_PROVIDER);
    other.setLatitude(1);
    other.setLongitude(-1);

    assertThat(location).isAt(other);
  }

  @Test
  public void isAt_notAt() {
    Location location = new Location(LocationManager.GPS_PROVIDER);
    location.setLatitude(1);
    location.setLongitude(-1);

    Location other = new Location(LocationManager.GPS_PROVIDER);
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
    Location location = new Location(LocationManager.GPS_PROVIDER);
    location.setLatitude(1);
    location.setLongitude(-1);

    Location other = new Location(LocationManager.GPS_PROVIDER);
    other.setLatitude(1.1);
    other.setLongitude(-1.1);

    assertThat(location).isNearby(other, 100000);
  }

  @Test
  public void isNearby_notNearby() {
    Location location = new Location(LocationManager.GPS_PROVIDER);
    location.setLatitude(1);
    location.setLongitude(-1);

    Location other = new Location(LocationManager.GPS_PROVIDER);
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
    Location location = new Location(LocationManager.GPS_PROVIDER);
    location.setLatitude(1);
    location.setLongitude(-1);

    Location other = new Location(LocationManager.GPS_PROVIDER);
    other.setLatitude(1.1);
    other.setLongitude(-1.1);

    assertThat(location).isFaraway(other, 10);
  }

  @Test
  public void isFaraway_notFaraway() {
    Location location = new Location(LocationManager.GPS_PROVIDER);
    location.setLatitude(1);
    location.setLongitude(-1);

    Location other = new Location(LocationManager.GPS_PROVIDER);
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
  public void extras() {
    Bundle bundle = new Bundle();
    bundle.putInt("extra", 2);

    Location location = new Location(LocationManager.GPS_PROVIDER);
    location.setExtras(bundle);

    assertThat(location).extras().containsKey("extra");
    assertThat(location).extras().integer("extra").isEqualTo(2);
  }
}
