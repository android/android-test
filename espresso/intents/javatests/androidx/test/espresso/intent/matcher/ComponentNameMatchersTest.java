/*
 * Copyright (C) 2015 The Android Open Source Project
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

package androidx.test.espresso.intent.matcher;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName;
import static androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasMyPackageName;
import static androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasPackageName;
import static androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasShortClassName;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.ComponentName;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link ComponentNameMatchers}. */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class ComponentNameMatchersTest {

  private static final String PKG = "com.some.wonderful.package.name";
  private static final String SHORT_CLASS_NAME = ".FooBar";
  private static final String CLS = PKG + SHORT_CLASS_NAME;
  private static final ComponentName c = new ComponentName(PKG, CLS);

  @Test
  public void hasClassNameTesting() {
    assertTrue(hasClassName(CLS).matches(c));
    assertTrue(hasClassName(equalTo(CLS)).matches(c));
  }

  @Test
  public void hasClassNameDoesNotMatch() {
    assertFalse(hasClassName("not_there").matches(c));
    assertFalse(hasClassName(containsString("not there")).matches(c));
  }

  @Test
  public void hasPackageNameTesting() {
    assertTrue(hasPackageName(PKG).matches(c));
    assertTrue(hasPackageName(equalTo(PKG)).matches(c));
  }

  @Test
  public void hasPackageNameDoesNotMatch() {
    assertFalse(hasPackageName("not_there").matches(c));
    assertFalse(hasPackageName(containsString(SHORT_CLASS_NAME)).matches(c));
  }

  @Test
  public void hasShortClassNameTesting() {
    assertTrue(hasShortClassName(SHORT_CLASS_NAME).matches(c));
    assertTrue(hasShortClassName(equalTo(SHORT_CLASS_NAME)).matches(c));
  }

  @Test
  public void hasShortClassNameDoesNotMatch() {
    assertFalse(hasShortClassName("not_there").matches(c));
    assertFalse(hasShortClassName(equalTo(CLS)).matches(c));
  }

  @Test
  public void hasMyPackageNameTesting() {
    String targetPackage = getApplicationContext().getPackageName();
    ComponentName targetComponent = new ComponentName(targetPackage, targetPackage + ".SomeClass ");
    assertTrue(hasMyPackageName().matches(targetComponent));
  }

  // This test will fail because PKG is not the target package for this instrumentation.
  @Test
  public void hasMyPackageNameDoesNotMatch() {
    assertFalse(hasMyPackageName().matches(c));
  }
}
