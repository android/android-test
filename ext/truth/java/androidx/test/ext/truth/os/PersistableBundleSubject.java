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

import android.os.Build;
import android.os.PersistableBundle;
import androidx.annotation.RequiresApi;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;
import java.util.HashMap;
import java.util.Map;

/**
 * Subject for making assertions about {@link PersistableBundle}s.
 *
 * <p>To assert about "regular" {@link android.os.Bundle}s, use {@link BundleSubject}.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public final class PersistableBundleSubject extends BaseBundleSubject {

  public static PersistableBundleSubject assertThat(PersistableBundle persistableBundle) {
    return Truth.assertAbout(persistableBundles()).that(persistableBundle);
  }

  public final void isEqualTo(PersistableBundle other) {
    // If either are null, just fall back to an equals() comparison.
    if (actual == null || other == null) {
      super.isEqualTo(other);
      return;
    }

    // PersistableBundle doesn't implement equals() so we convert it to a Map so we can use the
    // existing Truth implementation.
    Truth.assertThat(toMap(actual)).isEqualTo(toMap(other));
  }

  public final void isNotEqualTo(PersistableBundle other) {
    // If either are null, just fall back to an equals() comparison.
    if (actual == null || other == null) {
      super.isNotEqualTo(other);
      return;
    }

    // PersistableBundle doesn't implement equals() so we convert it to a Map so we can use the
    // existing Truth implementation.
    Truth.assertThat(toMap(actual)).isNotEqualTo(toMap(other));
  }

  /** Converts the {@link PersistableBundle} to a {@link Map} for comparison. */
  private final Map<String, Object> toMap(PersistableBundle bundle) {
    Map<String, Object> map = new HashMap<>();
    for (String key : bundle.keySet()) {
      Object value = bundle.get(key);
      if (value instanceof PersistableBundle) {
        value = toMap((PersistableBundle) value);
      }
      map.put(key, value);
    }
    return map;
  }

  public static Subject.Factory<PersistableBundleSubject, PersistableBundle> persistableBundles() {
    return PersistableBundleSubject::new;
  }

  private final PersistableBundle actual;

  PersistableBundleSubject(FailureMetadata failureMetadata, PersistableBundle subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  public PersistableBundleSubject persistableBundle(String key) {
    return check("getPersistableBundle(%s)", key)
        .about(persistableBundles())
        .that(actual.getPersistableBundle(key));
  }
}
