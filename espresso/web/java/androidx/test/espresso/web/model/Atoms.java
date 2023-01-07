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

import static androidx.test.internal.util.Checks.checkNotNull;

import androidx.annotation.VisibleForTesting;
import androidx.test.espresso.remote.annotation.RemoteMsgConstructor;
import androidx.test.espresso.remote.annotation.RemoteMsgField;
import java.util.List;
import kotlin.collections.CollectionsKt;

/** Utility class wrapping simple and more commonly used atoms. */
public final class Atoms {
  private Atoms() {}

  /**
   * Creates an atom which wraps the input atom and transforms its output using the given
   * transformer.
   */
  public static <I, O> Atom<O> transform(
      Atom<I> in, TransformingAtom.Transformer<I, O> transformer) {
    return new TransformingAtom(in, transformer);
  }

  /**
   * Creates an atom that will execute the provided script and return an object created by the given
   * transformer.
   */
  public static <O> Atom<O> script(
      String script, TransformingAtom.Transformer<Evaluation, O> transformer) {
    return transform(script(script), transformer);
  }

  /** Creates a transformer which will convert an Evaluation to a given type (or die trying). */
  public static <E> TransformingAtom.Transformer<Evaluation, E> castOrDie(final Class<E> clazz) {
    return new CastOrDieAtom<>(checkNotNull(clazz));
  }

  /** Creates an atom that will execute the provided script and return an evaluation object. */
  public static Atom<Evaluation> script(String script) {
    return new ScriptWithArgsSimpleAtom(script, CollectionsKt.listOf());
  }

  /** Returns the value of document.location.href. */
  public static Atom<String> getCurrentUrl() {
    return script(
        "function getCurrentUrl() {return document.location.href;}", castOrDie(String.class));
  }

  /** Returns the value of document.title. */
  public static Atom<String> getTitle() {
    return script("function getTitle() {return document.title;}", castOrDie(String.class));
  }

  /**
   * Creates an atom that will execute the provided script with the given non-contextual arguments.
   */
  public static Atom<Evaluation> scriptWithArgs(String script, final List<Object> args) {
    return new ScriptWithArgsSimpleAtom(script, args);
  }

  @VisibleForTesting
  static final class CastOrDieAtom<E> implements TransformingAtom.Transformer<Evaluation, E> {
    @RemoteMsgField(order = 0)
    private final Class<E> clazz;

    @RemoteMsgConstructor
    private CastOrDieAtom(Class<E> clazz) {
      this.clazz = clazz;
    }

    @Override
    public E apply(Evaluation in) {
      if (null == in.getValue()) {
        throw new RuntimeException("Atom evaluation returned null!");
      }

      if (clazz.isInstance(in.getValue())) {
        return clazz.cast(in.getValue());
      }

      throw new RuntimeException(
          String.format(
              "%s: is not compatible with Evaluation: %s",
              clazz.getName(), in.getValue().getClass().getName()));
    }
  }

  @VisibleForTesting
  static final class ScriptWithArgsSimpleAtom extends SimpleAtom {
    @SuppressWarnings("unused") // called reflectively
    private final String script;

    private final List<Object> args;

    public ScriptWithArgsSimpleAtom(String script, final List<Object> args) {
      super(script);
      this.script = checkNotNull(script);
      this.args = checkNotNull(args);
    }

    @Override
    public List<Object> getNonContextualArguments() {
      return args;
    }
  }
}
