/*
 * Copyright (C) 2014 The Android Open Source Project
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

package androidx.test.ui.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

/**
 * Our main Activity in this sample. Displays a transition_grid of items whith an image and title.
 * When the user clicks on an item, {@link TransitionDetailActivity} is launched, using the Activity
 * Scene Transitions framework to animatedly do so.
 */
public class TransitionActivityMain extends Activity
    implements AdapterView.OnItemClickListener {

    // Controls back button behavior.
    private boolean mExitOnBackPressed = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.transition_grid);

      // Setup the GridView and set the adapter
      GridView mGridView = (GridView) findViewById(R.id.grid);
      mGridView.setOnItemClickListener(this);
      GridAdapter mAdapter = new GridAdapter();
      mGridView.setAdapter(mAdapter);
  }

  /**
   * Called when an item in the {@link android.widget.GridView} is clicked. Here will launch the
   * {@link TransitionDetailActivity}, using the Scene Transition animation functionality.
   */
  @Override
  public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
      TransitionActivityItem item =
          (TransitionActivityItem) adapterView.getItemAtPosition(position);

      // Construct an Intent as normal
      Intent intent = new Intent(this, TransitionDetailActivity.class);
      intent.putExtra(TransitionDetailActivity.EXTRA_PARAM_ID, item.getId());

      /**
       * Now create an {@link android.app.ActivityOptions} instance using the
       * {@link ActivityOptionsCompat#makeSceneTransitionAnmation(Activity, Pair[])} factory
       * method.
       */
      ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
          this,

          // Now we provide a list of Pair items which contain the view we can transitioning
          // from, and the name of the view it is transitioning to, in the launched activity
          new Pair<View, String>(view.findViewById(R.id.imageview_item),
              TransitionDetailActivity.VIEW_NAME_HEADER_IMAGE),
          new Pair<View, String>(view.findViewById(R.id.textview_name),
              TransitionDetailActivity.VIEW_NAME_HEADER_TITLE));

      // Now we can start the Activity, providing the activity options as a bundle
      ActivityCompat.startActivity(this, intent, activityOptions.toBundle());
  }


  @Override
  public void onBackPressed() {
    if (mExitOnBackPressed) {
      super.onBackPressed();
    } else {
      Toast.makeText(this, "Back was pressed but intercepted.", Toast.LENGTH_SHORT).show();
    }
  }

  public void setExitOnBackPressed(boolean exitOnBackPressed) {
    mExitOnBackPressed = exitOnBackPressed;
  }

  /**
   * {@link android.widget.BaseAdapter} which displays items.
   */
  private class GridAdapter extends BaseAdapter {

    @Override
    public int getCount() {
      return TransitionActivityItem.ITEMS.length;
    }

    @Override
    public TransitionActivityItem getItem(int position) {
      return TransitionActivityItem.ITEMS[position];
    }

    @Override
    public long getItemId(int position) {
      return getItem(position).getId();
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
      if (view == null) {
          view = getLayoutInflater().inflate(R.layout.transition_grid_item, viewGroup, false);
      }

      final TransitionActivityItem item = getItem(position);

      // Load the thumbnail image
      ImageView image = (ImageView) view.findViewById(R.id.imageview_item);

      image.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));

      // Set the TextView's contents
      TextView name = (TextView) view.findViewById(R.id.textview_name);
      name.setText(item.getName());

      return view;
    }
  }
}
