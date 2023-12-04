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
package androidx.test.ext.junit.rules

import android.app.Activity
import android.app.AppComponentFactory
import android.app.Application
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class AppComponentFactoryRuleTest {

  private val appComponentFactoryRule = AppComponentFactoryRule(DummyAppComponentFactory())

  private val activityScenarioRule = ActivityScenarioRule(DummyActivity::class.java)

  @[JvmField Rule]
  val ruleChain: RuleChain =
    RuleChain.outerRule(appComponentFactoryRule).around(activityScenarioRule)

  @Test
  @Config(minSdk = 28)
  fun shouldCreateNewAppComponentsUsingAppComponentFactorySet() {
    activityScenarioRule.scenario.onActivity { activity: DummyActivity ->
      assertThat(activity.text).isEqualTo("instantiateActivity")
      assertThat(activity.application).isInstanceOf(DummyApplication::class.java)
      assertThat((activity.application as DummyApplication).text)
        .isEqualTo("instantiateApplication")
    }
  }
}

private class DummyAppComponentFactory : AppComponentFactory() {
  override fun instantiateApplication(cl: ClassLoader, className: String): Application =
    if (className == DummyApplication::class.java.name) {
      DummyApplication(text = "instantiateApplication")
    } else {
      super.instantiateApplication(cl, className)
    }

  override fun instantiateActivity(cl: ClassLoader, className: String, intent: Intent?): Activity =
    if (className == DummyActivity::class.java.name) {
      DummyActivity(text = "instantiateActivity")
    } else {
      super.instantiateActivity(cl, className, intent)
    }
}

private class DummyApplication(val text: String) : Application()

private class DummyActivity(val text: String) : Activity()
