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

package com.google.android.apps.common.testing.broker;

/**
 * An enumeration of OpenGL drivers of the Android Emulator.
 *
 * <p>For things to work openGlDriver.name().toLowerCase() must all be valid values
 * of unified_launcher.py's --open_gl_driver flag.
 */
public enum OpenGlDriver {
  /**
   * Go with unified_launcher's default decision.
   */
  DEFAULT,
 
  /**
   * Use of the host system's GPU.
   */
  HOST,

  /**
   * Mesa backed software emulation.
   */
  MESA,

  /**
   * Disabled.
   */
  NO_OPEN_GL,

  /**
   * Swiftshader backed software emulation, running on guest side.
   */
  GUEST,

  /**
   * Swiftshader backed software emulation, running on host side, best supported.
   */
  SWIFTSHADER,

  /** Newer variant of SwiftShader which works with snapshots. */
  SWIFTSHADER_INDIRECT,
}
