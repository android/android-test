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

import android.os.Build;
import android.os.PersistableBundle;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.google.common.truth.BooleanSubject;
import com.google.common.truth.DoubleSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.IntegerSubject;
import com.google.common.truth.LongSubject;
import com.google.common.truth.ObjectArraySubject;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;

// LINT.IfChange
// TODO(b/308978831) once minSdkVersion >= 21, unify most methods with BundleSubject through a
// (library-internal) BaseBundleSubject superclass.
/**
 * Subject for making assertions about {@link PersistableBundle}s.
 *
 * <p>To assert about "regular" {@link android.os.Bundle}s, use {@link BundleSubject}.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public final class PersistableBundleSubject extends Subject {

  public static PersistableBundleSubject assertThat(PersistableBundle persistableBundle) {
    return Truth.assertAbout(persistableBundles()).that(persistableBundle);
  }

  public static Subject.Factory<PersistableBundleSubject, PersistableBundle> persistableBundles() {
    return PersistableBundleSubject::new;
  }

  private final PersistableBundle actual;

  PersistableBundleSubject(FailureMetadata failureMetadata, PersistableBundle subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  public void hasSize(int size) {
    check("size()").that(actual.size()).isEqualTo(size);
  }

  public void isEmpty() {
    if (!actual.isEmpty()) {
      failWithActual(simpleFact("expected to be empty"));
    }
  }

  public void isNotEmpty() {
    if (actual.isEmpty()) {
      failWithActual(simpleFact("expected to be non-empty"));
    }
  }

  public StringSubject string(String key) {
    return check("getString(%s)", key).that(actual.getString(key));
  }

  public IntegerSubject integer(String key) {
    return check("getInt(%s)", key).that(actual.getInt(key));
  }

  public LongSubject longInt(String key) {
    return check("getLong(%s)", key).that(actual.getLong(key));
  }

  @NonNull
  public DoubleSubject doubleFloat(@NonNull String key) {
    return check("getDouble(%s)", key).that(actual.getDouble(key));
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
  public BooleanSubject bool(String key) {
    return check("getBoolean(%s)", key).that(actual.getBoolean(key));
  }

  @NonNull
  public ObjectArraySubject<String> stringArray(@NonNull String key) {
    return check("getStringArray(%s)", key).that(actual.getStringArray(key));
  }

  public PersistableBundleSubject persistableBundle(String key) {
    return check("getPersistableBundle(%s)", key)
        .about(persistableBundles())
        .that(actual.getPersistableBundle(key));
  }

  public void containsKey(String key) {
    if (!actual.containsKey(key)) {
      failWithActual(simpleFact("expected to contain key " + key));
    }
  }

  public void doesNotContainKey(String key) {
    if (actual.containsKey(key)) {
      failWithActual(simpleFact("expected to not contain key " + key));
    }
  }
}
// LINT.ThenChange(BundleSubject.java)
