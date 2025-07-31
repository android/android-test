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

import static androidx.test.ext.truth.os.BundleSubject.assertThat;

import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.truth.content.IntentSubject;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test for {@link BundleSubject}. */
@RunWith(AndroidJUnit4.class)
public class BundleSubjectTest {

  @Test
  public void isEmpty() {
    Bundle bundle = new Bundle();
    assertThat(bundle).isEmpty();
    bundle.putString("foo", "bar");

    assertThat(bundle).isNotEmpty();
  }

  @Test
  public void hasSize() {
    Bundle bundle = new Bundle();
    bundle.putString("foo", "bar");

    assertThat(bundle).hasSize(1);
  }

  @Test
  public void containsKey() {
    Bundle bundle = new Bundle();
    assertThat(bundle).doesNotContainKey("foo");
    bundle.putString("foo", "bar");

    assertThat(bundle).containsKey("foo");
  }

  @Test
  public void string() {
    Bundle bundle = new Bundle();
    bundle.putString("foo", "bar");

    assertThat(bundle).string("foo").isEqualTo("bar");
  }

  @Test
  public void integer() {
    Bundle bundle = new Bundle();
    bundle.putInt("foo", 1);

    assertThat(bundle).integer("foo").isEqualTo(1);
  }

  @Test
  public void longInt() {
    Bundle bundle = new Bundle();
    bundle.putLong("foo", 100_0000_000_000L);

    assertThat(bundle).longInt("foo").isEqualTo(100_0000_000_000L);
  }

  @Test
  public void doubleFloat() {
    Bundle bundle = new Bundle();
    bundle.putDouble("foo", 100.0);

    assertThat(bundle).doubleFloat("foo").isEqualTo(100.0);
  }

  @Test
  public void bool() {
    Bundle bundle = new Bundle();
    bundle.putBoolean("foo", true);

    assertThat(bundle).bool("foo").isTrue();
  }

  @Test
  public void byteArray() {
    Bundle bundle = new Bundle();
    bundle.putByteArray("foo", new byte[] {1, 2, 3});

    assertThat(bundle).byteArray("foo").isEqualTo(new byte[] {1, 2, 3});
  }

  @Test
  public void parcelable() {
    Bundle bundle = new Bundle();
    Account account = new Account("bar", "type");
    bundle.putParcelable("foo", account);

    assertThat(bundle).<Account>parcelable("foo").isEqualTo(account);
  }

  @Test
  public void parcelableAsType() {
    Bundle bundle = new Bundle();
    Intent intent = new Intent("bar");
    bundle.putParcelable("foo", intent);

    assertThat(bundle).parcelableAsType("foo", IntentSubject.intents()).hasAction("bar");
  }

  @Test
  public void booleanArray() {
    Bundle bundle = new Bundle();
    bundle.putBooleanArray("foo", new boolean[] {true, false});

    assertThat(bundle).booleanArray("foo").asList().containsExactly(true, false).inOrder();
  }

  @Test
  public void intArray() {
    Bundle bundle = new Bundle();
    bundle.putIntArray("foo", new int[] {1, 2, 3});

    assertThat(bundle).intArray("foo").asList().containsExactly(1, 2, 3).inOrder();
  }

  @Test
  public void longArray() {
    Bundle bundle = new Bundle();
    bundle.putLongArray("foo", new long[] {1L, 2L, 3L});

    assertThat(bundle).longArray("foo").asList().containsExactly(1L, 2L, 3L).inOrder();
  }

  @Test
  public void doubleArray() {
    Bundle bundle = new Bundle();
    bundle.putDoubleArray("foo", new double[] {1.0, 2.0, 3.0});

    assertThat(bundle)
        .doubleArray("foo")
        .usingExactEquality()
        .containsExactly(1.0, 2.0, 3.0)
        .inOrder();
  }

  @Test
  public void stringArray() {
    Bundle bundle = new Bundle();
    bundle.putStringArray("foo", new String[] {"bar", "baz"});

    assertThat(bundle).stringArray("foo").asList().containsExactly("bar", "baz").inOrder();
  }

  @Test
  public void stringArrayList() {
    Bundle bundle = new Bundle();
    bundle.putStringArrayList("foo", Lists.newArrayList("bar", "baz"));

    assertThat(bundle).stringArrayList("foo").containsExactly("bar", "baz").inOrder();
  }

  @Test
  public void parcelableArrayList() {
    Bundle bundle = new Bundle();
    Intent intent1 = new Intent("bar");
    Intent intent2 = new Intent("baz");
    bundle.putParcelableArrayList("foo", Lists.newArrayList(intent1, intent2));

    assertThat(bundle).parcelableArrayList("foo").containsExactly(intent1, intent2).inOrder();
  }

  @Test
  public void serializable() {
    Bundle bundle = new Bundle();
    bundle.putSerializable("color", Color.GREEN);

    assertThat(bundle).serializable("color").isEqualTo(Color.GREEN);
  }

  // An enum is a simple example of a Serializable object.
  enum Color {
    RED,
    GREEN,
    BLUE
  }
}
