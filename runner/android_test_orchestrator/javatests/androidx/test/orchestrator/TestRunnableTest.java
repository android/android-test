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
package androidx.test.orchestrator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import android.content.Context;
import android.os.Bundle;
import com.google.common.base.Joiner;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

/** Unit tests for {@link TestRunnable}. */
@RunWith(RobolectricTestRunner.class)
public class TestRunnableTest {

  private Bundle mArguments;
  private OutputStream mOutputStream;
  private Context mContext;

  /**
   * A {@link TestRunnable} which does not actually call on the shell command executor but rather
   * saves the params for inspection.
   */
  private static class FakeTestRunnable extends TestRunnable {
    public List<String> mParams;

    FakeTestRunnable(
        Context context,
        String secret,
        Bundle arguments,
        OutputStream outputStream,
        RunFinishedListener listener,
        String test,
        boolean collectTests) {
      super(context, secret, arguments, outputStream, listener, test, collectTests);
    }

    @Override
    InputStream runShellCommand(List<String> params) {
      mParams = params;
      InputStream stream =
          new InputStream() {
            int mByte = 'a';

            @Override
            public int read() throws IOException {
              // A very basic input stream which outputs a-g then terminates
              if (mByte <= 'g') {
                return mByte++;
              } else {
                return -1;
              }
            }
          };
      return stream;
    }
  }

  private static class FakeListener implements TestRunnable.RunFinishedListener {
    public boolean mFinishedCalled = false;

    @Override
    public void runFinished() {
      mFinishedCalled = true;
    }
  }

  @Before
  public void setUp() {
    mArguments = new Bundle();
    mArguments.putString("targetInstrumentation", "targetInstrumentation/targetRunner");
    mArguments.putString("class", "com.google.android.example.MyClass");
    mArguments.putString("arg1", "val1");

    mOutputStream = new ByteArrayOutputStream();
    mContext = RuntimeEnvironment.application;
  }

  @Test
  public void testRun_callsListener() {
    FakeListener listener = new FakeListener();
    FakeTestRunnable runnable =
        new FakeTestRunnable(mContext, "secret", mArguments, mOutputStream, listener, null, true);
    runnable.run();
    assertThat(listener.mFinishedCalled, is(true));
  }

  @Test
  public void testRun_returnsOutputStream() {
    FakeListener listener = new FakeListener();
    FakeTestRunnable runnable =
        new FakeTestRunnable(mContext, "secret", mArguments, mOutputStream, listener, null, true);
    runnable.run();
    assertThat(mOutputStream.toString().trim(), is("abcdefg"));
  }

  @Test
  public void testRun_buildsParams_givenClassNameAndMethod() {
    FakeListener listener = new FakeListener();
    FakeTestRunnable runnable =
        new FakeTestRunnable(
            null,
            "secret",
            mArguments,
            mOutputStream,
            listener,
            "com.google.android.example.MyClass#methodName",
            false);
    runnable.run();
    assertContainsRunnerArgs(
        runnable.mParams, "-e arg1 val1", "-e class com.google.android.example.MyClass#methodName");
  }

  @Test
  public void testRun_removesPackage_givenClassNameAndMethod() {
    FakeListener listener = new FakeListener();
    mArguments.putString("package", "package.to.be.deleted");
    FakeTestRunnable runnable =
        new FakeTestRunnable(
            null,
            "secret",
            mArguments,
            mOutputStream,
            listener,
            "com.google.android.example.MyClass#methodName",
            false);
    runnable.run();
    assertContainsRunnerArgs(
        runnable.mParams, "-e arg1 val1", "-e class com.google.android.example.MyClass#methodName");
  }

  @Test
  public void testRun_buildsParams_givenNullClassNameAndMethodForTestCollection() {
    FakeListener listener = new FakeListener();
    FakeTestRunnable runnable =
        new FakeTestRunnable(mContext, "secret", mArguments, mOutputStream, listener, null, true);
    runnable.run();
    assertContainsRunnerArgs(
        runnable.mParams,
        "-e listTestsForOrchestrator true",
        "-e arg1 val1",
        "-e class com.google.android.example.MyClass");
  }

  private static void assertContainsRunnerArgs(List<String> params, String... containsArgs) {
    String cmdArgs = Joiner.on(" ").join(params);
    assertThat(cmdArgs, startsWith("instrument -w -r"));
    for (String arg : containsArgs) {
      assertThat(cmdArgs, containsString(arg));
    }
    assertThat(cmdArgs, endsWith("targetInstrumentation/targetRunner"));
  }
}
