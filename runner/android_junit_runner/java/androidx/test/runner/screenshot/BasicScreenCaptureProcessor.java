/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.runner.screenshot;

import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStoragePublicDirectory;

import android.os.Build;
import androidx.annotation.VisibleForTesting;
import android.util.Log;
import androidx.test.annotation.Beta;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * A basic {@link ScreenCaptureProcessor} for processing a {@link ScreenCapture}.
 *
 * <p>This will perform basic processing on the given {@link ScreenCapture} such as saving to the
 * public Pictures directory, given by
 * android.os.Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES), with a simple name
 * that includes a few characteristics about the device it was saved on followed by a UUID.
 *
 * <p><b>This API is currently in beta.</b>
 */
@Beta
public class BasicScreenCaptureProcessor implements ScreenCaptureProcessor {
  private static int sAndroidRuntimeVersion = Build.VERSION.SDK_INT;
  private static String sAndroidDeviceName = Build.DEVICE;

  protected String mTag;
  protected String mFileNameDelimiter;
  protected String mDefaultFilenamePrefix;
  protected File mDefaultScreenshotPath;

  public BasicScreenCaptureProcessor() {
    this(new File(getExternalStoragePublicDirectory(DIRECTORY_PICTURES), "screenshots"));
  }

  BasicScreenCaptureProcessor(File defaultScreenshotPath) {
    mTag = "BasicScreenCaptureProcessor";
    mFileNameDelimiter = "-";
    mDefaultFilenamePrefix = "screenshot";
    mDefaultScreenshotPath = defaultScreenshotPath;
  }

  @Override
  public String process(ScreenCapture capture) throws IOException {
    String filename =
        capture.getName() == null ? getDefaultFilename() : getFilename(capture.getName());
    filename += "." + capture.getFormat().toString().toLowerCase();
    File imageFolder = mDefaultScreenshotPath;
    imageFolder.mkdirs();
    if (!imageFolder.isDirectory() && !imageFolder.canWrite()) {
      throw new IOException(
          String.format(
              "The directory %s does not exist and could not be created or is not " + "writable.",
              imageFolder));
    }

    File imageFile = new File(imageFolder, filename);
    BufferedOutputStream out = null;
    try {
      out = new BufferedOutputStream(new FileOutputStream(imageFile));
      capture.getBitmap().compress(capture.getFormat(), 100, out);
      out.flush();
    } finally {
      try {
        if (out != null) {
          out.close();
        }
      } catch (IOException e) {
        Log.e(mTag, "Could not close output steam.", e);
      }
    }
    return filename;
  }

  /** Returns the default filename for this class suffixed with a UUID. */
  protected String getDefaultFilename() {
    return getFilename(
        mDefaultFilenamePrefix
            + mFileNameDelimiter
            + sAndroidDeviceName
            + mFileNameDelimiter
            + sAndroidRuntimeVersion);
  }

  /** Returns the filename created from the given prifix and suffixed with a UUID. */
  protected String getFilename(String prefix) {
    return prefix + mFileNameDelimiter + UUID.randomUUID();
  }

  @VisibleForTesting
  static void setAndroidDeviceName(String deviceName) {
    sAndroidDeviceName = deviceName;
  }

  @VisibleForTesting
  static void setAndroidRuntimeVersion(int sdkInt) {
    sAndroidRuntimeVersion = sdkInt;
  }
}
