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

package androidx.test.services.speakeasy.client;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import androidx.test.services.speakeasy.SpeakEasyProtocol;

/** Provides a simple and type safe way of receiving FindResults. */
public abstract class FindResultReceiver extends ResultReceiver {

  public FindResultReceiver(Handler h) {
    super(h);
  }

  @Override
  public final void onReceiveResult(int resultCode, Bundle data) {
    SpeakEasyProtocol sep = SpeakEasyProtocol.fromBundle(data);
    if (null == sep) {
      Bundle invalid =
          SpeakEasyProtocol.FindResult.asBundle(
              false, null, "Server did not send back a sane result, got: " + data);
      sep = SpeakEasyProtocol.fromBundle(invalid);
    }
    if (null == sep.findResult) {
      Bundle invalid =
          SpeakEasyProtocol.FindResult.asBundle(
              false, null, "Server sent back wrong type - got: " + sep);
      sep = SpeakEasyProtocol.fromBundle(invalid);
    }
    handleFindResult(sep.findResult);
  }

  /** Implement to receive FindResults. */
  protected abstract void handleFindResult(SpeakEasyProtocol.FindResult findResult);
}
