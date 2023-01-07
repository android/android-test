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
 *
 */
package androidx.test.espresso.remote;

import static androidx.test.espresso.remote.ProtoUtils.capitalizeFirstChar;
import static androidx.test.espresso.remote.ProtoUtils.getFilteredFieldList;
import static kotlin.collections.CollectionsKt.listOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ProtoUtils} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class ProtoUtilsTest {

  @Test
  public void orderedFieldList_returnsOrderListOfFields_ContainsFieldsInOrder()
      throws NoSuchFieldException {
    String field1 = "field1";
    String field2 = "field2";
    String field3 = "field3";
    List<String> filters = listOf(field1, field2, field3);

    List<Field> orderedFieldList = getFilteredFieldList(FieldsFromClass.class, filters);
    assertThat(orderedFieldList.size(), equalTo(3));
    assertThat(orderedFieldList.get(0).getName(), equalTo(field1));
    assertThat(orderedFieldList.get(1).getName(), equalTo(field2));
    assertThat(orderedFieldList.get(2).getName(), equalTo(field3));
  }

  @Test
  public void orderedFieldList_withEmptyFilters_ReturnsEmptyList() throws NoSuchFieldException {
    List<String> filters = listOf();

    List<Field> orderedFieldList = getFilteredFieldList(FieldsFromClass.class, filters);
    assertThat(orderedFieldList.size(), equalTo(0));
  }

  @Test
  public void orderedFieldList_getFieldFromParentClass() throws NoSuchFieldException {
    String superField1 = "superField1";
    List<String> filters = listOf(superField1);

    List<Field> orderedFieldList = getFilteredFieldList(FieldsFromClass.class, filters);
    assertThat(orderedFieldList.size(), equalTo(1));
  }

  @Test
  public void orderedFieldList_getFieldFromClassAndParentClass_ContainsFieldsInOrder()
      throws NoSuchFieldException {
    String superField1 = "superField1";
    String superField2 = "superField2";
    String field1 = "field1";
    String field2 = "field2";
    List<String> filters = listOf(superField1, field1, superField2, field2);

    List<Field> orderedFieldList = getFilteredFieldList(FieldsFromClass.class, filters);
    assertThat(orderedFieldList.size(), equalTo(4));
    assertThat(orderedFieldList.get(0).getName(), equalTo(superField1));
    assertThat(orderedFieldList.get(1).getName(), equalTo(field1));
    assertThat(orderedFieldList.get(2).getName(), equalTo(superField2));
    assertThat(orderedFieldList.get(3).getName(), equalTo(field2));
  }

  @Test
  public void orderedFieldList_nonExistingField_ThrowsNSFE() {
    List<String> filters = listOf("iamNotAField");
    try {
      getFilteredFieldList(FieldsFromClass.class, filters);
    } catch (NoSuchFieldException expected) {
    }
  }

  @Test
  public void lowerCaseFirstCharIsConvertedToUpperCase() {
    String lowerCaseFirstChar = "lowerFirstChar";
    String capitalizedFirstChar = "LowerFirstChar";
    assertThat(capitalizeFirstChar(lowerCaseFirstChar), equalTo(capitalizedFirstChar));
  }

  private static class ParentClass {
    @SuppressWarnings("unused") // used reflectively
    private final int superField1 = 1;

    @SuppressWarnings("unused") // used reflectively
    private final int superField2 = 2;
  }

  private static final class FieldsFromClass extends ParentClass {
    @SuppressWarnings("unused") // used reflectively
    private final int field1 = 1;

    @SuppressWarnings("unused") // used reflectively
    protected final int field2 = 2;

    @SuppressWarnings("unused") // used reflectively
    public final String field3 = "2";

    @SuppressWarnings("unused") // used reflectively
    private final Object field4 = new Object();
  }
}
