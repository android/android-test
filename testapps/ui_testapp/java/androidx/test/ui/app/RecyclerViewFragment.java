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

import static com.google.common.base.Preconditions.checkNotNull;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener;
import 	androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.common.collect.Lists;
import java.util.List;

/**
 * Fragment which hosts a {@link androidx.recyclerview.widget.RecyclerView}. The {@link
 * androidx.recyclerview.widget.RecyclerView.LayoutManager} can be specified using {@link
 * androidx.test.ui.app.RecyclerViewFragment.LayoutManagerType}
 */
public class RecyclerViewFragment extends Fragment implements OnItemTouchListener {

  private static final String KEY_EXTRA_LAYOUT_MANAGER_TYPE = "LayoutManagerType";

  private static final int MAX_ITEMS = 1000;

  private TextView selectedItemView;
  private RecyclerView recyclerView;

  private GestureDetectorCompat gestureDetector;
  private LayoutManager layoutManager;
  private ItemListAdapter adapter;

  /**
   * The type of {@link androidx.recyclerview.widget.RecyclerView.LayoutManager} that will be used to
   * render the content of the {@link androidx.recyclerview.widget.RecyclerView}
   */
  public enum LayoutManagerType {
    LINEAR(R.layout.rv_llm_fragment, R.id.rv_llm_item_list, R.id.rv_llm_selected_item),
    GRID(R.layout.rv_glm_fragment, R.id.rv_glm_item_list, R.id.rv_glm_selected_item),
    STAGGERED(R.layout.rv_sglm_fragment, R.id.rv_sglm_item_list, R.id.rv_sglm_selected_item);

    private final int layoutId;
    private final int rvId;
    private final int selectedItemId;

    LayoutManagerType(int layoutId, int rvId, int selectedItemId) {
      this.layoutId = layoutId;
      this.rvId = rvId;
      this.selectedItemId = selectedItemId;
    }

    /**
     * @return the layoutId of the RV using the layout
     */
    public int getLayoutId() {
      return layoutId;
    }

    /**
     * @return the id of the RecylcerView in layoutId
     */
    public int getRVId() {
      return rvId;
    }

    /**
     * @return the id of the selected item TextView in layoutId
     */
    public int getSelectedItemId() {
      return selectedItemId;
    }
  }

  ;

  /**
   * Factory method to create an instance of
   * {@link androidx.test.ui.app.RecyclerViewFragment}
   * with a particular {@link androidx.recyclerview.widget.RecyclerView.LayoutManager}
   *
   * @param layoutManagerType the layout manager to render the content
   * @return fragment instance
   */
  public static RecyclerViewFragment newInstance(LayoutManagerType layoutManagerType) {
    checkNotNull(layoutManagerType, "layoutManagerType cannot be null!");
    RecyclerViewFragment recyclerViewFragment = new RecyclerViewFragment();

    Bundle args = new Bundle();
    args.putSerializable(KEY_EXTRA_LAYOUT_MANAGER_TYPE, layoutManagerType);
    recyclerViewFragment.setArguments(args);

    return recyclerViewFragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    final LayoutManagerType layoutManagerType = (LayoutManagerType) getArguments()
        .getSerializable(KEY_EXTRA_LAYOUT_MANAGER_TYPE);

    final View view = inflater
        .inflate(layoutManagerType.getLayoutId(), container, false);

    selectedItemView = (TextView) view.findViewById(layoutManagerType.getSelectedItemId());
    recyclerView = (RecyclerView) view.findViewById(layoutManagerType.getRVId());
    recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
        DividerItemDecoration.VERTICAL_LIST));
    recyclerView.setHasFixedSize(true);
    recyclerView.addOnItemTouchListener(this);
    gestureDetector = new GestureDetectorCompat(getActivity(), new ItemTouchGestureDetector());
    layoutManager = getLayoutManager(layoutManagerType);
    recyclerView.setLayoutManager(layoutManager);

    // Specify an adapter which displays items
    List<String> items = makeItems();
    adapter = ItemListAdapter.newItemListAdapter(items, inflater);
    recyclerView.setAdapter(adapter);
    return view;
  }

  @Override
  public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent event) {
    gestureDetector.onTouchEvent(event);
    return false;
  }

  @Override
  public void onTouchEvent(RecyclerView rv, MotionEvent event) {
    // nothing to do
  }

  // TODO: uncomment once we are passed api 22 @Override
  public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    // nothing to do
  }

  public void addItem(String newItem) {
    if (adapter != null) {
      adapter.addItem(newItem);
    }
  }

  private LayoutManager getLayoutManager(LayoutManagerType layoutManagerType) {
    final LayoutManager layoutManager;
    switch (layoutManagerType) {
      default:
      case LINEAR:
        layoutManager = new LinearLayoutManager(getActivity());
        break;
      case GRID:
        layoutManager = new GridLayoutManager(getActivity(), 2);
        break;
      case STAGGERED:
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        break;
    }
    return layoutManager;
  }

  private class ItemTouchGestureDetector extends SimpleOnGestureListener {

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      return handleClick(e);
    }

    @Override
    public void onLongPress(MotionEvent e) {
      // TODO(b/30140818): Remove this hack, clicks & taps should never turn into long presses
      handleClick(e);
    }

    private boolean handleClick(MotionEvent e) {
      View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
      if (R.id.rv_item_container == childView.getId()) {
        int childPosition = recyclerView.getChildPosition(childView);
        String item = adapter.getItem(childPosition);
        selectedItemView.setText(getString(R.string.selected_item_text, item));
      }
      return false;
    }
  }

  private static List<String> makeItems() {
    List<String> newItemList = Lists.newArrayList();
    for (int i = 0; i < MAX_ITEMS; i++) {
      newItemList.add("Item: " + i);
    }
    return newItemList;
  }
}
