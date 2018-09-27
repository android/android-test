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

import java.util.regex.Pattern;

/**
 * Processes "aapt dump apk" output and returns the package name of the given apk.
 */
class AaptDumpPackageLineProcessor extends AbstractRegexpLineProcessor<String> {
  // package: name='com.google.android.gm' versionCode='330' versionName='4.0.4-381230'
  private static final Pattern PACKAGE_PATTERN = Pattern.compile("package:\\sname=\\'(\\S*)\\'.*$");

  AaptDumpPackageLineProcessor() {
    super(PACKAGE_PATTERN, true);
    // match the package output pattern and stop processing the aapt output as soon as you see a
    // match.
  }

  @Override
  public String getResult() {
    if (getMatched().size() != 0) {
      return getMatched().get(0).group(1);
    } else {
      return null;
    }
  }
}
