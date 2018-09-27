/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.ui.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import java.lang.reflect.InvocationTargetException;

/**
 * Upon creating this activity will start the {@link ChatHeadService} that will create a floating
 * button on the screen.
 */
public class ChatHeadActivity extends Activity {

  private static final String TAG = "ChatHeadActivity";

  private static final int REQUEST_CODE = 1337;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.chat_head_activity);
  }

  @Override
  protected void onResume() {
    super.onResume();
    setContentView(R.layout.chat_head_activity);
  }

  public void createChatHeadButtonClick(View view) {
    checkDrawOverlayPermission();
  }

  public void destroyChatHeadButtonClick(View view) {
    Intent intent = new Intent(this, ChatHeadService.class);
    stopService(intent);
  }

  private void startChatHeadService() {
    Intent intent = new Intent(this, ChatHeadService.class);
    startService(intent);
  }

  private void checkDrawOverlayPermission() {
    if (VERSION.SDK_INT >= 23) {
      if (!reflectiveCanDrawOverlays()) {
        // To satisfy F5 project
        Intent intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION",
            Uri.parse("package:" + getPackageName()));
        this.startActivityForResult(intent, REQUEST_CODE);
      }
    } else {
      startChatHeadService();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (VERSION.SDK_INT >= 23) {
      if (requestCode == REQUEST_CODE) {
        if (reflectiveCanDrawOverlays()) {
          // continue here - permission was granted
          startChatHeadService();
        } else {
          Toast.makeText(this, "No permissions", Toast.LENGTH_LONG).show();
        }
      }
    }
  }

  private boolean reflectiveCanDrawOverlays() {
    try {
      return ((Boolean) Settings.class.getDeclaredMethod("canDrawOverlays", Context.class)
          .invoke(null, this));
    } catch (IllegalAccessException e) {
      Log.e(TAG, "IllegalAccessException", e);
    } catch (InvocationTargetException e) {
      Log.e(TAG, "InvocationTargetException", e);
    } catch (NoSuchMethodException e) {
      Log.e(TAG, "NoSuchMethodException", e);
    }
    return false;
  }
}
