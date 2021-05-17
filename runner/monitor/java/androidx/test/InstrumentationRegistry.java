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
 * and its arguments. Also provides an easy way for callers to get a hold of instrumentation,
 * application context and instrumentation arguments Bundle.
 *
 * @deprecated use {@link androidx.test.core.app.ApplicationProvider} or {@link
 *     androidx.test.platform.app.InstrumentationRegistry} instead
 */
@Deprecated
public final class InstrumentationRegistry {

  /**
   * Returns the instrumentation currently running. Use this to get an {@link Instrumentation} into
   * your test.
   *
   * @throws IllegalStateException if instrumentation hasn't been registered
   * @deprecated use {@link androidx.test.platform.app.InstrumentationRegistry#getInstrumentation()}
   */
  @InlineMe(replacement = "androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()")
  @Deprecated
  public static Instrumentation getInstrumentation() {
    return androidx.test.platform.app.InstrumentationRegistry.getInstrumentation();
  }

  /**
   * Returns a copy of instrumentation arguments Bundle. Use this to get a {@link Bundle} containing
   * the command line arguments passed to {@link Instrumentation} into your test.
   *
   * <p>This Bundle is not guaranteed to be present under all instrumentations.
   *
   * @return Bundle the arguments for this instrumentation.
   * @throws IllegalStateException if no argument Bundle has been registered.
   * @deprecated use {@link androidx.test.platform.app.InstrumentationRegistry#getArguments()}
   */
  @InlineMe(replacement = "androidx.test.platform.app.InstrumentationRegistry.getArguments()")
  @Deprecated
  public static Bundle getArguments() {
    return androidx.test.platform.app.InstrumentationRegistry.getArguments();
  }

  /**
   * Return the Context of this instrumentation's package. Use this to get a {@link Context}
   * representing {@link Instrumentation#getContext()} into your test.
   *
   * @deprecated In most scenarios, {@link
   *     androidx.test.core.app.ApplicationProvider#getApplicationContext()} should be used instead
   *     of the instrumentation test context. If you do need access to the test context for to
   *     access its resources, it is recommended to use {@link
   *     android.content.pm.PackageManager#getResourcesForApplication(String)} instead.
   */
  @Deprecated
  public static Context getContext() {
    return androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getContext();
  }

  /**
   * Return a Context for the target application being instrumented. Use this to get a {@link
   * Context} representing {@link Instrumentation#getTargetContext()} into your test.
   *
   * @deprecated use {@link androidx.test.core.app.ApplicationProvider#getApplicationContext()}
   *     instead.
   */
  @Deprecated
  public static Context getTargetContext() {
    return androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
        .getTargetContext();
  }

  /**
   * Records/exposes the instrumentation currently running and stores a copy of the instrumentation
   * arguments Bundle in the registry.
   *
   * <p>This is a global registry - so be aware of the impact of calling this method!
   *
   * @param instrumentation the instrumentation currently running.
   * @param arguments the arguments for this application. Null deregisters any existing arguments.
   * @deprecated use {@link
   *     androidx.test.platform.app.InstrumentationRegistry#registerInstance(Instrumentation,
   *     Bundle)}
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
