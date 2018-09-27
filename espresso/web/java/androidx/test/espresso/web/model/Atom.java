/*
 * Copyright (C) 2015 The Android Open Source Project
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

package androidx.test.espresso.web.model;

import java.util.List;
import javax.annotation.Nullable;

/**
 * An Atom is a thin wrapper around javascript.
 *
 * <p>The wrapped script can return a value or be a statement. The Atom can supply positional
 * arguments to pass to the wrapped script. The Atom knows how to transform the result of the
 * evaluation of the script into a higher level object.
 *
 * @param <R> the result type of the atom.
 */
public interface Atom<R> {

  /** Provides the script to be evaluated. */
  public String getScript();

  /**
   * Creates a list of arguments to pass to the script.
   *
   * @param elementContext null unless an ElementReference has been supplied to execute this atom
   *     with.
   * @return the List of objects to pass to the script as arguments.
   */
  public List<Object> getArguments(@Nullable ElementReference elementContext);

  /** Converts an Evaluation into another more suitable type. */
  public R transform(Evaluation evaluation);
}
