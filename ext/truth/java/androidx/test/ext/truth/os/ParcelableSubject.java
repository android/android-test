/*
 * Copyright (C) 2018 The Android Open Source Project
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

import static androidx.test.core.os.Parcelables.forceParcel;

import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;

/** Testing subject for {@link Parcelable}s. */
public final class ParcelableSubject<T extends Parcelable> extends Subject {

  public static <T extends Parcelable> ParcelableSubject<T> assertThat(T parcelable) {
    return Truth.assertAbout(ParcelableSubject.<T>parcelables()).that(parcelable);
  }

  public static <T extends Parcelable> Subject.Factory<ParcelableSubject<T>, T> parcelables() {
    return ParcelableSubject<T>::new;
  }

  private final T actual;

  ParcelableSubject(FailureMetadata failureMetadata, T subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  public void recreatesEqual(Creator<T> creator) {
    T recreated = forceParcel(actual, creator);
    check("recreatesEqual()").that(actual).isEqualTo(recreated);
  }
}
