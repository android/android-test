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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Executes an adb script as a fixture for tests.
 *
 * This feature is rather advanced and the interfaces may change with little notice.
 *
 * Currently your script will be invoked on device lease as follows:
 *
 * /path/to/your/script
 *
 */
@Beta
public class FixtureScriptExecutingDeviceBrokerDecorator implements DeviceBroker {
  private static final String SCRIPT_DIR = "googletest/fixture_scripts";
  private static final Logger logging = Logger.getLogger(
      FixtureScriptExecutingDeviceBrokerDecorator.class.getName());

  private final DeviceBroker delegate;
  private final List<File> adbScripts;
  private File scriptDir;

  public FixtureScriptExecutingDeviceBrokerDecorator(DeviceBroker delegate, List<File> adbScripts,
      boolean cleanUp) {
    this.adbScripts = checkNotNull(ImmutableList.copyOf(adbScripts));
    this.delegate = new DataStagingDeviceBrokerDecorator(
        checkNotNull(delegate),
        this.adbScripts,
        DataStagingDeviceBrokerDecorator.StorageType.INTERNAL,
        SCRIPT_DIR,
        DataStagingDeviceBrokerDecorator.PathHandlingStyle.GOOGLE_RELATIVE,
        cleanUp);
  }

  @Override
  public BrokeredDevice leaseDevice() {
    BrokeredDevice device = delegate.leaseDevice();
    if (adbScripts.size() > 0) {
      AdbController adbController = device.getAdbController();
      Map<String, String> deviceShellVars =  device.getShellVariables();
      String internalStorageDir = deviceShellVars.get("ANDROID_DATA");
      checkState(null != internalStorageDir, "No internal storage? Vars: %s", deviceShellVars);

      scriptDir = new File(internalStorageDir, SCRIPT_DIR);

      int originalTimeout = adbController.getDefaultTimeout();
      try {
        // Allow more time to run fixture scripts.
        adbController.setDefaultTimeout(originalTimeout * 3);

        for (File adbScript : adbScripts) {
          logging.info("Executing script: " + adbScript);
          adbController.executeScript(makeGoogleRelative(scriptDir, adbScript));
          logging.info("Execution complete: " + adbScript);
        }
      } finally {
        // Ensure that the timeout is reset.
        adbController.setDefaultTimeout(originalTimeout);
      }
    }

    return device;
  }

  private String makeGoogleRelative(File deviceDir, File pushFile) {
    String filePath = pushFile.getPath();
    int android_test_supportLocation = filePath.lastIndexOf("android_test_support");
    checkArgument(-1 != android_test_supportLocation, "artifact isn't under android_test_support: %s", pushFile);
    return new File(deviceDir, filePath.substring(android_test_supportLocation, filePath.length())).getPath();
  }

  @Override
  public void freeDevice(BrokeredDevice device) {
    delegate.freeDevice(device);
  }

  @Override
  public Map<String, Object> getExportedProperties() {
    return delegate.getExportedProperties();
  }
}
