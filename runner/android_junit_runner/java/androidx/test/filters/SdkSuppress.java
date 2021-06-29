/*
 * Copyright (C) 2014 The Android Open Source Project
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
package androidx.test.filters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a specific test or class requires a minimum or maximum API Level to execute.
 *
 * <p>Test(s) will be skipped when executed on android platforms less/more than specified level
 * (inclusive).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface SdkSuppress {
  /** The minimum API level to execute (inclusive) */
  int minSdkVersion() default 1;
  /** The maximum API level to execute (inclusive) */
  int maxSdkVersion() default Integer.MAX_VALUE;
  /**
   * An array of {@link android.os.Build.VERSION.CODENAME} to execute on. This is intended to be
   * used to run on a pre-release SDK, where the {@link android.os.Build.VERSION.SDK_INT} has not
   * yet been finalized. This is treated as an OR operation with respect to the minSdkVersion and
   * maxSdkVersion attributes.
   *
   * <p>Note that in some development versions of a pre-release SDK, notably on the AOSP main
   * branch, the codename can often be two versions behind the version code. This is because N+2 SDK
   * development may have started before the N+1 finalized SDK is merged into the branch. For that
   * reason, codeName should almost always contain two codenames, so that when the codename is
   * updated from SDK version+1 to SDK version+2, the test keeps running.
   *
   * <p>For example, to filter a test so it runs on only the prerelease S SDK: <code>
   * {@literal @}SdkSuppress(minSdkVersion = Build.VERSION_CODES.S, codeName = { "S", "T" })
   * </code>
   */
  String[] codeName() default {};
}
