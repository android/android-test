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


import android.os.Bundle;
import android.os.Parcelable;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;

/**
 * Subject for making assertions about {@link Bundle}s.
 *
 * <p>To assert about {@link android.os.PersistableBundle}s, use {@link PersistableBundleSubject}
 * instead.
 */
public final class BundleSubject extends BaseBundleSubject {

  public static BundleSubject assertThat(Bundle bundle) {
    return Truth.assertAbout(bundles()).that(bundle);
  }

  public static Subject.Factory<BundleSubject, Bundle> bundles() {
    return BundleSubject::new;
  }

  private final Bundle actual;

  BundleSubject(FailureMetadata failureMetadata, Bundle subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  public <T extends Parcelable> ParcelableSubject<T> parcelable(String key) {
    return check("getParcelable(%s)", key)
        .about(ParcelableSubject.<T>parcelables())
        .that(actual.<T>getParcelable(key));
  }

  public <T extends Parcelable, SubjectT extends Subject> SubjectT parcelableAsType(
      String key, Subject.Factory<SubjectT, T> subjectFactory) {
    return check("getParcelable(%s)", key).about(subjectFactory).that(actual.<T>getParcelable(key));
  }

  public IterableSubject stringArrayList(String key) {
    return check("getStringArrayList(%s)", key).that(actual.getStringArrayList(key));
  }

  public IterableSubject parcelableArrayList(String key) {
    return check("getParcelableArrayList(%s)", key).that(actual.getParcelableArrayList(key));
  }

  /** Returns a truth subject for the value associated with the given key. */
  public Subject serializable(String key) {
    return check("getSerializable(%s)", key).that(actual.getSerializable(key));
  }
}
