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

/** Tests for {@link TypeToAnyConverterTest} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class TypeToAnyConverterTest {

  private static final String ANY_TYPE_URL = TestType.class.getName();
  private static final String TEST_PROTO_CONTENT = "Espresso";

  private TypeToAnyConverter typeToAnyConverter;

  @Before
  public void init() {
    typeToAnyConverter = new TypeToAnyConverter(RemoteDescriptorRegistryInitializer.init());
  }

  @Test
  public void typeToAnyConversion() {
    Any anyTestProto = typeToAnyConverter.convert(new TestType(TEST_PROTO_CONTENT));
    assertThat(anyTestProto.getTypeUrl(), equalTo(ANY_TYPE_URL));
  }
}
