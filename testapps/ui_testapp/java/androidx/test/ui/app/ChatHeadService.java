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

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

/**
 * Will use WindowManager to create a chat head button on the screen.
 */
public class ChatHeadService extends Service {
  private static final String TAG = "ChatHeadService";

  private WindowManager windowManager;
  private ImageView chatHeadButton;
  private boolean isRedColor = true;

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    Log.i(TAG, "Destroying and re-creating the chat head because of a configuration changed: "
        + newConfig);
    destroyChatHead();
    createChatHead();
  }

  @Override
  public void onCreate() {
    super.onCreate();
    createChatHead();
  }

  private void createChatHead() {
    // create a chat head button
    chatHeadButton = new ImageView(this);
    chatHeadButton.setId(R.id.chat_head_btn_id);
    setChatHeadColor(isRedColor);

    windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    final LayoutParams layoutParams = new WindowManager.LayoutParams(
        LayoutParams.WRAP_CONTENT, // width
        LayoutParams.WRAP_CONTENT, // height
        LayoutParams.TYPE_PHONE, // type
        LayoutParams.FLAG_NOT_FOCUSABLE, // flags
        PixelFormat.TRANSLUCENT); // format
    layoutParams.gravity = Gravity.BOTTOM;
    // add the chat head to window manager
    Log.i(TAG, "Adding chat heat image view to WindowManager...");
    windowManager.addView(chatHeadButton, layoutParams);

    // for moving the button on touch and slide
    chatHeadButton.setOnTouchListener(new View.OnTouchListener() {
      private int initialX;
      private int initialY;
      private float initialTouchX;
      private float initialTouchY;

      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            initialX = layoutParams.x;
            initialY = layoutParams.y;
            initialTouchX = event.getRawX();
            initialTouchY = event.getRawY();
            break;
          case MotionEvent.ACTION_UP:
            if (Math.abs(event.getRawX() - initialTouchX) == 0
                && Math.abs(event.getRawY() - initialTouchY) == 0) {
              // required check to avoid confusion with MotionEvent.ACTION_MOVE
              chatHeadButton.performClick();
            }
            break;
          case MotionEvent.ACTION_MOVE:
            layoutParams.x = initialX + (int) (event.getRawX() - initialTouchX);
            layoutParams.y = initialY + (int) (event.getRawY() - initialTouchY);
            windowManager.updateViewLayout(v, layoutParams);
            break;
        }
        return true;
      }
    });

    chatHeadButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        toggleColorState();
      }
    });
  }

  private void toggleColorState() {
    if (isRedColor) {
      Log.d(TAG, "Chat head color should be red: " + getColor(chatHeadButton));
    } else {
      Log.d(TAG, "Chat head color should be blue: " + getColor(chatHeadButton));
    }
    isRedColor = !isRedColor;
    setChatHeadColor(isRedColor);
  }

  private void setChatHeadColor(boolean toRedColor) {
    int drawableResource;
    int backgroundColor;
    String tagText;
    if (toRedColor) {
      backgroundColor = Color.RED;
      drawableResource = R.drawable.ic_chat_head_red;
      tagText = "red";
    } else {
      backgroundColor = Color.BLUE;
      drawableResource = R.drawable.ic_chat_head_blue;
      tagText = "blue";
    }
    chatHeadButton.setImageResource(drawableResource);
    chatHeadButton.setBackgroundColor(backgroundColor);
    // simple tag string that corresponds with the chat head drawable color, only used for testing
    chatHeadButton.setTag(tagText);
    Log.d(TAG, "Changed chat head color to " + tagText + ": "
        + getColor(chatHeadButton));
  }

  private String getColor(View view) {
    int color = Color.TRANSPARENT;
    Drawable background = view.getBackground();
    if (background instanceof ColorDrawable) {
      if (Build.VERSION.SDK_INT >= 11) {
        color = ((ColorDrawable) background).getColor();
      }
    }
    int red = (color >> 16) & 0xFF;
    int green = (color >> 8) & 0xFF;
    int blue = (color >> 0) & 0xFF;
    return String.format("RGB(%d,%d,%d)", red, green, blue);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    destroyChatHead();
  }

  private void destroyChatHead() {
    if (chatHeadButton != null) {
      Log.i(TAG, "Removing chat head view from WindowManager...");
      ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(chatHeadButton);
      chatHeadButton = null;
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    // Not doing much with this service, ignore.
    return null;
  }
}
