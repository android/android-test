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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import android.os.Build;
import android.util.JsonReader;
import android.util.Log;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

/** Encodes/Decodes JSON. */
public final class ModelCodec {
  private static final String TAG = "JS_CODEC";

  private static final ImmutableSet<Class<?>> VALUEABLE_CLASSES =
      ImmutableSet.of(Boolean.class, Number.class, String.class, JSONObject.class, JSONArray.class);

  private static final ImmutableSet<Class<?>> TOP_LEVEL_CLASSES =
      ImmutableSet.of(
          JSONObject.class,
          JSONArray.class,
          Iterable.class,
          Object[].class,
          Map.class,
          JSONAble.class);

  private static final CopyOnWriteArrayList<JSONAble.DeJSONFactory> DEJSONIZERS =
      new CopyOnWriteArrayList<JSONAble.DeJSONFactory>(
          Lists.newArrayList(
              Evaluation.DEJSONIZER, WindowReference.DEJSONIZER, ElementReference.DEJSONIZER));

  private ModelCodec() {}

  /** Transforms a JSON string to an evaluation. */
  public static Evaluation decodeEvaluation(String json) {
    Object obj = decode(json);
    if (obj instanceof Evaluation) {
      return (Evaluation) obj;
    } else {
      throw new IllegalArgumentException(
          String.format(
              "Document: \"%s\" did not decode to an evaluation. Instead: \"%s\"", json, obj));
    }
  }

  /** Encodes a Java Object into a JSON string. */
  public static String encode(Object javaObject) {
    checkNotNull(javaObject);
    try {
      if (javaObject instanceof JSONObject) {
        return javaObject.toString();
      } else if (javaObject instanceof JSONArray) {
        return javaObject.toString();
      } else if (javaObject instanceof JSONAble) {
        return new JSONObject(((JSONAble) javaObject).toJSONString()).toString();
      } else if ((javaObject instanceof Iterable)
          || (javaObject instanceof Map)
          || (javaObject instanceof Object[])) {
        JSONStringer stringer = new JSONStringer();
        return encodeHelper(javaObject, stringer).toString();
      }
      throw new IllegalArgumentException(
          String.format(
              "%s: not a valid top level class. Want one of: %s",
              javaObject.getClass(), TOP_LEVEL_CLASSES));
    } catch (JSONException je) {
      throw new RuntimeException("Encode failed: " + javaObject, je);
    }
  }

  /**
   * Removes a DeJSONFactory from the list of factories that transform JSONObjects to java objects.
   */
  public static void removeDeJSONFactory(JSONAble.DeJSONFactory dejson) {
    DEJSONIZERS.remove(dejson);
  }

  /** Adds a DeJSONFactory to intercept JSONObjects and replace them with more suitable types. */
  public static void addDeJSONFactory(JSONAble.DeJSONFactory dejson) {
    DEJSONIZERS.add(checkNotNull(dejson));
  }

  static Object decode(String json) {
    checkNotNull(json);
    checkArgument(!"".equals(json), "Empty docs not supported.");

    try {
      if (Build.VERSION.SDK_INT < 13) {
        // After API 13, there is the JSONReader API - which is nicer to work with.
        return decodeViaJSONObject(json);
      } else {
        return decodeViaJSONReader(json);
      }
    } catch (JSONException je) {
      throw new RuntimeException(String.format("Could not parse: %s", json), je);
    } catch (IOException ioe) {
      throw new RuntimeException(String.format("Could not parse: %s", json), ioe);
    }
  }

  private static Object decodeViaJSONObject(String json) throws JSONException {
    JSONTokener tokener = new JSONTokener(json);
    Object value = tokener.nextValue();
    if (value instanceof JSONArray) {
      return decodeArray((JSONArray) value);
    } else if (value instanceof JSONObject) {
      return decodeObject((JSONObject) value);
    } else {
      throw new IllegalArgumentException("No top level object or array: " + json);
    }
  }

  private static List<Object> decodeArray(JSONArray array) throws JSONException {
    List<Object> data = Lists.newArrayList();
    for (int i = 0; i < array.length(); i++) {
      if (array.isNull(i)) {
        data.add(null);
      } else {
        Object value = array.get(i);
        if (value instanceof JSONObject) {
          data.add(decodeObject((JSONObject) value));
        } else if (value instanceof JSONArray) {
          data.add(decodeArray((JSONArray) value));
        } else {
          // boolean / string / or number.
          data.add(value);
        }
      }
    }
    return data;
  }

  private static Object decodeObject(JSONObject jsonObject) throws JSONException {
    List<String> nullKeys = Lists.newArrayList();
    Map<String, Object> obj = Maps.newHashMap();
    Iterator<String> keys = jsonObject.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      if (jsonObject.isNull(key)) {
        nullKeys.add(key);
        obj.put(key, JSONObject.NULL);
      } else {
        Object value = jsonObject.get(key);
        if (value instanceof JSONObject) {
          obj.put(key, decodeObject((JSONObject) value));
        } else if (value instanceof JSONArray) {
          obj.put(key, decodeArray((JSONArray) value));
        } else {
          // boolean / string / or number.
          obj.put(key, value);
        }
      }
    }
    Object replacement = maybeReplaceMap(obj);
    if (replacement != null) {
      return replacement;
    } else {
      for (String key : nullKeys) {
        obj.remove(key);
      }

      return obj;
    }
  }

  private static Object decodeViaJSONReader(String json) throws IOException {
    JsonReader reader = null;
    try {
      reader = new JsonReader(new StringReader(json));
      while (true) {
        switch (reader.peek()) {
          case BEGIN_OBJECT:
            return decodeObject(reader);
          case BEGIN_ARRAY:
            return decodeArray(reader);
          default:
            throw new IllegalStateException("Bogus document: " + json);
        }
      }
    } finally {
      if (null != reader) {
        try {
          reader.close();
        } catch (IOException ioe) {
          Log.i(TAG, "json reader - close exception", ioe);
        }
      }
    }
  }

  private static List<Object> decodeArray(JsonReader reader) throws IOException {
    List<Object> array = Lists.newArrayList();
    reader.beginArray();
    while (reader.hasNext()) {
      switch (reader.peek()) {
        case BEGIN_OBJECT:
          array.add(decodeObject(reader));
          break;
        case NULL:
          reader.nextNull();
          array.add(null);
          break;
        case STRING:
          array.add(reader.nextString());
          break;
        case BOOLEAN:
          array.add(reader.nextBoolean());
          break;
        case BEGIN_ARRAY:
          array.add(decodeArray(reader));
          break;
        case NUMBER:
          array.add(decodeNumber(reader.nextString()));
          break;
        default:
          throw new IllegalStateException(String.format("%s: bogus token", reader.peek()));
      }
    }

    reader.endArray();
    return array;
  }

  private static Number decodeNumber(String value) {
    try {
      return Integer.valueOf(value);
    } catch (NumberFormatException i) {
      try {
        return Long.valueOf(value);
      } catch (NumberFormatException i2) {
        try {
          return Double.valueOf(value);
        } catch (NumberFormatException i3) {
          try {
            return new BigInteger(value);
          } catch (NumberFormatException i4) {
            return new BigDecimal(value);
          }
        }
      }
    }
  }

  private static Object decodeObject(JsonReader reader) throws IOException {
    Map<String, Object> obj = Maps.newHashMap();
    List<String> nullKeys = Lists.newArrayList();
    reader.beginObject();
    while (reader.hasNext()) {
      String key = reader.nextName();
      Object value = null;
      switch (reader.peek()) {
        case BEGIN_OBJECT:
          obj.put(key, decodeObject(reader));
          break;
        case NULL:
          reader.nextNull();
          nullKeys.add(key);
          obj.put(key, JSONObject.NULL);
          break;
        case STRING:
          obj.put(key, reader.nextString());
          break;
        case BOOLEAN:
          obj.put(key, reader.nextBoolean());
          break;
        case NUMBER:
          obj.put(key, decodeNumber(reader.nextString()));
          break;
        case BEGIN_ARRAY:
          obj.put(key, decodeArray(reader));
          break;
        default:
          throw new IllegalStateException(String.format("%s: bogus token.", reader.peek()));
      }
    }
    reader.endObject();
    Object replacement = maybeReplaceMap(obj);
    if (null != replacement) {
      return replacement;
    } else {
      for (String key : nullKeys) {
        obj.remove(key);
      }
    }
    return obj;
  }

  private static Object maybeReplaceMap(Map<String, Object> obj) {
    for (JSONAble.DeJSONFactory dejsonizer : DEJSONIZERS) {
      Object maybe = dejsonizer.attemptDeJSONize(obj);
      if (null != maybe) {
        return maybe;
      }
    }
    return null;
  }

  private static JSONStringer encodeHelper(Object javaObject, JSONStringer stringer)
      throws JSONException {
    if (null == javaObject) {
      stringer.value(javaObject);
    } else if (javaObject instanceof Map) {
      stringer.object();
      Set<Map.Entry> entries = ((Map) javaObject).entrySet();
      for (Map.Entry entry : entries) {
        stringer.key(entry.getKey().toString());
        encodeHelper(entry.getValue(), stringer);
      }
      stringer.endObject();
    } else if (javaObject instanceof Iterable) {
      stringer.array();
      for (Object obj : ((Iterable) javaObject)) {
        encodeHelper(obj, stringer);
      }
      stringer.endArray();
    } else if (javaObject instanceof Object[]) {
      stringer.array();
      for (Object obj : ((Object[]) javaObject)) {
        encodeHelper(obj, stringer);
      }
      stringer.endArray();
    } else if (javaObject instanceof JSONAble) {
      JSONObject jsonObj = new JSONObject(((JSONAble) javaObject).toJSONString());
      stringer.value(jsonObj);
    } else {
      boolean converted = false;
      for (Class valuableClazz : VALUEABLE_CLASSES) {
        if (valuableClazz.isAssignableFrom(javaObject.getClass())) {
          converted = true;
          stringer.value(javaObject);
        }
      }
      checkState(
          converted,
          "%s: not encodable. Want one of: %s",
          javaObject.getClass(),
          VALUEABLE_CLASSES);
    }
    return stringer;
  }
}
