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

import androidx.test.filters.SmallTest;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.json.JSONObject;

/** Unit tests for {@link ModelCodec}. */
@SmallTest
public class ModelCodecTest extends TestCase {

  public void testEncodeDecodeWindowReference() {
    WindowReference ref = new WindowReference("fuzzy_cat");
    assertEquals(ref, ModelCodec.decode(ModelCodec.encode(ref)));
  }

  public void testEncodeDecodeElementReference() {
    ElementReference ref = new ElementReference("little_dog");
    assertEquals(ref, ModelCodec.decode(ModelCodec.encode(ref)));
  }

  public void testEncodeDecodeEvaluation_withValue() {
    Evaluation eval = new Evaluation.Builder().setValue("Hello World").setStatus(0).build();
    assertEquals(eval, ModelCodec.decode(ModelCodec.encode(eval)));
    assertEquals(eval, ModelCodec.decodeEvaluation(ModelCodec.encode(eval)));
  }

  public void testEncodeDecodeEvaluation_withBool() {
    Evaluation eval = new Evaluation.Builder().setValue(true).setStatus(0).build();
    assertEquals(eval, ModelCodec.decode(ModelCodec.encode(eval)));
    assertEquals(eval, ModelCodec.decodeEvaluation(ModelCodec.encode(eval)));
  }

  public void testEncodeDecodeEvaluation_withNumber() {
    Evaluation eval = new Evaluation.Builder().setValue(Integer.MAX_VALUE).setStatus(0).build();
    assertEquals(eval, ModelCodec.decode(ModelCodec.encode(eval)));
    assertEquals(eval, ModelCodec.decodeEvaluation(ModelCodec.encode(eval)));
  }

  public void testEncodeDecodeEvaluation_withList() {
    List<Object> results = Lists.newArrayList();
    results.add(1);
    results.add(2);
    results.add(3);
    results.add("foo");

    Evaluation eval = new Evaluation.Builder().setValue(results).setStatus(0).build();
    assertEquals(eval, ModelCodec.decode(ModelCodec.encode(eval)));
    assertEquals(eval, ModelCodec.decodeEvaluation(ModelCodec.encode(eval)));
  }

  public void testEncodeDecodeEvaluation_withJSONAbleValue() {
    ModelCodec.addDeJSONFactory(GoodJSONAble.DeJSONizer);
    Evaluation eval =
        new Evaluation.Builder().setValue(new GoodJSONAble("hello")).setStatus(0).build();
    assertEquals(eval, ModelCodec.decode(ModelCodec.encode(eval)));
    assertEquals(eval, ModelCodec.decodeEvaluation(ModelCodec.encode(eval)));
    ModelCodec.removeDeJSONFactory(GoodJSONAble.DeJSONizer);
  }

  public void testEncodeDecodeEvaluation_withMapValue() {
    Map<String, Object> mapResult = Maps.newHashMap();
    mapResult.put("foo", "bar");
    mapResult.put("baz", true);

    Evaluation eval = new Evaluation.Builder().setValue(mapResult).setStatus(0).build();
    assertEquals(eval, ModelCodec.decode(ModelCodec.encode(eval)));
    assertEquals(eval, ModelCodec.decodeEvaluation(ModelCodec.encode(eval)));
  }

  public void testEncodeDecodeEvaluation_withMessage() {
    Map<String, Object> payload = Maps.newHashMap();
    payload.put("message", "Error!");
    Evaluation eval =
        new Evaluation.Builder().setValue(payload).setMessage("Error!").setStatus(-1).build();
    assertEquals(eval, ModelCodec.decode(ModelCodec.encode(eval)));
    assertEquals(eval, ModelCodec.decodeEvaluation(ModelCodec.encode(eval)));
  }

  public void testEncodeDecodeJSONAble() {
    ModelCodec.addDeJSONFactory(GoodJSONAble.DeJSONizer);
    GoodJSONAble foo = new GoodJSONAble("bear-claws");
    assertEquals(foo, ModelCodec.decode(ModelCodec.encode(foo)));
    ModelCodec.removeDeJSONFactory(GoodJSONAble.DeJSONizer);
  }

  public void testEncodeDecodeJSONAble_withNull() {
    ModelCodec.addDeJSONFactory(GoodJSONAble.DeJSONizer);
    GoodJSONAble bar = new GoodJSONAble(null);
    assertEquals(bar, ModelCodec.decode(ModelCodec.encode(bar)));
    ModelCodec.removeDeJSONFactory(GoodJSONAble.DeJSONizer);
  }

  public void testEncodeDecode_map() {
    Map<String, Object> adhoc = Maps.newHashMap();
    adhoc.put("yellow", 1234);
    adhoc.put("bar", "frog");
    adhoc.put("int_max", Integer.MAX_VALUE);
    adhoc.put("int_min", Integer.MIN_VALUE);
    adhoc.put("double_min", Double.MIN_VALUE);
    adhoc.put("double_max", Double.MAX_VALUE);
    adhoc.put("sudz", Lists.newArrayList("goodbye"));
    assertEquals(adhoc, ModelCodec.decode(ModelCodec.encode(adhoc)));
  }

  public void testEncodeDecoded_array() {
    Object[] array = new Object[5];
    array[0] = Boolean.TRUE;
    array[1] = null;
    array[2] = Double.MIN_VALUE;
    array[3] = "Hello World";
    array[4] = 2;
    assertEquals(Lists.newArrayList(array), ModelCodec.decode(ModelCodec.encode(array)));
  }

  public void testEncodeDecoded_list() {
    ModelCodec.addDeJSONFactory(GoodJSONAble.DeJSONizer);
    Map<String, Object> adhoc = Maps.newHashMap();
    adhoc.put("foobar", 1234);
    adhoc.put("buzzz", Boolean.FALSE);
    adhoc.put("sudz", Lists.newArrayList("goodbye"));
    List<Object> blah =
        Lists.newArrayList(
            (Object) new GoodJSONAble("something"),
            new GoodJSONAble(null),
            new ElementReference("haha"),
            "Hello world",
            Boolean.TRUE,
            42,
            null,
            Lists.newArrayList((Object) "a nested list", 12345),
            adhoc,
            new GoodJSONAble("otherthing"));
    assertEquals(blah, ModelCodec.decode(ModelCodec.encode(blah)));
    ModelCodec.removeDeJSONFactory(GoodJSONAble.DeJSONizer);
  }

  public void testInvalidTopLevelEncodings() {
    try {
      ModelCodec.encode(1);
      fail("must be a valid top level object.");
    } catch (RuntimeException expected) {
    }
    try {
      ModelCodec.encode("so what");
      fail("must be a valid top level object.");
    } catch (RuntimeException expected) {
    }
    try {
      ModelCodec.encode(false);
      fail("must be a valid top level object.");
    } catch (RuntimeException expected) {
    }
    try {
      ModelCodec.encode(null);
      fail("must be a valid top level object.");
    } catch (RuntimeException expected) {
    }
  }

  public void testBadJSONAble() {
    try {
      ModelCodec.encode(
          new JSONAble() {
            @Override
            public String toJSONString() {
              return "{'foobar':'1234'"; // no }
            }
          });
      fail("Should throw");
    } catch (RuntimeException expected) {
    }
  }

  private static class GoodJSONAble implements JSONAble {

    private final String foo;

    public GoodJSONAble(String foo) {
      this.foo = foo;
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof GoodJSONAble) {
        if (null == foo) {
          return ((GoodJSONAble) o).foo == null;
        } else {
          return foo.equals(((GoodJSONAble) o).foo);
        }
      }

      return false;
    }

    @Override
    public int hashCode() {
      return 0; // not important.
    }

    public String toString() {
      return "GoodJSONAble: foo=" + foo;
    }

    @Override
    public String toJSONString() {
      if (null != foo) {
        return "{ 'foo':'" + foo + "' }";
      } else {
        return "{ 'foo': null }";
      }
    }

    private static final JSONAble.DeJSONFactory DeJSONizer =
        new JSONAble.DeJSONFactory() {
          @Override
          public Object attemptDeJSONize(Map<String, Object> obj) {
            if (obj.size() == 1) {
              Object maybeFoo = obj.get("foo");
              if (maybeFoo instanceof String) {
                return new GoodJSONAble((String) maybeFoo);
              } else if (maybeFoo == JSONObject.NULL) {
                return new GoodJSONAble(null);
              }
            }
            return null;
          }
        };
  }
}
