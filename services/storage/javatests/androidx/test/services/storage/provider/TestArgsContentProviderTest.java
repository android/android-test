/*
 * Copyright (C) 2019 The Android Open Source Project
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
 */
package androidx.test.services.storage.provider;

import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.test.ProviderTestCase2;
import androidx.test.services.storage.TestStorageConstants;
import androidx.test.services.storage.TestStorageServiceProto.TestArgument;
import androidx.test.services.storage.TestStorageServiceProto.TestArguments;
import androidx.test.services.storage.file.PropertyFile;
import com.google.common.base.Optional;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link TestArgsContentProvider}.
 *
 * TODO(b/145236542): Converts the tests to JUnit4.
 */
public class TestArgsContentProviderTest extends ProviderTestCase2<TestArgsContentProvider> {

  private static final String[] ARGS = {"arg1", "arg2", "arg3", "someth_server_address"};
  private static final String[] VALUES = {"value1", "value2", "value3", "foo:124"};

  private File testArgsFile;

  public TestArgsContentProviderTest() {
    super(TestArgsContentProvider.class, PropertyFile.Authority.TEST_ARGS.getAuthority());
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    getProvider().setSystemPropertyClassNameForTest(FakeSystemProperties.class.getName());
  }

  @Override
  public void tearDown() throws Exception {
    if (testArgsFile != null) {
      testArgsFile.delete();
    }
    super.tearDown();
  }

  public void testGetOneArg() throws IOException {
    createTestArgsFileWithMultipleArgs();

    int argIndex = 2;
    Uri uri = PropertyFile.buildUri(PropertyFile.Authority.TEST_ARGS, ARGS[argIndex]);
    Cursor cursor = getMockContentResolver().query(uri, null, null, null, null);

    try {
      assertEquals(1, cursor.getCount());
      cursor.moveToFirst();
      assertEquals(cursor.getString(PropertyFile.Column.VALUE.getPosition()), VALUES[argIndex]);
    } finally {
      cursor.close();
    }
  }

  public void testGetMultipleArgs() throws IOException {
    createTestArgsFileWithMultipleArgs();

    Uri uri = PropertyFile.buildUri(PropertyFile.Authority.TEST_ARGS);
    Cursor cursor = getMockContentResolver().query(uri, null, null, null, null);
    Map<String, String> argMap = getProperties(cursor);

    try {
      assertEquals(argMap.entrySet().size(), cursor.getCount());
      for (int i = 0; i < ARGS.length; i++) {
        assertEquals(VALUES[i], argMap.get(ARGS[i]));
      }
    } finally {
      cursor.close();
    }
  }

  public void testGetWrongArg() throws IOException {
    createTestArgsFileWithMultipleArgs();

    Uri uri = PropertyFile.buildUri(PropertyFile.Authority.TEST_ARGS, "wrong");
    Cursor cursor = getMockContentResolver().query(uri, null, null, null, null);

    try {
      assertEquals(0, cursor.getCount());
    } finally {
      cursor.close();
    }
  }

  public void testEmptyArgsDataFile() throws IOException {
    createTestArgsFile(TestArguments.getDefaultInstance());
    Uri uri = PropertyFile.buildUri(PropertyFile.Authority.TEST_ARGS);
    Cursor cursor = getMockContentResolver().query(uri, null, null, null, null);
    assertEquals(0, cursor.getCount());
    cursor.close();
  }

  public void testServerSpecOverride_behavesWellWhenNotSet() throws IOException {
    String localhost = "fake.machine.company.com";
    FakeSystemProperties.props.put("qemu.host.hostname", "");
    createTestArgsFile(makeSomeServerSpecArgs(localhost));

    Uri uri = PropertyFile.buildUri(PropertyFile.Authority.TEST_ARGS);
    Cursor cursor = getMockContentResolver().query(uri, null, null, null, null);
    Map<String, String> argMap = getProperties(cursor);

    assertEquals(localhost + ":12345", argMap.get("local_server_address"));
    assertEquals(localhost + ":984", argMap.get("local_2_server_address"));
    assertEquals("www.google.com:80", argMap.get("non_local_server_address"));
    assertEquals("fake.machine.company.com:100", argMap.get("val_is_a_spec_but_not_key"));
  }

  public void testServerSpecOverride() throws IOException {
    String localhost = "fake.machine.company.com";
    FakeSystemProperties.props.put("qemu.host.hostname", localhost);
    createTestArgsFile(makeSomeServerSpecArgs(localhost));

    Uri uri = PropertyFile.buildUri(PropertyFile.Authority.TEST_ARGS);
    Cursor cursor = getMockContentResolver().query(uri, null, null, null, null);
    Map<String, String> argMap = getProperties(cursor);

    assertEquals("10.0.2.2:12345", argMap.get("local_server_address"));
    assertEquals("10.0.2.2:984", argMap.get("local_2_server_address"));
    assertEquals("www.google.com:80", argMap.get("non_local_server_address"));
    assertEquals("fake.machine.company.com:100", argMap.get("val_is_a_spec_but_not_key"));
  }

  static class FakeSystemProperties {
    private static final Map<String, String> props =
        Collections.synchronizedMap(new HashMap<String, String>());

    public static String get(String key, String def) {
      return Optional.fromNullable(props.get(key)).or(def);
    }
  }

  private static TestArguments makeSomeServerSpecArgs(String localhost) {
    return TestArguments.newBuilder()
        .addArg(
            TestArgument.newBuilder()
                .setName("non_local_server_address")
                .setValue("www.google.com:80")
                .build())
        .addArg(
            TestArgument.newBuilder()
                .setName("local_server_address")
                .setValue(localhost + ":12345")
                .build())
        .addArg(
            TestArgument.newBuilder()
                .setName("local_2_server_address")
                .setValue(localhost + ":984")
                .build())
        .addArg(
            TestArgument.newBuilder()
                .setName("val_is_a_spec_but_not_key")
                .setValue(localhost + ":100")
                .build())
        .addArg(
            TestArgument.newBuilder()
                .setName(TestStorageConstants.USE_QEMU_IPS_IF_POSSIBLE_ARG_TAG)
                .setValue(String.valueOf(true))
                .build())
        .build();
  }

  private static void createTestArgsFileWithMultipleArgs() throws IOException {
    TestArguments.Builder builder = TestArguments.newBuilder();
    for (int i = 0; i < ARGS.length; i++) {
      builder.addArg(TestArgument.newBuilder().setName(ARGS[i]).setValue(VALUES[i]).build());
    }
    createTestArgsFile(builder.build());
  }

  private static void createTestArgsFile(TestArguments proto) throws IOException {
    File args =
        new File(
            Environment.getExternalStorageDirectory(),
            TestStorageConstants.ON_DEVICE_PATH_INTERNAL_USE
                + TestStorageConstants.TEST_ARGS_FILE_NAME);
    Files.write(proto.toByteArray(), args);
  }

  private Map<String, String> getProperties(Cursor cursor) {
    Map<String, String> properties = new HashMap<>();
    while (cursor.moveToNext()) {
      properties.put(
          cursor.getString(PropertyFile.Column.NAME.getPosition()),
          cursor.getString(PropertyFile.Column.VALUE.getPosition()));
    }
    return properties;
  }
}
