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

import static android.graphics.Bitmap.CompressFormat.JPEG;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.test.filters.MediumTest;
import java.io.File;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@MediumTest
public final class BasicScreenCaptureProcessorTest {

  @Rule public TemporaryFolder folder = new TemporaryFolder(getApplicationContext().getCacheDir());
  private Bitmap mStubBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
  private File mTmpFolder;

  private BasicScreenCaptureProcessor mDefaultScreenshotProcessor;

  @Before
  public void before() throws Exception {
    mTmpFolder = folder.newFolder("screenshots-tmp");
    mDefaultScreenshotProcessor = new BasicScreenCaptureProcessor(mTmpFolder);
    BasicScreenCaptureProcessor.setAndroidDeviceName(Build.DEVICE);
    BasicScreenCaptureProcessor.setAndroidRuntimeVersion(Build.VERSION.SDK_INT);
  }

  @Test
  public void process_shouldStoreBitmapWithDefaultNameAndFormat() throws Exception {
    BasicScreenCaptureProcessor.setAndroidRuntimeVersion(22);
    BasicScreenCaptureProcessor.setAndroidDeviceName("hammerhead");
    String filename = mDefaultScreenshotProcessor.process(new ScreenCapture(mStubBitmap));

    File expectedFile = new File(mTmpFolder.getAbsolutePath(), filename);
    assertTrue(expectedFile.exists());
    Bitmap bitmapFromFile = BitmapFactory.decodeFile(expectedFile.toString());
    assertTrue(bitmapFromFile.sameAs(mStubBitmap));
    assertTrue(filename.matches("screenshot-hammerhead-22-.*\\.png"));
  }

  @Test
  public void process_multipleCapturesShouldNotConflictNaming() throws Exception {
    BasicScreenCaptureProcessor.setAndroidRuntimeVersion(22);
    BasicScreenCaptureProcessor.setAndroidDeviceName("hammerhead");
    String filename = mDefaultScreenshotProcessor.process(new ScreenCapture(mStubBitmap));
    String filename2 = mDefaultScreenshotProcessor.process(new ScreenCapture(mStubBitmap));

    assertTrue(!filename.equals(filename2));

    File expectedFile = new File(mTmpFolder.getAbsolutePath(), filename);
    assertTrue(expectedFile.exists());
    Bitmap bitmapFromFile = BitmapFactory.decodeFile(expectedFile.toString());
    assertTrue(bitmapFromFile.sameAs(mStubBitmap));
    assertTrue(filename.matches("screenshot-hammerhead-22-.*\\.png"));

    File expectedFile2 = new File(mTmpFolder.getAbsolutePath(), filename2);
    assertTrue(expectedFile2.exists());
    Bitmap bitmapFromFile2 = BitmapFactory.decodeFile(expectedFile2.toString());
    assertTrue(bitmapFromFile2.sameAs(mStubBitmap));
    assertTrue(filename2.matches("screenshot-hammerhead-22-.*\\.png"));
  }

  @Test
  public void process_shouldStoreBitmapWithGivenName() throws Exception {
    ScreenCapture capture = new ScreenCapture(mStubBitmap).setName("givenName");
    String filename = mDefaultScreenshotProcessor.process(capture);

    File expectedFile = new File(mTmpFolder.getAbsolutePath(), filename);
    assertTrue(expectedFile.exists());
    assertTrue(filename.matches("givenName-.*\\.png"));
  }

  @Test
  public void process_multipleCapturesShouldNotConflictNamingWithGivenNames() throws Exception {
    ScreenCapture capture = new ScreenCapture(mStubBitmap).setName("givenName");
    ScreenCapture capture2 = new ScreenCapture(mStubBitmap).setName("givenName");
    String filename = mDefaultScreenshotProcessor.process(capture);
    String filename2 = mDefaultScreenshotProcessor.process(capture2);

    assertTrue(!filename.equals(filename2));

    File expectedFile = new File(mTmpFolder.getAbsolutePath(), filename);
    assertTrue(expectedFile.exists());
    assertTrue(filename.matches("givenName-.*\\.png"));

    File expectedFile2 = new File(mTmpFolder.getAbsolutePath(), filename2);
    assertTrue(expectedFile2.exists());
    assertTrue(filename2.matches("givenName-.*\\.png"));
  }

  @Test
  public void process_shouldStoreBitmapWithGivenFormat() throws Exception {
    BasicScreenCaptureProcessor.setAndroidRuntimeVersion(22);
    BasicScreenCaptureProcessor.setAndroidDeviceName("hammerhead");
    ScreenCapture capture = new ScreenCapture(mStubBitmap).setFormat(JPEG);
    String filename = mDefaultScreenshotProcessor.process(capture);

    File expectedFile = new File(mTmpFolder.getAbsolutePath(), filename);
    assertTrue(expectedFile.exists());
    assertTrue(filename.matches("screenshot-hammerhead-22-.*\\.jpeg"));
  }
}
