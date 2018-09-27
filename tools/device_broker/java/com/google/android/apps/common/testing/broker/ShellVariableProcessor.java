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

import com.google.common.base.Splitter;
import com.google.common.base.Splitter.MapSplitter;
import com.google.common.collect.Maps;
import com.google.common.io.LineProcessor;
import java.util.Map;

/**
 * Consumes output from adb shell printenv and converts it into a map of
 * environment variables.
 *
 */
class ShellVariableProcessor implements LineProcessor<Map<String, String>> {
  private static final MapSplitter EQUALS_SPLITTER = Splitter.on('\n').withKeyValueSeparator("=");
  private StringBuilder input = new StringBuilder();

  @Override
  public boolean processLine(String in) {
    input.append(in);
    input.append("\n");
    return true;
  }

  @Override
  public Map<String, String> getResult() {
    if (input.length() > 0) {
      return EQUALS_SPLITTER.split(input.substring(0, input.length() - 1));
    } else {
      return Maps.newHashMap();
    }
  }

}
