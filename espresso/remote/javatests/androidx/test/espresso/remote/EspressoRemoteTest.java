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
 */

package androidx.test.espresso.remote;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.remote.EspressoRemote.BUNDLE_KEY_PROTO;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.proto.UiInteraction.InteractionResultProto;
import androidx.test.espresso.remote.EspressoRemote.RemoteInteractionStrategy;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.internal.runner.InstrumentationConnection;
import androidx.test.internal.util.ParcelableIBinder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Tests exercise {@link EspressoRemote}'s logic of remote Espresso instance discovery and
 * communication
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class EspressoRemoteTest {
  @Mock private InstrumentationConnection mockedInstrumentation;
  private ListeningExecutorService remoteExecutor;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    remoteExecutor =
        MoreExecutors.listeningDecorator(
            new ThreadPoolExecutor(
                0 /*corePoolSize*/,
                5 /*maximumPoolSize*/,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadFactoryBuilder().setNameFormat("Espresso Remote #%d").build()));
  }

  @Test
  public void verifyInitTerminateRegistersUnregistersWithInstrumentation()
      throws InterruptedException {
    final EspressoRemote espressoRemote = new EspressoRemote(mockedInstrumentation);
    espressoRemote.init();
    Messenger messenger = espressoRemote.incomingHandler.messengerHandler;
    verify(mockedInstrumentation).registerClient(EspressoRemote.TYPE, messenger);
    espressoRemote.terminate();
    // Termination happens synchronously on the handler thread. At the end of the termination
    // method handler thread is going to quit so there is no good way to synchronise this test other
    // than sleep
    Thread.sleep(100);
    verify(mockedInstrumentation).unregisterClient(EspressoRemote.TYPE, messenger);
  }

  @Test
  public void verifyMultipleInitsAreIgnored() {
    final EspressoRemote espressoRemote = new EspressoRemote(mockedInstrumentation);
    espressoRemote.init();
    espressoRemote.init();
    espressoRemote.init();
    Messenger messenger = espressoRemote.incomingHandler.messengerHandler;
    verify(mockedInstrumentation).registerClient(EspressoRemote.TYPE, messenger);
  }

  @Test
  public void verifyValidRequestResponseToRemoteEspresso() throws InterruptedException {
    final EspressoRemote espressoRemote = new EspressoRemote(mockedInstrumentation);
    espressoRemote.init();

    // create a tmp handler to receive a response from the EspressoRemote under test
    HandlerThread handlerThread = new HandlerThread("OtherEspresso");
    handlerThread.start();
    Handler handler =
        new Handler(handlerThread.getLooper()) {
          @Override
          public void handleMessage(Message msg) {
            // verify that other remote espresso received a request
            assertEquals(EspressoRemote.MSG_HANDLE_ESPRESSO_REQUEST, msg.what);
            // TODO: remove when actual proto is ready
            try {
              assertEquals(
                  "TBDProto", new String(msg.getData().getByteArray(BUNDLE_KEY_PROTO), "UTF-8"));

            } catch (UnsupportedEncodingException e) {
              fail("UnsupportedEncodingException was thrown");
            }

            // mimic a response
            msg.what = EspressoRemote.MSG_HANDLE_ESPRESSO_RESPONSE;
            Bundle resultData = msg.getData();
            resultData.putByteArray(
                BUNDLE_KEY_PROTO,
                InteractionResultProto.newBuilder().setOk(true).build().toByteArray());
            msg.setData(resultData);
            espressoRemote.incomingHandler.handleMessage(msg);

            getLooper().quit();
          }
        };
    // create a tmp messenger to represent "other" remote Espresso
    Messenger otherEspressoMessenger = new Messenger(handler);
    Set<Messenger> clients = new HashSet<>();
    clients.add(otherEspressoMessenger);
    when(mockedInstrumentation.getClientsForType(EspressoRemote.TYPE)).thenReturn(clients);

    espressoRemote.initiateRemoteCall("TBDProto".getBytes(), null);
  }

  @Test
  public void verifyExceptionResultReturnedBackToTheCaller() {
    final EspressoRemote espressoRemote = new EspressoRemote(mockedInstrumentation);
    espressoRemote.init();

    // create a tmp handler to receive a response from the EspressoRemote under test
    HandlerThread handlerThread = new HandlerThread("OtherEspresso");
    handlerThread.start();
    Handler handler =
        new Handler(handlerThread.getLooper()) {
          @Override
          public void handleMessage(Message msg) {
            // verify that other remote espresso received a request
            assertEquals(EspressoRemote.MSG_HANDLE_ESPRESSO_REQUEST, msg.what);
            espressoRemote.incomingHandler.handleMessage(msg);

            getLooper().quit();
          }
        };
    // create a tmp messenger to represent "other" remote Espresso
    Messenger otherEspressoMessenger = new Messenger(handler);
    Set<Messenger> clients = new HashSet<>();
    clients.add(otherEspressoMessenger);
    when(mockedInstrumentation.getClientsForType(EspressoRemote.TYPE)).thenReturn(clients);

    try {
      espressoRemote.initiateRemoteCall(null, null);
      fail("Expected RemoteEspressoException to be thrown");
    } catch (RemoteEspressoException e) {
      // expected
    }
  }

  @Test
  public void verifyInterruptedRequest_followsUpWithEmptyRequest() {
    final EspressoRemote espressoRemote = new EspressoRemote(mockedInstrumentation);
    espressoRemote.init();

    final CountDownLatch espressoRequestLatch = new CountDownLatch(1);
    final CountDownLatch emptyRequestLatch = new CountDownLatch(1);

    // create a tmp handler to receive a response from the EspressoRemote under test
    final HandlerThread handlerThread = new HandlerThread("OtherEspresso");
    handlerThread.start();
    Handler handler =
        new Handler(handlerThread.getLooper()) {
          @Override
          public void handleMessage(Message msg) {
            switch (msg.what) {
              case EspressoRemote.MSG_HANDLE_ESPRESSO_REQUEST:
                espressoRequestLatch.countDown();
                break;
              case EspressoRemote.MSG_HANDLE_EMPTY_REQUEST:
                emptyRequestLatch.countDown();
                break;
              default:
                super.handleMessage(msg);
            }
          }
        };

    // create a tmp messenger to represent "other" remote Espresso
    Messenger otherEspressoMessenger = new Messenger(handler);
    Set<Messenger> clients = new HashSet<>();
    clients.add(otherEspressoMessenger);
    clients.add(espressoRemote.incomingHandler.messengerHandler);
    when(mockedInstrumentation.getClientsForType(EspressoRemote.TYPE)).thenReturn(clients);

    // send out an interaction request to remote espresso
    ListenableFuture<Void> future =
        remoteExecutor.submit(
            espressoRemote.createRemoteCheckCallable(
                RootMatchers.DEFAULT, withId(123), null, matches(withText(is("test")))));

    try {
      // wait until remote Espresso receives an interaction request
      assertTrue(espressoRequestLatch.await(200, TimeUnit.MILLISECONDS));
      // interrupt the remote interaction request
      future.cancel(true);
      // ensure extra empty message was sent out to flush out the remote instance handler queue
      assertTrue(emptyRequestLatch.await(200, TimeUnit.MILLISECONDS));
    } catch (InterruptedException e) {
      fail("Unexpected InterruptedException");
    }

    // clean up
    handlerThread.getLooper().quit();
  }

  @Test
  public void remoteInteractionStrategy_addsViewActionBinders_fromBundle() {
    Matcher viewMatcherMock = Mockito.mock(Matcher.class);
    Matcher rootMatcherMock = Mockito.mock(Matcher.class);
    IBinder binderMock = Mockito.mock(IBinder.class);
    Bindable bindableMock = Mockito.mock(Bindable.class);
    String bindableViewActionId = BindableViewAction.class.getSimpleName();
    when(bindableMock.getId()).thenReturn(bindableViewActionId);
    ViewAction bindableViewAction = new BindableViewAction(bindableMock);

    InteractionRequest interactionRequest =
        new InteractionRequest(rootMatcherMock, viewMatcherMock, bindableViewAction, null);

    Bundle bundle = new Bundle();
    bundle.putParcelable(bindableMock.getId(), new ParcelableIBinder(binderMock));

    RemoteInteractionStrategy.from(interactionRequest, bundle);

    verify(bindableMock).setIBinder(binderMock);
  }

  @Test
  public void remoteInteractionStrategy_addsViewAssertionBinders_fromBundle() {
    Matcher viewMatcherMock = Mockito.mock(Matcher.class);
    Matcher rootMatcherMock = Mockito.mock(Matcher.class);
    IBinder binderMock = Mockito.mock(IBinder.class);
    Bindable bindableMock = Mockito.mock(Bindable.class);
    String bindableViewAssertionId = BindableViewAssertion.class.getSimpleName();
    when(bindableMock.getId()).thenReturn(bindableViewAssertionId);
    ViewAssertion bindableViewAssertion = new BindableViewAssertion(bindableMock);

    InteractionRequest interactionRequest =
        new InteractionRequest(rootMatcherMock, viewMatcherMock, null, bindableViewAssertion);

    Bundle bundle = new Bundle();
    bundle.putParcelable(bindableMock.getId(), new ParcelableIBinder(binderMock));
    RemoteInteractionStrategy.from(interactionRequest, bundle);

    verify(bindableMock).setIBinder(binderMock);
  }

  private static final class BindableViewAction implements ViewAction, Bindable {

    private final Bindable bindableMock;

    BindableViewAction(Bindable bindableMock) {
      this.bindableMock = bindableMock;
    }

    @Override
    public Matcher<View> getConstraints() {
      return null;
    }

    @Override
    public String getDescription() {
      return "";
    }

    @Override
    public void perform(UiController uiController, View view) {
      // do nothing
    }

    @Override
    public String getId() {
      return bindableMock.getId();
    }

    @Override
    public IBinder getIBinder() {
      return bindableMock.getIBinder();
    }

    @Override
    public void setIBinder(IBinder binder) {
      this.bindableMock.setIBinder(binder);
    }
  }

  private static final class BindableViewAssertion implements ViewAssertion, Bindable {

    private final Bindable bindableMock;

    BindableViewAssertion(Bindable bindableMock) {
      this.bindableMock = bindableMock;
    }

    @Override
    public String getId() {
      return bindableMock.getId();
    }

    @Override
    public IBinder getIBinder() {
      return bindableMock.getIBinder();
    }

    @Override
    public void setIBinder(IBinder binder) {
      this.bindableMock.setIBinder(binder);
    }

    @Override
    public void check(View view, NoMatchingViewException noViewFoundException) {
      // do nothing
    }
  }
}
