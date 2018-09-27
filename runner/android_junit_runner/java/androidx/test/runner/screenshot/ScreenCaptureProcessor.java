/*
 * Copyright (C) 2016 The Android Open Source Project
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

package androidx.test.runner.screenshot;

import androidx.test.annotation.Beta;
import java.io.IOException;

/**
 * Interface for an object that is capable of processing {@link ScreenCapture}s.
 *
 * <p><b>This API is currently in beta.</b>
 */
@Beta
public interface ScreenCaptureProcessor {

  /**
   * Process the given {@link ScreenCapture}.
   *
   * <p>The given {@link ScreenCapture} defines optional properties like filename and format that
   * should be respected when defining the behavior of this method.
   *
   * @param capture the {@link ScreenCapture} that specifies the bitmap to process
   * @return the filename the bitmap was saved as
   * @throws IOException if there was an I/O error saving the screenshot
   */
  public String process(ScreenCapture capture) throws IOException;
}
