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

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.test.services.speakeasy.SpeakEasyProtocol;
import java.security.SecureRandom;
import java.util.Random;

/** Allows callers to access the speakeasy binder registry when they have a Context. */
public final class AppConnection implements Connection {
  private static final String PACKAGE_NAME = "androidx.test.services";
  static final String SERVICE =
      "androidx.test.services.speakeasy.server.SpeakEasyService";
  private final String packageName;
  private final String service;
  private final Random random;
  private final Context context;

  public AppConnection(Context context) {
    this(context, PACKAGE_NAME, SERVICE, new SecureRandom());
  }

  AppConnection(Context context, String packageName, String service, Random random) {
    this.context = context.getApplicationContext();
    this.packageName = checkNotNull(packageName);
    this.service = checkNotNull(service);
    this.random = checkNotNull(random);
  }

  @Override
  public void publish(IBinder binder, PublishResultReceiver rr) {
    checkNotNull(binder);
    checkNotNull(rr);

    String key = Long.toHexString(random.nextLong());
    Intent intent = makeIntent();
    intent.putExtras(SpeakEasyProtocol.Publish.asBundle(key, binder, rr));
    startForegroundService(context, intent);
  }

  @Override
  public void find(String key, FindResultReceiver rr) {
    checkNotNull(key);
    checkNotNull(rr);
    Intent intent = makeIntent();
    intent.putExtras(SpeakEasyProtocol.Find.asBundle(key, rr));
    startForegroundService(context, intent);
  }

  private Intent makeIntent() {
    Intent i = new Intent();
    i.setClassName(packageName, service);
    return i;
  }

  private static <T> T checkNotNull(T ref) {
    if (null == ref) {
      throw new NullPointerException();
    }
    return ref;
  }

  // copy of ContentCompat.startForegroundService
  private static void startForegroundService(Context context, Intent intent) {
    if (Build.VERSION.SDK_INT >= 26) {
      context.startForegroundService(intent);
    } else {
      // Pre-O behavior.
      context.startService(intent);
    }
  }
}
