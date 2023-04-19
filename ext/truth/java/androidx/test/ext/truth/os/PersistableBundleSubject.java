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

import android.os.PersistableBundle;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;

/**
 * Subject for making assertions about {@link PersistableBundle}s.
 *
 * <p>To assert about "regular" {@link android.os.Bundle}s, use {@link BundleSubject} instead.
 */
public final class PersistableBundleSubject extends BaseBundleSubject {

  public static PersistableBundleSubject assertThat(PersistableBundle bundle) {
    return Truth.assertAbout(persistableBundles()).that(bundle);
  }

  public static Subject.Factory<PersistableBundleSubject, PersistableBundle> persistableBundles() {
    return PersistableBundleSubject::new;
  }

  private final PersistableBundle actual;

  PersistableBundleSubject(FailureMetadata failureMetadata, PersistableBundle actual) {
    super(failureMetadata, actual);
    this.actual = actual;
  }

  public PersistableBundleSubject persistableBundle(String key) {
    return check("getPersistableBundle(%s)", key)
        .about(persistableBundles())
        .that(actual.getPersistableBundle(key));
  }
}
