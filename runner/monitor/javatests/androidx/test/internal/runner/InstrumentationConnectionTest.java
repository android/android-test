/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package androidx.test.internal.runner;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import androidx.test.filters.SmallTest;
import androidx.test.internal.runner.InstrumentationConnection.IncomingHandler;
import androidx.test.internal.util.ParcelableIBinder;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.runner.MonitoringInstrumentation;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests exercise {@link InstrumentationConnection}'s logic of remote Instrumentation runner
 * instance discovery and communication.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class InstrumentationConnectionTest {

  @Mock private Context mockedContext;
  @Mock private Instrumentation mockedInstrumentation;
  @Mock private MonitoringInstrumentation.ActivityFinisher mockedFinisher;

  InstrumentationConnection instrumentationConnection;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    instrumentationConnection = new InstrumentationConnection(mockedContext);
  }

  /**
   * Verify the typical happy path of InstrumentationConnection initialization and termination: 1.
   * Create InstrumentationConnection instance 2. initialize 2.1 verify appropriate broadcast was
   * sent to notify others of self existence 2.2 verify that this instance is registered to receive
   * broadcasts from future other new instances. 3. terminate 3.1 verify that his instance is
   * unregistered from receiving future broadcasts 3.2 verify that the set of other instrumentations
   * is cleared.
   */
  @Test
  public void verifyHappyReceiverFlow() {
    instrumentationConnection.init(mockedInstrumentation, mockedFinisher);

    // verify broadcast was sent
    ArgumentCaptor<Intent> intentArg = ArgumentCaptor.forClass(Intent.class);
    verify(mockedContext).sendBroadcast(intentArg.capture());
    Intent receivedIntent = intentArg.getValue();
    assertNotNull(receivedIntent.getAction());
    ParcelableIBinder parcelableIBinder =
        receivedIntent
            .getBundleExtra(InstrumentationConnection.BUNDLE_BR_NEW_BINDER)
            .getParcelable(InstrumentationConnection.BUNDLE_BR_NEW_BINDER);
    Messenger msgr = new Messenger(parcelableIBinder.getIBinder());
    assertEquals(instrumentationConnection.incomingHandler.messengerHandler, msgr);

    // verify broadcast registration
    ArgumentCaptor<BroadcastReceiver> brArg = ArgumentCaptor.forClass(BroadcastReceiver.class);
    ArgumentCaptor<IntentFilter> ifArg = ArgumentCaptor.forClass(IntentFilter.class);
    verify(mockedContext).registerReceiver(brArg.capture(), ifArg.capture());
    assertEquals(instrumentationConnection.messengerReceiver, brArg.getValue());
    assertEquals(InstrumentationConnection.BROADCAST_FILTER, ifArg.getValue().getAction(0));

    instrumentationConnection.terminate();

    // very broadcast un-registration
    verify(mockedContext).unregisterReceiver(brArg.capture());
    assertEquals(instrumentationConnection.messengerReceiver, brArg.getValue());
    assertNull(instrumentationConnection.incomingHandler);
  }

  /**
   * When an instance of InstrumentationConnection receive a broadcast that notifies of other
   * instances of InstrumentationConnection it than sends back its own IBinder to the new instances
   * in order facilitated future IPC. This tests mimics this behaviour to ensure the
   * InstrumentationConnection under tests: 1. properly keeps track of received messenger 2. sends
   * back an ACK response that the message was received and handled 3. clears its messenger set
   * after termination
   */
  @Test
  public void verifyMessengerCommunicationBetweenTwoInstrumentations()
      throws RemoteException, InterruptedException {
    instrumentationConnection.init(mockedInstrumentation, mockedFinisher);

    // use a latch to ensure synchronous response
    final CountDownLatch latch = new CountDownLatch(1);

    // create a tmp handler to receive a response from the InstrumentationConnection under test
    HandlerThread handlerThread = new HandlerThread("MySuperAwesomeHandlerThread");
    handlerThread.start();
    Handler handler =
        new Handler(handlerThread.getLooper()) {
          @Override
          public void handleMessage(Message msg) {
            // verify that we got a reply from InstrumentationConnection messenger
            assertEquals(InstrumentationConnection.MSG_ADD_CLIENTS_IN_BUNDLE, msg.what);
            latch.countDown();
            getLooper().quit();
          }
        };
    // create a tmp messenger to represent "other" instance of InstrumentationConnection
    Messenger tmpReplyToMessenger = new Messenger(handler);

    // add a fake client to get a response from the caller instrumentation
    Set<Messenger> clients = new HashSet<>();
    clients.add(tmpReplyToMessenger);
    instrumentationConnection.incomingHandler.typedClients.put("123", clients);

    // mimic a MSG_ADD_INSTRUMENTATION response with the tmp messenger
    Message msg = Message.obtain(null, InstrumentationConnection.MSG_ADD_INSTRUMENTATION);
    msg.replyTo = tmpReplyToMessenger;
    instrumentationConnection.incomingHandler.sendMessage(msg);

    // to insure synchronization, wait for the the tmp handler to receive the
    // MSG_ADD_CLIENTS_IN_BUNDLE message from the InstrumentationConnection under test
    assertTrue("latch timed out!", latch.await(1, TimeUnit.SECONDS));

    // ensue InstrumentationConnection keeps track of the newly received messenger
    assertEquals(1, instrumentationConnection.incomingHandler.otherInstrumentations.size());
    assertTrue(
        instrumentationConnection.incomingHandler.otherInstrumentations.contains(
            tmpReplyToMessenger));

    // ensure we clear the set of other messenger after termination
    instrumentationConnection.terminate();
    assertNull(instrumentationConnection.incomingHandler);
  }

  @Test
  public void verifyCallingTerminateBeforeInitInitializeShouldNotExplode() {
    instrumentationConnection.terminate();
    instrumentationConnection.terminate();
    instrumentationConnection.terminate();

    // verify unregisterReceiver is never called
    verify(mockedContext, never()).unregisterReceiver(any(BroadcastReceiver.class));
  }

  @Test
  public void verifyBroadcastRegistrationOnlyCalledOnce() {
    instrumentationConnection.init(mockedInstrumentation, mockedFinisher);
    instrumentationConnection.init(mockedInstrumentation, mockedFinisher);
    instrumentationConnection.init(mockedInstrumentation, mockedFinisher);
    instrumentationConnection.init(mockedInstrumentation, mockedFinisher);

    // verify registerReceiver called only once
    ArgumentCaptor<BroadcastReceiver> brArg = ArgumentCaptor.forClass(BroadcastReceiver.class);
    ArgumentCaptor<IntentFilter> ifArg = ArgumentCaptor.forClass(IntentFilter.class);
    verify(mockedContext).registerReceiver(brArg.capture(), ifArg.capture());
    assertEquals(instrumentationConnection.messengerReceiver, brArg.getValue());
    assertEquals(InstrumentationConnection.BROADCAST_FILTER, ifArg.getValue().getAction(0));
  }

  @Test
  public void verifyClientRegAndUnReg() throws InterruptedException {
    instrumentationConnection.init(mockedInstrumentation, mockedFinisher);
    IncomingHandler incomingHandler = instrumentationConnection.incomingHandler;
    // create client
    Messenger client = new Messenger(new Handler(Looper.getMainLooper()));
    String clientType = "1";
    // register client
    instrumentationConnection.registerClient(clientType, client);
    // wait for all messages to be handled
    waitForMsgHandling(incomingHandler);
    // ensure TypedClient list contain client
    assertEquals(1, incomingHandler.typedClients.size());
    assertTrue(incomingHandler.typedClients.get(clientType).contains(client));
    // un-register only client
    instrumentationConnection.unregisterClient(clientType, client);
    waitForMsgHandling(incomingHandler);
    // ensure no clients
    assertEquals(0, incomingHandler.typedClients.size());
    assertNull(incomingHandler.typedClients.get(clientType));
    // terminate
    instrumentationConnection.terminate();
    assertNull(instrumentationConnection.incomingHandler);
  }

  @Test
  public void verifyMultiClientRegAndUnReg() throws InterruptedException {
    instrumentationConnection.init(mockedInstrumentation, mockedFinisher);
    IncomingHandler incomingHandler = instrumentationConnection.incomingHandler;

    // Create clients
    Messenger client1 = new Messenger(new Handler(Looper.getMainLooper()));
    Messenger client2 = new Messenger(new Handler(Looper.getMainLooper()));
    Messenger client3 = new Messenger(new Handler(Looper.getMainLooper()));
    String clientType = "2";

    // register clients
    instrumentationConnection.registerClient(clientType, client1);
    instrumentationConnection.registerClient(clientType, client2);
    instrumentationConnection.registerClient(clientType, client3);

    // wait for all messages to be handled
    waitForMsgHandling(incomingHandler);

    // ensure TypedClient list contain all clients
    assertEquals(1, incomingHandler.typedClients.size());
    assertEquals(3, incomingHandler.typedClients.get(clientType).size());
    assertTrue(incomingHandler.typedClients.get(clientType).contains(client1));
    assertTrue(incomingHandler.typedClients.get(clientType).contains(client2));
    assertTrue(incomingHandler.typedClients.get(clientType).contains(client3));

    // un-register only client1
    instrumentationConnection.unregisterClient(clientType, client1);
    waitForMsgHandling(incomingHandler);

    // ensure TypedClient list contain all client2 and client3 while client1 is gone
    assertEquals(1, incomingHandler.typedClients.size());
    assertEquals(2, incomingHandler.typedClients.get(clientType).size());
    assertFalse(incomingHandler.typedClients.get(clientType).contains(client1));
    assertTrue(incomingHandler.typedClients.get(clientType).contains(client2));
    assertTrue(incomingHandler.typedClients.get(clientType).contains(client3));

    // un-register the rest of the clients
    instrumentationConnection.unregisterClient(clientType, client2);
    instrumentationConnection.unregisterClient(clientType, client3);
    waitForMsgHandling(incomingHandler);

    // ensure all clients are gone
    assertEquals(0, incomingHandler.typedClients.size());
    assertNull(incomingHandler.typedClients.get(clientType));

    instrumentationConnection.terminate();
    assertNull(instrumentationConnection.incomingHandler);
  }

  @Test
  public void verifyDupClientRegAddsOnlyOne() throws InterruptedException {
    instrumentationConnection.init(mockedInstrumentation, mockedFinisher);
    IncomingHandler incomingHandler = instrumentationConnection.incomingHandler;

    // Create clients
    Messenger client1 = new Messenger(new Handler(Looper.getMainLooper()));
    String clientType = "3";

    // register clients
    instrumentationConnection.registerClient(clientType, client1);
    instrumentationConnection.registerClient(clientType, client1);
    instrumentationConnection.registerClient(clientType, client1);

    // wait for all messages to be handled
    waitForMsgHandling(incomingHandler);

    // ensure TypedClient list contain all clients
    assertEquals(1, incomingHandler.typedClients.size());
    assertEquals(1, incomingHandler.typedClients.get(clientType).size());
    assertTrue(incomingHandler.typedClients.get(clientType).contains(client1));

    // un-register only client1
    instrumentationConnection.unregisterClient(clientType, client1);
    waitForMsgHandling(incomingHandler);

    // ensure all client1 is gone
    assertEquals(0, incomingHandler.typedClients.size());
    assertNull(incomingHandler.typedClients.get(clientType));

    instrumentationConnection.terminate();
    assertNull(instrumentationConnection.incomingHandler);
  }

  @Test
  public void verifyDupClientUnRegShouldNotThrow() throws InterruptedException {
    instrumentationConnection.init(mockedInstrumentation, mockedFinisher);
    IncomingHandler incomingHandler = instrumentationConnection.incomingHandler;

    // Create clients
    Messenger client1 = new Messenger(new Handler(Looper.getMainLooper()));
    String clientType = "4";

    // register clients
    instrumentationConnection.registerClient(clientType, client1);

    // wait for all messages to be handled
    waitForMsgHandling(incomingHandler);

    // ensure TypedClient list contain all clients
    assertEquals(1, incomingHandler.typedClients.size());
    assertEquals(1, incomingHandler.typedClients.get(clientType).size());
    assertTrue(incomingHandler.typedClients.get(clientType).contains(client1));

    // un-register only client1
    instrumentationConnection.unregisterClient(clientType, client1);
    instrumentationConnection.unregisterClient(clientType, client1);
    instrumentationConnection.unregisterClient(clientType, client1);
    waitForMsgHandling(incomingHandler);

    // ensure all client1 is gone
    assertEquals(0, incomingHandler.typedClients.size());
    assertNull(incomingHandler.typedClients.get(clientType));

    instrumentationConnection.terminate();
    assertNull(instrumentationConnection.incomingHandler);
  }

  @Test
  public void verifyGetClientsOfType() throws InterruptedException {
    instrumentationConnection.init(mockedInstrumentation, mockedFinisher);
    IncomingHandler incomingHandler = instrumentationConnection.incomingHandler;

    // Create clients
    Messenger client1 = new Messenger(new Handler(Looper.getMainLooper()));
    Messenger client2 = new Messenger(new Handler(Looper.getMainLooper()));
    String desiredClientType = "5";
    String unknownType = "999";

    // register clients
    instrumentationConnection.registerClient(desiredClientType, client1);
    instrumentationConnection.registerClient(desiredClientType, client2);

    // wait for all messages to be handled
    waitForMsgHandling(incomingHandler);

    // ensure client set contain all clients of the desired type
    assertEquals(1, incomingHandler.typedClients.size());
    assertEquals(2, instrumentationConnection.getClientsForType(desiredClientType).size());
    // ensure client set does not contain clients of an unknown type
    assertNull(instrumentationConnection.getClientsForType(unknownType));

    instrumentationConnection.terminate();
    assertNull(instrumentationConnection.incomingHandler);
  }

  @Test
  public void verifyActivityFinisher() {
    instrumentationConnection.init(mockedInstrumentation, mockedFinisher);

    // mimic remote instrumentation presence
    InstrumentationConnection other = new InstrumentationConnection(mockedContext);
    other.init(mockedInstrumentation, mockedFinisher);
    instrumentationConnection.incomingHandler.otherInstrumentations.add(
        new Messenger(other.incomingHandler));

    // request cleanup
    instrumentationConnection.requestRemoteInstancesActivityCleanup();

    // verify clean up was done
    verify(mockedInstrumentation).runOnMainSync(mockedFinisher);

    instrumentationConnection.terminate();
    assertNull(instrumentationConnection.incomingHandler);
  }

  private void waitForMsgHandling(Handler handler) throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    handler.post(
        new Runnable() {
          @Override
          public void run() {
            latch.countDown();
          }
        });
    if (!latch.await(100, TimeUnit.MILLISECONDS)) {
      fail("waitForMsgHandling timed out");
    }
  }
}
