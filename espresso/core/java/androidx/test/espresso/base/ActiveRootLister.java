/*
 * Copyright (C) 2014 The Android Open Source Project
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

package androidx.test.espresso.base;

import androidx.test.espresso.Root;
import java.util.List;

/** Provides access to all root views in an application. */
public interface ActiveRootLister {

  /**
   * Lists the active roots in an application at this moment.
   *
   * @return a list of all the active roots in the application.
   * @throws IllegalStateException if invoked from a thread besides the main thread.
   */
  public List<Root> listActiveRoots();
}
