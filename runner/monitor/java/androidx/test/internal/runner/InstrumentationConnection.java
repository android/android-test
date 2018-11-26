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

package androidx.test.internal.runner;

import static androidx.test.internal.util.Checks.checkNotNull;
import static androidx.test.internal.util.Checks.checkState;
import static androidx.test.internal.util.LogUtil.logDebugWithProcess;

import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import android.util.Log;
import androidx.test.annotation.Beta;
import androidx.test.internal.util.ParcelableIBinder;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.MonitoringInstrumentation;
import androidx.test.runner.MonitoringInstrumentation.ActivityFinisher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * A singleton class that facilitates the communication between different instrumentation test
 * runner instances running on different processes.
 *
 * <p>Upon creation, a broadcast with an Intent containing a {@link #BROADCAST_FILTER} action, will
 * be sent out to notify other runners of this instance. During handshake, both instances will
 * exchange their own {@link IBinder}'s for further IPC, along with the list of all known client's
 * {@link Messenger}s. Where a client could be any other testing framework that is built on top of
 * AndroidJUnitRunner (example: Espresso, UiAutomator, etc.)
 *
 * <p>To get the instance of this object {@link #getInstance()} should be called. The user of this
 * class should then call {@link #init(Instrumentation, ActivityFinisher)} prior to attempting to
 * use any functionality of this class. Call {@link #terminate()} after using
 * InstrumentationConnection to release any resources. Failure to do so will lead to memory leaks
 * and unexpected behavior.
 *
 * <p><b>This API is currently in beta.</b>
 */
@Beta
public class InstrumentationConnection {
  private static final String TAG = "InstrConnection";

  private static final InstrumentationConnection DEFAULT_INSTANCE =
      new InstrumentationConnection(
          InstrumentationRegistry.getInstrumentation().getTargetContext());

  private static final String BUNDLE_KEY_CLIENTS = "instr_clients";
  private static final String BUNDLE_KEY_CLIENT_TYPE = "instr_client_type";
  private static final String BUNDLE_KEY_CLIENT_MESSENGER = "instr_client_msgr";
  private static final String BUNDLE_KEY_UUID = "instr_uuid";

  @VisibleForTesting static final String BUNDLE_BR_NEW_BINDER = "new_instrumentation_binder";

  /** The intent action used to discover other instrumentation instances */
  public static final String BROADCAST_FILTER =
      "androidx.test.runner.InstrumentationConnection.event";

  private static final int MSG_REMOTE_ADD_CLIENT = 0;
  private static final int MSG_REMOTE_REMOVE_CLIENT = 1;
  private static final int MSG_TERMINATE = 2;
  private static final int MSG_HANDLE_INSTRUMENTATION_FROM_BROADCAST = 3;
  @VisibleForTesting static final int MSG_ADD_INSTRUMENTATION = 4;
  private static final int MSG_REMOVE_INSTRUMENTATION = 5;
  @VisibleForTesting static final int MSG_ADD_CLIENTS_IN_BUNDLE = 6;
  private static final int MSG_REMOVE_CLIENTS_IN_BUNDLE = 7;
  private static final int MSG_REG_CLIENT = 8;
  private static final int MSG_UN_REG_CLIENT = 9;
  @VisibleForTesting static final int MSG_REMOTE_CLEANUP_REQUEST = 10;
  private static final int MSG_PERFORM_CLEANUP = 11;
  private static final int MSG_PERFORM_CLEANUP_FINISHED = 12;

  private Context targetContext;
  private static Instrumentation instrumentation;
  private static MonitoringInstrumentation.ActivityFinisher activityFinisher;

  /**
   * The {@link IncomingHandler} that will handle all the incoming messages via {@link
   * IncomingHandler#messengerHandler}
   */
  IncomingHandler incomingHandler;

  /** Receiver used to discover and establish communication with new instrumentation instances */
  @VisibleForTesting final BroadcastReceiver messengerReceiver = new MessengerReceiver();

  @VisibleForTesting
  InstrumentationConnection(@NonNull Context context) {
    targetContext = checkNotNull(context, "Context can't be null");
  }

  /**
   * Returns a {@link InstrumentationConnection} object
   *
   * @return an instance of {@link InstrumentationConnection} object.
   */
  public static InstrumentationConnection getInstance() {
    return DEFAULT_INSTANCE;
  }

  /**
   * The initialization consists of:
   *
   * <ol>
   *   <li>Sending a broadcast via a well known {@link Intent} that contains a {@link
   *       IncomingHandler} which others can use the send messages.
   *   <li>Registering this instance for the same intent broadcasts to enable future discovery of
   *       newly started instrumentation runners on other processes.
   * </ol>
   *
   * The caller of this method must call {@link #terminate()} to preform the proper clean up which
   * includes unregister from the above defined broadcast.
   *
   * @param instrumentation the {@link Instrumentation} instance this running in
   * @param finisher an activity finisher to use when performing cleanup
   */
  public synchronized void init(
      Instrumentation instrumentation, MonitoringInstrumentation.ActivityFinisher finisher) {
    logDebugWithProcess(TAG, "init");

    if (null == incomingHandler) {
      InstrumentationConnection.instrumentation = instrumentation;
      activityFinisher = finisher;
      HandlerThread ht = new HandlerThread("InstrumentationConnectionThread");
      ht.start();
      incomingHandler = new IncomingHandler(ht.getLooper());

      // Inform other instances of yourself
      Intent intent = new Intent(BROADCAST_FILTER);
      Bundle bundle = new Bundle();
      bundle.putParcelable(
          BUNDLE_BR_NEW_BINDER,
          new ParcelableIBinder(incomingHandler.messengerHandler.getBinder()));
      intent.putExtra(BUNDLE_BR_NEW_BINDER, bundle);
      try {
        targetContext.sendBroadcast(intent);
        // TODO: Consider enforcing permissions when registering for a receiver
        targetContext.registerReceiver(messengerReceiver, new IntentFilter(BROADCAST_FILTER));
      } catch (SecurityException isolatedProcess) {
        Log.i(TAG, "Could not send broadcast or register receiver (isolatedProcess?)");
      }
    }
  }

  /**
   * This methods should be called after {@link #init(Instrumentation, ActivityFinisher)} was
   * called. The purpose of this method is to preform any required clean up along with unregistering
   * from any broadcast receivers.
   */
  public synchronized void terminate() {
    logDebugWithProcess(TAG, "Terminate is called");
    if (incomingHandler != null) {
      // post termination message to the handler in case there messages in flight
      incomingHandler.runSyncTask(
          new Callable<Void>() {
            @Override
            public Void call() {
              incomingHandler.doDie();
              return null;
            }
          });
      targetContext.unregisterReceiver(messengerReceiver);
      incomingHandler = null;
    }
  }

  /**
   * Request all remote instrumentation instances to finish all activities in order to insure a
   * clean state before/after each test.
   */
  public synchronized void requestRemoteInstancesActivityCleanup() {
    checkState(incomingHandler != null, "Instrumentation Connection in not yet initialized");

    UUID uuid = UUID.randomUUID();
    CountDownLatch latch = new CountDownLatch(1);
    incomingHandler.associateLatch(uuid, latch);

    Message msg = incomingHandler.obtainMessage(MSG_REMOTE_CLEANUP_REQUEST);
    msg.replyTo = incomingHandler.messengerHandler;
    Bundle bundle = msg.getData();
    bundle.putSerializable(BUNDLE_KEY_UUID, uuid);
    msg.setData(bundle);
    incomingHandler.sendMessage(msg);

    // block until remote clean up is complete, will timeout no reply received within 2 sec
    try {
      if (!latch.await(2, TimeUnit.SECONDS)) {
        Log.w(TAG, "Timed out while attempting to perform activity clean up for " + uuid);
      }
    } catch (InterruptedException e) {
      Log.e(TAG, "Interrupted while waiting for response from message with id: " + uuid, e);
    } finally {
      incomingHandler.disassociateLatch(uuid);
    }
  }

  /**
   * Register a client and notify all other clients of the same type if needed.
   *
   * @param type the type of the client
   * @param messenger a {@link Messenger} to use for future communication with the client
   */
  public synchronized void registerClient(String type, Messenger messenger) {
    checkState(incomingHandler != null, "Instrumentation Connection in not yet initialized");
    Log.i(TAG, "Register client of type: " + type);
    Bundle bundle = new Bundle();
    bundle.putString(BUNDLE_KEY_CLIENT_TYPE, type);
    bundle.putParcelable(BUNDLE_KEY_CLIENT_MESSENGER, messenger);
    Message msg = incomingHandler.obtainMessage(MSG_REG_CLIENT);
    msg.setData(bundle);
    incomingHandler.sendMessage(msg);
  }

  /**
   * Helper method to obtain a set of clients of the same type.
   *
   * @param type the type of the client
   * @return a Set of Messengers of the desired client type, {@code null} is returned if client type
   *     is unknown
   */
  public synchronized Set<Messenger> getClientsForType(final String type) {
    return incomingHandler.getClientsForType(type);
  }

  /**
   * Un-register a client and notify all other clients of the same type if needed.
   *
   * @param type the type of the client
   * @param messenger a {@link Messenger} to use for future communication with the client
   */
  public synchronized void unregisterClient(String type, Messenger messenger) {
    checkState(incomingHandler != null, "Instrumentation Connection in not yet initialized");
    Log.i(TAG, "Unregister client of type: " + type);
    Bundle bundle = new Bundle();
    bundle.putString(BUNDLE_KEY_CLIENT_TYPE, type);
    bundle.putParcelable(BUNDLE_KEY_CLIENT_MESSENGER, messenger);
    Message msg = incomingHandler.obtainMessage(MSG_UN_REG_CLIENT);
    msg.setData(bundle);
    incomingHandler.sendMessage(msg);
  }

  /** Receiver to handle Intent broadcasts with {@link #BROADCAST_FILTER} event. */
  @VisibleForTesting
  class MessengerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      logDebugWithProcess(TAG, "Broadcast received");

      // Extract data included in the Intent
      Bundle extras = intent.getBundleExtra(BUNDLE_BR_NEW_BINDER);
      if (null == extras) {
        Log.w(TAG, "Broadcast intent doesn't contain any extras, ignoring it..");
        return;
      }
      ParcelableIBinder iBinder = extras.getParcelable(BUNDLE_BR_NEW_BINDER);
      if (iBinder != null) {
        Messenger msgr = new Messenger(iBinder.getIBinder());
        Message msg = incomingHandler.obtainMessage(MSG_HANDLE_INSTRUMENTATION_FROM_BROADCAST);
        msg.replyTo = msgr;
        incomingHandler.sendMessage(msg);
      }
    }
  }

  /**
   * Handler of incoming messages from other instrumentation runners.
   *
   * <p>Addition or removal of instrumentation or clients must always be done on the handler thread.
   */
  @VisibleForTesting
  static class IncomingHandler extends Handler {
    /** Target we publish for clients to send messages to IncomingHandler. */
    @VisibleForTesting Messenger messengerHandler = new Messenger(this);
    /**
     * Keeps track of all currently registered clients. Note: This Set should only be modified via
     * the incomingHandler.
     */
    @VisibleForTesting Set<Messenger> otherInstrumentations = new HashSet<>();

    /**
     * Keeps track of all Messengers for each unique client type. Note: This Map should only be
     * modified via the incomingHandler.
     */
    @VisibleForTesting Map<String, Set<Messenger>> typedClients = new HashMap<>();

    /**
     * Keeps track of {@link CountDownLatch}s mapped to {@link UUID}s to aid with synchronization.
     */
    private final Map<UUID, CountDownLatch> latches = new HashMap<>();

    public IncomingHandler(Looper looper) {
      super(looper);
      if (Looper.getMainLooper() == looper || Looper.myLooper() == looper) {
        throw new IllegalStateException(
            "This handler should not be using the main thread "
                + "looper nor the instrumentation thread looper.");
      }
    }

    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case MSG_TERMINATE:
          logDebugWithProcess(TAG, "handleMessage(MSG_TERMINATE)");
          doDie();
          break;
        case MSG_HANDLE_INSTRUMENTATION_FROM_BROADCAST:
          logDebugWithProcess(TAG, "handleMessage(MSG_HANDLE_INSTRUMENTATION_FROM_BROADCAST)");
          // This message comes from the local MessengerReceiver#onReceive method to
          // register the remote instrumentation instance came in the broadcast and
          // reply back with local binder for future IPC.
          if (otherInstrumentations.add(msg.replyTo)) {
            sendMessageWithReply(msg.replyTo, MSG_ADD_INSTRUMENTATION, null);
          } else {
            Log.w(TAG, "Broadcast with existing binder was received, ignoring it..");
          }
          break;
        case MSG_ADD_INSTRUMENTATION:
          logDebugWithProcess(TAG, "handleMessage(MSG_ADD_INSTRUMENTATION)");
          // This message comes from instrumentation instances running in a
          // separate process who receive {@link BROADCAST_FILTER} broadcast. The message
          // should include: the sender's Messenger in Message#replyTo and a bundle with
          // a list of its potential clients.
          if (otherInstrumentations.add(msg.replyTo)) {
            // Reply back with local list of clients if exist
            if (!typedClients.isEmpty()) {
              sendMessageWithReply(msg.replyTo, MSG_ADD_CLIENTS_IN_BUNDLE, null);
            }
            // Save remote clients
            clientsRegistrationFromBundle(msg.getData(), true);
          } else {
            Log.w(TAG, "Message with existing binder was received, ignoring it..");
          }
          break;
        case MSG_REMOVE_INSTRUMENTATION:
          logDebugWithProcess(TAG, "handleMessage(MSG_REMOVE_INSTRUMENTATION)");
          // A message notifying the termination of a remote instrumentation instance.
          // This instance should no longer keep track of the sender's Messenger.
          if (!otherInstrumentations.remove(msg.replyTo)) {
            Log.w(TAG, "Attempting to remove a non-existent binder!");
          }
          break;
        case MSG_ADD_CLIENTS_IN_BUNDLE:
          logDebugWithProcess(TAG, "handleMessage(MSG_ADD_CLIENTS_IN_BUNDLE)");
          // A message from a new or an existing remote instrumentation instance that
          // provides a bundle containing all clients registered to that instrumentation.
          // Add all the remote clients
          clientsRegistrationFromBundle(msg.getData(), true);
          break;
        case MSG_REMOVE_CLIENTS_IN_BUNDLE:
          logDebugWithProcess(TAG, "handleMessage(MSG_REMOVE_CLIENTS_IN_BUNDLE)");
          // A message from an existing remote instrumentation instance provides a bundle
          // containing clients to be removed.
          clientsRegistrationFromBundle(msg.getData(), false);
          break;
        case MSG_REG_CLIENT:
          logDebugWithProcess(TAG, "handleMessage(MSG_REG_CLIENT)");
          // A new client is registering with this instance of instrumentation. Register
          // the client and potentially notify other instrumentation instances if needed.
          registerClient(
              msg.getData().getString(BUNDLE_KEY_CLIENT_TYPE),
              (Messenger) msg.getData().getParcelable(BUNDLE_KEY_CLIENT_MESSENGER));
          sendMessageToOtherInstr(MSG_REMOTE_ADD_CLIENT, msg.getData());
          break;
        case MSG_REMOTE_ADD_CLIENT:
          logDebugWithProcess(TAG, "handleMessage(MSG_REMOTE_ADD_CLIENT)");
          registerClient(
              msg.getData().getString(BUNDLE_KEY_CLIENT_TYPE),
              (Messenger) msg.getData().getParcelable(BUNDLE_KEY_CLIENT_MESSENGER));
          break;
        case MSG_UN_REG_CLIENT:
          logDebugWithProcess(TAG, "handleMessage(MSG_UN_REG_CLIENT)");
          // A client is un-registering from this instance of instrumentation. Un-register
          // the client and notify other instrumentation instances if needed.
          unregisterClient(
              msg.getData().getString(BUNDLE_KEY_CLIENT_TYPE),
              (Messenger) msg.getData().getParcelable(BUNDLE_KEY_CLIENT_MESSENGER));
          sendMessageToOtherInstr(MSG_REMOTE_REMOVE_CLIENT, msg.getData());
          break;
        case MSG_REMOTE_REMOVE_CLIENT:
          logDebugWithProcess(TAG, "handleMessage(MSG_REMOTE_REMOVE_CLIENT)");
          unregisterClient(msg.getData().getString(BUNDLE_KEY_CLIENT_TYPE), msg.replyTo);
          break;
        case MSG_REMOTE_CLEANUP_REQUEST:
          logDebugWithProcess(TAG, "handleMessage(MSG_REMOTE_CLEANUP_REQUEST)");
          if (otherInstrumentations.isEmpty()) {
            Message m = obtainMessage(MSG_PERFORM_CLEANUP_FINISHED);
            m.setData(msg.getData());
            sendMessage(m);
            break;
          }
          sendMessageToOtherInstr(MSG_PERFORM_CLEANUP, msg.getData());
          break;
        case MSG_PERFORM_CLEANUP:
          logDebugWithProcess(TAG, "handleMessage(MSG_PERFORM_CLEANUP)");
          instrumentation.runOnMainSync(activityFinisher);
          sendMessageWithReply(msg.replyTo, MSG_PERFORM_CLEANUP_FINISHED, msg.getData());
          break;
        case MSG_PERFORM_CLEANUP_FINISHED:
          logDebugWithProcess(TAG, "handleMessage(MSG_PERFORM_CLEANUP_FINISHED)");
          notifyLatch((UUID) msg.getData().getSerializable(BUNDLE_KEY_UUID));
          break;
        default:
          Log.w(TAG, "Unknown message code received: " + msg.what);
          super.handleMessage(msg);
      }
    }

    private void notifyLatch(UUID uuid) {
      if (uuid != null && latches.containsKey(uuid)) {
        latches.get(uuid).countDown();
      } else {
        Log.w(TAG, "Latch not found " + uuid);
      }
    }

    private void associateLatch(final UUID latchId, final CountDownLatch latch) {
      runSyncTask(
          new Callable<Void>() {
            @Override
            public Void call() {
              latches.put(latchId, latch);
              return null;
            }
          });
    }

    private void disassociateLatch(final UUID latchId) {
      runSyncTask(
          new Callable<Void>() {
            @Override
            public Void call() {
              latches.remove(latchId);
              return null;
            }
          });
    }

    private <T> T runSyncTask(Callable<T> task) {
      FutureTask<T> futureTask = new FutureTask<>(task);
      post(futureTask);

      try {
        return futureTask.get();
      } catch (InterruptedException e) {
        throw new IllegalStateException(e.getCause());
      } catch (ExecutionException e) {
        throw new IllegalStateException(e.getCause());
      }
    }

    private void doDie() {
      Log.i(TAG, "terminating process");
      // notify others of self termination
      sendMessageToOtherInstr(MSG_REMOVE_INSTRUMENTATION, null);
      otherInstrumentations.clear();
      typedClients.clear();
      logDebugWithProcess(TAG, "quitting looper...");
      getLooper().quit();
      logDebugWithProcess(TAG, "finishing instrumentation...");
      instrumentation.finish(0, null);
      instrumentation = null;
      activityFinisher = null;
    }

    private Set<Messenger> getClientsForType(final String type) {
      FutureTask<Set<Messenger>> associationTask =
          new FutureTask<>(
              new Callable<Set<Messenger>>() {
                @Override
                public Set<Messenger> call() {
                  return typedClients.get(type);
                }
              });
      post(associationTask);

      try {
        return associationTask.get();
      } catch (InterruptedException e) {
        // Shouldn't happen, always waiting for finish
        throw new IllegalStateException(e);
      } catch (ExecutionException e) {
        // Shouldn't happen, just adding (key,value) to a map
        throw new IllegalStateException(e.getCause());
      }
    }

    /**
     * Helper method to send a message to a given Instrumentation Messenger. The message will have
     * the msg.replyTo field set to this {@link IncomingHandler} and it will also include the map of
     * uniquely typed client Messenger's it contains.
     *
     * @param toMessenger who to send the message to.
     * @param what the type of message, value to assign to the what member.
     * @param data the arbitrary data associated with this message, ignored if {@code null}.
     */
    private void sendMessageWithReply(Messenger toMessenger, int what, Bundle data) {
      logDebugWithProcess(TAG, "sendMessageWithReply type: " + what + " called");

      // Construct a message for a given code and with the local Messenger
      Message msg = obtainMessage(what);
      msg.replyTo = messengerHandler;
      if (data != null) {
        msg.setData(data);
      }

      // The message should include all known clients
      if (!typedClients.isEmpty()) {
        Bundle clientsBundle = msg.getData();
        // Flatten the map of clients to send it as part of a bundle.
        ArrayList<String> keyList = new ArrayList<>(typedClients.keySet());
        clientsBundle.putStringArrayList(BUNDLE_KEY_CLIENTS, keyList);
        for (Map.Entry<String, Set<Messenger>> entry : typedClients.entrySet()) {
          String clientType = String.valueOf(entry.getKey());
          Messenger[] clientArray =
              entry.getValue().toArray(new Messenger[entry.getValue().size()]);
          clientsBundle.putParcelableArray(clientType, clientArray);
        }
        msg.setData(clientsBundle);
      }

      // Send the message
      try {
        toMessenger.send(msg);
      } catch (RemoteException e) {
        // In this case the process was terminated or crashed before we could
        // even do anything with it; there is nothing we can do other than removing
        // this instrumentation connection instance.
        Log.w(TAG, "The remote process is terminated unexpectedly", e);
        // Clean up our otherInstrumentations list
        instrBinderDied(toMessenger);
      }
    }

    private void sendMessageToOtherInstr(int what, Bundle data) {
      logDebugWithProcess(
          TAG, "sendMessageToOtherInstr() called with: what = [%s], data = [%s]", what, data);
      for (Messenger otherInstr : otherInstrumentations) {
        sendMessageWithReply(otherInstr, what, data);
      }
    }

    /**
     * Helper method to extract the all clients from the given bundle and call {@link
     * #registerClient(String, Messenger)} or {@link #unregisterClient(String, Messenger)}.
     *
     * @param clientsBundle The message bundle containing clients info
     * @param shouldRegister Whether to register or unregister given clients
     */
    private void clientsRegistrationFromBundle(Bundle clientsBundle, boolean shouldRegister) {
      logDebugWithProcess(TAG, "clientsRegistrationFromBundle called");

      if (null == clientsBundle) {
        Log.w(TAG, "The client bundle is null, ignoring...");
        return;
      }

      ArrayList<String> clientTypes = clientsBundle.getStringArrayList(BUNDLE_KEY_CLIENTS);

      if (null == clientTypes) {
        Log.w(TAG, "No clients found in the given bundle");
        return;
      }

      for (String type : clientTypes) {
        Parcelable[] clientArray = clientsBundle.getParcelableArray(String.valueOf(type));
        if (clientArray != null) {
          for (Parcelable client : clientArray) {
            if (shouldRegister) {
              registerClient(type, (Messenger) client);
            } else {
              unregisterClient(type, (Messenger) client);
            }
          }
        }
      }
    }

    private void registerClient(String type, Messenger client) {
      logDebugWithProcess(
          TAG, "registerClient called with type = [%s] client = [%s]", type, client);
      checkNotNull(type, "type cannot be null!");
      checkNotNull(client, "client cannot be null!");

      Set<Messenger> clientSet = typedClients.get(type);

      if (null == clientSet) {
        // Add the new client
        clientSet = new HashSet<>();
        clientSet.add(client);
        typedClients.put(type, clientSet);
        return;
      }

      // Add the new client
      clientSet.add(client);
    }

    private void unregisterClient(String type, Messenger client) {
      logDebugWithProcess(
          TAG, "unregisterClient called with type = [%s] client = [%s]", type, client);
      checkNotNull(type, "type cannot be null!");
      checkNotNull(client, "client cannot be null!");

      if (!typedClients.containsKey(type)) {
        Log.w(TAG, "There are no registered clients for type: " + type);
        return;
      }

      Set<Messenger> clientSet = typedClients.get(type);

      if (!clientSet.contains(client)) {
        Log.w(
            TAG,
            "Could not unregister client for type "
                + type
                + " because it doesn't seem to be registered");
        return;
      }

      // Remove this client first, to avoid notifying itself
      clientSet.remove(client);

      if (clientSet.isEmpty()) {
        typedClients.remove(type);
      }
    }

    private void instrBinderDied(Messenger instrMessenger) {
      Message msg = obtainMessage(MSG_REMOVE_INSTRUMENTATION);
      msg.replyTo = instrMessenger;
      sendMessage(msg);
    }
  } // close IncomingHandler
}
