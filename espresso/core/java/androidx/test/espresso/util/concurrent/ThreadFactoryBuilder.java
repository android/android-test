/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.test.espresso.util.concurrent;

import static androidx.test.internal.util.Checks.checkNotNull;

import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A minimal implementation of Guava's ThreadFactoryBuilder
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY)
public class ThreadFactoryBuilder {

  private String nameFormat;

  public ThreadFactoryBuilder setNameFormat(String nameFormat) {
    String unused = format(nameFormat, 0); // fail fast if the format is bad or null
    this.nameFormat = nameFormat;
    return this;
  }

  public ThreadFactory build() {
    // espresso usages currently always set nameFormat, so lets just assert its nonnull to
    // simplify logic
    checkNotNull(nameFormat);
    ThreadFactory backingFactory = Executors.defaultThreadFactory();
    AtomicLong count = new AtomicLong(0);
    return r -> {
      Thread thread = backingFactory.newThread(r);
      thread.setName(format(nameFormat, count.getAndIncrement()));
      return thread;
    };
  }

  private static String format(String format, Object... args) {
    return String.format(Locale.ROOT, format, args);
  }
}
