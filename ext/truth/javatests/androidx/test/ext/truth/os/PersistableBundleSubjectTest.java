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

import static androidx.test.ext.truth.os.PersistableBundleSubject.assertThat;

import android.os.PersistableBundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test for {@link PersistableBundleSubject}. */
@RunWith(AndroidJUnit4.class)
public class PersistableBundleSubjectTest {

  @Test
  public void isEmpty() {
    PersistableBundle bundle = new PersistableBundle();
    assertThat(bundle).isEmpty();
    bundle.putString("foo", "bar");

    assertThat(bundle).isNotEmpty();
  }

  @Test
  public void hasSize() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putString("foo", "bar");

    assertThat(bundle).hasSize(1);
  }

  @Test
  public void containsKey() {
    PersistableBundle bundle = new PersistableBundle();
    assertThat(bundle).doesNotContainKey("foo");
    bundle.putString("foo", "bar");

    assertThat(bundle).containsKey("foo");
  }

  @Test
  public void string() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putString("foo", "bar");

    assertThat(bundle).string("foo").isEqualTo("bar");
  }

  @Test
  public void integer() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putInt("foo", 1);

    assertThat(bundle).integer("foo").isEqualTo(1);
  }

  @Test
  public void longInt() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putLong("foo", 100_0000_000_000L);

    assertThat(bundle).longInt("foo").isEqualTo(100_0000_000_000L);
  }

  @Test
  public void doubleFloat() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putDouble("foo", 100.0);

    assertThat(bundle).doubleFloat("foo").isEqualTo(100.0);
  }

  @Test
  public void bool() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putBoolean("foo", true);

    assertThat(bundle).bool("foo").isTrue();
  }

  @Test
  public void booleanArray() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putBooleanArray("foo", new boolean[] {true, false});

    assertThat(bundle).booleanArray("foo").asList().containsExactly(true, false).inOrder();
  }

  @Test
  public void intArray() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putIntArray("foo", new int[] {1, 2, 3});

    assertThat(bundle).intArray("foo").asList().containsExactly(1, 2, 3).inOrder();
  }

  @Test
  public void longArray() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putLongArray("foo", new long[] {1L, 2L, 3L});

    assertThat(bundle).longArray("foo").asList().containsExactly(1L, 2L, 3L).inOrder();
  }

  @Test
  public void doubleArray() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putDoubleArray("foo", new double[] {1.0, 2.0, 3.0});

    assertThat(bundle)
        .doubleArray("foo")
        .usingExactEquality()
        .containsExactly(1.0, 2.0, 3.0)
        .inOrder();
  }

  @Test
  public void stringArray() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putStringArray("foo", new String[] {"bar", "baz"});

    assertThat(bundle).stringArray("foo").asList().containsExactly("bar", "baz").inOrder();
  }

  @Test
  public void persistableBundle() {
    PersistableBundle bundle = new PersistableBundle();
    PersistableBundle innerBundle = new PersistableBundle();
    innerBundle.putString("foo", "bar");
    bundle.putPersistableBundle("nested", innerBundle);

    assertThat(bundle).persistableBundle("invalid").isNull();
    assertThat(bundle).persistableBundle("nested").isNotNull();
    assertThat(bundle).persistableBundle("nested").hasSize(1);
    assertThat(bundle).persistableBundle("nested").containsKey("foo");
  }
}
