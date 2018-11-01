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

import static androidx.test.ext.truth.app.NotificationSubject.assertThat;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import androidx.test.core.app.testing.RecreationRecordingActivity;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link NotificationSubject}. */
@RunWith(AndroidJUnit4.class)
public class NotificationSubjectTest {

  @Rule
  public ActivityTestRule<RecreationRecordingActivity> activityTestRule =
      new ActivityTestRule<>(RecreationRecordingActivity.class);

  @Test
  public void contentIntent() {
    PendingIntent pendingIntent =
        PendingIntent.getActivity(activityTestRule.getActivity(), 0, new Intent(), 0);
    Notification notification = new Notification();
    notification.contentIntent = pendingIntent;

    assertThat(notification).contentIntent().isEqualTo(pendingIntent);
  }

  @Test
  public void deleteIntent() {
    PendingIntent pendingIntent =
        PendingIntent.getActivity(activityTestRule.getActivity(), 0, new Intent(), 0);
    Notification notification = new Notification();
    notification.deleteIntent = pendingIntent;

    assertThat(notification).deleteIntent().isEqualTo(pendingIntent);
  }

  @Test
  public void tickerText() {
    Notification notification = new Notification();
    notification.tickerText = "foo";

    assertThat(notification).tickerText().isEqualTo("foo");
  }

  @Test
  public void flags() {
    Notification notification = new Notification();
    notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_FOREGROUND_SERVICE;

    assertThat(notification).hasFlags(Notification.FLAG_AUTO_CANCEL);
    assertThat(notification)
        .doesNotHaveFlags(Notification.FLAG_NO_CLEAR | Notification.FLAG_INSISTENT);
  }
}
