/*
 * Copyright (C) 2022 The Android Open Source Project
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
package androidx.test.espresso.core.internal.deps.guava.base;

import static androidx.test.internal.util.Checks.checkNotNull;

import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import kotlin.collections.SetsKt;

/**
 * Redefinition of Guava's Optional, created to avoid compatibilty breakages for users of
 * EspressoOptional.
 *
 * @deprecated use androidx.annotation.Nullable instead
 */
@Deprecated
public class Optional<T> implements java.io.Serializable {

  @Nullable private final T value;

  private Optional(T value) {
    this.value = value;
  }

  public static <T> Optional<T> fromNullable(@Nullable T value) {
    return new Optional<>(value);
  }

  public static <T> Optional<T> of(T value) {
    return new Optional<>(checkNotNull(value));
  }

  @Override
  public int hashCode() {
    // copy the Guava implementation
    return isPresent() ? 0x598df91c + value.hashCode() : 0x79a31aac;
  }

  public Optional<T> or(Optional<T> other) {
    return isPresent() ? this : other;
  }

  public T or(Supplier<T> supplier) {
    return or(supplier.get());
  }

  public T or(T other) {
    return isPresent() ? value : other;
  }

  public <V> Optional<V> transform(Function<? super T, V> function) {
    return fromNullable(function.apply(value));
  }

  public static <T> Iterable<T> presentInstances(
      final Iterable<? extends Optional<? extends T>> optionals) {
    List<T> list = new ArrayList<>();
    for (Optional<? extends T> optional : optionals) {
      if (optional.isPresent()) {
        list.add(optional.get());
      }
    }
    return list;
  }

  public T get() {
    return checkNotNull(value);
  }

  public T orNull() {
    return value;
  }

  public Set<T> asSet() {
    return isPresent() ? SetsKt.setOf(value) : SetsKt.emptySet();
  }

  public boolean isPresent() {
    return value != null;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof Optional) {
      Optional<?> other = (Optional<?>) object;
      if (isPresent()) {
        return value.equals(other.get());
      } else if (!other.isPresent()) {
        return true;
      }
    }
    return false;
  }

  private static final long serialVersionUID = 0;
}
