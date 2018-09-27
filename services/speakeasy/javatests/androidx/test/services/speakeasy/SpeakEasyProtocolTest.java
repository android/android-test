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

package androidx.test.services.speakeasy;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Unit tests for {@link SpeakEasyProtocol}. */
@RunWith(RobolectricTestRunner.class)
public class SpeakEasyProtocolTest {

  @Test
  public void testRemove() {
    Bundle b = SpeakEasyProtocol.Remove.asBundle("to_remove");
    SpeakEasyProtocol sep = SpeakEasyProtocol.fromBundle(b);
    assertThat(sep).isNotNull();
    assertThat(sep.type).isEqualTo(SpeakEasyProtocol.REMOVE_TYPE);
    assertThat(sep.remove).isNotNull();
    assertThat(sep.remove.key).isEqualTo("to_remove");
  }

  @Test
  public void testPublish() {
    Binder binder = new Binder();
    ResultReceiver rr =
        new ResultReceiver(new Handler(Looper.getMainLooper())) {

          @Override
          public void onReceiveResult(int code, Bundle data) {}
        };

    try {
      SpeakEasyProtocol.Publish.asBundle(null, binder, rr);
      fail("null key should throw");
    } catch (NullPointerException expected) {
    }

    try {
      SpeakEasyProtocol.Publish.asBundle("stuff", null, rr);
      fail("null ibinder should throw");
    } catch (NullPointerException expected) {
    }

    try {
      SpeakEasyProtocol.Publish.asBundle("stuff", binder, null);
      fail("null receiver throw");
    } catch (NullPointerException expected) {
    }

    Bundle b = SpeakEasyProtocol.Publish.asBundle("foobar", binder, rr);
    SpeakEasyProtocol sep = SpeakEasyProtocol.fromBundle(b);
    assertThat(sep).isNotNull();
    assertThat(sep.type).isEqualTo(SpeakEasyProtocol.PUBLISH_TYPE);
    assertThat(sep.publish).isNotNull();
    assertThat(sep.publish.key).isEqualTo("foobar");
    assertThat(sep.publish.value).isEqualTo(binder);
    assertThat(sep.publish.resultReceiver).isNotNull();
  }

  @Test
  public void testPublishResult() {
    try {
      SpeakEasyProtocol.PublishResult.asBundle(null, false, "oops");
      fail("Should throw");
    } catch (NullPointerException expected) {
    }
    try {
      SpeakEasyProtocol.PublishResult.asBundle("stuff", false, null);
      fail("Should throw");
    } catch (NullPointerException expected) {
    }

    Bundle b = SpeakEasyProtocol.PublishResult.asBundle("stuff", true, null);
    SpeakEasyProtocol sep = SpeakEasyProtocol.fromBundle(b);
    assertThat(sep).isNotNull();
    assertThat(sep.type).isEqualTo(SpeakEasyProtocol.PUBLISH_RESULT_TYPE);
    assertThat(sep.publishResult).isNotNull();
    assertThat(sep.publishResult.key).isEqualTo("stuff");
    assertThat(sep.publishResult.published).isTrue();
    assertThat(sep.publishResult.error).isNull();

    b = SpeakEasyProtocol.PublishResult.asBundle("stuff", false, "oops");
    sep = SpeakEasyProtocol.fromBundle(b);
    assertThat(sep).isNotNull();
    assertThat(sep.type).isEqualTo(SpeakEasyProtocol.PUBLISH_RESULT_TYPE);
    assertThat(sep.publishResult).isNotNull();
    assertThat(sep.publishResult.key).isEqualTo("stuff");
    assertThat(sep.publishResult.published).isFalse();
    assertThat(sep.publishResult.error).isEqualTo("oops");
  }

  @Test
  public void testFind() {
    ResultReceiver rr =
        new ResultReceiver(new Handler(Looper.getMainLooper())) {
          @Override
          public void onReceiveResult(int code, Bundle data) {}
        };

    try {
      SpeakEasyProtocol.Find.asBundle(null, rr);
      fail("Should throw");
    } catch (NullPointerException expected) {
    }

    try {
      SpeakEasyProtocol.Find.asBundle("foo", null);
      fail("Should throw");
    } catch (NullPointerException expected) {
    }

    Bundle b = SpeakEasyProtocol.Find.asBundle("foo", rr);
    SpeakEasyProtocol sep = SpeakEasyProtocol.fromBundle(b);
    assertThat(sep).isNotNull();
    assertThat(sep.type).isEqualTo(SpeakEasyProtocol.FIND_TYPE);
    assertThat(sep.find).isNotNull();
    assertThat(sep.find.key).isEqualTo("foo");
    assertThat(sep.find.resultReceiver).isNotNull();
  }

  @Test
  public void testFindResult() {
    Binder binder = new Binder();
    try {
      SpeakEasyProtocol.FindResult.asBundle(false, binder, null);
      fail("Should throw without an error message");
    } catch (NullPointerException expected) {
    }
    try {
      SpeakEasyProtocol.FindResult.asBundle(true, null, "oops");
      fail("Should throw without a binder");
    } catch (NullPointerException expected) {
    }

    Bundle b = SpeakEasyProtocol.FindResult.asBundle(true, binder, null);

    SpeakEasyProtocol sep = SpeakEasyProtocol.fromBundle(b);
    assertThat(sep).isNotNull();
    assertThat(sep.type).isEqualTo(SpeakEasyProtocol.FIND_RESULT_TYPE);
    assertThat(sep.findResult.binder).isEqualTo(binder);
    assertThat(sep.findResult.found).isTrue();

    b = SpeakEasyProtocol.FindResult.asBundle(false, null, "oops");

    sep = SpeakEasyProtocol.fromBundle(b);
    assertThat(sep).isNotNull();
    assertThat(sep.type).isEqualTo(SpeakEasyProtocol.FIND_RESULT_TYPE);
    assertThat(sep.findResult).isNotNull();
    assertThat(sep.findResult.error).isEqualTo("oops");
    assertThat(sep.findResult.found).isFalse();
  }
}
