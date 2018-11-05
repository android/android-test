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

  /** A factory for creating default implementations of service classes. */
  public interface Factory<T> {
    T create();
  }

  /**
   * A wrapper method around loadService that strictly enforces there is only one implementation of
   * the service.
   *
   * @param serviceClass the service type class to load implementation for
   * @param defaultImplFactory the factory implementation for creating instances
   * @return the implementing service, or a new instance via defaultImplFactory.create() if no
   *     implementations are defined.
   * @throws IllegalStateException if more than one service implementations are found
   */
  public static <T> T loadSingleService(Class<T> serviceClass, Factory<T> defaultImplFactory) {
    List<T> impls = ServiceLoaderWrapper.loadService(serviceClass);
    if (impls.isEmpty()) {
      return defaultImplFactory.create();
    } else if (impls.size() == 1) {
      return impls.get(0);
    } else {
      throw new IllegalStateException(
          "Found more than one implementation for " + serviceClass.getName());
    }
  }
}
