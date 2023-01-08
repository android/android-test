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
package androidx.test.espresso.util;

import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A reimplementation of needed methods from Guava, to avoid the direct dependency.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class Iterators {

  private Iterators() {}

  /**
   * Converts an iterator into a array.
   *
   * @param iterator the {@link Iterator} to convert
   * @param clazz the element type
   * @return an array of all of the iterators' elements
   * @hide
   */
  @RestrictTo(Scope.LIBRARY_GROUP)
  public static <T> T[] toArray(Iterator<T> iterator, Class<T> clazz) {
    ArrayList<T> arrayList = new ArrayList<>();
    while (iterator.hasNext()) {
      arrayList.add(iterator.next());
    }
    return arrayList.toArray((T[]) Array.newInstance(clazz, arrayList.size()));
  }
}
