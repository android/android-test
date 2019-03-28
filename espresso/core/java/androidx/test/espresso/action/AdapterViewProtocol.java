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

package androidx.test.espresso.action;

import static com.google.common.base.Preconditions.checkNotNull;

import android.database.Cursor;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import androidx.test.espresso.util.EspressoOptional;
import androidx.test.espresso.util.HumanReadables;
import java.util.Locale;
import javax.annotation.Nullable;

/**
 * A sadly necessary layer of indirection to interact with AdapterViews.
 *
 * <p>Generally any subclass should respect the contracts and behaviors of its superclass. Otherwise
 * it becomes impossible to work generically with objects that all claim to share a supertype - you
 * need special cases to perform the same operation 'owned' by the supertype for each sub-type. The
 * 'is - a' relationship is broken.
 *
 * <p>Android breaks the Liskov substitution principal with ExpandableListView - you can't use
 * getAdapter(), getItemAtPosition(), and other methods common to AdapterViews on an
 * ExpandableListView because an ExpandableListView isn't an adapterView - they just share a lot of
 * code.
 *
 * <p>This interface exists to work around this wart (which sadly is copied in other projects too)
 * and lets the implementor translate Espresso's needs and manipulations of the AdapterView into
 * calls that make sense for the given subtype and context.
 *
 * <p><i> If you have to implement this to talk to widgets your own project defines - I'm sorry.
 * </i>
 */
public interface AdapterViewProtocol {

  /**
   * Returns all data this AdapterViewProtocol can find within the given AdapterView.
   *
   * <p>Any AdaptedData returned by this method can be passed to makeDataRenderedWithinView and the
   * implementation should make the AdapterView bring that data item onto the screen.
   *
   * @param adapterView the AdapterView we want to interrogate the contents of.
   * @return an {@link Iterable} of AdaptedDatas representing all data the implementation sees in
   *     this view
   * @throws IllegalArgumentException if the implementation doesn't know how to manipulate the given
   *     adapter view.
   */
  Iterable<AdaptedData> getDataInAdapterView(AdapterView<? extends Adapter> adapterView);

  /**
   * Returns the data object this particular view is rendering if possible.
   *
   * <p>Implementations are expected to create a relationship between the data in the AdapterView
   * and the descendant views of the AdapterView that obeys the following conditions:
   *
   * <ul>
   *   <li>For each descendant view there exists either 0 or 1 data objects it is rendering.
   *   <li>For each data object the AdapterView there exists either 0 or 1 descendant views which
   *       claim to be rendering it.
   * </ul>
   *
   * <p>For example - if a PersonObject is rendered into: <code>
   * LinearLayout
   *   ImageView picture
   *   TextView firstName
   *   TextView lastName
   * </code>
   *
   * <p>It would be expected that getDataRenderedByView(adapter, LinearLayout) would return the
   * PersonObject. If it were called instead with the TextView or ImageView it would return
   * Object.absent().
   *
   * @param adapterView the adapterview hosting the data.
   * @param descendantView a view which is a child, grand-child, or deeper descendant of adapterView
   * @return an optional data object the descendant view is rendering.
   * @throws IllegalArgumentException if this protocol cannot interrogate this class of adapterView
   */
  EspressoOptional<AdaptedData> getDataRenderedByView(
      AdapterView<? extends Adapter> adapterView, View descendantView);

  /**
   * Requests that a particular piece of data held in this AdapterView is actually rendered by it.
   *
   * <p>After calling this method it expected that there will exist some descendant view of
   * adapterView for which calling getDataRenderedByView(adapterView, descView).get() == data.data
   * is true.
   *
   * <p>Note: this need not happen immediately. EG: an implementor handling ListView may call
   * listView.smoothScrollToPosition(data.opaqueToken) - which kicks off an animated scroll over the
   * list to the given position. The animation may be in progress after this call returns. The only
   * guarantee is that eventually - with no further interaction necessary - this data item will be
   * rendered as a child or deeper descendant of this AdapterView.
   *
   * @param adapterView the adapterView hosting the data.
   * @param data an AdaptedData instance retrieved by a prior call to getDataInAdapterView
   * @throws IllegalArgumentException if this protocol cannot manipulate adapterView or if data is
   *     not owned by this AdapterViewProtocol.
   */
  void makeDataRenderedWithinAdapterView(
      AdapterView<? extends Adapter> adapterView, AdaptedData data);

  /**
   * Indicates whether or not there now exists a descendant view within adapterView that is
   * rendering this data.
   *
   * @param adapterView the AdapterView hosting this data.
   * @param adaptedData the data we are checking the display state for.
   * @return true if the data is rendered by a view in the adapterView, false otherwise.
   */
  boolean isDataRenderedWithinAdapterView(
      AdapterView<? extends Adapter> adapterView, AdaptedData adaptedData);

  /**
   * A custom function that is applied when {@link AdaptedData#getData()} is executed.
   *
   * @see AdapterViewProtocol.AdaptedData.Builder#withDataFunction(DataFunction)
   */
  public interface DataFunction {
    public Object getData();
  }

  /**
   * A holder that associates a data object from an AdapterView with a token the AdapterViewProtocol
   * can use to force that data object to be rendered as a child or deeper descendant of the adapter
   * view.
   */
  public static class AdaptedData {

    /**
     * One of the objects the AdapterView is exposing to the user.
     *
     * @deprecated use {@link AdaptedData#getData()} instead.
     */
    @Nullable @Deprecated public final Object data;

    /**
     * A token the implementor of AdapterViewProtocol can use to force the adapterView to display
     * this data object as a child or deeper descendant in it. Equal opaqueToken point to the same
     * data object on the AdapterView.
     */
    public final Object opaqueToken;

    private final DataFunction dataFunction;

    public Object getData() {
      return dataFunction.getData();
    }

    @Override
    public String toString() {
      Object myData = getData();
      String itsClass = null == myData ? "null" : myData.getClass().getName();
      if (myData instanceof Cursor) {
        myData = HumanReadables.describe((Cursor) myData);
      }
      return String.format(
          Locale.ROOT, "Data: %s (class: %s) token: %s", myData, itsClass, opaqueToken);
    }

    private AdaptedData(Object data, Object opaqueToken, DataFunction dataFunction) {
      this.data = data;
      this.opaqueToken = checkNotNull(opaqueToken);
      this.dataFunction = checkNotNull(dataFunction);
    }

    public static class Builder {
      private Object data;
      private Object opaqueToken;
      private DataFunction dataFunction;

      public Builder withDataFunction(@Nullable DataFunction dataFunction) {
        this.dataFunction = dataFunction;
        return this;
      }

      public Builder withData(@Nullable Object data) {
        this.data = data;
        return this;
      }

      public Builder withOpaqueToken(@Nullable Object opaqueToken) {
        this.opaqueToken = opaqueToken;
        return this;
      }

      public AdaptedData build() {
        if (null != dataFunction) {
          data = dataFunction.getData();
        } else {
          dataFunction =
              new DataFunction() {
                @Override
                public Object getData() {
                  return data;
                }
              };
        }

        return new AdaptedData(data, opaqueToken, dataFunction);
      }
    }
  }
}
