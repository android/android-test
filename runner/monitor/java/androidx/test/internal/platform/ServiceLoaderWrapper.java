/*
 * Copyright (C) 2018 The Android Open Source Project
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
package androidx.test.internal.platform;

import android.os.StrictMode;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Wrapper class for {@link ServiceLoader} that disables StrictMode.
 *
 * <p>Loading a service via ServiceLoader can result in disk I/O. Disk I/O on Android can be
 * restricted via StrictMode. This class disables disk access checking and then reenables StrictMode
 * when loading a service.
 */
public final class ServiceLoaderWrapper {


  private ServiceLoaderWrapper() {}

  /**
   * Loads the implementing classes for given service.
   *
   * @return List of implementing classes. Returns an empty list if no implementing classes were
   *     defined.
   */
  public static <T> List<T> loadService(Class<T> serviceClass) {
    StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();

    // load using ServiceLoader
    List<T> services = new ArrayList<>();
    for (T element : ServiceLoader.load(serviceClass)) {
      services.add(element);
    }
    StrictMode.setThreadPolicy(oldPolicy);
    return services;
  }
}
