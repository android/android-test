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
package androidx.test.ext.truth.app;

import android.app.PendingIntent;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;

/** Subject for making assertions about {@link android.app.PendingIntent}s. */
public class PendingIntentSubject extends Subject {

  public static PendingIntentSubject assertThat(PendingIntent intent) {
    return Truth.assertAbout(pendingIntents()).that(intent);
  }

  public static Subject.Factory<PendingIntentSubject, PendingIntent> pendingIntents() {
    return PendingIntentSubject::new;
  }

  private PendingIntentSubject(FailureMetadata failureMetadata, PendingIntent subject) {
    super(failureMetadata, subject);
  }
}
