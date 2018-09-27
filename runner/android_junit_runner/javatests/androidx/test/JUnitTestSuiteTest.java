/*
 * Copyright (C) 2014 The Android Open Source Project
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

package androidx.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * A suite test to verify {@link InstrumentationRegistry} works as intended when ran
 * within @RunWith(Suite.class)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
  BundleJUnit4Test.class,
  ContextJUnit4Test.class,
  InstrumentationJUnit4Test.class,
  MyParameterizedTest.class
})
public class JUnitTestSuiteTest {}
