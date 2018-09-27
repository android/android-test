/*
 * Copyright (C) 2018 The Android Open Source Project
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

package androidx.test.espresso.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import java.util.Set;

/**
 * This class is a wrapper around {@link com.google.common.base.Optional} in order to avoid having
 * public references to Guava API.
 *
 * @param <T> the type of instance that can be contained. {@code Optional} is naturally covariant on
 *     this type, so it is safe to cast an {@code Optional<T>} to {@code Optional<S>} for any
 *     supertype {@code S} of {@code T}.
 * @see com.google.common.base.Optional
 */
public final class EspressoOptional<T> {

  private final Optional<T> delegate;

  public static <T> EspressoOptional<T> of(T reference) {
    return new EspressoOptional<>(Optional.of(reference));
  }

  public static <T> EspressoOptional<T> absent() {
    return new EspressoOptional<T>(Optional.absent());
  }

  public static <T> EspressoOptional<T> fromNullable(T nullableReference) {
    return new EspressoOptional<>(Optional.fromNullable(nullableReference));
  }

  private EspressoOptional(Optional<T> op) {
    delegate = op;
  }

  public boolean isPresent() {
    return delegate.isPresent();
  }

  public T get() {
    return delegate.get();
  }

  public Optional<T> or(Optional<? extends T> secondChoice) {
    return delegate.or(secondChoice);
  }

  public T or(Supplier<? extends T> supplier) {
    return delegate.or(supplier);
  }

  public T or(T defaultValue) {
    return delegate.or(defaultValue);
  }

  public T orNull() {
    return delegate.orNull();
  }

  public Set<T> asSet() {
    return delegate.asSet();
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof EspressoOptional) {
      EspressoOptional<?> other = (EspressoOptional<?>) object;
      return other.delegate.equals(this.delegate);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  public <V> Optional<V> transform(Function<? super T, V> function) {
    return delegate.transform(function);
  }

  @Beta
  public static <T> Iterable<T> presentInstances(
      final Iterable<? extends Optional<? extends T>> optionals) {
    return Optional.presentInstances(optionals);
  }

  private static final long serialVersionUID = 0;
}
