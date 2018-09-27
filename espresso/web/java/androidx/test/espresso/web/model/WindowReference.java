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
import java.util.Map;
import org.json.JSONException;
import org.json.JSONStringer;

/**
 * A reference to a javascript window/frame existing within a WebView.
 *
 * <p>This reference is only a pointer to data held within the javascript context of a given
 * WebView. It may no longer be valid the next time you attempt to use it. For instance the page
 * could be navigated away from. There is not much you can use an WindowReference for in Java, it
 * exists primarily to pass back to a WebView for further action.
 */
public final class WindowReference implements JSONAble {
  static final String KEY = "WINDOW";

  @RemoteMsgField(order = 0)
  private final String opaque;

  @Override
  public int hashCode() {
    return opaque.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof WindowReference) {
      return (((WindowReference) other).opaque.equals(opaque));
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return toJSONString();
  }

  @RemoteMsgConstructor
  WindowReference(String opaque) {
    this.opaque = checkNotNull(opaque);
  }

  String getOpaque() {
    return opaque;
  }

  @Override
  public String toJSONString() {
    try {
      return new JSONStringer().object().key(KEY).value(opaque).endObject().toString();
    } catch (JSONException je) {
      throw new RuntimeException(je);
    }
  }

  static final JSONAble.DeJSONFactory DEJSONIZER =
      new JSONAble.DeJSONFactory() {
        @Override
        public Object attemptDeJSONize(Map<String, Object> map) {
          if (map.size() == 1) {
            Object maybeOpaque = map.get(KEY);
            if (maybeOpaque instanceof String) {
              return new WindowReference((String) maybeOpaque);
            }
          }
          return null;
        }
      };
}
