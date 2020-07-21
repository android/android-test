/*
 * Copyright (C) 2020 The Android Open Source Project
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

package androidx.test.internal.events.client;

import static androidx.test.internal.events.client.TestEventServiceConnectionBase.getServiceNameOnly;
import static androidx.test.internal.events.client.TestEventServiceConnectionBase.getServicePackage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests fpr {@link TestEventServiceConnectionBase}. */
@RunWith(AndroidJUnit4.class)
public class TestEventServiceConnectionBaseTest {

  @Test
  public void getServiceNameOnly_fullServiceNameProvided() {
    assertThat(getServiceNameOnly("com.foo.Service"), is("com.foo.Service"));
  }

  @Test
  public void getServiceNameOnly_packageAndFullNameProvided() {
    assertThat(getServiceNameOnly("com.sample.foo/com.foo.Service"), is("com.foo.Service"));
  }

  @Test
  public void getServiceNameOnly_packageAndAbbreviatedNameProvided() {
    assertThat(getServiceNameOnly("com.sample.foo/.Service"), is("com.sample.foo.Service"));
  }

  @Test
  public void getServicePackage_fullServiceNameProvided() {
    assertThat(getServicePackage("com.foo.Service"), nullValue());
  }

  @Test
  public void getServicePackage_packageAndFullNameProvided() {
    assertThat(getServicePackage("com.sample.foo/com.foo.Service"), is("com.sample.foo"));
  }

  @Test
  public void getServicePackage_packageAndAbbreviatedNameProvided() {
    assertThat(getServicePackage("com.sample.foo/.Service"), is("com.sample.foo"));
  }
}
