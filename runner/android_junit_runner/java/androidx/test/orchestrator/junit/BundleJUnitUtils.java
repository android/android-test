/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.test.orchestrator.junit;

import android.os.Bundle;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Utility methods to turn JUnit objects into bundles holding imitations that can be used by {@link
 * androidx.test.orchestrator.AndroidTestOrchestrator}.
 */
public final class BundleJUnitUtils {
  private static final String KEY_DESCRIPTION = "description";
  private static final String KEY_FAILURE = "failure";
  private static final String KEY_RESULT = "result";

  private BundleJUnitUtils() {}

  public static Bundle getBundleFromDescription(Description description) {
    Bundle bundle = new Bundle();
    bundle.putParcelable(KEY_DESCRIPTION, new ParcelableDescription(description));
    return bundle;
  }

  public static Bundle getBundleFromFailure(Failure failure) {
    Bundle bundle = new Bundle();
    bundle.putParcelable(KEY_FAILURE, new ParcelableFailure(failure));
    return bundle;
  }

  public static Bundle getBundleFromResult(Result result) {
    Bundle bundle = new Bundle();
    bundle.putParcelable(KEY_RESULT, new ParcelableResult(result));
    return bundle;
  }

  /**
   * Generates a bundle from a description and a given error.
   *
   * @param description A JUnit Description of the test
   * @param throwable The root exception cause
   * @return A bundle containing a ParcelableFailure constructed from the description and throwable
   */
  public static Bundle getBundleFromThrowable(Description description, Throwable throwable) {
    Bundle bundle = new Bundle();
    bundle.putParcelable(
        KEY_FAILURE, new ParcelableFailure(new ParcelableDescription(description), throwable));
    return bundle;
  }

  public static ParcelableDescription getDescription(Bundle bundle) {
    return (ParcelableDescription) bundle.getParcelable(BundleJUnitUtils.KEY_DESCRIPTION);
  }

  public static ParcelableFailure getFailure(Bundle bundle) {
    return (ParcelableFailure) bundle.getParcelable(BundleJUnitUtils.KEY_FAILURE);
  }

  public static ParcelableResult getResult(Bundle bundle) {
    return (ParcelableResult) bundle.getParcelable(BundleJUnitUtils.KEY_RESULT);
  }
}
