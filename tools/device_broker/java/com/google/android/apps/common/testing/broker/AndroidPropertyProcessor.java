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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.LineProcessor;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the output of adb getprop
 *
 */
class AndroidPropertyProcessor implements LineProcessor<ImmutableMap<String, String>> {
  private static final Pattern PROPERTY_PATTERN = Pattern.compile(
      "^\\[([\\w.\\-]+)\\]:\\s\\[(.*)\\]$");
  private static final Logger logger = Logger.getLogger(AndroidPropertyProcessor.class.getName());

  private final Map<String, String> properties = Maps.newHashMap();

  @Override
  public boolean processLine(String line) {
    Matcher matcher = PROPERTY_PATTERN.matcher(line.trim());
    if (matcher.matches()) {
      properties.put(matcher.group(1), matcher.group(2));
    } else {
      logger.warning(String.format("Line '%s' doesnt match regexp '%s'. Discarding.", line,
          PROPERTY_PATTERN));
    }
    return true;
  }

  @Override
  public ImmutableMap<String, String> getResult() {
    return ImmutableMap.copyOf(properties);
  }

}
