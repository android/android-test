/*
 * Copyright (C) 2017 The Android Open Source Project
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

package androidx.test.services.speakeasy.client;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import androidx.test.services.speakeasy.SpeakEasyProtocol;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.Random;

/** Creates a connection to SpeakEasy without requiring a context. */
public abstract class ToolConnection implements Connection {
  private static final String PACKAGE_NAME = "androidx.test.services";
  private static final String CONTENT_PROVIDER = "androidx_test_services.speak_easy";
  private static final String SERVICE =
      "androidx.test.services.speakeasy.server.SpeakEasyService";
  private static final String TAG = "ToolConnection";

  private final Random random = new SecureRandom();
  protected final String packageName;
  protected final String contentProvider;

  /** Creates a connection to speakeasy. */
  public static Connection makeConnection() {
    return makeConnection(PACKAGE_NAME, CONTENT_PROVIDER);
  }

  static Connection makeConnection(String packageName, String contentProvider) {
    if (Build.VERSION.SDK_INT < 17) {
      return new ToolConnectionCompat(packageName, contentProvider);
    } else if (Build.VERSION.SDK_INT <= 25) {
      return new ToolConnectionJBToN(packageName, contentProvider);
    } else {
      return new ToolConnectionO(packageName, contentProvider);
    }
  }

  private ToolConnection(String packageName, String contentProvider) {
    this.packageName = checkNotNull(packageName);
    this.contentProvider = checkNotNull(contentProvider);
  }

  @Override
  public void publish(IBinder binder, PublishResultReceiver rr) {
    publish(Long.toHexString(random.nextLong()), binder, rr);
  }

  // VisibleForTesting.
  void publish(String key, IBinder binder, PublishResultReceiver rr) {
    checkNotNull(binder);
    checkNotNull(rr);
    // consider timing out  (could happen when speakeasy is not installed)
    Bundle msg = SpeakEasyProtocol.Publish.asBundle(key, binder, rr);
    try {
      doCall(msg);
    } catch (RemoteException re) {
      throw new RuntimeException(re);
    }
  }

  @Override
  public void find(String key, FindResultReceiver rr) {
    checkNotNull(key);
    checkNotNull(rr);
    // consider timing out  (could happen when speakeasy is not installed)
    try {
      doCall(SpeakEasyProtocol.Find.asBundle(key, rr));
    } catch (RemoteException re) {
      throw new RuntimeException(re);
    }
  }

  protected abstract void doCall(Bundle b) throws RemoteException;

  private static class ToolConnectionCompat extends ToolConnection {

    ToolConnectionCompat(String packageName, String contentProvider) {
      super(packageName, contentProvider);
    }

    @Override
    protected final void doCall(Bundle b) throws RemoteException {
      Intent intent = new Intent();
      intent.setClassName(packageName, SERVICE);
      intent.putExtras(b);
      Log.i(TAG, "Invoking ActivityManagerNative.getDefault().startService(...)");
      ActivityManagerNative.getDefault().startService(null, intent, null);
      Log.i(TAG, "Intent sent!");
    }
  }

  private abstract static class ToolConnectionPostIcs extends ToolConnection {

    protected abstract Object getActivityManager();

    ToolConnectionPostIcs(String packageName, String contentProvider) {
      super(packageName, contentProvider);
    }

    @Override
    protected final void doCall(Bundle b) throws RemoteException {
      try {
        Log.i(TAG, "looking up IActivityManager");
        Class<?> iam = Class.forName("android.app.IActivityManager");
        Log.i(TAG, "looking up getContentProviderExternal");

        Method getCPEMethod = null;
        boolean getCPEPostP;
        try {
          getCPEMethod =
              iam.getMethod(
                  "getContentProviderExternal", String.class, Integer.TYPE, IBinder.class);
          getCPEPostP = false;
        } catch (NoSuchMethodException nsm) {
          // API Level 29 and above have changed the API Signature.
          getCPEMethod =
              iam.getMethod(
                  "getContentProviderExternal",
                  String.class,
                  Integer.TYPE,
                  IBinder.class,
                  String.class);
          getCPEPostP = true;
        }
        Log.i(TAG, "looking up removeContentProviderExternal");
        Method removeCPE =
            iam.getMethod("removeContentProviderExternal", String.class, IBinder.class);
        IBinder token = new Binder();
        try {
          Log.i(TAG, "Getting a content provider holder for: " + contentProvider);
          Object cph;
          int userId = getCurrentUserOrUserZero();
          Log.d(TAG, "Starting contentProvider as user: " + userId);
          if (getCPEPostP) {
            cph = getCPEMethod.invoke(getActivityManager(), contentProvider, userId, token, null);
          } else {
            cph = getCPEMethod.invoke(getActivityManager(), contentProvider, userId, token);
          }
          if (null == cph) {
            throw new IllegalStateException(
                String.format(
                    "Call to getContentProviderExternal for: %s returns null!", contentProvider));
          }
          Log.i(TAG, "Getting the provider field");
          Field f = cph.getClass().getDeclaredField("provider");
          f.setAccessible(true);
          Object provider = f.get(cph);

          if (null == provider) {
            throw new IllegalStateException(
                String.format(
                    "Call to getContentProviderExternal for: %s returns null provider!",
                    contentProvider));
          }
          Log.i(TAG, "Finding the call method");
          Method call = null;
          for (Method m : provider.getClass().getDeclaredMethods()) {
            if ("call".equals(m.getName())) {
              call = m;
            }
          }
          if (call == null) {
            Log.e(TAG, "No call method!");
            throw new RuntimeException("Could not find call method on content provider!");
          }
          if (call.getParameterTypes().length == 4) {
            Log.i(TAG, "Invoking modern call method");
            call.invoke(provider, null, null, null, b);
          } else if (call.getParameterTypes().length == 5) {
            Log.i(TAG, "Invoking Android Q call method");
            call.invoke(provider, null, CONTENT_PROVIDER, null, null, b);
          } else if (call.getParameterTypes().length == 6) {
            Log.i(TAG, "Invoking Android R call method");
            call.invoke(provider, null, null, CONTENT_PROVIDER, null, null, b);
          } else {
            Log.i(TAG, "Invoking legacy call method");
            call.invoke(provider, null, null, b);
          }
          Log.i(TAG, "Intent sent!");
        } finally {
          Log.i(TAG, "Releasing content provider");
          removeCPE.invoke(getActivityManager(), contentProvider, token);
          Log.i(TAG, "Released content provider");
        }
      } catch (IllegalAccessException
          | ClassNotFoundException
          | NoSuchMethodException
          | InvocationTargetException
          | NoSuchFieldException ex) {
        Log.e(TAG, "Connecting to content providers has failed!", ex);
        throw new RuntimeException(ex);
      }
    }

    private static int getCurrentUserOrUserZero() {
      try {
        Log.d(TAG, "looking up getCurrentUser");
        Method method = ActivityManager.class.getMethod("getCurrentUser");
        return (int) method.invoke(null);
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        Log.e(TAG, "looking up getCurrentUser error ", e);
        return 0;
      }
    }
  }

  private static class ToolConnectionJBToN extends ToolConnectionPostIcs {

    ToolConnectionJBToN(String packageName, String contentProvider) {
      super(packageName, contentProvider);
    }

    @Override
    protected Object getActivityManager() {
      Log.i(TAG, "Invoking ActivityManagerNative.getDefault");
      return ActivityManagerNative.getDefault();
    }
  }

  private static class ToolConnectionO extends ToolConnectionPostIcs {

    ToolConnectionO(String packageName, String contentProvider) {
      super(packageName, contentProvider);
    }

    @Override
    protected Object getActivityManager() {
      try {
        Log.i(TAG, "Invoking getService");
        Method getServiceMethod = ActivityManager.class.getMethod("getService");
        return getServiceMethod.invoke(null);
      } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
        Log.e(TAG, "Could not find / invoke get service", ex);
        throw new RuntimeException(ex);
      }
    }
  }

  private static <T> T checkNotNull(T val) {
    if (null == val) {
      throw new NullPointerException();
    }
    return val;
  }
}
