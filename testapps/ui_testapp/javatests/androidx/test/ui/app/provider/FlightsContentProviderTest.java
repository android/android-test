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

package androidx.test.ui.app.provider;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import androidx.test.filters.SmallTest;
import androidx.test.rule.provider.ProviderTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.ui.app.provider.FlightsDatabaseContract.FlightsColumns;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/**
 * Unit tests for {@link FlightsContentProvider} using {@link ProviderTestRule}.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class FlightsContentProviderTest {

  private static final String AUTHORITY = FlightsContentProvider.AUTHORITY;

  private ContentResolver testResolver;
  private ContentValues cvUnited1;
  private ContentValues cvAmerican;
  private ContentValues cvUnited2;

  private static final Uri URI_DIR = Uri.EMPTY.buildUpon().scheme("content")
      .authority(AUTHORITY).path(FlightsDatabaseContract.PATH).build();
  private static final Uri URI_ITEM = Uri.withAppendedPath(Uri.EMPTY.buildUpon().scheme("content")
      .authority(AUTHORITY).path(FlightsDatabaseContract.PATH).build(), "2");
  private static final Uri URI_INVALID = Uri.EMPTY.buildUpon().scheme("content")
      .authority(AUTHORITY).path("invalid").build();

  @Rule
  public ProviderTestRule providerRule =
      new ProviderTestRule.Builder(FlightsContentProvider.class, AUTHORITY).build();

  @Rule
  public final ExpectedException expected = ExpectedException.none();

  @Before
  public void init() {
    testResolver = providerRule.getResolver();

    cvUnited1 = new ContentValues();
    cvUnited1.put(FlightsColumns.FLIGHT_TIME, 1469502629418L);
    cvUnited1.put(FlightsColumns.FLIGHT_AIRLINE, "United");
    cvUnited1.put(FlightsColumns.FLIGHT_NUMBER, 12);
    cvUnited1.put(FlightsColumns.FLIGHT_CUSTOMER, "Y");
    cvUnited1.put(FlightsColumns.FLIGHT_SOURCE, "DCA");
    cvUnited1.put(FlightsColumns.FLIGHT_DESTINATION, "ORD");

    cvAmerican = new ContentValues();
    cvAmerican.put(FlightsColumns.FLIGHT_TIME, 1470374129880L);
    cvAmerican.put(FlightsColumns.FLIGHT_AIRLINE, "American");
    cvAmerican.put(FlightsColumns.FLIGHT_NUMBER, 42);
    cvAmerican.put(FlightsColumns.FLIGHT_CUSTOMER, "S");
    cvAmerican.put(FlightsColumns.FLIGHT_SOURCE, "ORD");
    cvAmerican.put(FlightsColumns.FLIGHT_DESTINATION, "SFO");

    cvUnited2 = new ContentValues();
    cvUnited2.put(FlightsColumns.FLIGHT_TIME, 1470374529880L);
    cvUnited2.put(FlightsColumns.FLIGHT_AIRLINE, "United");
    cvUnited2.put(FlightsColumns.FLIGHT_NUMBER, 380);
    cvUnited2.put(FlightsColumns.FLIGHT_CUSTOMER, "Y");
    cvUnited2.put(FlightsColumns.FLIGHT_SOURCE, "SFO");
    cvUnited2.put(FlightsColumns.FLIGHT_DESTINATION, "IAD");
  }

  @Test
  public void verifyGetTypeForUrisAreCorrect() {
    assertEquals(testResolver.getType(URI_DIR), FlightsContentProvider.TYPE_FLIGHT_DIR);
    assertEquals(testResolver.getType(URI_ITEM), FlightsContentProvider.TYPE_FLIGHT_ITEM);
    assertNull(testResolver.getType(URI_INVALID));
  }

  @Test
  public void verifyThreeEntriesInsertedAndQueriedCorrectly() {
    // Firstly no entry exists
    assertAirlineEntryCount("United", 0);
    assertAirlineEntryCount("American", 0);

    // Insert 3 entries including 2 United and 1 American.
    assertNotNull(testResolver.insert(URI_DIR, cvUnited1));
    assertNotNull(testResolver.insert(URI_DIR, cvAmerican));
    assertNotNull(testResolver.insert(URI_DIR, cvUnited2));

    assertAirlineEntryCount("United", 2);
    assertAirlineEntryCount("American", 1);
  }

  @Test
  public void verifyInsertionWithImproperUrlThrowsException() {
    expected.expect(IllegalArgumentException.class);
    testResolver.insert(URI_ITEM, cvUnited1);
    expected.expect(IllegalArgumentException.class);
    testResolver.insert(URI_INVALID, cvUnited1);
  }

  @Test
  public void verifyDeleteFlightEntries() {
    // Insert 2 United entries;
    assertNotNull(testResolver.insert(URI_DIR, cvUnited1));
    assertNotNull(testResolver.insert(URI_DIR, cvUnited2));
    // Delete United entries
    String where = FlightsColumns.FLIGHT_AIRLINE + "=?";
    String[] args = {"United"};
    assertEquals(2, testResolver.delete(URI_DIR, where, args));
    // No more "United" remains
    assertAirlineEntryCount("United", 0);
  }

  private void assertAirlineEntryCount(String airline, int expectedCount) {
    String where = FlightsColumns.FLIGHT_AIRLINE + "=?";
    String[] args = {airline};

    Cursor c = null;
    try {
      c = testResolver.query(URI_DIR, null, where, args, null);
      assertNotNull(c);
      assertEquals(expectedCount, c.getCount());
    } finally {
      if (c != null && !c.isClosed()) {
        c.close();
      }
    }
  }
}
