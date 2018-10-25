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

import static com.google.common.truth.Fact.simpleFact;

import android.os.Bundle;
import android.os.Parcelable;
import com.google.common.truth.BooleanSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.IntegerSubject;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;

/** Subject for making assertions about {@link Bundle}s. */
public final class BundleSubject extends Subject<BundleSubject, Bundle> {

  public static BundleSubject assertThat(Bundle bundle) {
    return Truth.assertAbout(bundles()).that(bundle);
  }

  public static Subject.Factory<BundleSubject, Bundle> bundles() {
    return BundleSubject::new;
  }

  BundleSubject(FailureMetadata failureMetadata, Bundle subject) {
    super(failureMetadata, subject);
  }

  public void hasSize(int size) {
    check("size()").that(actual().size()).isEqualTo(size);
  }

  public void isEmpty() {
    if (!actual().isEmpty()) {
      failWithActual(simpleFact("expected to be empty"));
    }
  }

  public void isNotEmpty() {
    if (actual().isEmpty()) {
      failWithActual(simpleFact("expected to be non-empty"));
    }
  }

  public StringSubject string(String key) {
    return check("getString(%s)", key).that(actual().getString(key));
  }

  public IntegerSubject integer(String key) {
    return check("getInt(%s)", key).that(actual().getInt(key));
  }

  public BooleanSubject bool(String key) {
    return check("getBoolean(%s)", key).that(actual().getBoolean(key));
  }

  public <T extends Parcelable> ParcelableSubject<T> parcelable(String key) {
    return check("getParcelable(%s)", key)
        .about(ParcelableSubject.<T>parcelables())
        .that(actual().<T>getParcelable(key));
  }

  public <T extends Parcelable, SubjectT extends Subject<SubjectT, T>> SubjectT parcelableAsType(
      String key, Subject.Factory<SubjectT, T> subjectFactory) {
    return check("getParcelable(%s)", key)
        .about(subjectFactory)
        .that(actual().<T>getParcelable(key));
  }

  public void containsKey(String key) {
    if (!actual().containsKey(key)) {
      failWithActual(simpleFact("expected to contain key " + key));
    }
  }

  public void doesNotContainKey(String key) {
    if (actual().containsKey(key)) {
      failWithActual(simpleFact("expected to not contain key " + key));
    }
  }
}
