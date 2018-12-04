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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import androidx.test.espresso.remote.TestTypes.TestType;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.google.protobuf.Any;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link AnyToTypeConverter} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class AnyToTypeConverterTest {

  private static final String TEST_TYPE_CONTENT = "Espresso";

  private AnyToTypeConverter<TestType> anyToTypeConverter;
  private TypeToAnyConverter typeToAnyConverter;

  @Before
  public void init() {
    RemoteDescriptorRegistry remoteDescriptorRegistry = RemoteDescriptorRegistryInitializer.init();
    anyToTypeConverter = new AnyToTypeConverter(remoteDescriptorRegistry);
    typeToAnyConverter = new TypeToAnyConverter(remoteDescriptorRegistry);
  }

  @Test
  public void anyToTypeConversion() {
    // Create test type and convert it to Any using an ProtoToAnyConverter
    TestType testTargetType = new TestType(TEST_TYPE_CONTENT);
    Any anyTestProto = typeToAnyConverter.convert(testTargetType);

    // Convert the any to the test type
    TestType testResultType = anyToTypeConverter.convert(anyTestProto);

    // verify hello field
    assertThat(testResultType.getHello(), equalTo(TEST_TYPE_CONTENT));
  }
}
