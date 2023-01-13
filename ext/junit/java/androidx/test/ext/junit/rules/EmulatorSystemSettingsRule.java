/*
 * Copyright 2023 The Android Open Source Project
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
package androidx.test.ext.junit.rules;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.services.shellexecutor.ShellExecSharedConstants;
import androidx.test.services.shellexecutor.ShellExecutorImpl;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Modifies the system settings to reduce flakiness.
 *
 * <p>Another possible modification is "secure show_ime_with_hard_keyboard 0", which can be used if
 * flakes occur due to the software keyboard lagging
 */
public final class EmulatorSystemSettingsRule implements TestRule {
  public EmulatorSystemSettingsRule() {}

  @Override
  public Statement apply(Statement statement, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        increaseClickTimeout();
        disableAnimations();
        statement.evaluate();
      }
    };
  }

  /**
   * Mitigate "MotionEvents: Overslept and turned a tap into a long press". Override value was
   * chosen arbitrarily. The default is 500ms.
   *
   * <p>Note: This value causes long presses to take at least this long. Setting this too high may
   * lead to timeouts in tests that trigger or await long presses.
   */
  // See b/37078920 // MOE:strip_line
  private static void increaseClickTimeout() throws IOException {
    // This is safe to change without breaking long presses because Espresso and Android uses this
    // value in long press detection and generation.
    if (Build.VERSION.SDK_INT >= 16) {
      putSetting("secure", "long_press_timeout", "3000");
    }
  }

  /**
   * Disable system animations.
   *
   * <p>This is recommended by
   * https://google.github.io/android-testing-support-library/docs/espresso/setup/
   *
   * <p>Note: this will not disable in-app animations unless they use these values as multipliers.
   */
  private static void disableAnimations() throws IOException {
    if (Build.VERSION.SDK_INT == 16) {
      // These settings were added in 16.
      putSetting("system", "window_animation_scale", "0.0");
      putSetting("system", "transition_animation_scale", "0.0");
      putSetting("system", "animator_duration_scale", "0.0");
    } else if (Build.VERSION.SDK_INT >= 17) {
      // These settings were moved in 17.
      putSetting("global", "window_animation_scale", "0.0");
      putSetting("global", "transition_animation_scale", "0.0");
      putSetting("global", "animator_duration_scale", "0.0");
    }
  }

  private static void putSetting(String group, String setting, String value) throws IOException {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    String string =
        InstrumentationRegistry.getArguments().getString(ShellExecSharedConstants.BINDER_KEY);
    String output =
        new ShellExecutorImpl(context, string)
            .executeShellCommandSync(
                "settings",
                ImmutableList.of("put", group, setting, value),
                /* shellEnv= */ null,
                /* executeThroughShell= */ false);
    Log.i("EmulatorSystemSettings", output);
  }
}
