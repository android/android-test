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

import java.util.Map;

/** Allows implementors to instruct ModelCodec on how to encode this object as JSON. */
public interface JSONAble {

  public String toJSONString();

  /**
   * Allows implementors to replace a JSONObject (representated as a map) with a more applicable
   * object.
   */
  public interface DeJSONFactory {
    /**
     * Attempt to convert this map to another Java object.
     *
     * @param jsonObject the json object encountered
     * @return null if it could not be converted, or the object.
     */
    public Object attemptDeJSONize(Map<String, Object> jsonObject);
  }
}
