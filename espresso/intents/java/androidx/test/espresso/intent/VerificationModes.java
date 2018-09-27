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

package androidx.test.espresso.intent;

import static junit.framework.Assert.fail;

import android.content.Intent;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.Matcher;

/** Implementations of {@link VerificationMode}. */
public final class VerificationModes {
  /**
   * Creates a {@link VerificationMode} in which all matching intents must have been previously
   * verified.
   */
  public static VerificationMode noUnverifiedIntents() {
    return new NoUnverifiedIntents();
  }

  /** Creates a {@link VerificationMode} in which a specified number of intents must match. */
  public static VerificationMode times(int times) {
    return new Times(times);
  }

  /**
   * A {@link VerificationMode} in which all matching intents must have been previously verified.
   */
  private static final class NoUnverifiedIntents implements VerificationMode {
    @Override
    public void verify(Matcher<Intent> matcher, List<VerifiableIntent> recordedIntents) {
      List<VerifiableIntent> unverifiedIntents = new ArrayList<VerifiableIntent>();
      for (VerifiableIntent verifiableIntent : recordedIntents) {
        if (matcher.matches(verifiableIntent.getIntent()) && !verifiableIntent.hasBeenVerified()) {
          unverifiedIntents.add(verifiableIntent);
        }
      }
      if (!unverifiedIntents.isEmpty()) {
        fail(
            String.format(
                "Found unverified intents.\n\nUnverified intents:%s\n\nRecorded intents:%s",
                joinOnDash(unverifiedIntents), joinOnDash(recordedIntents)));
      }
    }
  }

  /** A {@link VerificationMode} in which a specified number of intents must match. */
  private static final class Times implements VerificationMode {
    private final int times;

    public Times(int times) {
      this.times = times;
    }

    @Override
    public void verify(Matcher<Intent> matcher, List<VerifiableIntent> recordedIntents) {
      List<VerifiableIntent> matchedIntents = new ArrayList<VerifiableIntent>();
      for (VerifiableIntent verifiableIntent : recordedIntents) {
        if (matcher.matches(verifiableIntent.getIntent())) {
          matchedIntents.add(verifiableIntent);
        }
      }
      int matches = matchedIntents.size();
      if (matches != times) {
        fail(
            String.format(
                "Wanted to match %d intents. Actually matched %d intents.\n\n"
                    + "IntentMatcher: %s\n\nMatched intents:%s\n\nRecorded intents:%s",
                times, matches, matcher, joinOnDash(matchedIntents), joinOnDash(recordedIntents)));
      }

      // Wait until the verification succeeds to mark intents as verified.
      for (VerifiableIntent verifiableIntent : matchedIntents) {
        verifiableIntent.markAsVerified();
      }
    }
  }

  private static String joinOnDash(List<VerifiableIntent> recordedIntents) {
    if (recordedIntents.isEmpty()) {
      return "[]";
    }
    StringBuilder sb = new StringBuilder();
    for (VerifiableIntent i : recordedIntents) {
      sb.append(String.format("\n-%s)", i));
    }
    return sb.toString();
  }

  private VerificationModes() {} // Prevent instantiation.
}
