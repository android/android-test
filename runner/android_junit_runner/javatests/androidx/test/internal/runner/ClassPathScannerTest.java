/*
 * Copyright (C) 2012 The Android Open Source Project
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
package androidx.test.internal.runner;

import android.test.suitebuilder.annotation.SmallTest;
import androidx.test.InstrumentationRegistry;
import androidx.test.internal.runner.ClassPathScanner.ExcludePackageNameFilter;
import androidx.test.internal.runner.ClassPathScanner.ExternalClassNameFilter;
import androidx.test.internal.runner.ClassPathScanner.InclusivePackageNamesFilter;
import androidx.test.runner.AndroidJUnit4;
import dalvik.system.DexFile;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link ClassPathScanner}. */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class ClassPathScannerTest {

  private ClassPathScanner mClassPathScanner;
  private Enumeration<String> mDexEntries;

  @Before
  public void setUp() throws Exception {
    mClassPathScanner =
        new ClassPathScanner(InstrumentationRegistry.getTargetContext().getPackageCodePath()) {
          @Override
          Enumeration<String> getDexEntries(DexFile dexFile) {
            return mDexEntries;
          }
        };
  }

  @Test
  public void externalClassNameFilter() throws IOException {
    mDexEntries = createEntries("com.example.MyName", "com.example.MyName$Inner");
    Set<String> result = mClassPathScanner.getClassPathEntries(new ExternalClassNameFilter());
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains("com.example.MyName"));
  }

  @Test
  public void inclusivePackageNamesFilter() throws IOException {
    mDexEntries =
        createEntries("com.example.MyName", "com.exclude.Excluded", "com.example2.MyName");
    Set<String> result =
        mClassPathScanner.getClassPathEntries(
            new InclusivePackageNamesFilter(Arrays.asList("com.example")));
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains("com.example.MyName"));
  }

  @Test
  public void exclusivePackageNameFilter() throws IOException {
    mDexEntries =
        createEntries("com.example.MyName", "com.exclude.Excluded", "com.exclude2.Excluded");
    Set<String> result =
        mClassPathScanner.getClassPathEntries(new ExcludePackageNameFilter("com.exclude"));
    Assert.assertEquals(2, result.size());
    Assert.assertTrue(result.contains("com.example.MyName"));
    Assert.assertTrue(result.contains("com.exclude2.Excluded"));
  }

  private Enumeration<String> createEntries(String... entries) {
    Vector<String> v = new Vector<String>(entries.length);
    for (String entry : entries) {
      v.add(entry);
    }
    return v.elements();
  }
}
