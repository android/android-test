/*
 * Copyright (C) 2018 The Android Open Source Project
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
package androidx.test.testapp.flutter.app;

import android.app.Activity;
import android.os.Bundle;
import io.flutter.view.FlutterMain;
import io.flutter.view.FlutterRunArguments;
import io.flutter.view.FlutterView;

/** An Android activity that loads a Flutter application inside. */
public class FlutterActivity extends Activity {
  private FlutterView flutterView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    // Initializes the Dart VM.
    FlutterMain.startInitialization(this);
    FlutterMain.ensureInitializationComplete(this, null);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.flutter_activity);
    flutterView = findViewById(R.id.flutter_counter_view);
    String bundlePath = FlutterMain.findAppBundlePath(getApplicationContext());
    FlutterRunArguments args = new FlutterRunArguments();
    args.bundlePath = bundlePath;
    // This is the name of the main function to execute in the app.
    args.entrypoint = "main";
    flutterView.runFromBundle(args);
  }

  @Override
  public void onDestroy() {
    if (flutterView != null) {
      flutterView.destroy();
    }
    super.onDestroy();
  }

  @Override
  protected void onPause() {
    super.onPause();
    flutterView.onPause();
  }

  @Override
  protected void onPostResume() {
    super.onPostResume();
    flutterView.onPostResume();
  }
}
