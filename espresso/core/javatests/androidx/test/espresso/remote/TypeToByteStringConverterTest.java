/*
 * Copyright (C) 2016 The Android Open Source Project
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
package androidx.test.espresso.remote;

import static org.hamcrest.CoreMatchers.notNullValue;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.google.protobuf.ByteString;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link TypeToByteStringConverter} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class TypeToByteStringConverterTest {

  @Test
  public void convertTypeToByteString() {
    TypeToByteStringConverter<String> typeToByteStringConverter = new TypeToByteStringConverter<>();
    ByteString byteString = typeToByteStringConverter.convert("Hello");
    Assert.assertThat(byteString, notNullValue());
  }
}
