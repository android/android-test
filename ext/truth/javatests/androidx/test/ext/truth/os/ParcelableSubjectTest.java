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
package androidx.test.ext.truth.os;

import static androidx.test.ext.truth.os.ParcelableSubject.assertThat;
import static androidx.test.ext.truth.os.ParcelableSubject.parcelables;
import static com.google.common.truth.ExpectFailure.assertThat;

import android.accounts.Account;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.truth.ExpectFailure;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class ParcelableSubjectTest {

  @Rule public final ExpectFailure expectFailure = new ExpectFailure();

  @Test
  public void marshallsEquallyTo() {
    Account account = new Account("name", "type");
    Account other = new Account("name", "type");
    assertThat(account).marshallsEquallyTo(other);
  }

  @Test
  public void marshallsEquallyTo_failure() {
    Account account = new Account("name", "type");
    Account other = new Account("different name", "type");
    expectFailure.whenTesting().about(parcelables()).that(account).marshallsEquallyTo(other);
    assertThat(expectFailure.getFailure())
        .factValue("expected to serialize like")
        .isEqualTo(other.toString());
  }
}
