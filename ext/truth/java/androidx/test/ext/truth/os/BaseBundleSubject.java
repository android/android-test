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
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import com.google.common.truth.BooleanSubject;
import com.google.common.truth.DoubleSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.IntegerSubject;
import com.google.common.truth.LongSubject;
import com.google.common.truth.ObjectArraySubject;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Subject;

/**
 * Subject for making assertions about {@link BaseBundle}s.
 *
 * <p>Prior to API 21, {@link BaseBundle} and {@link android.os.PersistableBundle} don't actually
 * exist, so we have to do SDK checks and some casting to types that are safe to use on older SDKs.
 *
 * <p>Concrete implementations for external users are {@link BundleSubject} and {@link
 * PersistableBundleSubject}.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY)
public abstract class BaseBundleSubject extends Subject {
  /**
   * The safe type to cast this object to before usage will be either:
   *
   * <ul>
   *   <li>{@link Bundle} on APIs prior to 21, or
   *   <li>{@link BaseBundle} on 21+.
   * </ul>
   *
   * Use {@link #baseExists} to check which type this should be cast to.
   */
  private final Object actual;

  private static boolean baseExists() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
  }

  protected BaseBundleSubject(FailureMetadata failureMetadata, Bundle actual) {
    // NOTE: This constructor will be called from BundleSubject even on API 21+ due to Java's
    // overload resolution preferring this "more specific" type signature for Bundle.
    super(failureMetadata, actual);
    this.actual = actual;
  }

  protected BaseBundleSubject(FailureMetadata failureMetadata, BaseBundle actual) {
    super(failureMetadata, actual);
    this.actual = actual;
  }

  public void hasSize(int size) {
    int actualSize = baseExists() ? ((BaseBundle) actual).size() : ((Bundle) actual).size();
    check("size()").that(actualSize).isEqualTo(size);
  }

  public void isEmpty() {
    boolean isEmpty = baseExists() ? ((BaseBundle) actual).isEmpty() : ((Bundle) actual).isEmpty();
    if (!isEmpty) {
      failWithActual(simpleFact("expected to be empty"));
    }
  }

  public void isNotEmpty() {
    boolean isEmpty = baseExists() ? ((BaseBundle) actual).isEmpty() : ((Bundle) actual).isEmpty();
    if (isEmpty) {
      failWithActual(simpleFact("expected to be non-empty"));
    }
  }

  public StringSubject string(String key) {
    String value =
        baseExists() ? ((BaseBundle) actual).getString(key) : ((Bundle) actual).getString(key);
    return check("getString(%s)", key).that(value);
  }

  public IntegerSubject integer(String key) {
    int value = baseExists() ? ((BaseBundle) actual).getInt(key) : ((Bundle) actual).getInt(key);
    return check("getInt(%s)", key).that(value);
  }

  public LongSubject longInt(String key) {
    long value = baseExists() ? ((BaseBundle) actual).getLong(key) : ((Bundle) actual).getLong(key);
    return check("getLong(%s)", key).that(value);
  }

  @NonNull
  public DoubleSubject doubleFloat(@NonNull String key) {
    double value =
        baseExists() ? ((BaseBundle) actual).getDouble(key) : ((Bundle) actual).getDouble(key);
    return check("getDouble(%s)", key).that(value);
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
  public BooleanSubject bool(String key) {
    // No baseExists check needed before this cast since the method already requires API 22+
    return check("getBoolean(%s)", key).that(((BaseBundle) actual).getBoolean(key));
  }

  @NonNull
  public ObjectArraySubject<String> stringArray(@NonNull String key) {
    String[] value =
        baseExists()
            ? ((BaseBundle) actual).getStringArray(key)
            : ((Bundle) actual).getStringArray(key);
    return check("getStringArray(%s)", key).that(value);
  }

  public void containsKey(String key) {
    boolean containsKey =
        baseExists() ? ((BaseBundle) actual).containsKey(key) : ((Bundle) actual).containsKey(key);
    if (!containsKey) {
      failWithActual(simpleFact("expected to contain key " + key));
    }
  }

  public void doesNotContainKey(String key) {
    boolean containsKey =
        baseExists() ? ((BaseBundle) actual).containsKey(key) : ((Bundle) actual).containsKey(key);
    if (containsKey) {
      failWithActual(simpleFact("expected to not contain key " + key));
    }
  }
}
