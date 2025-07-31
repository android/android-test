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

import android.os.BaseBundle;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import com.google.common.truth.BooleanSubject;
import com.google.common.truth.DoubleSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.IntegerSubject;
import com.google.common.truth.LongSubject;
import com.google.common.truth.ObjectArraySubject;
import com.google.common.truth.PrimitiveBooleanArraySubject;
import com.google.common.truth.PrimitiveDoubleArraySubject;
import com.google.common.truth.PrimitiveIntArraySubject;
import com.google.common.truth.PrimitiveLongArraySubject;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Subject;

/**
 * Subject for making assertions about {@link BaseBundle}s.
 *
 * <p>Concrete implementations for external users are {@link BundleSubject} and {@link
 * PersistableBundleSubject}.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY)
public abstract class BaseBundleSubject extends Subject {

  private final BaseBundle actual;

  BaseBundleSubject(FailureMetadata failureMetadata, BaseBundle subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  public final void isEmpty() {
    if (!actual.isEmpty()) {
      failWithActual(simpleFact("expected to be empty"));
    }
  }

  public final void isNotEmpty() {
    if (actual.isEmpty()) {
      failWithActual(simpleFact("expected to be non-empty"));
    }
  }

  public final void hasSize(int size) {
    check("size()").that(actual.size()).isEqualTo(size);
  }

  public final void containsKey(String key) {
    if (!actual.containsKey(key)) {
      failWithActual(simpleFact("expected to contain key " + key));
    }
  }

  public final void doesNotContainKey(String key) {
    if (actual.containsKey(key)) {
      failWithActual(simpleFact("expected to not contain key " + key));
    }
  }

  public final BooleanSubject bool(String key) {
    return check("getBoolean(%s)", key).that(actual.getBoolean(key));
  }

  public final IntegerSubject integer(String key) {
    return check("getInt(%s)", key).that(actual.getInt(key));
  }

  public final LongSubject longInt(String key) {
    return check("getLong(%s)", key).that(actual.getLong(key));
  }

  public final DoubleSubject doubleFloat(String key) {
    return check("getDouble(%s)", key).that(actual.getDouble(key));
  }

  public final StringSubject string(String key) {
    return check("getString(%s)", key).that(actual.getString(key));
  }

  public final PrimitiveBooleanArraySubject booleanArray(String key) {
    return check("getBooleanArray(%s)", key).that(actual.getBooleanArray(key));
  }

  public final PrimitiveIntArraySubject intArray(String key) {
    return check("getIntArray(%s)", key).that(actual.getIntArray(key));
  }

  public final PrimitiveLongArraySubject longArray(String key) {
    return check("getLongArray(%s)", key).that(actual.getLongArray(key));
  }

  public final PrimitiveDoubleArraySubject doubleArray(String key) {
    return check("getDoubleArray(%s)", key).that(actual.getDoubleArray(key));
  }

  public final ObjectArraySubject<String> stringArray(String key) {
    return check("getStringArray(%s)", key).that(actual.getStringArray(key));
  }
}
