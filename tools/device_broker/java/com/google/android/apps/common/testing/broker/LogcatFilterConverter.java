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

import com.beust.jcommander.IStringConverter;
import com.google.common.collect.ImmutableList;

/** Exposes LogcatFilter as a jCommander converter. */
public class LogcatFilterConverter implements IStringConverter<LogcatFilter> {

  @Override
  public LogcatFilter convert(String filterString) {
    return LogcatFilter.fromString(filterString);
  }

  /** jCommander converter for converting lists of {@code LogCatFilter}. */
  public static class Multiple implements IStringConverter<ImmutableList<LogcatFilter>> {
    @Override
    public ImmutableList<LogcatFilter> convert(String filterString) {
      return LogcatFilter.fromStringList(filterString);
    }
  }
}
