/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.test.gradletests.espresso;

import  androidx.test.espresso.Espresso.onView
import  androidx.test.espresso.action.ViewActions.click
import  androidx.test.espresso.assertion.ViewAssertions.matches
import  androidx.test.espresso.intent.Intents.getIntents
import  androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import  androidx.test.espresso.matcher.ViewMatchers.withId
import  androidx.test.espresso.matcher.ViewMatchers.withText
import  androidx.test.ext.truth.content.IntentCorrespondences.action
import  com.google.common.truth.Truth.assertThat

import android.content.Intent

import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Basic integration test for espresso
 */
@RunWith(AndroidJUnit4::class)
class EspressoTest {

    @get:Rule val activityScenarioRule = ActivityScenarioRule(EspressoActivity::class.java);
    @get:Rule val intentsRule = IntentsRule()

    @Test
    fun espressoCheck() {
        onView(withText("Text View")).check(matches(isDisplayed()))
    }

    @Test
    fun espressoIntents() {
        onView(withId(R.id.button)).perform(click())
        assertThat(getIntents())
                .comparingElementsUsing(action())
                .contains(Intent(Intent.ACTION_VIEW))
    }
}