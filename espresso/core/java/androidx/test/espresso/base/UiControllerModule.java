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

package androidx.test.espresso.base;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.test.espresso.InjectEventSecurityException;
import androidx.test.espresso.UiController;
import androidx.test.internal.platform.ServiceLoaderWrapper;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/**
 * Dagger module for UiController.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY)
@Module
public class UiControllerModule {

  @Provides
  @Singleton
  public UiController provideUiController(UiControllerImpl uiControllerImpl) {
    androidx.test.platform.ui.UiController platformUiController =
        ServiceLoaderWrapper.loadSingleServiceOrNull(androidx.test.platform.ui.UiController.class);
    if (platformUiController == null) {
      return uiControllerImpl;
    } else {
      return new EspressoUiControllerAdapter(platformUiController);
    }
  }

  private static class EspressoUiControllerAdapter implements InterruptableUiController {
    private final androidx.test.platform.ui.UiController platformUiController;

    private EspressoUiControllerAdapter(
        androidx.test.platform.ui.UiController platformUiController) {
      this.platformUiController = platformUiController;
    }

    @Override
    public boolean injectMotionEvent(MotionEvent event) throws InjectEventSecurityException {
      try {
        return platformUiController.injectMotionEvent(event);
      } catch (androidx.test.platform.ui.InjectEventSecurityException e) {
        throw new InjectEventSecurityException(e);
      }
    }

    @Override
    public boolean injectKeyEvent(KeyEvent event) throws InjectEventSecurityException {
      try {
        return platformUiController.injectKeyEvent(event);
      } catch (androidx.test.platform.ui.InjectEventSecurityException e) {
        throw new InjectEventSecurityException(e);
      }
    }

    @Override
    public boolean injectString(String str) throws InjectEventSecurityException {
      try {
        return platformUiController.injectString(str);
      } catch (androidx.test.platform.ui.InjectEventSecurityException e) {
        throw new InjectEventSecurityException(e);
      }
    }

    @Override
    public void loopMainThreadUntilIdle() {
      platformUiController.loopMainThreadUntilIdle();
    }

    @Override
    public void loopMainThreadForAtLeast(long millisDelay) {
      platformUiController.loopMainThreadForAtLeast(millisDelay);
    }

    @Override
    public void interruptEspressoTasks() {
      Log.w("UiController", "interruptEspressoTasks called, no-op");
    }
  }
}
