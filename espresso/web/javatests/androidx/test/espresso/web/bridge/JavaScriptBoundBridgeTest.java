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

package androidx.test.espresso.web.bridge;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import androidx.concurrent.futures.ResolvableFuture;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for JSBB. */
@RunWith(AndroidJUnit4.class)
public class JavaScriptBoundBridgeTest {

  private JavaScriptBoundBridge bridge;

  @Before
  public void setUp() throws Exception {
    bridge = new JavaScriptBoundBridge();
  }

  @Test
  public void testStandardFlow() throws Exception {
    Conduit c = makeConduit("hello");
    bridge.addConduit(c);
    bridge.setResult("hello", "world");
    assertThat(c.getResult().isDone()).isTrue();
    assertThat(c.getResult().get()).isEqualTo("world");
  }

  @Test
  public void testStandardFlow_Exception() throws Exception {
    Conduit c = makeConduit("hello");
    bridge.addConduit(c);
    bridge.setError("hello", "OhNo!");
    assertThat(c.getResult().isDone()).isTrue();
    assertThrows(ExecutionException.class, () -> c.getResult().get());
  }

  @Test
  public void testUFODoesNothing() throws Exception {
    Conduit c = makeConduit("hello");
    bridge.addConduit(c);
    bridge.setResult("something unknown", "blah!");
    assertThat(c.getResult().isDone()).isFalse();
  }

  @Test
  public void testUFODoesNothing_Exception() throws Exception {
    Conduit c = makeConduit("hello");
    bridge.addConduit(c);
    bridge.setError("something unknown", "blah!");
    assertThat(c.getResult().isDone()).isFalse();
  }

  private Conduit makeConduit(String token) {
    return new Conduit.Builder()
        .withBridgeName("junk")
        .withErrorMethod("foo")
        .withSuccessMethod("bar")
        .withToken(token)
        .withJsResult(ResolvableFuture.<String>create())
        .build();
  }
}
