/*
 * Copyright 2023 The Android Open Source Project
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
package androidx.test.platform.app

import android.app.Activity
import android.app.AppComponentFactory
import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@SmallTest
class AppComponentFactoryRegistryTest {

  @Test
  @Config(minSdk = 28)
  fun shouldReturnNullIfAppComponentHasNotBeenSet() {
    assertThat(AppComponentFactoryRegistry.appComponentFactory).isNull()
  }

  @Test
  @Config(minSdk = 28)
  fun shouldSetFactoryIfVersionCodeIsGreaterOrEqualThan28() {
    with(AppComponentFactoryRegistry) {
      appComponentFactory = AppComponentFactory()
      assertThat(appComponentFactory).isNotNull()
    }
  }

  @Test(expected = IllegalStateException::class)
  @Config(maxSdk = 27)
  fun shouldThrowIfVersionCodeIsSmallerThan28WhenAppComponentIsSet() {
    with(AppComponentFactoryRegistry) {
      appComponentFactory = AppComponentFactory()
    }
  }

  @Test
  @Config(minSdk = 28)
  fun shouldNotInstantiateActivityIfFactoryNotSet() {
    with(AppComponentFactoryRegistry) {
      val activity = instantiateActivity(
        cl = javaClass.classLoader,
        className = TestActivity::class.java.name,
        intent = null,
      )
      assertThat(activity).isNull()
    }
  }

  @Test
  @Config(maxSdk = 27)
  fun shouldNotInstantiateActivityIfFactorySetButVersionCodeSmallerThan28() {
    with(AppComponentFactoryRegistry) {
      appComponentFactory = AppComponentFactory()

      val activity =
        instantiateActivity(
          cl = javaClass.classLoader,
          className = TestActivity::class.java.name,
          intent = null,
        )

      assertThat(activity).isNull()
    }
  }

  @Test
  @Config(minSdk = 28)
  fun shouldInstantiateActivityIfFactoryIsSetAndVersionCodeIsGraterOrEqualThan28() {
    with(AppComponentFactoryRegistry) {
      appComponentFactory = AppComponentFactory()

      val activity =
        instantiateActivity(
          cl = javaClass.classLoader,
          className = TestActivity::class.java.name,
          intent = null,
        )

      assertThat(activity).isInstanceOf(TestActivity::class.java)
    }
  }

  @Test
  @Config(minSdk = 28)
  fun shouldNotInstantiateApplicationIfFactoryNotSet() {
    with(AppComponentFactoryRegistry) {
      val activity =
        instantiateApplication(
          cl = javaClass.classLoader,
          className = TestApplication::class.java.name,
        )

      assertThat(activity).isNull()
    }
  }

  @Test
  @Config(maxSdk = 27)
  fun shouldNotInstantiateApplicationIfFactorySetButVersionCodeSmallerThan28() {
    with(AppComponentFactoryRegistry) {
      appComponentFactory = AppComponentFactory()

      val activity =
        instantiateApplication(
          cl = javaClass.classLoader,
          className = TestApplication::class.java.name,
        )

      assertThat(activity).isNull()
    }
  }

  @Test
  @Config(minSdk = 28)
  fun shouldInstantiateApplicationIfFactoryIsSetAndVersionCodeIsGraterOrEqualThan28() {
    with(AppComponentFactoryRegistry) {
      appComponentFactory = AppComponentFactory()

      val activity =
        instantiateApplication(
          cl = javaClass.classLoader,
          className = TestApplication::class.java.name,
        )

      assertThat(activity).isInstanceOf(TestApplication::class.java)
    }
  }

  private class TestActivity : Activity()

  private class TestApplication : Application()
}
