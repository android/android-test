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
package androidx.test.ext.truth.content;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.ext.truth.content.IntentSubject.assertThat;
import static com.google.common.truth.ExpectFailure.assertThat;
import static org.junit.Assert.fail;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.truth.Truth;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class IntentSubjectTest {

  @Test
  public void hasAction() {
    Intent intent = new Intent(Intent.ACTION_ASSIST);
    assertThat(intent).hasAction(Intent.ACTION_ASSIST);
  }

  @Test
  public void hasAction_noAction() {
    Intent intent = new Intent();
    try {
      assertThat(intent).hasAction(Intent.ACTION_ASSIST);
    } catch (AssertionError e) {
      Truth.assertThat(e.getMessage()).contains("expected  : android.intent.action.ASSIST");
      Truth.assertThat(e.getMessage()).contains("but was   : null");
    }
  }

  @Test
  public void hasNoAction() {
    Intent intent = new Intent();
    assertThat(intent).hasNoAction();
  }

  @Test
  public void hasNoAction_withAction() {
    Intent intent = new Intent(Intent.ACTION_ASSIST);
    try {
      assertThat(intent).hasNoAction();
    } catch (AssertionError e) {
      Truth.assertThat(e.getMessage()).contains("expected  : null");
      Truth.assertThat(e.getMessage()).contains("but was   : android.intent.action.ASSIST");
    }
  }

  @Test
  public void hasComponentClass() {
    Intent intent = new Intent();
    intent.setClassName(getApplicationContext(), "Foo");
    assertThat(intent).hasComponentClass("Foo");
  }

  @Test
  public void hasComponentPackage() {
    Intent intent = new Intent();
    intent.setClassName("com.foo", "Foo");
    assertThat(intent).hasComponentPackage("com.foo");
  }

  @Test
  public void hasComponent() {
    Intent intent = new Intent();
    intent.setClassName("com.foo", "Foo");
    assertThat(intent).hasComponent("com.foo", "Foo");
    assertThat(intent).hasComponent(new ComponentName("com.foo", "Foo"));
  }

  @Test
  public void hasData() {
    Intent intent = new Intent();
    intent.setData(Uri.parse("http://developer.android.com"));
    assertThat(intent).hasData(Uri.parse("http://developer.android.com"));
  }

  @Test
  public void hasFlags() {
    Intent intent = new Intent();
    intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    try {
      assertThat(intent)
          .hasFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_NEW_TASK);
      fail("assertion unexpectedly passed");
    } catch (AssertionError e) {
      // test error message formatting
      Truth.assertThat(e.getMessage()).contains("0x2000000, 0x10000000");
      Truth.assertThat(e.getMessage()).contains("0x8000, 0x400000");
    }
    assertThat(intent).hasFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
  }

  @Test
  public void extras() {
    Intent intent = new Intent();
    intent.putExtra("extra", 2);
    assertThat(intent).extras().containsKey("extra");
    assertThat(intent).extras().integer("extra").isEqualTo(2);
  }

  @Test
  public void categories() {
    Intent intent = new Intent();
    intent.addCategory("cat");
    assertThat(intent).categories().containsExactly("cat");
  }

  @Test
  public void filtersEquallyTo_equal() {
    Intent intent = new Intent(Intent.ACTION_ASSIST);
    Intent intentWithExtra = new Intent(Intent.ACTION_ASSIST).putExtra("key", "value");

    assertThat(intent).filtersEquallyTo(new Intent(intent));
    assertThat(intent).filtersEquallyTo(intentWithExtra);
  }

  @Test
  public void filtersEquallyTo_notEqual() {
    try {
      assertThat(new Intent("FOO")).filtersEquallyTo(new Intent("BAR"));
      fail("Should have thrown");
    } catch (AssertionError e) {
      assertThat(e)
          .factValue("expected to be equal for intent filters to")
          .isEqualTo("Intent { act=BAR }");
      assertThat(e).factValue("but was").isEqualTo("Intent { act=FOO }");
    }
  }
}
