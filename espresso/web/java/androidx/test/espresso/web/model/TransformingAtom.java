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

import androidx.test.espresso.remote.annotation.RemoteMsgConstructor;
import androidx.test.espresso.remote.annotation.RemoteMsgField;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Decorates another atom and transforms its output to another type.
 *
 * @param <I> the type of the parent Atom.
 * @param <O> the type of the parent atom after applying a transformation.
 */
public class TransformingAtom<I, O> implements Atom<O> {
  @RemoteMsgField(order = 0)
  private final Atom<I> parent;

  @RemoteMsgField(order = 1)
  private final Transformer<I, O> transformer;

  @RemoteMsgConstructor
  public TransformingAtom(Atom<I> parent, Transformer<I, O> transformer) {
    this.parent = checkNotNull(parent);
    this.transformer = checkNotNull(transformer);
  }

  @Override
  public String getScript() {
    return parent.getScript();
  }

  @Override
  public List<Object> getArguments(@Nullable ElementReference elementContext) {
    return parent.getArguments(elementContext);
  }

  @Override
  public O transform(Evaluation eval) {
    return transformer.apply(parent.transform(eval));
  }

  /** Converts input to output. */
  public interface Transformer<I, O> {
    public O apply(I input);
  }
}
