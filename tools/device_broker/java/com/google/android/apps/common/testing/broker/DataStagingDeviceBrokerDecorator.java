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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Decorates a DeviceBroker and stages data on the device.
 *
 */
public class DataStagingDeviceBrokerDecorator implements DeviceBroker {

  private static final Logger logger = Logger.getLogger(
      DataStagingDeviceBrokerDecorator.class.getName());

  private final DeviceBroker delegate;
  private final List<File> hostFilesToPush;
  private final String deviceSubDirectory;
  private final StorageType storageType;
  private final PathHandlingStyle pathHandlingStyle;
  private final boolean cleanUp;
  private final List<String> createdFiles = Lists.newArrayList();

  /**
   * Indicates whether to stage the data on the device's internal storage
   * or external storage (sdcard). Note executables must be on internal
   * storage.
   */
  public enum StorageType {EXTERNAL, INTERNAL}

  /**
   * Converts the host file's path to the proper file path on device given
   * where the data stager is rooted.
   */
  public enum PathHandlingStyle {
    GOOGLE_RELATIVE {
      @Override
      protected File makeDeviceFile(String deviceRootDir, File hostFile) {
        String filePath = hostFile.getPath();
        int android_test_supportLocation = filePath.lastIndexOf("/android_test_support/");
        checkArgument(-1 != android_test_supportLocation, "artifact isn't under android_test_support: %s", filePath);
        return new File(deviceRootDir,
            filePath.substring(android_test_supportLocation, filePath.length()));
      }
    }, FLATTEN {
      @Override
      protected File makeDeviceFile(String deviceRootDir, File hostFile) {
        return new File(deviceRootDir, hostFile.getName());
      }
    };

    /**
     * Given the root directory we're staging to on the device and the file we're pushing
     * compute the full path that file should be on in the device.
     */
    public final String computePathForFileOnDevice(String deviceRootDir, File hostFile) {
      checkNotNull(deviceRootDir);
      checkNotNull(hostFile);

      File deviceFile = makeDeviceFile(deviceRootDir, hostFile);
      try {
        String canonicalPath = deviceFile.getCanonicalPath();
        checkState(deviceFile.getCanonicalPath().startsWith(deviceRootDir),
            "%s (canonical path: %s) is not a subdirectory of the device root directory: %s",
            deviceFile.getPath(), canonicalPath, deviceRootDir);
      } catch (IOException ioe) {
        // skip check - its best effort.
        logger.log(Level.WARNING, "Couldn't calculate canonical path, skipping subdir check", ioe);
      }

      return deviceFile.getPath();
    }

    protected abstract File makeDeviceFile(String deviceRootDir, File hostFile);
  }

  public DataStagingDeviceBrokerDecorator(DeviceBroker delegate, List<File> hostFilesToPush,
    StorageType storageType, String deviceSubDirectory, PathHandlingStyle pathHandlingStyle,
    boolean cleanUp) {
    this.delegate = checkNotNull(delegate);
    this.hostFilesToPush = checkNotNull(ImmutableList.copyOf(hostFilesToPush));
    this.storageType = checkNotNull(storageType);
    this.deviceSubDirectory = checkNotNull(deviceSubDirectory);
    this.pathHandlingStyle = checkNotNull(pathHandlingStyle);
    this.cleanUp = cleanUp;
  }

  @Override
  public BrokeredDevice leaseDevice() {
    BrokeredDevice device = delegate.leaseDevice();
    AdbController adbController = device.getAdbController();
    if (hostFilesToPush.size() > 0) {
      String deviceDir = getDeviceDir(device);
      for (File pushFile : hostFilesToPush) {
        String deviceFile = pathHandlingStyle.computePathForFileOnDevice(deviceDir, pushFile);
        adbController.push(pushFile, deviceFile);
        createdFiles.add(deviceFile);
      }
    }

    return device;
  }

  private String makeGoogleRelative(String deviceDir, File pushFile) {
    String filePath = pushFile.getPath();
    int android_test_supportLocation = filePath.lastIndexOf("/android_test_support/");
    checkArgument(-1 != android_test_supportLocation, "artifact isn't under android_test_support: %s", pushFile);
    return new File(deviceDir, filePath.substring(android_test_supportLocation, filePath.length())).getPath();
  }

  private String getDeviceDir(BrokeredDevice device) {
    Map<String, String> deviceShellVars = device.getShellVariables();
    if (StorageType.EXTERNAL == storageType) {
      String externalStorageDir = deviceShellVars.get("EXTERNAL_STORAGE");
      checkState(null != externalStorageDir, "No external storage? Vars: %s", deviceShellVars);
      return new File(externalStorageDir, deviceSubDirectory).getPath();
    } else {
      String internalStorageDir = deviceShellVars.get("ANDROID_DATA");
      checkState(null != internalStorageDir, "No internal storage? Vars: %s", deviceShellVars);
      return new File(internalStorageDir, deviceSubDirectory).getPath();
    }
  }

  @Override
  public void freeDevice(BrokeredDevice device) {
    if (cleanUp && !createdFiles.isEmpty()) {
      try {
        device.getAdbController().deleteFiles(createdFiles);
      } catch (RuntimeException re) {
        // might be the device was seriously borked.
        logger.log(
            Level.WARNING,
            String.format("Couldnt clean the device of files: %s", createdFiles),
            re);
      }
    }
    delegate.freeDevice(device);
  }

  @Override
  public Map<String, Object> getExportedProperties() {
    return delegate.getExportedProperties();
  }
}
