/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.services.shellexecutor;

import android.content.Context;

/** Factory class for providing ShellExecutors. */
public final class ShellExecutorFactory {

  private final Context context;
  private final String binderKey;

  public ShellExecutorFactory(Context context, String binderKey) {
    this.context = context;
    this.binderKey = binderKey;
  }

  public ShellExecutor create() {
    // Binder keys for SpeakEasy are a string of hex digits. Binder keys for the LocalSocket
    // protocol start and end with a colon.
    if (LocalSocketProtocol.isBinderKey(binderKey)) {
      return new ShellExecutorLocalSocketImpl(binderKey);
    } else {
      return new ShellExecutorImpl(context, binderKey);
    }
  }
}
