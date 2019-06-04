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

import android.app.Notification.Action;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;

/**
 * Subject for making assertions about {@link android.app.Notification.Action}s.
 *
 * <p>Only supports on Android APIs >= 16
 */
public class NotificationActionSubject extends Subject {

  public static NotificationActionSubject assertThat(Action action) {
    return Truth.assertAbout(notificationActions()).that(action);
  }

  public static Subject.Factory<NotificationActionSubject, Action> notificationActions() {
    return NotificationActionSubject::new;
  }

  private final Action actual;

  NotificationActionSubject(FailureMetadata failureMetadata, Action subject) {
    super(failureMetadata, subject);
    this.actual = subject;
  }

  public final StringSubject title() {
    return check("title").that(actual.title != null ? actual.title.toString() : null);
  }
}
