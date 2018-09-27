/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.apps.common.testing.broker;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.HttpProxy;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.HostAndPort;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

/** Configures proxy settings upon device lease. */
public final class ProxyConfiguringDeviceBrokerDecorator implements DeviceBroker {

  private final DeviceBroker delegate;
  private final HostAndPort proxyAddress;

  public ProxyConfiguringDeviceBrokerDecorator(DeviceBroker delegate, HostAndPort proxyAddress) {
    this.proxyAddress = checkNotNull(proxyAddress);
    this.delegate = checkNotNull(delegate);
  }

  @Override
  public BrokeredDevice leaseDevice() {
    BrokeredDevice device = delegate.leaseDevice();
    AdbController controller = device.getAdbController();
    String proxyString = proxyAddress.toString();

    try {
      InetAddress proxyInet = InetAddress.getByName(proxyAddress.getHost());
      if (proxyInet.isLoopbackAddress()) {
        // the emulator/device will consider local host to be ITSELF, not the machine running
        // the device broker - a very common mistake.
        if (device.getDeviceType() == BrokeredDevice.DeviceType.EMULATOR) {
          proxyString = "10.0.2.2:" + proxyAddress.getPort();
        } else {
          proxyString = InetAddress.getLocalHost().getCanonicalHostName() 
              + ":" + proxyAddress.getPort();
        }
      }
    } catch (UnknownHostException uhe) {
      // yeah - nothings going to work if this happens.
      throw new RuntimeException(uhe);
    }
    List<Instrumentation> instrumentations = controller.listInstrumentations();
    Instrumentation proxyInstrumentation = null;
    for (Instrumentation instrumentation : instrumentations) {
      if (".net.ProxyControlInstrumentation".equals(instrumentation.getInstrumentationClass())) {
        proxyInstrumentation = instrumentation;
        break;
      }
    }
    if (null == proxyInstrumentation) {
      throw new RuntimeException("Could not find proxy instrumentation on device!");
    }

    controller.startInstrumentation(
        proxyInstrumentation,
        ImmutableMap.of("http_proxy", proxyString),
        true);

    return device;
  }

  @Override
  public void freeDevice(BrokeredDevice device) {
    delegate.freeDevice(device);
  }

  @Override
  public Map<String, Object> getExportedProperties() {
    return delegate.getExportedProperties();
  }

  static class Decorator implements DeviceBrokerDecorator {
    private final String httpProxy;

    @Inject
    Decorator(@HttpProxy String httpProxy) {
      this.httpProxy = httpProxy;
    }

    @Override
    public DeviceBroker decorate(DeviceBroker db) {
      if ("".equals(httpProxy)) {
        return db;
      }
      return new ProxyConfiguringDeviceBrokerDecorator(db, HostAndPort.fromString(httpProxy));
    }
  }
}
