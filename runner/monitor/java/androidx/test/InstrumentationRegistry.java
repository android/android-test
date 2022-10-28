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

package androidx.test;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import com.google.errorprone.annotations.InlineMe;

/**
 * An exposed registry instance that holds a reference to the instrumentation running in the process
 * and the instrumentation arguments. Provides an easy way for callers to get access to the
 * instrumentation, application context, and instrumentation arguments bundle.
 *
 * @deprecated Use {@link androidx.test.platform.app.InstrumentationRegistry} or <code>
 *     <a href="/reference/androidx/test/core/app/ApplicationProvider">
 *     androidx.test.core.app.ApplicationProvider</a></code> instead.
 */
@Deprecated
public final class InstrumentationRegistry {

  /**
   * Returns the instrumentation currently running. Use this method to get an <code>
   * <a href="/reference/android/app/Instrumentation">Instrumentation</a></code> into your test.
   *
   * @return The current instrumentation.
   * @throws IllegalStateException If instrumentation hasn't been registered.
   * @deprecated Use {@link androidx.test.platform.app.InstrumentationRegistry#getInstrumentation()
   *     androidx.test.platform.app.InstrumentationRegistry#getInstrumentation()}.
   */
  @InlineMe(replacement = "androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()")
  @Deprecated
  public static Instrumentation getInstrumentation() {
    return androidx.test.platform.app.InstrumentationRegistry.getInstrumentation();
  }

  /**
   * Returns a copy of the instrumentation arguments bundle. Use this method to get a <code>
   * <a href="/reference/android/os/Bundle">Bundle</a></code> containing the command-line arguments
   * passed to <code><a href="/reference/android/app/Instrumentation">Instrumentation</a></code>
   * into your test.
   *
   * <p>The bundle is not guaranteed to be present under all instrumentations.
   *
   * @return The arguments bundle for this instrumentation.
   * @throws IllegalStateException If no argument bundle has been registered.
   * @deprecated Use {@link androidx.test.platform.app.InstrumentationRegistry#getArguments()
   *     androidx.test.platform.app.InstrumentationRegistry#getArguments()}.
   */
  @InlineMe(replacement = "androidx.test.platform.app.InstrumentationRegistry.getArguments()")
  @Deprecated
  public static Bundle getArguments() {
    return androidx.test.platform.app.InstrumentationRegistry.getArguments();
  }

  /**
   * Returns the context of this instrumentation's package. Use this method to get a <code>
   * <a href="/reference/android/content/Context">Context</a></code> representing <code>
   * <a href="/reference/android/app/Instrumentation#getContext()">Instrumentation#getContext()</a>
   * </code> into your test.
   *
   * @return The instrumentation context.
   * @deprecated In most scenarios, <code>
   *     <a href="/reference/androidx/test/core/app/ApplicationProvider#getApplicationContext()">
   *     androidx.test.core.app.ApplicationProvider#getApplicationContext()</a></code> should be
   *     used instead of the instrumentation test context. If you do need access to the test context
   *     to access its resources, use <code>
   *     <a href="/reference/android/content/pm/PackageManager#getResourcesForApplication(java.lang.String)">
   *     android.content.pm.PackageManager#getResourcesForApplication(String)</a></code> instead.
   */
  @Deprecated
  public static Context getContext() {
    return androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getContext();
  }

  /**
   * Returns a context for the target application being instrumented. Use this method to get a
   * <code><a href="/reference/android/content/Context">Context</a></code> representing <code>
   * <a href="/reference/android/app/Instrumentation#getTargetContext()">
   * Instrumentation#getTargetContext()</a></code> into your test.
   *
   * @return The target application context.
   * @deprecated Use <code>
   *     <a href="/reference/androidx/test/core/app/ApplicationProvider#getApplicationContext()">
   *     androidx.test.core.app.ApplicationProvider#getApplicationContext()</a></code>.
   */
  @Deprecated
  public static Context getTargetContext() {
    return androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
        .getTargetContext();
  }

  /**
   * Records/exposes the instrumentation currently running and stores a copy of the instrumentation
   * arguments bundle in the registry.
   *
   * <p>This is a global registry, so be aware of the impact of calling this method!
   *
   * @param instrumentation The instrumentation currently running.
   * @param arguments The arguments for this application. Null deregisters any existing arguments.
   * @deprecated Use {@link
   *     androidx.test.platform.app.InstrumentationRegistry#registerInstance(Instrumentation,
   *     Bundle)
   *     androidx.test.platform.app.InstrumentationRegistry#registerInstance(Instrumentation,
   *     Bundle)}.
   */
  @InlineMe(
      replacement =
          "androidx.test.platform.app.InstrumentationRegistry.registerInstance(instrumentation,"
              + " arguments)")
  @Deprecated
  public static void registerInstance(Instrumentation instrumentation, Bundle arguments) {
    androidx.test.platform.app.InstrumentationRegistry.registerInstance(instrumentation, arguments);
  }

  private InstrumentationRegistry() {}
}
