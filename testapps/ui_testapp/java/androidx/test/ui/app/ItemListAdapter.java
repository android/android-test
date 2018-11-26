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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;

import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

/**
 * An {@link androidx.recyclerview.widget.RecyclerView.Adapter} with a list of String items.
 */
public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemViewHolder> {

  private final LayoutInflater inflater;
  private List<String> items;

  public ItemListAdapter(LayoutInflater inflater) {
    this.inflater = inflater;
  }

  public void setItems(List<String> items) {
    this.items = items;
    notifyDataSetChanged();
  }

  @Override
  public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
    View itemsListView = inflater.inflate(R.layout.recyclerview_item, viewGroup, false);
    // Use CustomViewHolder for viewType other than 0.
    ItemViewHolder itemViewHolder =
        viewType == 0 ? new ItemViewHolder(itemsListView) : new CustomViewHolder(itemsListView);
    return itemViewHolder;
  }

  @Override
  public final int getItemViewType(int position) {
    // Use view type 1 for item at 100, 200, 300, .. positions.
    // This is to test View Holder matcher that matches more than one position in the adapter.
    return position != 0 && position % 100 == 0 ? 1 : 0;
  }

  @Override
  public void onBindViewHolder(ItemViewHolder itemHolder, int position) {
    items.get(position);
    itemHolder.textView.setText(items.get(position));
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  /**
   * {@link androidx.recyclerview.widget.RecyclerView.ViewHolder} used for list items.
   */
  public static class ItemViewHolder extends RecyclerView.ViewHolder {

    public TextView textView;

    public ItemViewHolder(View itemsListView) {
      super(itemsListView);
      textView = (TextView) itemsListView.findViewById(R.id.rv_item);
    }
  }

  /**
   * Custom view holder for list items.
   */
  public static class CustomViewHolder extends ItemViewHolder {

    public CustomViewHolder(View itemsListView) {
      super(itemsListView);
    }
  }

  public static ItemListAdapter newItemListAdapter(List<String> items, LayoutInflater inflater) {
    checkArgument(items != null && items.size() > 0, "items nur size must be > 0");
    checkNotNull(inflater, "inflater cannot be null");
    ItemListAdapter taskListAdapter = new ItemListAdapter(inflater);
    taskListAdapter.setItems(items);
    return taskListAdapter;
  }

  public String getItem(int childPosition) {
    checkPositionIndex(childPosition, getItemCount(), "child postion out of bounds");
    return items.get(childPosition);
  }

  public void addItem(String newItem) {
    checkArgument(!TextUtils.isEmpty(newItem), "item cannot be null or empty");
    int lastIndex = items.size();
    items.add(lastIndex, newItem);
    notifyItemInserted(lastIndex);
  }
}
