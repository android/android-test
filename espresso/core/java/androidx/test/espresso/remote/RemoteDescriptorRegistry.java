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
 */

package androidx.test.espresso.remote;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A registry for registering remote descriptors. Remote descriptors are registered in the form of a
 * {@link RemoteDescriptor} object.
 */
public final class RemoteDescriptorRegistry {
  private static final String TAG = "RemoteDescrRegistry";

  private static final RemoteDescriptorRegistry DEFAULT_INSTANCE = new RemoteDescriptorRegistry();

  private final Map<Class<?>, RemoteDescriptor> instanceTypeToRemoteTargetTypeLookup =
      new HashMap<>();
  private final Map<Class<?>, RemoteDescriptor> protoMsgToRemoteTargetTypeLookup = new HashMap<>();
  private final Map<String, RemoteDescriptor> remoteTypeUrlToRemoteTypeLookup = new HashMap<>();

  @VisibleForTesting
  RemoteDescriptorRegistry() {
    // noOp instance
  }

  /**
   * Returns a {@link RemoteDescriptorRegistry} object
   *
   * @return an instance of {@link RemoteDescriptorRegistry} object.
   */
  public static RemoteDescriptorRegistry getInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final <K, V> void throwIfMapNotContains(
      Map<K, V> map, K key, String fmtError, Object... fmtArgs) {
    if (!map.containsKey(key)) {
      throw new RemoteProtocolException(String.format(Locale.ROOT, fmtError, fmtArgs));
    }
  }

  public boolean registerRemoteTypeArgs(@NonNull List<RemoteDescriptor> remoteDescriptors) {
    checkNotNull(remoteDescriptors, "remoteDescriptors cannot be null!");
    boolean registerSuccessful = true;
    for (RemoteDescriptor remoteDescriptor : remoteDescriptors) {
      if (isRegistered(remoteDescriptor)) {
        registerSuccessful = false;
        Log.w(
            TAG,
            String.format(
                Locale.ROOT,
                "Attempted to register RemoteDescriptor for target type: %s, that "
                    + "was already registered",
                remoteDescriptor.getInstanceType()));
      }
      remoteTypeUrlToRemoteTypeLookup.put(remoteDescriptor.getInstanceTypeName(), remoteDescriptor);
      instanceTypeToRemoteTargetTypeLookup.put(
          remoteDescriptor.getInstanceType(), remoteDescriptor);
      protoMsgToRemoteTargetTypeLookup.put(remoteDescriptor.getProtoType(), remoteDescriptor);
    }
    return registerSuccessful;
  }

  public void unregisterRemoteTypeArgs(@NonNull List<RemoteDescriptor> remoteDescriptors) {
    checkNotNull(remoteDescriptors, "remoteDescriptors cannot be null!");
    for (RemoteDescriptor remoteDescriptor : remoteDescriptors) {
      if (!isRegistered(remoteDescriptor)) {
        throw new IllegalStateException(
            String.format(
                Locale.ROOT,
                "Attempted to unregister RemoteDescriptor "
                    + "for target type: %s, that was not registered",
                remoteDescriptor.getInstanceType()));
      }
      remoteTypeUrlToRemoteTypeLookup.remove(remoteDescriptor.getInstanceTypeName());
      instanceTypeToRemoteTargetTypeLookup.remove(remoteDescriptor.getInstanceType());
      protoMsgToRemoteTargetTypeLookup.remove(remoteDescriptor.getProtoType());
    }
  }

  /**
   * Returns an {@link RemoteDescriptor} by its any type url. The any type url in this case must
   * match the remote type class, which knows how to convert a class to and from its target type!
   *
   * @return an {@link RemoteDescriptor} object by its remote type url.
   */
  public RemoteDescriptor argForRemoteTypeUrl(@NonNull String typeUrl) {
    checkState(!TextUtils.isEmpty(typeUrl));
    throwIfMapNotContains(
        remoteTypeUrlToRemoteTypeLookup,
        typeUrl,
        "Parser not found for type url: %s. All remote "
            + "types must be registered using "
            + "RemoteDescriptorRegistry#registerRemoteTypeArgs(List<RemoteDescriptor>",
        typeUrl);
    return remoteTypeUrlToRemoteTypeLookup.get(typeUrl);
  }

  /** @return an {@link RemoteDescriptor} object by its target type. */
  public RemoteDescriptor argForInstanceType(@NonNull Class<?> targetType) {
    checkNotNull(targetType, "messageType cannot be null!");
    throwIfMapNotContains(
        instanceTypeToRemoteTargetTypeLookup,
        targetType,
        "No such message type registered: %s. "
            + "All remote types must be registered using "
            + "RemoteDescriptorRegistry#registerRemoteTypeArgs(List<RemoteDescriptor>)",
        targetType);
    return instanceTypeToRemoteTargetTypeLookup.get(targetType);
  }

  /** @return an {@link RemoteDescriptor} object by its proto message type. */
  public RemoteDescriptor argForMsgType(@NonNull Class<?> protoMsgType) {
    checkNotNull(protoMsgType, "protoMsgType cannot be null!");
    throwIfMapNotContains(
        protoMsgToRemoteTargetTypeLookup,
        protoMsgType,
        "No such message type registered: %s. All "
            + "proto msg types must be registered using "
            + "RemoteDescriptorRegistry#registerRemoteTypeArgs(List<RemoteDescriptor>)",
        protoMsgType);
    return protoMsgToRemoteTargetTypeLookup.get(protoMsgType);
  }

  /**
   * Checks if an instance type is registered with this registry.
   *
   * @param instanceType the instance to check
   * @return true if a {@link RemoteDescriptor} is registered for instance type
   */
  public boolean hasArgForInstanceType(@NonNull Class<?> instanceType) {
    checkNotNull(instanceType, "instanceType cannot be null!");
    return instanceTypeToRemoteTargetTypeLookup.containsKey(instanceType);
  }

  private boolean isRegistered(RemoteDescriptor remoteDescriptor) {
    return remoteTypeUrlToRemoteTypeLookup.containsKey(remoteDescriptor.getInstanceTypeName())
        && instanceTypeToRemoteTargetTypeLookup.containsKey(remoteDescriptor.getInstanceType())
        && protoMsgToRemoteTargetTypeLookup.containsKey(remoteDescriptor.getProtoType());
  }

  @VisibleForTesting
  void clear() {
    remoteTypeUrlToRemoteTypeLookup.clear();
    instanceTypeToRemoteTargetTypeLookup.clear();
    protoMsgToRemoteTargetTypeLookup.clear();
  }
}
