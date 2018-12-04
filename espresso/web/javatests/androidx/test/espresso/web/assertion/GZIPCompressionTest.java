/*
 * Copyright (C) 2017 The Android Open Source Project
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
 *
 */
package androidx.test.espresso.web.assertion;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import androidx.test.espresso.web.assertion.CompressorDecompressor.GZIPCompressor;
import androidx.test.espresso.web.assertion.CompressorDecompressor.GZIPDecompressor;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link GZIPCompressor} */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class GZIPCompressionTest {
  private static final String STRING_TO_COMPRESS = "Hello Compression!";

  @Test
  public void compressionDecrompression() throws IOException {
    byte[] compressed = new GZIPCompressor().compress(STRING_TO_COMPRESS.getBytes());
    byte[] decompressed = new GZIPDecompressor().decompress(compressed);
    assertThat(new String(decompressed), equalTo(STRING_TO_COMPRESS));
  }
}
