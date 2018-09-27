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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/** A simple implementation of Atom suitable for subclassing. */
public class SimpleAtom implements Atom<Evaluation> {

  private final String script;
  private final ElementReferencePlacement elementPlacement;

  /** Controls whether the ElementReference appears as the first arg or last arg to the script. */
  public enum ElementReferencePlacement {
    FIRST,
    LAST
  };

  /**
   * Creates a SimpleAtom which runs the given script and places any ElementReference as the first
   * argument to the script.
   */
  public SimpleAtom(String script) {
    this(script, ElementReferencePlacement.FIRST);
  }

  /**
   * Creates a SimpleAtom which runs the given script and places any supplied ElementReference
   * either as the first or last argument to the script.
   */
  public SimpleAtom(String script, ElementReferencePlacement elementPlacement) {
    this.script = checkNotNull(script);
    this.elementPlacement = checkNotNull(elementPlacement);
  }

  /** Returns the script this SimpleAtom was created with. */
  @Override
  public final String getScript() {
    return script;
  }

  /**
   * The SimpleAtom's transform method checks the Evaluation object for success.
   *
   * <p>If the Evaluation object has an error, the default behaviour is to throw an exception,
   * subclasses may change this behaviour via the handleBadEvaluation method.
   */
  @Override
  public final Evaluation transform(Evaluation e) {
    if (e.getStatus() != 0) {
      return checkNotNull(handleBadEvaluation(e), "Evaluation bad and handler returned null! " + e);
    }
    return e;
  }

  /**
   * The SimpleAtom presents an argument list to the script which follows some basic conventions.
   *
   * <p>If an ElementReference is provided, it is placed either in the first or last position based
   * on the ElementReferencePlacement provided to the constructor.
   *
   * <p>The nonContextualArguments (if provided via getNonContextualArguments) will appear after the
   * ElementReference.
   */
  @Override
  public final List<Object> getArguments(@Nullable ElementReference elementRef) {
    List<Object> nonContextualArguments = checkNotNull(getNonContextualArguments());
    if (null == elementRef) {
      handleNoElementReference();
    }

    if (nonContextualArguments.size() == 0 && null == elementRef) {
      return Collections.EMPTY_LIST;
    } else {
      if (null == elementRef) {
        return nonContextualArguments;
      } else {
        List<Object> args = new ArrayList<Object>(nonContextualArguments.size() + 1);
        if (elementPlacement == ElementReferencePlacement.FIRST) {
          args.add(elementRef);
          args.addAll(nonContextualArguments);
        } else {
          args.addAll(nonContextualArguments);
          args.add(elementRef);
        }
        return args;
      }
    }
  }

  /**
   * Extend this method to handle the case where getArguments() has been called without an
   * ElementReference. Implementors may want to throw an exception here if they require an
   * ElementReference to evaluate properly.
   */
  protected void handleNoElementReference() {}

  /**
   * Extend this method to pass additional arguments to the script.
   *
   * @return a list of arguments (non-null)
   */
  protected List<Object> getNonContextualArguments() {
    return Collections.EMPTY_LIST;
  }

  /**
   * Extend this method to handle a failure code in the Evaluation object.
   *
   * <p>The default implementation will throw an exception, subclasses may want to ignore certain
   * failure cases.
   *
   * @return Evaluation the evaluation object (must be non-null)
   * @throws RuntimeException if the badness level is too high.
   */
  protected Evaluation handleBadEvaluation(Evaluation e) {
    throw new RuntimeException("Error in evaluation" + e);
  }
}
