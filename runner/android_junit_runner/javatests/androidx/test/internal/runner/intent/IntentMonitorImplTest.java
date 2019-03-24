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

package androidx.test.internal.runner.intent;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.content.Intent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.runner.intent.IntentCallback;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;

/** IntentMonitorImpl tests. */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class IntentMonitorImplTest {

  private final IntentMonitorImpl monitor = new IntentMonitorImpl();

  @Test
  public void addRemoveListener() {
    IntentCallback callback = mock(IntentCallback.class);

    // multiple adds should only register once.
    monitor.addIntentCallback(callback);
    monitor.addIntentCallback(callback);
    monitor.addIntentCallback(callback);

    Intent intentBeforeRemove = new Intent(Intent.ACTION_VIEW);
    monitor.signalIntent(intentBeforeRemove);

    // multiple removes should no-op.
    monitor.removeIntentCallback(callback);
    monitor.removeIntentCallback(callback);

    Intent intentAfterRemove = new Intent(Intent.ACTION_DIAL);
    monitor.signalIntent(intentAfterRemove);

    verify(callback).onIntentSent(withAction(Intent.ACTION_VIEW));
    verify(callback, never()).onIntentSent(withAction(Intent.ACTION_DIAL));
  }

  private static Intent withAction(final String action) {
    return argThat(
        new ArgumentMatcher<Intent>() {
          @Override
          public boolean matches(Intent intent) {
            return ((Intent) intent).getAction().equals(action);
          }
        });
  }
}
