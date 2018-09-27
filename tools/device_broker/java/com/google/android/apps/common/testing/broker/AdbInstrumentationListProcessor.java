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


import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.LineProcessor;
import java.util.List;

/**
 * Parses the output of adb shell pm list instrumentation.
 *
 */
class AdbInstrumentationListProcessor implements LineProcessor<List<Instrumentation>> {

  private static final String HEADER = "instrumentation:";
  private static final String TARGET = "(target=";
  private final List<Instrumentation> instrumentations = Lists.newArrayList();

  @Override
  public boolean processLine(String line) {
    line = line.trim();
    // We begin to see such line in result of pm list instrumentation.
    // 'WARNING: linker: Warning: unable to normalize ""', just ignore it now.
    if (line.startsWith("WARNING:")) {
      return true;
    }
    // We sometimes see FORTIFY failures here, like
    // 'FORTIFY: pthread_mutex_lock called on a destroyed mutex (0xf58ad13c)'
    // These indicate bugs like b/77473515, but they're likely unrelated to our instrumentation
    // command, so we ignore them.
    if (line.startsWith("FORTIFY:")) {
      return true;
    }
    checkState(line.startsWith("instrumentation:"), "Not an instrumentation line: %s", line);

    int packageClassSeperatorIndex = line.indexOf("/");
    int classTargetSeperatorIndex = line.indexOf(" ");
    checkState(-1 != packageClassSeperatorIndex, "Seperating / not found: %s", line);
    checkState(-1 != classTargetSeperatorIndex, "Seperating whitespace not found: %s", line);

    String androidPackage = line.substring(HEADER.length(), packageClassSeperatorIndex);
    String instrumentationClass = line.substring(packageClassSeperatorIndex + 1,
        classTargetSeperatorIndex);
    int targetStartPosition = line.indexOf(TARGET);
    int targetEndPosition = line.indexOf(")");
    checkState(-1 != targetStartPosition, "target= missing. %s", line);
    checkState(-1 != targetEndPosition, "closing ) not found. %s", line);
    String targetPackage = line.substring(targetStartPosition + TARGET.length(),
                                          targetEndPosition);
    // .intern() breaks the reference to line. otherwise substring holds on to line.
    instrumentations.add(
        new Instrumentation.Builder()
          .withAndroidPackage(androidPackage.intern())
          .withInstrumentationClass(instrumentationClass.intern())
          .withTargetPackage(targetPackage.intern())
          .build());
    return true;
  }

  @Override
  public List<Instrumentation> getResult() {
    return ImmutableList.copyOf(instrumentations);
  }

}
