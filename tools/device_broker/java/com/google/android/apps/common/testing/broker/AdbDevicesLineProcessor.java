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


import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.LineProcessor;
import java.util.List;

/** Parses the output of "adb devices" command. */
public class AdbDevicesLineProcessor implements LineProcessor<List<String>> {

  private static final String SUFFIX = "device";
  private final List<String> deviceSerialIds = Lists.newArrayList();

  @Override
  public boolean processLine(String line) {
    line = line.trim();
    if (line.endsWith(SUFFIX) && line.length() > SUFFIX.length()) {
      int endIndex = line.indexOf(SUFFIX) - 1;
      deviceSerialIds.add(line.substring(0, endIndex).trim());
    }
    return true;
  }

  @Override
  public List<String> getResult() {
    return ImmutableList.copyOf(deviceSerialIds);
  }
}
