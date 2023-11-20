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

import static androidx.test.internal.util.Checks.checkNotNull;

import androidx.annotation.Nullable;
import androidx.test.espresso.core.internal.deps.guava.base.Function;
import androidx.test.espresso.core.internal.deps.guava.base.Optional;
import androidx.test.espresso.core.internal.deps.guava.base.Supplier;
import com.google.common.collect.ImmutableSet;
import java.util.Set;

/**
 * This class is a reimplementation of {@link com.google.common.base.Optional} to maintain API
 * compatibility with older versions of espresso.
 *
 * @param <T> the type of instance that can be contained. {@code Optional} is naturally covariant on
 *     this type, so it is safe to cast an {@code Optional<T>} to {@code Optional<S>} for any
 *     supertype {@code S} of {@code T}.
 * @see com.google.common.base.Optional
 * @deprecated use androidx.annotation.Nullable/NonNull instead
 */
@Deprecated
public final class EspressoOptional<T> {

  private static final EspressoOptional ABSENT = new EspressoOptional<>(null);
  @Nullable private final T value;

  public static <T> EspressoOptional<T> of(T reference) {
    return new EspressoOptional<>(checkNotNull(reference));
  }

  public static <T> EspressoOptional<T> absent() {
    return ABSENT;
  }

  public static <T> EspressoOptional<T> fromNullable(T nullableReference) {
    return new EspressoOptional<>(nullableReference);
  }

  private EspressoOptional(@Nullable T value) {
    this.value = value;
  }

  public boolean isPresent() {
    return value != null;
  }

  public T get() {
    return checkNotNull(value);
  }

  public Optional<T> or(Optional<? extends T> secondChoice) {
    return isPresent() ? Optional.of(value) : (Optional<T>) secondChoice;
  }

  public T or(Supplier<? extends T> supplier) {
    return isPresent() ? value : supplier.get();
  }

  public T or(T defaultValue) {
    return isPresent() ? value : defaultValue;
  }

  public T orNull() {
    return isPresent() ? value : null;
  }

  public Set<T> asSet() {
    return isPresent() ? ImmutableSet.of(value) : ImmutableSet.of();
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof EspressoOptional) {
      EspressoOptional<?> other = (EspressoOptional<?>) object;
      if (isPresent()) {
        return value.equals(other.get());
      } else if (!other.isPresent()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    // copy the guava implementation
    return isPresent() ? 0x598df91c + value.hashCode() : 0x79a31aac;
  }

  @Override
  public String toString() {
    return isPresent() ? value.toString() : "null";
  }

  public <V> Optional<V> transform(Function<? super T, V> function) {
    return Optional.fromNullable(value).transform(function);
  }

  public static <T> Iterable<T> presentInstances(
      final Iterable<? extends Optional<? extends T>> optionals) {
    return Optional.presentInstances(optionals);
  }


  private static final long serialVersionUID = 0;
}
