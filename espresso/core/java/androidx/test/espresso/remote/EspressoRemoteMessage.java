/*
 * Copyright (C) 2016 The Android Open Source Project
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

package androidx.test.espresso.remote;

/**
 * Transforms an Espresso remote message from and to a proto message. TODO: Once this API is stable
 * document on how and when these IFs need to be implemented
 */
public interface EspressoRemoteMessage {

  /**
   * Transforms a class implementing this interface to a proto message.
   *
   * @param <M> This describes my type parameter M. M should either extend {@link
   *     com.google.protobuf.MessageLite} or enum message
   */
  interface To<M> {
    M toProto();
  }

  /**
   * Transforms a proto message of type M into a class of type T. M should either extend
   *
   * @param <T> Type parameter T represent the return type of the transformation, typically a
   *     ViewMatcher, ViewAction or ViewAssertion.
   * @param <M> Type parameter M should either extend {@link com.google.protobuf.MessageLite} or
   *     enum message
   */
  interface From<T, M> {
    T fromProto(M message);
  }
}
