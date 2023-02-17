/*
 * Copyright (C) 2021 The Android Open Source Project
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

package androidx.test.services.events;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.os.Build;
import android.os.Parcel;
import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.time.Clock;
import java.time.Instant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/**
 * Unit tests for the parcelable {@link TimeStamp}. We write and read from the parcel to test
 * everything is done correctly.
 */
@RunWith(AndroidJUnit4.class)
@DoNotInstrument // needed for now_legacy_ok
public class TimeStampTest {

  @Test
  public void timeStampToParcelable_ok() {
    long seconds = 3000000000L;
    int nanos = 456;
    TimeStamp timeStamp = new TimeStamp(seconds, nanos);
    Parcel parcel = Parcel.obtain();
    timeStamp.writeToParcel(parcel, 0);

    parcel.setDataPosition(0);

    TimeStamp timeStampFromParcel = TimeStamp.CREATOR.createFromParcel(parcel);

    assertThat(timeStampFromParcel.seconds).isEqualTo(seconds);
    assertThat(timeStampFromParcel.nanos).isEqualTo(nanos);
  }

  // BEGIN_STRIP
  // flaky due to timeouts on github CI.

  @Test
  @Config(
      minSdk = Config.OLDEST_SDK,
      maxSdk = Build.VERSION_CODES.N_MR1,
      instrumentedPackages = {"androidx.test.services"})
  public void now_legacy_ok() {
    long seconds = 1000000000L; // Sunday, September 9, 2001 1:46:40 AM GMT
    long ms = (seconds * 1000L) + 123L;
    SystemClock.setCurrentTimeMillis(ms);
    TimeStamp timeStamp = TimeStamp.now();
    assertThat(timeStamp.seconds).isEqualTo(seconds);
    assertThat(timeStamp.nanos).isEqualTo(123000000L);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  @SuppressWarnings("AndroidJdkLibsChecker")
  public void now_modern_ok() {
    TimeStamp.clock = mock(Clock.class);
    long seconds = 1000000000L; // Sunday, September 9, 2001 1:46:40 AM GMT
    long nanos = 123456789L;
    when(TimeStamp.clock.instant()).thenReturn(Instant.ofEpochSecond(seconds, nanos));
    TimeStamp timeStamp = TimeStamp.now();
    assertThat(timeStamp.seconds).isEqualTo(seconds);
    assertThat(timeStamp.nanos).isEqualTo(nanos);
  }

  // END-STRIP
}
