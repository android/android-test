/*
 * Copyright (C) 2014 The Android Open Source Project
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

package androidx.test.espresso.matcher;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.is;

import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.widget.AdapterView;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.SelfDescribing;

/**
 * A collection of Hamcrest matchers that matches a data row in a {@link Cursor}. <br>
 *
 * <p>{@link AdapterView}s that are backed by a {@link Cursor} are very common. This class contains
 * <a href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
 * </code></a>s that can be used together with {@link
 * androidx.test.espresso.Espresso#onData(Matcher)} to match a data row in a {@link Cursor}.
 * The <a href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>
 * Matcher</code></a>s can only operate on a single data row of the cursor and Espresso internally
 * moves the {@link Cursor} to the correct adapter position.
 */
public final class CursorMatchers {

  private static final int COLUMN_NOT_FOUND = -1;
  private static final int MULTIPLE_COLUMNS_FOUND = -2;
  private static final int USE_COLUMN_PICKER = -3;

  private CursorMatchers() {
    // no instance
  }

  /** A {@link Matcher} that matches {@link Cursor}s based on values in their columns. */
  public static class CursorMatcher extends BoundedDiagnosingMatcher<Object, Cursor> {

    private final int columnIndex;
    private final Matcher<String> columnNameMatcher;
    private final Matcher<?> valueMatcher;
    private final CursorDataRetriever<?> cursorDataRetriever;
    private boolean checkColumns = false;

    private CursorMatcher(
        int columnIndex, Matcher<?> valueMatcher, CursorDataRetriever<?> cursorDataRetriever) {
      super(Cursor.class);
      checkArgument(columnIndex >= 0);
      this.columnIndex = columnIndex;
      this.valueMatcher = checkNotNull(valueMatcher);
      this.cursorDataRetriever = checkNotNull(cursorDataRetriever);
      this.columnNameMatcher = null;
    }

    private CursorMatcher(
        Matcher<String> columnPicker,
        Matcher<?> valueMatcher,
        CursorDataRetriever<?> cursorDataRetriever) {
      super(Cursor.class);
      this.columnNameMatcher = checkNotNull(columnPicker);
      this.valueMatcher = checkNotNull(valueMatcher);
      this.cursorDataRetriever = checkNotNull(cursorDataRetriever);
      this.columnIndex = USE_COLUMN_PICKER;
    }

    @Override
    protected boolean matchesSafely(Cursor cursor, Description mismatchDescription) {
      int chosenColumn = columnIndex;
      if (chosenColumn < 0) {
        chosenColumn = findColumnIndex(columnNameMatcher, cursor);
        if (chosenColumn < 0) {
          if (chosenColumn == MULTIPLE_COLUMNS_FOUND) {
            mismatchDescription
                .appendText("Multiple columns in ")
                .appendValue(cursor.getColumnNames())
                .appendText(" match ")
                .appendDescriptionOf(columnNameMatcher);
          } else {
            mismatchDescription
                .appendText("Couldn't find column in ")
                .appendValue(cursor.getColumnNames())
                .appendText(" matching ")
                .appendDescriptionOf(columnNameMatcher);
          }
          if (checkColumns) {
            throw new IllegalArgumentException(mismatchDescription.toString());
          }
          return false;
        }
      }
      try {
        Object data = cursorDataRetriever.getData(cursor, chosenColumn);
        boolean result = valueMatcher.matches(data);
        if (!result) {
          mismatchDescription
              .appendText("value at column ")
              .appendValue(chosenColumn)
              .appendText(" ");
          valueMatcher.describeMismatch(data, mismatchDescription);
        }
        return result;
      } catch (CursorIndexOutOfBoundsException e) {
        mismatchDescription
            .appendText("Column index ")
            .appendValue(chosenColumn)
            .appendText(" is invalid");
        if (checkColumns) {
          throw new IllegalArgumentException(mismatchDescription.toString(), e);
        }
        // this cursor position doesn't have a column with this index, but other ones might
        // (e.g. in case of MergeCursor), so if columnChecks are off continue the search.
        return false;
      }
    }

    @Override
    protected void describeMoreTo(Description description) {
      description.appendText("Rows with column: ");
      if (columnIndex < 0) {
        columnNameMatcher.describeTo(description);
      } else {
        description.appendText("index = " + columnIndex);
      }
      description
          .appendText(" ")
          .appendDescriptionOf(cursorDataRetriever)
          .appendText(" matching ")
          .appendDescriptionOf(valueMatcher);
    }

    /**
     * Allows test authors to override whether the the matcher should throw an {@link
     * IllegalArgumentException} if the column name/count is not valid. This is useful in the case
     * where a cursor may iterates over a data set with variable columns. By default, the checks are
     * off.
     */
    public CursorMatcher withStrictColumnChecks(boolean checkColumns) {
      this.checkColumns = checkColumns;
      return this;
    }
  }

  private static int findColumnIndex(Matcher<String> nameMatcher, Cursor cursor) {
    int result = COLUMN_NOT_FOUND;
    String[] columnNames = cursor.getColumnNames();
    for (int i = 0; i < columnNames.length; i++) {
      String column = columnNames[i];
      if (nameMatcher.matches(column)) {
        if (result == COLUMN_NOT_FOUND) {
          result = i;
        } else {
          result = MULTIPLE_COLUMNS_FOUND;
          break;
        }
      }
    }
    return result;
  }

  private interface CursorDataRetriever<T> extends SelfDescribing {
    T getData(Cursor cursor, int chosenColumn);
  }

  private static final CursorDataRetriever<byte[]> BLOB_MATCHER_APPLIER =
      new CursorDataRetriever<byte[]>() {
        @Override
        public void describeTo(Description description) {
          description.appendText("with Blob");
        }

        @Override
        public byte[] getData(Cursor cursor, int chosenColumn) {
          return cursor.getBlob(chosenColumn);
        }
      };

  private static final CursorDataRetriever<Long> LONG_MATCHER_APPLIER =
      new CursorDataRetriever<Long>() {
        @Override
        public void describeTo(Description description) {
          description.appendText("with Long");
        }

        @Override
        public Long getData(Cursor cursor, int chosenColumn) {
          return cursor.getLong(chosenColumn);
        }
      };

  private static final CursorDataRetriever<Short> SHORT_MATCHER_APPLIER =
      new CursorDataRetriever<Short>() {
        @Override
        public void describeTo(Description description) {
          description.appendText("with Short");
        }

        @Override
        public Short getData(Cursor cursor, int chosenColumn) {
          return cursor.getShort(chosenColumn);
        }
      };

  private static final CursorDataRetriever<Integer> INT_MATCHER_APPLIER =
      new CursorDataRetriever<Integer>() {
        @Override
        public void describeTo(Description description) {
          description.appendText("with Int");
        }

        @Override
        public Integer getData(Cursor cursor, int chosenColumn) {
          return cursor.getInt(chosenColumn);
        }
      };

  private static final CursorDataRetriever<Float> FLOAT_MATCHER_APPLIER =
      new CursorDataRetriever<Float>() {
        @Override
        public void describeTo(Description description) {
          description.appendText("with Float");
        }

        @Override
        public Float getData(Cursor cursor, int chosenColumn) {
          return cursor.getFloat(chosenColumn);
        }
      };

  private static final CursorDataRetriever<Double> DOUBLE_MATCHER_APPLIER =
      new CursorDataRetriever<Double>() {
        @Override
        public void describeTo(Description description) {
          description.appendText("with Double");
        }

        @Override
        public Double getData(Cursor cursor, int chosenColumn) {
          return cursor.getDouble(chosenColumn);
        }
      };

  private static final CursorDataRetriever<String> STRING_MATCHER_APPLIER =
      new CursorDataRetriever<String>() {
        @Override
        public void describeTo(Description description) {
          description.appendText("with String");
        }

        @Override
        public String getData(Cursor cursor, int chosenColumn) {
          return cursor.getString(chosenColumn);
        }
      };

  /**
   * Returns a matcher that matches a {@link Short} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnIndex int column index
   * @param value a short value to match
   */
  public static CursorMatcher withRowShort(int columnIndex, short value) {
    return withRowShort(columnIndex, is(value));
  }

  /**
   * Returns a matcher that matches a {@link Short} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnIndex int column index
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a {@link Short} value
   */
  public static CursorMatcher withRowShort(int columnIndex, Matcher<Short> valueMatcher) {
    return new CursorMatcher(columnIndex, valueMatcher, SHORT_MATCHER_APPLIER);
  }

  /**
   * Returns a matcher that matches a {@link Short} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnName as a {@link String}
   * @param value a short value to match
   */
  public static CursorMatcher withRowShort(String columnName, short value) {
    return withRowShort(columnName, is(value));
  }

  /**
   * Returns a matcher that matches a {@link Short} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnName as a {@link String}
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a {@link Short} value
   */
  public static CursorMatcher withRowShort(String columnName, Matcher<Short> valueMatcher) {
    return withRowShort(is(columnName), valueMatcher);
  }

  /**
   * Returns a matcher that matches a {@link Short} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnNameMatcher as a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> of {@link String}
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a {@link Short} value
   */
  public static CursorMatcher withRowShort(
      Matcher<String> columnNameMatcher, Matcher<Short> valueMatcher) {
    return new CursorMatcher(columnNameMatcher, valueMatcher, SHORT_MATCHER_APPLIER);
  }

  /**
   * Returns a matcher that matches a {@link Integer} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnIndex int column index
   * @param value a int value to match
   */
  public static CursorMatcher withRowInt(int columnIndex, int value) {
    return withRowInt(columnIndex, is(value));
  }

  /**
   * Returns a matcher that matches a {@link Integer} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnIndex int column index
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a {@link Integer} value
   */
  public static CursorMatcher withRowInt(int columnIndex, Matcher<Integer> valueMatcher) {
    return new CursorMatcher(columnIndex, valueMatcher, INT_MATCHER_APPLIER);
  }

  /**
   * Returns a matcher that matches a {@link Integer} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnName as a {@link String}
   * @param value a int value to match
   */
  public static CursorMatcher withRowInt(String columnName, int value) {
    return withRowInt(columnName, is(value));
  }

  /**
   * Returns a matcher that matches a {@link Integer} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnName as a {@link String}
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a {@link Integer} value
   */
  public static CursorMatcher withRowInt(String columnName, Matcher<Integer> valueMatcher) {
    return withRowInt(is(columnName), valueMatcher);
  }

  /**
   * Returns a matcher that matches a {@link Integer} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnNameMatcher as a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> of {@link String}
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a {@link Integer} value
   */
  public static CursorMatcher withRowInt(
      Matcher<String> columnNameMatcher, final Matcher<Integer> valueMatcher) {
    return new CursorMatcher(columnNameMatcher, valueMatcher, INT_MATCHER_APPLIER);
  }

  /**
   * Returns a matcher that matches a {@link Long} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnIndex int column index
   * @param value a long value to match
   */
  public static CursorMatcher withRowLong(int columnIndex, long value) {
    return withRowLong(columnIndex, is(value));
  }

  /**
   * Returns a matcher that matches a {@link Long} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnIndex int column index
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a {@link Long} value
   */
  public static CursorMatcher withRowLong(int columnIndex, Matcher<Long> valueMatcher) {
    return new CursorMatcher(columnIndex, valueMatcher, LONG_MATCHER_APPLIER);
  }

  /**
   * Returns a matcher that matches a {@link Long} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnName as a {@link String}
   * @param value a long value to match
   */
  public static CursorMatcher withRowLong(String columnName, long value) {
    return withRowLong(columnName, is(value));
  }

  /**
   * Returns a matcher that matches a {@link Long} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnName as a {@link String}
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a {@link Long} value
   */
  public static CursorMatcher withRowLong(String columnName, Matcher<Long> valueMatcher) {
    return withRowLong(is(columnName), valueMatcher);
  }

  /**
   * Returns a matcher that matches a {@link Long} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnNameMatcher as a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> of {@link String}
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a {@link Long} value
   */
  public static CursorMatcher withRowLong(
      Matcher<String> columnNameMatcher, Matcher<Long> valueMatcher) {
    return new CursorMatcher(columnNameMatcher, valueMatcher, LONG_MATCHER_APPLIER);
  }

  /**
   * Returns a matcher that matches a {@link Float} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnIndex int column index
   * @param value a float value to match
   */
  public static CursorMatcher withRowFloat(int columnIndex, float value) {
    return withRowFloat(columnIndex, is(value));
  }

  /**
   * Returns a matcher that matches a {@link Float} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnIndex int column index
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a {@link Float} value
   */
  public static CursorMatcher withRowFloat(int columnIndex, Matcher<Float> valueMatcher) {
    return new CursorMatcher(columnIndex, valueMatcher, FLOAT_MATCHER_APPLIER);
  }

  /**
   * Returns a matcher that matches a {@link Float} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnName as a {@link String}
   * @param value a float value to match
   */
  public static CursorMatcher withRowFloat(String columnName, float value) {
    return withRowFloat(columnName, is(value));
  }

  /**
   * Returns a matcher that matches a {@link Float} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnName as a {@link String}
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a {@link Float} value
   */
  public static CursorMatcher withRowFloat(String columnName, Matcher<Float> valueMatcher) {
    return withRowFloat(is(columnName), valueMatcher);
  }

  /**
   * Returns a matcher that matches a {@link Float} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnNameMatcher as a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> of {@link String}
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a {@link Float} value
   */
  public static CursorMatcher withRowFloat(
      Matcher<String> columnNameMatcher, Matcher<Float> valueMatcher) {
    return new CursorMatcher(columnNameMatcher, valueMatcher, FLOAT_MATCHER_APPLIER);
  }

  /**
   * Returns a matcher that matches a {@link Double} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnIndex int column index
   * @param value a double value to match
   */
  public static CursorMatcher withRowDouble(int columnIndex, double value) {
    return withRowDouble(columnIndex, is(value));
  }

  /**
   * Returns a matcher that matches a {@link Double} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnIndex int column index
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a {@link Double} value
   */
  public static CursorMatcher withRowDouble(int columnIndex, Matcher<Double> valueMatcher) {
    return new CursorMatcher(columnIndex, valueMatcher, DOUBLE_MATCHER_APPLIER);
  }

  /**
   * Returns a matcher that matches a {@link Double} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnName as a {@link String}
   * @param value a double value to match
   */
  public static CursorMatcher withRowDouble(String columnName, double value) {
    return withRowDouble(columnName, is(value));
  }

  /**
   * Returns a matcher that matches a {@link Double} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnName as a {@link String}
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a {@link Double} value
   */
  public static CursorMatcher withRowDouble(String columnName, Matcher<Double> valueMatcher) {
    return withRowDouble(is(columnName), valueMatcher);
  }

  /**
   * Returns a matcher that matches a {@link Double} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnNameMatcher as a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> of {@link String}
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a {@link Double} value
   */
  public static CursorMatcher withRowDouble(
      Matcher<String> columnNameMatcher, Matcher<Double> valueMatcher) {
    return new CursorMatcher(columnNameMatcher, valueMatcher, DOUBLE_MATCHER_APPLIER);
  }

  /**
   * Returns a matcher that matches a {@link String} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnIndex int column index
   * @param value a {@link String} value to match
   */
  public static CursorMatcher withRowString(int columnIndex, String value) {
    return withRowString(columnIndex, is(value));
  }

  /**
   * Returns a matcher that matches a {@link String} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnIndex int column index
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a {@link String} value
   */
  public static CursorMatcher withRowString(int columnIndex, Matcher<String> valueMatcher) {
    return new CursorMatcher(columnIndex, valueMatcher, STRING_MATCHER_APPLIER);
  }

  /**
   * Returns a matcher that matches a {@link String} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnName as a {@link String}
   * @param value a {@link String} value to match
   */
  public static CursorMatcher withRowString(String columnName, String value) {
    return withRowString(is(columnName), is(value));
  }

  /**
   * Returns a matcher that matches a {@link String} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnName as a {@link String}
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a {@link String} value
   */
  public static CursorMatcher withRowString(String columnName, Matcher<String> valueMatcher) {
    return withRowString(is(columnName), valueMatcher);
  }

  /**
   * Returns a matcher that matches a {@link String} value at a given column index in a {@link
   * Cursor}s data row. <br>
   *
   * @param columnPicker as a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> of {@link String}
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a {@link String} value
   */
  public static CursorMatcher withRowString(
      final Matcher<String> columnPicker, final Matcher<String> valueMatcher) {
    return new CursorMatcher(columnPicker, valueMatcher, STRING_MATCHER_APPLIER);
  }

  /**
   * Returns a matcher that matches a byte[] at a given column index in a {@link Cursor}s data row.
   * <br>
   *
   * @param columnIndex int column index
   * @param value byte[] to match
   */
  public static CursorMatcher withRowBlob(int columnIndex, byte[] value) {
    return withRowBlob(columnIndex, is(value));
  }

  /**
   * Returns a matcher that matches a byte[] at a given column index in a {@link Cursor}s data row.
   * <br>
   *
   * @param columnIndex int column index
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a byte[]
   */
  public static CursorMatcher withRowBlob(int columnIndex, Matcher<byte[]> valueMatcher) {
    return new CursorMatcher(columnIndex, valueMatcher, BLOB_MATCHER_APPLIER);
  }

  /**
   * Returns a matcher that matches a byte[] at a given column index in a {@link Cursor}s data row.
   * <br>
   *
   * @param columnName as a {@link String}
   * @param value byte[] to match
   */
  public static CursorMatcher withRowBlob(String columnName, byte[] value) {
    return withRowBlob(is(columnName), is(value));
  }

  /**
   * Returns a matcher that matches a byte[] at a given column index in a {@link Cursor}s data row.
   * <br>
   *
   * @param columnName as a {@link String}
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a byte[]
   */
  public static CursorMatcher withRowBlob(String columnName, Matcher<byte[]> valueMatcher) {
    return withRowBlob(is(columnName), valueMatcher);
  }

  /**
   * Returns a matcher that matches a byte[] at a given column index in a {@link Cursor}s data row.
   * <br>
   *
   * @param columnPicker as a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> of {@link String}
   * @param valueMatcher a <a
   *     href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html"><code>Matcher
   *     </code></a> that matches a byte[]
   */
  public static CursorMatcher withRowBlob(
      Matcher<String> columnPicker, Matcher<byte[]> valueMatcher) {
    return new CursorMatcher(columnPicker, valueMatcher, BLOB_MATCHER_APPLIER);
  }
}
