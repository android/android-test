/*
 * Copyright (C) 2019 The Android Open Source Project
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

package androidx.test.services.events.client;

import static org.mockito.Mockito.verify;

import android.os.RemoteException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.orchestrator.callback.LegacyOrchestratorConnection;
import androidx.test.orchestrator.callback.OrchestratorCallback;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests fpr {@link androidx.test.services.events.client.TestDiscovery}. */
@RunWith(AndroidJUnit4.class)
public class TestDiscoveryTest {
  @Mock OrchestratorCallback mockCallback;
  @Mock TestEventClientConnectListener mockConnectListener;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void addTests() throws RemoteException, TestEventClientException {
    LegacyOrchestratorConnection connection = new LegacyOrchestratorConnection(mockConnectListener);
    connection.service = mockCallback;
    TestDiscovery testDiscovery = new TestDiscovery(connection);
    Description testDescription = Description.createTestDescription(getClass(), "sampleTest");

    testDiscovery.addTests(testDescription);

    verify(mockCallback)
        .addTest("androidx.test.services.events.client.TestDiscoveryTest#sampleTest");
  }
}
