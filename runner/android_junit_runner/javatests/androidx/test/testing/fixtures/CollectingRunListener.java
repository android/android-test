/*
 * Copyright (C) 2021 The Android Open Source Project
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
package androidx.test.testing.fixtures;

import java.util.ArrayList;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class CollectingRunListener extends RunListener {

  public List<Description> tests = new ArrayList<>();
  public List<Failure> failures = new ArrayList<>();

  @Override
  public void testFinished(Description description) throws Exception {
    super.testFinished(description);
    tests.add(description);
  }

  @Override
  public void testFailure(Failure failure) throws Exception {
    super.testFailure(failure);
    failures.add(failure);
  }
}
