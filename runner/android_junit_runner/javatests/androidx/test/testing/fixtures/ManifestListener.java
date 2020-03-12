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
package androidx.test.testing.fixtures;

import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

/**
 * A <a href="http://junit.org/javadoc/latest/org/junit/runner/notification/RunListener.html"><code>
 * RunListener</code></a> fixture used to ensure listener classes specified via meta-data tags in
 * AndroidManifest are loaded and used properly.
 */
public class ManifestListener extends RunListener {

  private static boolean runStarted = false;

  @Override
  public void testRunStarted(Description description) throws Exception {
    // just do simple verification - set a boolean flag so test can verify it was called
    runStarted = true;
  }

  /**
   * Return <code>true</code> if the testRunStarted method was called for any object of this type.
   * Intended to be used to verify in this listener was loaded and invoked properly.
   */
  public static boolean isRunStarted() {
    return runStarted;
  }
}
