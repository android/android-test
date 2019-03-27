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

import static androidx.test.ext.truth.app.NotificationActionSubject.assertThat;

import android.app.Notification;
import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NotificationActionSubjectTest {

  private static final String EXPECTED_TEXT = "Hello World";

  @Test
  public void hasTitle() {
    if (Build.VERSION.SDK_INT >= 16) {
      final Notification.Action action = new Notification.Action(0, EXPECTED_TEXT, null);

      assertThat(action).title().isEqualTo(EXPECTED_TEXT);
    }
  }
}
