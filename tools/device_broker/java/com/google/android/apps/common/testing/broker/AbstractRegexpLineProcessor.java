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

import com.google.common.collect.Lists;
import com.google.common.io.LineProcessor;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Applies a regexp to each line processed and makes the match avaliable.
 *
 */
abstract class AbstractRegexpLineProcessor<T> implements LineProcessor<T> {

  private final List<Matcher> matched = Lists.newArrayList();
  private final Pattern pattern;
  private final boolean stopOnFirstMatch;

  public AbstractRegexpLineProcessor(Pattern pattern, boolean stopOnFirstMatch) {
    this.pattern = checkNotNull(pattern);
    this.stopOnFirstMatch = stopOnFirstMatch;
  }

  protected List<Matcher> getMatched() {
    return Lists.newArrayList(matched);
  }

  @Override
  public final boolean processLine(String line) {
    Matcher matcher = pattern.matcher(line);
    if (matcher.matches()) {
      matched.add(matcher);
      return !stopOnFirstMatch;
    }
    return true;
  }

  /**
   * Verifies that a particular pattern was detected in the stream.
   */
  static class RegexpPresentProcessor extends AbstractRegexpLineProcessor<Boolean> {
    public RegexpPresentProcessor(Pattern pattern) {
      super(pattern, true);
    }

    @Override
    public Boolean getResult() {
      return Boolean.valueOf(!getMatched().isEmpty());
    }
  }

  /**
   * Returns the first match detected in the stream.
   */
  static class FirstMatchProcessor extends AbstractRegexpLineProcessor<String> {
    private FirstMatchProcessor(Pattern pattern) {
      super(pattern, true);
    }

    @Override
    public String getResult() {
      return !getMatched().isEmpty() ? getMatched().get(0).group(1) : null;
    }
  }

  static class RegexpProcessorBuilder {
    private Pattern pattern;

    public RegexpProcessorBuilder withPattern(Pattern pattern) {
      this.pattern = pattern;
      return this;
    }

    public FirstMatchProcessor buildFirstMatchProcessor() {
      return new FirstMatchProcessor(pattern);
    }

    public RegexpPresentProcessor buildRegexpPresentProcessor() {
      return new RegexpPresentProcessor(pattern);
    }
  }

}
