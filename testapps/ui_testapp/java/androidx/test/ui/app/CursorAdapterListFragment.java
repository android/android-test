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

package androidx.test.ui.app;

import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import androidx.fragment.app.ListFragment;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import com.google.common.annotations.VisibleForTesting;

/**
 * A {@link ListFragment} which uses a {@link CursorAdapter} as a data source.
 */
public class CursorAdapterListFragment extends ListFragment {

  private static final String COLUMN_ID = "_id";
  @VisibleForTesting
  public static final String COLUMN_STR = "column_str";
  @VisibleForTesting
  public static final String COLUMN_LEN = "column_length";
  private static final String[] COLUMN_NAMES = new String[] {
      COLUMN_ID, COLUMN_STR, COLUMN_LEN
  };

  private MatrixCursor listItemCursor;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    listItemCursor = new MatrixCursor(COLUMN_NAMES);
    populateData();

    // Throw in a row with different column names... for the sake of science (testing).
    MatrixCursor surprise =
        new MatrixCursor(new String[] {"surprise!", "columns", "are different"});
    surprise.addRow(new Object[] {1, 2, 3});
    MergeCursor mergeCursor = new MergeCursor(new MatrixCursor[] {listItemCursor, surprise});

    final ListAdapter cursorAdapter = new SimpleCursorAdapter(getActivity(),
        android.R.layout.simple_list_item_2, mergeCursor,
        new String[] {
          COLUMN_STR, COLUMN_LEN
        },
        new int[] {
            android.R.id.text1, android.R.id.text2
        }, 0);
    setListAdapter(cursorAdapter);
  }

  @Override
  public void onListItemClick(ListView listView, View view, int position, long id) {
    TextView selectedItemValue = (TextView) getActivity().findViewById(R.id.selected_item_value);
    selectedItemValue.setText("item: " + position);
  }

  private static Object[] makeItem(int forRow) {
    return new Object[] {
        forRow, "item: " + forRow, ("item: " + forRow).length()
    };
  }

  private void populateData() {
    for (int i = 0; i < 20; i++) {
      listItemCursor.addRow(makeItem(i));
    }
  }
}
