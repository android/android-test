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

import static androidx.test.ext.truth.util.SparseBooleanArraySubject.assertThat;
import static androidx.test.ext.truth.util.SparseBooleanArraySubject.expectFailure;

import android.util.SparseBooleanArray;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link SparseBooleanArraySubject}. */
@SuppressWarnings("ThrowableNotThrown")
@RunWith(AndroidJUnit4.class)
public class SparseBooleanArraySubjectTest {

  @Test
  public void hasTrueValueAt() {
    SparseBooleanArray subject = new SparseBooleanArray();
    subject.put(42, true);
    subject.put(41, false);

    assertThat(subject).hasTrueValueAt(42);
    expectFailure(whenTesting -> whenTesting.that(subject).hasTrueValueAt(41));
    expectFailure(whenTesting -> whenTesting.that(subject).hasTrueValueAt(0));
  }

  @Test
  public void hasFalseValueAt() {
    SparseBooleanArray subject = new SparseBooleanArray();
    subject.put(42, true);
    subject.put(41, false);

    assertThat(subject).hasFalseValueAt(41);
    expectFailure(whenTesting -> whenTesting.that(subject).hasFalseValueAt(42));
    expectFailure(whenTesting -> whenTesting.that(subject).hasFalseValueAt(0));
  }

  @Test
  public void hasKey() {
    SparseBooleanArray subject = new SparseBooleanArray();
    subject.put(42, true);

    assertThat(subject).containsKey(42);
    expectFailure(whenTesting -> whenTesting.that(subject).containsKey(41));
  }

  @Test
  public void doesNotHaveKey() {
    SparseBooleanArray subject = new SparseBooleanArray();
    subject.put(42, true);

    assertThat(subject).doesNotContainKey(41);
    expectFailure(whenTesting -> whenTesting.that(subject).doesNotContainKey(42));
  }

  @Test
  public void hasSize() {
    SparseBooleanArray subject = new SparseBooleanArray();
    subject.put(42, true);

    assertThat(subject).hasSize(subject.size());
    expectFailure(whenTesting -> whenTesting.that(subject).hasSize(subject.size() + 1));
  }

  @Test
  public void isEmpty() {
    SparseBooleanArray subject = new SparseBooleanArray();

    assertThat(subject).isEmpty();

    subject.put(42, true);
    expectFailure(whenTesting -> whenTesting.that(subject).isEmpty());
  }

  @Test
  public void isNotEmpty() {
    SparseBooleanArray subject = new SparseBooleanArray();

    expectFailure(whenTesting -> whenTesting.that(subject).isNotEmpty());

    subject.put(42, true);
    assertThat(subject).isNotEmpty();
  }
}
