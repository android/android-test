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
package androidx.test.orchestrator.listeners;

import android.app.Instrumentation;
import androidx.test.orchestrator.junit.ParcelableDescription;
import androidx.test.orchestrator.junit.ParcelableFailure;
import androidx.test.orchestrator.junit.ParcelableResult;

/** Listens to events created during an orchestration run. */
public abstract class OrchestrationRunListener {

  private Instrumentation instrumentation;

  public void setInstrumentation(Instrumentation instrumentation) {
    if (null == instrumentation) {
      throw new IllegalArgumentException("Instrumentation should not be null");
    }

    this.instrumentation = instrumentation;
  }

  public Instrumentation getInstrumentation() {
    return instrumentation;
  }

  public void orchestrationRunStarted(int testCount) {}

  public void testRunStarted(ParcelableDescription description) {}

  public void testStarted(ParcelableDescription description) {}

  public void testFinished(ParcelableDescription description) {}

  public void testFailure(ParcelableFailure failure) {}

  public void testAssumptionFailure(ParcelableFailure failure) {}

  public void testIgnored(ParcelableDescription description) {}

  public void testRunFinished(ParcelableResult result) {}

  public void testProcessFinished(String message) {}
}
