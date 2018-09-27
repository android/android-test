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
import static com.google.common.io.Closeables.close;

import com.google.common.base.Charsets;
import com.google.common.io.LineProcessor;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Copies all lines to an output stream.
 *
 */
class StreamWritingProcessor implements LineProcessor<Void> {

  private final OutputStream outStream;

  public StreamWritingProcessor(OutputStream outStream) {
    this.outStream = checkNotNull(outStream);
  }

  @Override
  public final boolean processLine(String line) {
    try {
      outStream.write(line.getBytes(Charsets.UTF_8));
      outStream.write('\n');
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }

    return true;
  }

  @Override
  /**
   * Closes the output stream.
   */
  public Void getResult() {
    try {
      close(outStream, true);
    } catch (IOException e) {
      // Exception logged as Level.WARNING by close()
    }
    return null;
  }

}
