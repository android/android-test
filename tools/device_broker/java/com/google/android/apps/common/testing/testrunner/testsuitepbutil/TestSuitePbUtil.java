/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.google.android.apps.common.testing.testrunner.testsuitepbutil;

import com.google.android.apps.common.testing.proto.TestInfo.InfoPb;
import com.google.android.apps.common.testing.proto.TestInfo.TestSuitePb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Utility methods for {@link TestSuitePb}. Canonical test name formatting and sorting methods.
 */
public class TestSuitePbUtil {
  /**
   * Formats test case name into canonical format from provided {@link InfoPb}.
   *
   * @param infoPb {link InfoPb} from which to extract canonical test case name.
   * @return canonical name format for test case.
   */
  public static String getCanonicalTestMethod(InfoPb infoPb) {
    checkArgument(infoPb.hasTestPackage());
    checkArgument(infoPb.hasTestClass());
    checkArgument(infoPb.hasTestMethod());

    return String.format(
        "%s.%s#%s", infoPb.getTestPackage(), infoPb.getTestClass(), infoPb.getTestMethod());
  }

  /**
   * Formats test case names into canonical format from provided {@link List} of {@link InfoPb}.
   *
   * @param infoPbs {@link List} of {@link InfoPb} from which to extract canonical test case names.
   * @return {@link List} of canonical formatted names of test cases.
   */
  public static List<String> getCanonicalTestMethods(List<InfoPb> infoPbs) {
    List<String> canonicalTestMethods = new ArrayList<String>();

    for (InfoPb infoPb : infoPbs) {
      canonicalTestMethods.add(getCanonicalTestMethod(infoPb));
    }

    return canonicalTestMethods;
  }

  /**
   * Sorts {@link InfoPb}s from provided {@link TestSuitePb}, and creates sorted
   * {@link TestSuitePb}.
   *
   * @param testSuite {@link TestSuitePb} to sort.
   * @return Sorted {@link List} of {@link InfoPb}s.
   */
  public static TestSuitePb sortTestSuite(TestSuitePb testSuite) {
    List<InfoPb> testInfoPbs = new ArrayList<InfoPb>(testSuite.getInfoList());
    Collections.sort(testInfoPbs, new Comparator<InfoPb>() {
      @Override
      public int compare(InfoPb left, InfoPb right) {
        return getCanonicalTestMethod(left).compareTo(getCanonicalTestMethod(right));
      }
    });

    return TestSuitePb.newBuilder().addAllInfo(testInfoPbs).build();
  }

  private static void checkArgument(boolean expression) {
    if (!expression) {
      throw new IllegalArgumentException();
    }
  }
}
