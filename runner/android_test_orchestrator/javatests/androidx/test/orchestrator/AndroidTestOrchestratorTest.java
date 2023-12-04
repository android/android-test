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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link AndroidTestOrchestrator}. */
@RunWith(AndroidJUnit4.class)
public class AndroidTestOrchestratorTest {

  @Test
  public void testSingleMethodTest() {
    assertThat(AndroidTestOrchestrator.isSingleMethodTest("org.example.class#method"), is(true));
    assertThat(AndroidTestOrchestrator.isSingleMethodTest("org.example.class"), is(false));
    assertThat(
        AndroidTestOrchestrator.isSingleMethodTest("org.example.class,org.example.another#method"),
        is(false));
    assertThat(
        AndroidTestOrchestrator.isSingleMethodTest(
            "org.example.class#method,org.example.class#anotherMethod"),
        is(false));
  }

  @Test
  public void testSingleMethodTest_blankInput() {
    assertThat(AndroidTestOrchestrator.isSingleMethodTest(null), is(false));
    assertThat(AndroidTestOrchestrator.isSingleMethodTest(""), is(false));
  }

  @Test
  public void testMakeValidFilename_notTooLong() {
    final int maxLength = 20;

    assertThat(
        // len("hello_world") = 11, with ".txt" appended it's still way below maxLength
        AndroidTestOrchestrator.makeValidFilename("hello_world", maxLength), is("hello_world.txt"));
  }

  @Test
  public void testMakeValidFilename_equalToMaxLength() {
    final int maxLength = 20;

    assertThat(
        // len("hello_world_abcd") = 16, with ".txt" appended it's equal to maxLength
        AndroidTestOrchestrator.makeValidFilename("hello_world_abcd", maxLength),
        is("hello_world_abcd.txt"));
  }

  @Test
  public void testMakeValidFilename_differrentWhenTooLong() {
    final int maxLength = 20;

    // len() of both of these is 18, with ".txt" appended they exceed maxLength
    String filename1 = AndroidTestOrchestrator.makeValidFilename("hello_world_abcdef", maxLength);
    String filename2 = AndroidTestOrchestrator.makeValidFilename("hello_world_ghijkl", maxLength);
    assertThat(filename1, is(not(equalTo(filename2))));
  }

  @Test
  public void testMakeValidFilename_tooLong() {
    final int maxLength = 20;

    assertThat(
        // len("hello_world_abcde") is 17, with ".txt" appended it exceeds maxLength
        AndroidTestOrchestrator.makeValidFilename("hello_world_abcde", maxLength),
        // hash code of "hello_world_abcde" is "889cc989"
        is("hello_wo889cc989.txt"));
  }
}
