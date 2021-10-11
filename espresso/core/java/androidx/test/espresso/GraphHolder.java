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

package androidx.test.espresso;

import static com.google.common.base.Preconditions.checkNotNull;

import android.util.Log;
import androidx.test.espresso.base.ActiveRootLister;
import androidx.test.platform.io.PlatformTestStorage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/** Holds Espresso's object graph. */
public final class GraphHolder {
  private static final String TAG = GraphHolder.class.getSimpleName();

  private static final AtomicReference<GraphHolder> instance =
      new AtomicReference<GraphHolder>(null);

  private final BaseLayerComponent component;

  private GraphHolder(BaseLayerComponent component) {
    this.component = checkNotNull(component);
  }

  static BaseLayerComponent baseLayer() {
    GraphHolder instanceRef = instance.get();
    if (null == instanceRef) {
      instanceRef = new GraphHolder(DaggerBaseLayerComponent.create());
      if (instance.compareAndSet(null, instanceRef)) {
        // Also adds the usage data as test output properties. By default it's no-op.
        Map<String, Serializable> usageProperties = new HashMap<>();
        usageProperties.put("Espresso", "1");
        addUsageToOutputProperties(usageProperties, instanceRef.component.testStorage());
        return instanceRef.component;
      } else {
        return instance.get().component;
      }
    } else {
      return instanceRef.component;
    }
  }

  private static void addUsageToOutputProperties(
      Map<String, Serializable> usageProperties, PlatformTestStorage testStorage) {
    try {
      testStorage.addOutputProperties(usageProperties);
    } catch (RuntimeException e) {
      // The properties.dat file can be created only once on an automotive emulator with API 30,
      // which causes the `addOutputProperties` call to fail when running multiple test cases. Catch
      // the exception and log until the issue is fixed in the emulator.
      Log.w(
          TAG,
          "Failed to add the output properties. This could happen when running on Robolectric or an"
              + " automotive emulator with API 30. Ignore for now.");
    }
  }
}
