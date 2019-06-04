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

import android.app.Notification;
import androidx.test.ext.truth.internal.FlagUtil;
import androidx.test.ext.truth.os.BundleSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;
import java.util.List;

/** Subject for making assertions about {@link android.app.Notification}s. */
public class NotificationSubject extends Subject {

  public static NotificationSubject assertThat(Notification notification) {
    return Truth.assertAbout(notifications()).that(notification);
  }

  public static Subject.Factory<NotificationSubject, Notification> notifications() {
    return NotificationSubject::new;
  }

  private final Notification actual;

  NotificationSubject(FailureMetadata failureMetadata, Notification subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  public final BundleSubject extras() {
    return check("extras").about(BundleSubject.bundles()).that(actual.extras);
  }

  public final PendingIntentSubject contentIntent() {
    return check("contentIntent")
        .about(PendingIntentSubject.pendingIntents())
        .that(actual.contentIntent);
  }

  public final PendingIntentSubject deleteIntent() {
    return check("deleteIntent")
        .about(PendingIntentSubject.pendingIntents())
        .that(actual.deleteIntent);
  }

  public final StringSubject tickerText() {
    return check("tickerText")
        .that(actual.tickerText != null ? actual.tickerText.toString() : null);
  }

  /** Assert that the notification has the given flags set. */
  public final void hasFlags(int flags) {
    List<String> actualFlags = FlagUtil.flagNames(actual.flags);
    List<String> expectedFlags = FlagUtil.flagNames(flags);
    check("flags").that(actualFlags).containsAtLeastElementsIn(expectedFlags);
  }

  public final void doesNotHaveFlags(int flags) {
    List<String> actualFlags = FlagUtil.flagNames(actual.flags);
    List<String> expectedFlags = FlagUtil.flagNames(flags);
    check("flags").that(actualFlags).containsNoneIn(expectedFlags);
  }
}
