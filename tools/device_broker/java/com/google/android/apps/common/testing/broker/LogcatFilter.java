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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Models a Logcat filter string.
 */
public class LogcatFilter {
  private static final Splitter COMMA = Splitter.on(',').omitEmptyStrings();
  private static final Splitter COLON = Splitter.on(':').limit(2);
  private static final Pattern VALID_TAGS = Pattern.compile("\\*|[\\w\\.]+");

  /**
   * The logcat level for a given tag.
   */
  public enum Level {
    VERBOSE("V"),
    DEBUG("D"),
    INFO("I"),
    WARNING("W"),
    ERROR("E"),
    FATAL("F"),
    SILENT("S");

    private final String logcatCode;

    Level(String logcatCode) {
      this.logcatCode = logcatCode;
    }

    public String getLogcatCode() {
      return logcatCode;
    }

    public static Level fromLogcatCode(String logcatCode) {
      for (Level l : Level.values()) {
        if (l.logcatCode.equalsIgnoreCase(logcatCode)) {
          return l;
        }
      }
      throw new IllegalArgumentException("code: " + logcatCode + " is unknown");
    }
  }

  private final String tagName;
  private final Level level;

  private LogcatFilter(String tagName, Level level) {
    this.level = level;
    this.tagName = tagName;
  }

  public Level getLevel() {
    return level;
  }

  public String getTagName() {
    return tagName;
  }

  @Override
  public String toString() {
    return String.format("%s:%s", getTagName(), getLevel().getLogcatCode());
  }

  public static LogcatFilter fromString(String s) {
    List<String> tagAndLevel = COLON.splitToList(s);
    checkState(VALID_TAGS.matcher(tagAndLevel.get(0)).matches(), "%s: Tag %s doesnt match %s",
        s, tagAndLevel.get(0), VALID_TAGS);
    return new LogcatFilter(tagAndLevel.get(0), Level.fromLogcatCode(tagAndLevel.get(1)));
  }

  /** Splits a comma-separated string into a list of filters. */
  static ImmutableList<LogcatFilter> fromStringList(String in) {
    List<LogcatFilter> results = Lists.newArrayList();
    for (String stringFilter : COMMA.splitToList(in)) {
      results.add(LogcatFilter.fromString(stringFilter));
    }
    return ImmutableList.copyOf(results);
  }

  static String toStringList(List<LogcatFilter> filters) {
    checkNotNull(filters);
    List<String> strs = Lists.newArrayList();
    for (LogcatFilter f : filters) {
      strs.add(f.toString());
    }
    return Joiner.on(",").join(strs);
  }

}
