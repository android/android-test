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

import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for the parcelable {@link TimeStamp}. We write and read from the parcel to test
 * everything is done correctly.
 */
@RunWith(AndroidJUnit4.class)
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
}
