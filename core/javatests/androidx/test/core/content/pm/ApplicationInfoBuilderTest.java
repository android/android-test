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
package androidx.test.core.content.pm;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.Assert.fail;

import android.content.pm.ApplicationInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link ApplicationInfoBuilder}. */
@RunWith(AndroidJUnit4.class)
public final class ApplicationInfoBuilderTest {

  @Test
  public void buildAllFields() {
    String name = "TestName";
    String packageName = "test.package.name";

    ApplicationInfo applicationInfo =
        ApplicationInfoBuilder.newBuilder().setName(name).setPackageName(packageName).build();

    assertThat(applicationInfo.name).isEqualTo(name);
    assertThat(applicationInfo.packageName).isEqualTo(packageName);
  }

  @Test
  public void build_throwsException_whenPackageNameMissing() {
    try {
      ApplicationInfoBuilder.newBuilder().build();
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessageThat().isEqualTo("Mandatory field 'packageName' missing.");
    }
  }
}
