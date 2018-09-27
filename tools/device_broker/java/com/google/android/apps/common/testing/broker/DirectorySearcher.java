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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileFilter;
import java.util.List;

/**
 * Recursively searches a directory for all files with a given name.
 *
 * This is useful for pulling files out of the data directory.
 *
 * @author thomaswk@google.com (Thomas Knych)
 */
public class DirectorySearcher {
  private static final DirFilter DIR_FILTER = new DirFilter();
  private final File baseDir;
  private final Predicate<CharSequence> fileNamePredicate;
  private final FileNameMatcher fileNameMatcher;

  public DirectorySearcher(File baseDir, String pattern) {
    this.baseDir = checkNotNull(baseDir);
    this.fileNamePredicate = Predicates.containsPattern(checkNotNull(pattern));
    this.fileNameMatcher = new FileNameMatcher();
  }

  public List<File> findMatches() {
    return findMatches(baseDir);
  }

  private List<File> findMatches(File dir) {
    File[] files = dir.listFiles(fileNameMatcher);
    if (files == null) {
        return Lists.newArrayList();
    }

    List<File> matches = Lists.newArrayList(files);
    for (File subdir : dir.listFiles(DIR_FILTER)) {
      matches.addAll(findMatches(subdir));
    }
    return matches;
  }

  private class FileNameMatcher implements FileFilter {
    @Override
    public boolean accept(File file) {
      return file.isFile() && fileNamePredicate.apply(file.getName());
    }
  }

  private static class DirFilter implements FileFilter {
    @Override
    public boolean accept(File file) {
      return file.isDirectory();
    }
  }
}
