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
package androidx.test.ext.truth.util;

import android.util.SparseBooleanArray;
import com.google.common.truth.ExpectFailure;
import com.google.common.truth.Fact;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;

/** Subject for making assertions about {@link SparseBooleanArray}s. */
public final class SparseBooleanArraySubject extends Subject {

  private final SparseBooleanArray actual;

  private static Factory<SparseBooleanArraySubject, SparseBooleanArray> sparseBooleanArrays() {
    return SparseBooleanArraySubject::new;
  }

  SparseBooleanArraySubject(FailureMetadata failureMetadata, SparseBooleanArray subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  /** Assert that a {@link SparseBooleanArray} for the given key returns the value true */
  public void hasTrueValueAt(int key) {
    check("value for key <%s> expected to be true but was not", key).that(actual.get(key)).isTrue();
  }

  /** Assert that a {@link SparseBooleanArray} for the given key returns the value false */
  public void hasFalseValueAt(int key) {
    // We have to check for the presence of a key first, because {@link SparseBooleanArray#get}
    // returns false if no mapping to that key exists.
    if (actual.indexOfKey(key) == -1) {
      failWithoutActual(Fact.fact("key <%s> expected to be present but was not", key));
    }
    check("value for key <%s> expected to be false but was not", key)
        .that(actual.get(key))
        .isFalse();
  }

  /** Assert that a {@link SparseBooleanArray} contains the given key */
  public void containsKey(int key) {
    check("key <%s> expected to be present but was not.", key)
        .that(actual.indexOfKey(key))
        .isGreaterThan(-1);
  }

  /** Assert that a {@link SparseBooleanArray} doesn't contain the given key */
  public void doesNotContainKey(int key) {
    check("key <%s> expected to not be present but was.", key)
        .that(actual.indexOfKey(key))
        .isLessThan(0);
  }

  /** Assert that a {@link SparseBooleanArray} has size */
  public void hasSize(int size) {
    check("size()").that(actual.size()).isEqualTo(size);
  }

  /** Assert that a {@link SparseBooleanArray} is empty */
  public void isEmpty() {
    check(" expected to be empty but was not").that(actual.size() == 0).isTrue();
  }

  /** Assert that a {@link SparseBooleanArray} is not empty */
  public void isNotEmpty() {
    check(" expected not to be empty but it was").that(actual.size() == 0).isFalse();
  }

  /** Begins an assertion on a {@link SparseBooleanArray} */
  public static SparseBooleanArraySubject assertThat(SparseBooleanArray actual) {
    return Truth.assertAbout(sparseBooleanArrays()).that(actual);
  }

  /** Begins an assertion on a {@link SparseBooleanArray} where a predicate is expected to fail */
  public static AssertionError expectFailure(
      ExpectFailure.SimpleSubjectBuilderCallback<SparseBooleanArraySubject, SparseBooleanArray>
          callback) {
    return ExpectFailure.expectFailureAbout(sparseBooleanArrays(), callback);
  }
}
