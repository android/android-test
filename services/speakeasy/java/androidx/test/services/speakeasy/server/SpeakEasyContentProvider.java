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

package androidx.test.services.speakeasy.server;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

/** Proxies the call method from the ContentProvider to the SpeakEasy service. */
public class SpeakEasyContentProvider extends ContentProvider {

  @Override
  public boolean onCreate() {
    return true;
  }

  @Override
  public Cursor query(
      Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    return null;
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    return null;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public Bundle call(String unusedMethod, String unusedpackageName, Bundle extras) {
    Intent i = new Intent();
    i.setClass(getContext(), SpeakEasyService.class);
    i.putExtras(extras);

    startForegroundService(getContext(), i);
    return new Bundle();
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
