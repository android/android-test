/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.test.ext.truth.os;

import static androidx.test.ext.truth.os.PersistableBundleSubject.assertThat;

import android.os.PersistableBundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test for {@link PersistableBundleSubject}. */
@RunWith(AndroidJUnit4.class)
public class PersistableBundleSubjectTest {

  private static final String NULL_KEY = "Null";
  private static final String BOOLEAN_KEY = "Boolean";
  private static final String DOUBLE_KEY = "Double";
  private static final String INT_KEY = "Int";
  private static final String LONG_KEY = "Long";
  private static final String STRING_KEY = "String";
  private static final String BOOLEAN_ARRAY_KEY = "BooleanArray";
  private static final String DOUBLE_ARRAY_KEY = "DoubleArray";
  private static final String INT_ARRAY_KEY = "IntArray";
  private static final String LONG_ARRAY_KEY = "LongArray";
  private static final String PERSISTABLE_BUNDLE_KEY = "PersistableBundle";
  private static final String STRING_ARRAY_KEY = "StringArray";

  private static final boolean BOOLEAN_VALUE = true;
  private static final double DOUBLE_VALUE = 1.0;
  private static final int INT_VALUE = 1;
  private static final long LONG_VALUE = 1L;
  private static final String STRING_VALUE = "bar";

  // Be weary of reference equality here. getTestBundle() uses #clone to perform a shallow copy.
  private static final boolean[] booleanArrayValue = new boolean[] {true, false};
  private static final double[] doubleArrayValue = new double[] {1.0, 2.0, 3.0};
  private static final int[] intArrayValue = new int[] {1, 2, 3};
  private static final long[] longArrayValue = new long[] {1L, 2L, 3L};
  private static final String[] stringArrayValue = new String[] {"bar", "baz"};

  // List values: Due to lack of autoboxing of primitive arrays, we define an equivalent list for
  // comparison using the array subjects. This isn't required for double array value since it has a
  // PrimitiveDoubleArraySubject.
  private static final List<Boolean> booleanArrayListValue = Arrays.asList(true, false);
  private static final List<Integer> intArrayListValue = Arrays.asList(1, 2, 3);
  private static final List<Long> longArrayListValue = Arrays.asList(1L, 2L, 3L);

  @Test
  public void isEmpty() {
    PersistableBundle bundle = new PersistableBundle();
    assertThat(bundle).isEmpty();
    bundle.putString(STRING_KEY, STRING_VALUE);

    assertThat(bundle).isNotEmpty();
  }

  @Test
  public void hasSize() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putString(STRING_KEY, STRING_VALUE);

    assertThat(bundle).hasSize(1);
  }

  @Test
  public void containsKey() {
    PersistableBundle bundle = new PersistableBundle();
    assertThat(bundle).doesNotContainKey(STRING_KEY);
    bundle.putString(STRING_KEY, STRING_VALUE);

    assertThat(bundle).containsKey(STRING_KEY);
  }

  @Test
  public void isEqualTo() {
    // Test all possible values.
    PersistableBundle bundle = getTestBundle();
    PersistableBundle bundle2 = getTestBundle();

    assertThat(bundle).isEqualTo(bundle2);
  }

  @Test
  public void isEqualTo_nullBundles() {
    PersistableBundle bundle = null;
    PersistableBundle bundle2 = null;

    assertThat(bundle).isEqualTo(bundle2);
  }

  @Test
  public void isEqualTo_emptyBundles() {
    assertThat(new PersistableBundle()).isEqualTo(new PersistableBundle());
  }

  @Test
  public void isNotEqualTo_nullBundle() {
    PersistableBundle nullBundle = null;

    assertThat(getTestBundle()).isNotEqualTo(nullBundle);
    // Test the inverse as well.
    assertThat(nullBundle).isNotEqualTo(getTestBundle());
  }

  @Test
  public void isNotEqualTo_sameSize_differentKeys() {
    PersistableBundle bundle = getTestBundle();
    bundle.putString(STRING_KEY + "2", STRING_VALUE);

    assertThat(bundle).isNotEqualTo(getTestBundle());
  }

  @Test
  public void isNotEqualTo_differentSize() {
    PersistableBundle bundle = getTestBundle();
    bundle.remove(STRING_KEY);

    assertThat(bundle).isNotEqualTo(getTestBundle());
  }

  @Test
  public void isNotEqualTo_differentType_oneIsPersistableBundle() {
    PersistableBundle bundle = getTestBundle();
    bundle.putString(PERSISTABLE_BUNDLE_KEY, STRING_VALUE);

    assertThat(bundle).isNotEqualTo(getTestBundle());
  }

  @Test
  public void isNotEqualTo_differentType_otherType() {
    PersistableBundle bundle = getTestBundle();
    bundle.putBoolean(STRING_KEY, false);

    assertThat(bundle).isNotEqualTo(getTestBundle());
  }

  @Test
  public void isNotEqualTo_differentBooleanValue() {
    PersistableBundle bundle = getTestBundle();
    // Original value is true.
    bundle.putBoolean(BOOLEAN_KEY, false);

    assertThat(bundle).isNotEqualTo(getTestBundle());
  }

  @Test
  public void isNotEqualTo_differentIntValue() {
    PersistableBundle bundle = getTestBundle();
    bundle.putInt(INT_KEY, INT_VALUE * 2);

    assertThat(bundle).isNotEqualTo(getTestBundle());
  }

  @Test
  public void isNotEqualTo_differentDoubleValue() {
    PersistableBundle bundle = getTestBundle();
    bundle.putDouble(DOUBLE_KEY, DOUBLE_VALUE * 2.0);

    assertThat(bundle).isNotEqualTo(getTestBundle());
  }

  @Test
  public void isNotEqualTo_differentLongValue() {
    PersistableBundle bundle = getTestBundle();
    bundle.putLong(LONG_KEY, LONG_VALUE * 2L);

    assertThat(bundle).isNotEqualTo(getTestBundle());
  }

  @Test
  public void isNotEqualTo_differentStringValue() {
    PersistableBundle bundle = getTestBundle();
    bundle.putString(STRING_KEY, STRING_VALUE + "baz");

    assertThat(bundle).isNotEqualTo(getTestBundle());
  }

  @Test
  public void isNotEqualTo_differentBooleanArrayValue() {
    PersistableBundle bundle = getTestBundle();
    // Original value is {true, false}.
    bundle.putBooleanArray(BOOLEAN_ARRAY_KEY, new boolean[] {false, true});

    assertThat(bundle).isNotEqualTo(getTestBundle());
  }

  @Test
  public void isNotEqualTo_differentDoubleArrayValue() {
    PersistableBundle bundle = getTestBundle();
    // Original value is {1.0, 2.0, 3.0}.
    bundle.putDoubleArray(DOUBLE_ARRAY_KEY, new double[] {3.0, 2.0, 1.0});

    assertThat(bundle).isNotEqualTo(getTestBundle());
  }

  @Test
  public void isNotEqualTo_differentIntArrayValue() {
    PersistableBundle bundle = getTestBundle();
    // Original value is {1, 2, 3}.
    bundle.putIntArray(INT_ARRAY_KEY, new int[] {3, 2, 1});

    assertThat(bundle).isNotEqualTo(getTestBundle());
  }

  @Test
  public void isNotEqualTo_differentLongArrayValue() {
    PersistableBundle bundle = getTestBundle();
    // Original value is {1, 2, 3}.
    bundle.putLongArray(LONG_ARRAY_KEY, new long[] {3L, 2L, 1L});

    assertThat(bundle).isNotEqualTo(getTestBundle());
  }

  @Test
  public void isNotEqualTo_differentNestedPersistableBundleValue() {
    PersistableBundle bundle = getTestBundle();
    PersistableBundle innerBundle = new PersistableBundle();
    innerBundle.putString(STRING_KEY, STRING_VALUE + "baz");
    bundle.putPersistableBundle(PERSISTABLE_BUNDLE_KEY, innerBundle);

    assertThat(bundle).isNotEqualTo(getTestBundle());
  }

  @Test
  public void isNotEqualTo_differentStringArrayValue() {
    PersistableBundle bundle = getTestBundle();
    // Original value is {"bar", "baz"}.
    bundle.putStringArray(STRING_ARRAY_KEY, new String[] {"foo", "bar"});

    assertThat(bundle).isNotEqualTo(getTestBundle());
  }

  @Test
  public void string() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putString(STRING_KEY, STRING_VALUE);

    assertThat(bundle).string(STRING_KEY).isEqualTo(STRING_VALUE);
  }

  @Test
  public void integer() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putInt(INT_KEY, INT_VALUE);

    assertThat(bundle).integer(INT_KEY).isEqualTo(INT_VALUE);
  }

  @Test
  public void longInt() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putLong(LONG_KEY, LONG_VALUE);

    assertThat(bundle).longInt(LONG_KEY).isEqualTo(LONG_VALUE);
  }

  @Test
  public void doubleFloat() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putDouble(DOUBLE_KEY, DOUBLE_VALUE);

    assertThat(bundle).doubleFloat(DOUBLE_KEY).isEqualTo(DOUBLE_VALUE);
  }

  @Test
  public void bool() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putBoolean(BOOLEAN_KEY, BOOLEAN_VALUE);

    assertThat(bundle).bool(BOOLEAN_KEY).isTrue();
  }

  @Test
  public void booleanArray() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putBooleanArray(BOOLEAN_ARRAY_KEY, booleanArrayValue);

    assertThat(bundle)
        .booleanArray(BOOLEAN_ARRAY_KEY)
        .asList()
        .containsExactlyElementsIn(booleanArrayListValue)
        .inOrder();
  }

  @Test
  public void intArray() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putIntArray(INT_ARRAY_KEY, intArrayValue);

    assertThat(bundle)
        .intArray(INT_ARRAY_KEY)
        .asList()
        .containsExactlyElementsIn(intArrayListValue)
        .inOrder();
  }

  @Test
  public void longArray() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putLongArray(LONG_ARRAY_KEY, longArrayValue);

    assertThat(bundle)
        .longArray(LONG_ARRAY_KEY)
        .asList()
        .containsExactlyElementsIn(longArrayListValue)
        .inOrder();
  }

  @Test
  public void doubleArray() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putDoubleArray(DOUBLE_ARRAY_KEY, doubleArrayValue);

    assertThat(bundle)
        .doubleArray(DOUBLE_ARRAY_KEY)
        .usingExactEquality()
        .containsExactly(doubleArrayValue)
        .inOrder();
  }

  @Test
  public void stringArray() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putStringArray(STRING_ARRAY_KEY, stringArrayValue);

    List<String> expectedList = Arrays.asList(stringArrayValue);
    assertThat(bundle)
        .stringArray(STRING_ARRAY_KEY)
        .asList()
        .containsExactlyElementsIn(expectedList)
        .inOrder();
  }

  @Test
  public void persistableBundle() {
    PersistableBundle bundle = new PersistableBundle();
    PersistableBundle innerBundle = new PersistableBundle();
    innerBundle.putString(STRING_KEY, STRING_VALUE);
    bundle.putPersistableBundle(PERSISTABLE_BUNDLE_KEY, innerBundle);

    assertThat(bundle).persistableBundle("invalid").isNull();
    assertThat(bundle).persistableBundle(PERSISTABLE_BUNDLE_KEY).isNotNull();
    assertThat(bundle).persistableBundle(PERSISTABLE_BUNDLE_KEY).hasSize(1);
    assertThat(bundle).persistableBundle(PERSISTABLE_BUNDLE_KEY).containsKey(STRING_KEY);
  }

  private PersistableBundle getTestBundle() {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putString(NULL_KEY, null);
    bundle.putBoolean(BOOLEAN_KEY, BOOLEAN_VALUE);
    bundle.putDouble(DOUBLE_KEY, DOUBLE_VALUE);
    bundle.putInt(INT_KEY, INT_VALUE);
    bundle.putLong(LONG_KEY, LONG_VALUE);
    bundle.putString(STRING_KEY, STRING_VALUE);
    // Perform a shallow copy with clone to avoid reference equality. The underlying types support
    // content equals just fine, so we don't have to worry about them being the same reference.
    bundle.putBooleanArray(BOOLEAN_ARRAY_KEY, booleanArrayValue.clone());
    bundle.putDoubleArray(DOUBLE_ARRAY_KEY, doubleArrayValue.clone());
    bundle.putIntArray(INT_ARRAY_KEY, intArrayValue.clone());
    bundle.putLongArray(LONG_ARRAY_KEY, longArrayValue.clone());
    bundle.putStringArray(STRING_ARRAY_KEY, stringArrayValue.clone());
    PersistableBundle innerBundle = new PersistableBundle();
    innerBundle.putString(STRING_KEY, STRING_VALUE);
    bundle.putPersistableBundle(PERSISTABLE_BUNDLE_KEY, innerBundle);
    return bundle;
  }
}
