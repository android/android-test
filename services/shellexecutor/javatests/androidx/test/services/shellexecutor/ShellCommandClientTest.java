/*
 * Copyright (C) 2017 The Android Open Source Project
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

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.Assert.fail;

import android.os.RemoteException;
import androidx.test.runner.AndroidJUnit4;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Unit tests for {@link ShellCommandClient}. */
@RunWith(AndroidJUnit4.class)
@DoNotInstrument
public class ShellCommandClientTest {

  @Test
  public void testBlankCommand()
      throws ClientNotConnected, IOException, RemoteException, InterruptedException {
    try {
      ShellCommandClient.execOnServer(getTargetContext(), "secret", "", null, null, false, 0L);
      fail("Passing blank command should throw IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      // Pass
    }
  }

  @Test
  public void testNullCommand()
      throws ClientNotConnected, IOException, RemoteException, InterruptedException {
    try {
      ShellCommandClient.execOnServer(getTargetContext(), "secret", null, null, null, false, 0L);
      fail("Passing null command should throw IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      // Pass
    }
  }

  @Test
  public void testShellClientNoRunOnMainThread()
      throws ClientNotConnected, IOException, RemoteException, InterruptedException {
    try {
      ShellCommandClient.execOnServer(
          getTargetContext(), "secret", "command", null, null, false, 0L);
      fail("Calling execServer on main thread should throw exception");
    } catch (IllegalStateException expected) {
      // Pass
    }
  }
}
