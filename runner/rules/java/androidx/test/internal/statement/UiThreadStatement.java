/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.internal.statement;

import static androidx.test.InstrumentationRegistry.getInstrumentation;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.runners.model.Statement;

/** {@link Statement} that executes a test on the application's main thread (or UI thread). */
public class UiThreadStatement extends Statement {
  private final Statement base;
  private final boolean runOnUiThread;

  public UiThreadStatement(Statement base, boolean runOnUiThread) {
    this.base = base;
    this.runOnUiThread = runOnUiThread;
  }

  @Override
  public void evaluate() throws Throwable {
    if (runOnUiThread) {
      final AtomicReference<Throwable> exceptionRef = new AtomicReference<>();
      getInstrumentation()
          .runOnMainSync(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    base.evaluate();
                  } catch (Throwable throwable) {
                    exceptionRef.set(throwable);
                  }
                }
              });
      Throwable throwable = exceptionRef.get();
      if (throwable != null) {
        throw throwable;
      }
    } else {
      base.evaluate();
    }
  }
}
