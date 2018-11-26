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
import androidx.annotation.NonNull;
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

  private final Bitmap bitmap;

  private ScreenCaptureProcessor defaultProcessor = new BasicScreenCaptureProcessor();
  private String filename;
  private CompressFormat format;
  private Set<ScreenCaptureProcessor> processorSet = new HashSet<>();

  ScreenCapture(Bitmap bitmap) {
    this.bitmap = bitmap;
    this.format = DEFAULT_FORMAT;
  }

  ScreenCapture(Bitmap bitmap, ScreenCaptureProcessor defaultProcessor) {
    this.bitmap = bitmap;
    this.format = DEFAULT_FORMAT;
    this.defaultProcessor = defaultProcessor;
  }

  /** Returns the {@link Bitmap} that was set when the {@link ScreenCapture} was created. */
  public Bitmap getBitmap() {
    return bitmap;
  }

  /** Returns the filename to save the bitmap as or null if none has been set. */
  public String getName() {
    return filename;
  }

  /** Returns the format to save the bitmap as or PNG if none has been set. */
  public CompressFormat getFormat() {
    return format;
  }

  /**
   * Sets the filename to save the {@link ScreenCapture} as.
   *
   * @param filename the filename to use to save the capture as
   * @return a fluent interface
   */
  public ScreenCapture setName(String filename) {
    this.filename = filename;
    return this;
  }

  /**
   * Sets the format to save the {@link ScreenCapture} as.
   *
   * @param format the format to use to save the screenshot as
   * @return a fluent interface
   */
  public ScreenCapture setFormat(CompressFormat format) {
    this.format = format;
    return this;
  }

  /**
   * Set the set of processors that belong to this {@link ScreenCapture} to the given set.
   *
   * @param processorSet the set of processor to set.
   */
  ScreenCapture setProcessors(@NonNull Set<ScreenCaptureProcessor> processorSet) {
    this.processorSet = checkNotNull(processorSet);
    return this;
  }

  /** Returns the set of processors that belong to this {@link ScreenCapture}. */
  Set<ScreenCaptureProcessor> getProcessors() {
    return processorSet;
  }

  /**
   * Process the {@link ScreenCapture} using the global set of {@link ScreenCaptureProcessor}s or
   * the {@link BasicScreenCaptureProcessor} if no processors are set.
   *
   * @throws IOException if there is an IOException while any of the processors are processing the
   *     ScreenCapture
   */
  public void process() throws IOException {
    process(processorSet);
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
      defaultProcessor.process(this);
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
    if (bitmap != null) {
      result = prime * result + bitmap.hashCode();
    }
    if (format != null) {
      result = prime * result + format.hashCode();
    }
    if (filename != null) {
      result = prime * result + filename.hashCode();
    }
    if (!processorSet.isEmpty()) {
      result = prime * result + processorSet.hashCode();
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
    if (bitmap == null) {
      bitmapsEqual = (other.getBitmap() == null) ? true : false;
    } else {
      bitmapsEqual = getBitmap().sameAs(other.getBitmap());
    }

    boolean nameEqual;
    if (filename == null) {
      nameEqual = (other.getName() == null) ? true : false;
    } else {
      nameEqual = filename.equals(other.getName());
    }
    boolean formatEqual;
    if (format == null) {
      formatEqual = (other.getFormat() == null) ? true : false;
    } else {
      formatEqual = format.equals(other.getFormat());
    }
    return bitmapsEqual
        && nameEqual
        && formatEqual
        && processorSet.containsAll(other.getProcessors())
        && other.getProcessors().containsAll(processorSet);
  }
}
