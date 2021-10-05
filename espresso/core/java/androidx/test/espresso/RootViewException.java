/*
 * Copyright (C) 2021 The Android Open Source Project
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

package androidx.test.espresso;

import android.view.View;

/**
 * An {@link EspressoException} that can provide a Root View.
 *
 * <p>Types which implement this are also expected to extend {@link Throwable} or any of its
 * sub-types, as is typical of all Java exceptions.
 */
public interface RootViewException extends EspressoException {

  /** Returns the root view where this exception is thrown. */
  View getRootView();
}
