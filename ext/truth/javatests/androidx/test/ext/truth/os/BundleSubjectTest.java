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
    bundle.putLong("foo", 1000000000000L);
    assertThat(bundle).longInt("foo").isEqualTo(1000000000000L);
  }

  @Test
  public void bool() {
    Bundle bundle = new Bundle();
    bundle.putBoolean("foo", true);
    assertThat(bundle).bool("foo").isTrue();
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
}
