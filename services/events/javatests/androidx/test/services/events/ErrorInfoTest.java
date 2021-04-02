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
 * Class to test parcelable {@link ErrorInfo}. We write and read from the parcel to test everything
 * is done correctly.
 */
@RunWith(AndroidJUnit4.class)
public class ErrorInfoTest {

  @Test
  public void errorToParcelableTest_basicException() {
    String stackTrace = "DummyTrace";
    String exceptionType = "NullPointerException";
    String message = "message";
    ErrorInfo error = new ErrorInfo(message, exceptionType, stackTrace);
    Parcel parcel = Parcel.obtain();
    error.writeToParcel(parcel, 0);

    parcel.setDataPosition(0);

    ErrorInfo errorFromParcel = ErrorInfo.CREATOR.createFromParcel(parcel);

    assertThat(errorFromParcel.errorMessage).isEqualTo(message);
    assertThat(errorFromParcel.errorType).isEqualTo(exceptionType);
    assertThat(errorFromParcel.stackTrace).isEqualTo(stackTrace);
  }
}
