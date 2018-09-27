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

import static android.graphics.Bitmap.CompressFormat.PNG;
import static androidx.test.internal.util.Checks.checkNotNull;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.support.annotation.NonNull;
import androidx.test.annotation.Beta;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * A ScreenCapture contains a bitmap of a device screen along with a set of {@link
 * ScreenCaptureProcessor}s that will be used to process the {@link ScreenCapture} when {@link
 * #process} is called.
 *
 * <p>If no {@link ScreenCaptureProcessor}s are added to the list the {@link
 * BasicScreenCaptureProcessor} is used when {@link #process} is called.
 *
 * <p>If a set of processors are supplied with the call to {@link #process} then those processors
 * will take precedence over the set of global processors and the {@link
 * BasicScreenCaptureProcessor}.
 *
 * <p><b>This API is currently in beta.</b>
 */
@Beta
public final class ScreenCapture {
  private static final Bitmap.CompressFormat DEFAULT_FORMAT = PNG;

  private final Bitmap mBitmap;

  private ScreenCaptureProcessor mDefaultProcessor = new BasicScreenCaptureProcessor();
  private String mFilename;
  private CompressFormat mFormat;
  private Set<ScreenCaptureProcessor> mProcessorSet = new HashSet<>();

  ScreenCapture(Bitmap bitmap) {
    this.mBitmap = bitmap;
    this.mFormat = DEFAULT_FORMAT;
  }

  ScreenCapture(Bitmap bitmap, ScreenCaptureProcessor defaultProcessor) {
    this.mBitmap = bitmap;
    this.mFormat = DEFAULT_FORMAT;
    this.mDefaultProcessor = defaultProcessor;
  }

  /** Returns the {@link Bitmap} that was set when the {@link ScreenCapture} was created. */
  public Bitmap getBitmap() {
    return mBitmap;
  }

  /** Returns the filename to save the bitmap as or null if none has been set. */
  public String getName() {
    return mFilename;
  }

  /** Returns the format to save the bitmap as or PNG if none has been set. */
  public CompressFormat getFormat() {
    return mFormat;
  }

  /**
   * Sets the filename to save the {@link ScreenCapture} as.
   *
   * @param filename the filename to use to save the capture as
   * @return a fluent interface
   */
  public ScreenCapture setName(String filename) {
    this.mFilename = filename;
    return this;
  }

  /**
   * Sets the format to save the {@link ScreenCapture} as.
   *
   * @param format the format to use to save the screenshot as
   * @return a fluent interface
   */
  public ScreenCapture setFormat(CompressFormat format) {
    this.mFormat = format;
    return this;
  }

  /**
   * Set the set of processors that belong to this {@link ScreenCapture} to the given set.
   *
   * @param processorSet the set of processor to set.
   */
  ScreenCapture setProcessors(@NonNull Set<ScreenCaptureProcessor> processorSet) {
    this.mProcessorSet = checkNotNull(processorSet);
    return this;
  }

  /** Returns the set of processors that belong to this {@link ScreenCapture}. */
  Set<ScreenCaptureProcessor> getProcessors() {
    return mProcessorSet;
  }

  /**
   * Process the {@link ScreenCapture} using the global set of {@link ScreenCaptureProcessor}s or
   * the {@link BasicScreenCaptureProcessor} if no processors are set.
   *
   * @throws IOException if there is an IOException while any of the processors are processing the
   *     ScreenCapture
   */
  public void process() throws IOException {
    process(mProcessorSet);
  }

  /**
   * Process the {@link ScreenCapture} using the given set of {@link ScreenCaptureProcessor}s or the
   * {@link BasicScreenCaptureProcessor} if no processors are in the given set.
   *
   * @param processorSet the set of processors to use to process the ScreenCapture
   * @throws IOException if there is an IOException while any of the processors are processing the
   *     ScreenCapture
   */
  public void process(@NonNull Set<ScreenCaptureProcessor> processorSet) throws IOException {
    checkNotNull(processorSet);
    if (processorSet.isEmpty()) {
      mDefaultProcessor.process(this);
      return;
    }
    for (ScreenCaptureProcessor processor : processorSet) {
      processor.process(this);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 37;
    int result = 1;
    if (mBitmap != null) {
      result = prime * result + mBitmap.hashCode();
    }
    if (mFormat != null) {
      result = prime * result + mFormat.hashCode();
    }
    if (mFilename != null) {
      result = prime * result + mFilename.hashCode();
    }
    if (!mProcessorSet.isEmpty()) {
      result = prime * result + mProcessorSet.hashCode();
    }
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ScreenCapture)) {
      return false;
    }

    ScreenCapture other = (ScreenCapture) obj;
    boolean bitmapsEqual;
    if (mBitmap == null) {
      bitmapsEqual = (other.getBitmap() == null) ? true : false;
    } else {
      bitmapsEqual = getBitmap().sameAs(other.getBitmap());
    }

    boolean nameEqual;
    if (mFilename == null) {
      nameEqual = (other.getName() == null) ? true : false;
    } else {
      nameEqual = mFilename.equals(other.getName());
    }
    boolean formatEqual;
    if (mFormat == null) {
      formatEqual = (other.getFormat() == null) ? true : false;
    } else {
      formatEqual = mFormat.equals(other.getFormat());
    }
    return bitmapsEqual
        && nameEqual
        && formatEqual
        && mProcessorSet.containsAll(other.getProcessors())
        && other.getProcessors().containsAll(mProcessorSet);
  }
}
