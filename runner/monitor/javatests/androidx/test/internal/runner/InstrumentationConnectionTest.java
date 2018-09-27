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
import static org.mockito.Matchers.any;
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

  @Mock private Context mMockedContext;
  @Mock private Instrumentation mMockedInstrumentation;
  @Mock private MonitoringInstrumentation.ActivityFinisher mMockedFinisher;

  InstrumentationConnection mInstrumentationConnection;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    mInstrumentationConnection = new InstrumentationConnection(mMockedContext);
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
    mInstrumentationConnection.init(mMockedInstrumentation, mMockedFinisher);

    // verify broadcast was sent
    ArgumentCaptor<Intent> intentArg = ArgumentCaptor.forClass(Intent.class);
    verify(mMockedContext).sendBroadcast(intentArg.capture());
    Intent receivedIntent = intentArg.getValue();
    assertNotNull(receivedIntent.getAction());
    ParcelableIBinder parcelableIBinder =
        receivedIntent
            .getBundleExtra(InstrumentationConnection.BUNDLE_BR_NEW_BINDER)
            .getParcelable(InstrumentationConnection.BUNDLE_BR_NEW_BINDER);
    Messenger msgr = new Messenger(parcelableIBinder.getIBinder());
    assertEquals(mInstrumentationConnection.mIncomingHandler.mMessengerHandler, msgr);

    // verify broadcast registration
    ArgumentCaptor<BroadcastReceiver> brArg = ArgumentCaptor.forClass(BroadcastReceiver.class);
    ArgumentCaptor<IntentFilter> ifArg = ArgumentCaptor.forClass(IntentFilter.class);
    verify(mMockedContext).registerReceiver(brArg.capture(), ifArg.capture());
    assertEquals(mInstrumentationConnection.mMessengerReceiver, brArg.getValue());
    assertEquals(InstrumentationConnection.BROADCAST_FILTER, ifArg.getValue().getAction(0));

    mInstrumentationConnection.terminate();

    // very broadcast un-registration
    verify(mMockedContext).unregisterReceiver(brArg.capture());
    assertEquals(mInstrumentationConnection.mMessengerReceiver, brArg.getValue());
    assertNull(mInstrumentationConnection.mIncomingHandler);
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
    mInstrumentationConnection.init(mMockedInstrumentation, mMockedFinisher);

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
    mInstrumentationConnection.mIncomingHandler.mTypedClients.put("123", clients);

    // mimic a MSG_ADD_INSTRUMENTATION response with the tmp messenger
    Message msg = Message.obtain(null, InstrumentationConnection.MSG_ADD_INSTRUMENTATION);
    msg.replyTo = tmpReplyToMessenger;
    mInstrumentationConnection.mIncomingHandler.sendMessage(msg);

    // to insure synchronization, wait for the the tmp handler to receive the
    // MSG_ADD_CLIENTS_IN_BUNDLE message from the InstrumentationConnection under test
    assertTrue("latch timed out!", latch.await(1, TimeUnit.SECONDS));

    // ensue InstrumentationConnection keeps track of the newly received messenger
    assertEquals(1, mInstrumentationConnection.mIncomingHandler.mOtherInstrumentations.size());
    assertTrue(
        mInstrumentationConnection.mIncomingHandler.mOtherInstrumentations.contains(
            tmpReplyToMessenger));

    // ensure we clear the set of other messenger after termination
    mInstrumentationConnection.terminate();
    assertNull(mInstrumentationConnection.mIncomingHandler);
  }

  @Test
  public void verifyCallingTerminateBeforeInitInitializeShouldNotExplode() {
    mInstrumentationConnection.terminate();
    mInstrumentationConnection.terminate();
    mInstrumentationConnection.terminate();

    // verify unregisterReceiver is never called
    verify(mMockedContext, never()).unregisterReceiver(any(BroadcastReceiver.class));
  }

  @Test
  public void verifyBroadcastRegistrationOnlyCalledOnce() {
    mInstrumentationConnection.init(mMockedInstrumentation, mMockedFinisher);
    mInstrumentationConnection.init(mMockedInstrumentation, mMockedFinisher);
    mInstrumentationConnection.init(mMockedInstrumentation, mMockedFinisher);
    mInstrumentationConnection.init(mMockedInstrumentation, mMockedFinisher);

    // verify registerReceiver called only once
    ArgumentCaptor<BroadcastReceiver> brArg = ArgumentCaptor.forClass(BroadcastReceiver.class);
    ArgumentCaptor<IntentFilter> ifArg = ArgumentCaptor.forClass(IntentFilter.class);
    verify(mMockedContext).registerReceiver(brArg.capture(), ifArg.capture());
    assertEquals(mInstrumentationConnection.mMessengerReceiver, brArg.getValue());
    assertEquals(InstrumentationConnection.BROADCAST_FILTER, ifArg.getValue().getAction(0));
  }

  @Test
  public void verifyClientRegAndUnReg() throws InterruptedException {
    mInstrumentationConnection.init(mMockedInstrumentation, mMockedFinisher);
    IncomingHandler incomingHandler = mInstrumentationConnection.mIncomingHandler;
    // create client
    Messenger client = new Messenger(new Handler(Looper.getMainLooper()));
    String clientType = "1";
    // register client
    mInstrumentationConnection.registerClient(clientType, client);
    // wait for all messages to be handled
    waitForMsgHandling(incomingHandler);
    // ensure TypedClient list contain client
    assertEquals(1, incomingHandler.mTypedClients.size());
    assertTrue(incomingHandler.mTypedClients.get(clientType).contains(client));
    // un-register only client
    mInstrumentationConnection.unregisterClient(clientType, client);
    waitForMsgHandling(incomingHandler);
    // ensure no clients
    assertEquals(0, incomingHandler.mTypedClients.size());
    assertNull(incomingHandler.mTypedClients.get(clientType));
    // terminate
    mInstrumentationConnection.terminate();
    assertNull(mInstrumentationConnection.mIncomingHandler);
  }

  @Test
  public void verifyMultiClientRegAndUnReg() throws InterruptedException {
    mInstrumentationConnection.init(mMockedInstrumentation, mMockedFinisher);
    IncomingHandler incomingHandler = mInstrumentationConnection.mIncomingHandler;

    // Create clients
    Messenger client1 = new Messenger(new Handler(Looper.getMainLooper()));
    Messenger client2 = new Messenger(new Handler(Looper.getMainLooper()));
    Messenger client3 = new Messenger(new Handler(Looper.getMainLooper()));
    String clientType = "2";

    // register clients
    mInstrumentationConnection.registerClient(clientType, client1);
    mInstrumentationConnection.registerClient(clientType, client2);
    mInstrumentationConnection.registerClient(clientType, client3);

    // wait for all messages to be handled
    waitForMsgHandling(incomingHandler);

    // ensure TypedClient list contain all clients
    assertEquals(1, incomingHandler.mTypedClients.size());
    assertEquals(3, incomingHandler.mTypedClients.get(clientType).size());
    assertTrue(incomingHandler.mTypedClients.get(clientType).contains(client1));
    assertTrue(incomingHandler.mTypedClients.get(clientType).contains(client2));
    assertTrue(incomingHandler.mTypedClients.get(clientType).contains(client3));

    // un-register only client1
    mInstrumentationConnection.unregisterClient(clientType, client1);
    waitForMsgHandling(incomingHandler);

    // ensure TypedClient list contain all client2 and client3 while client1 is gone
    assertEquals(1, incomingHandler.mTypedClients.size());
    assertEquals(2, incomingHandler.mTypedClients.get(clientType).size());
    assertFalse(incomingHandler.mTypedClients.get(clientType).contains(client1));
    assertTrue(incomingHandler.mTypedClients.get(clientType).contains(client2));
    assertTrue(incomingHandler.mTypedClients.get(clientType).contains(client3));

    // un-register the rest of the clients
    mInstrumentationConnection.unregisterClient(clientType, client2);
    mInstrumentationConnection.unregisterClient(clientType, client3);
    waitForMsgHandling(incomingHandler);

    // ensure all clients are gone
    assertEquals(0, incomingHandler.mTypedClients.size());
    assertNull(incomingHandler.mTypedClients.get(clientType));

    mInstrumentationConnection.terminate();
    assertNull(mInstrumentationConnection.mIncomingHandler);
  }

  @Test
  public void verifyDupClientRegAddsOnlyOne() throws InterruptedException {
    mInstrumentationConnection.init(mMockedInstrumentation, mMockedFinisher);
    IncomingHandler incomingHandler = mInstrumentationConnection.mIncomingHandler;

    // Create clients
    Messenger client1 = new Messenger(new Handler(Looper.getMainLooper()));
    String clientType = "3";

    // register clients
    mInstrumentationConnection.registerClient(clientType, client1);
    mInstrumentationConnection.registerClient(clientType, client1);
    mInstrumentationConnection.registerClient(clientType, client1);

    // wait for all messages to be handled
    waitForMsgHandling(incomingHandler);

    // ensure TypedClient list contain all clients
    assertEquals(1, incomingHandler.mTypedClients.size());
    assertEquals(1, incomingHandler.mTypedClients.get(clientType).size());
    assertTrue(incomingHandler.mTypedClients.get(clientType).contains(client1));

    // un-register only client1
    mInstrumentationConnection.unregisterClient(clientType, client1);
    waitForMsgHandling(incomingHandler);

    // ensure all client1 is gone
    assertEquals(0, incomingHandler.mTypedClients.size());
    assertNull(incomingHandler.mTypedClients.get(clientType));

    mInstrumentationConnection.terminate();
    assertNull(mInstrumentationConnection.mIncomingHandler);
  }

  @Test
  public void verifyDupClientUnRegShouldNotThrow() throws InterruptedException {
    mInstrumentationConnection.init(mMockedInstrumentation, mMockedFinisher);
    IncomingHandler incomingHandler = mInstrumentationConnection.mIncomingHandler;

    // Create clients
    Messenger client1 = new Messenger(new Handler(Looper.getMainLooper()));
    String clientType = "4";

    // register clients
    mInstrumentationConnection.registerClient(clientType, client1);

    // wait for all messages to be handled
    waitForMsgHandling(incomingHandler);

    // ensure TypedClient list contain all clients
    assertEquals(1, incomingHandler.mTypedClients.size());
    assertEquals(1, incomingHandler.mTypedClients.get(clientType).size());
    assertTrue(incomingHandler.mTypedClients.get(clientType).contains(client1));

    // un-register only client1
    mInstrumentationConnection.unregisterClient(clientType, client1);
    mInstrumentationConnection.unregisterClient(clientType, client1);
    mInstrumentationConnection.unregisterClient(clientType, client1);
    waitForMsgHandling(incomingHandler);

    // ensure all client1 is gone
    assertEquals(0, incomingHandler.mTypedClients.size());
    assertNull(incomingHandler.mTypedClients.get(clientType));

    mInstrumentationConnection.terminate();
    assertNull(mInstrumentationConnection.mIncomingHandler);
  }

  @Test
  public void verifyGetClientsOfType() throws InterruptedException {
    mInstrumentationConnection.init(mMockedInstrumentation, mMockedFinisher);
    IncomingHandler incomingHandler = mInstrumentationConnection.mIncomingHandler;

    // Create clients
    Messenger client1 = new Messenger(new Handler(Looper.getMainLooper()));
    Messenger client2 = new Messenger(new Handler(Looper.getMainLooper()));
    String desiredClientType = "5";
    String unknownType = "999";

    // register clients
    mInstrumentationConnection.registerClient(desiredClientType, client1);
    mInstrumentationConnection.registerClient(desiredClientType, client2);

    // wait for all messages to be handled
    waitForMsgHandling(incomingHandler);

    // ensure client set contain all clients of the desired type
    assertEquals(1, incomingHandler.mTypedClients.size());
    assertEquals(2, mInstrumentationConnection.getClientsForType(desiredClientType).size());
    // ensure client set does not contain clients of an unknown type
    assertNull(mInstrumentationConnection.getClientsForType(unknownType));

    mInstrumentationConnection.terminate();
    assertNull(mInstrumentationConnection.mIncomingHandler);
  }

  @Test
  public void verifyActivityFinisher() {
    mInstrumentationConnection.init(mMockedInstrumentation, mMockedFinisher);

    // mimic remote instrumentation presence
    InstrumentationConnection other = new InstrumentationConnection(mMockedContext);
    other.init(mMockedInstrumentation, mMockedFinisher);
    mInstrumentationConnection.mIncomingHandler.mOtherInstrumentations.add(
        new Messenger(other.mIncomingHandler));

    // request cleanup
    mInstrumentationConnection.requestRemoteInstancesActivityCleanup();

    // verify clean up was done
    verify(mMockedInstrumentation).runOnMainSync(mMockedFinisher);

    mInstrumentationConnection.terminate();
    assertNull(mInstrumentationConnection.mIncomingHandler);
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
