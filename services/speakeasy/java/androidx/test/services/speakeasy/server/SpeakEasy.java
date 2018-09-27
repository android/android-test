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

package androidx.test.services.speakeasy.server;

import static com.google.common.base.Preconditions.checkNotNull;

import android.app.Activity;
import android.os.IBinder;
import android.util.Log;
import androidx.test.services.speakeasy.SpeakEasyProtocol;
import java.util.HashMap;
import java.util.Map;

/** Contains the core logic of pairing binders with identifiers for SpeakEasy. */
class SpeakEasy {
  private static final String TAG = "SpeakEasy";
  private final Map<String, Holder> binders = new HashMap<>();
  private final BinderDeathCallback binderDeathCallback;

  private static final class Holder {
    private final IBinder binder;
    private final IBinder.DeathRecipient death;

    Holder(IBinder binder, IBinder.DeathRecipient death) {
      this.binder = binder;
      this.death = death;
    }
  }

  SpeakEasy(BinderDeathCallback binderDeathCallback) {
    this.binderDeathCallback = checkNotNull(binderDeathCallback);
  }

  int size() {
    return binders.size();
  }

  void serve(SpeakEasyProtocol sep) {

    switch (sep.type) {
      case SpeakEasyProtocol.PUBLISH_TYPE:
        doPublish(sep.publish);
        break;
      case SpeakEasyProtocol.REMOVE_TYPE:
        doRemove(sep.remove);
        break;
      case SpeakEasyProtocol.FIND_TYPE:
        doFind(sep.find);
        break;
      default:
        throw new IllegalStateException("Invalid/Unknown protocol: " + sep);
    }
  }

  /**
   * Looks up a binder for a find request.
   *
   * <p>Users provide a key to search under, and we will respond back to the ResultReceiver they
   * include with their request.
   */
  private void doFind(SpeakEasyProtocol.Find f) {
    Holder h = binders.get(f.key);

    if (null == h) {
      f.resultReceiver.send(
          Activity.RESULT_OK,
          SpeakEasyProtocol.FindResult.asBundle(
              false, null, String.format("no binder for key: '%s'", f.key)));
      return;
    }
    f.resultReceiver.send(
        Activity.RESULT_OK, SpeakEasyProtocol.FindResult.asBundle(true, h.binder, ""));
  }

  /** Removes a binder paired with a given key. */
  private void doRemove(SpeakEasyProtocol.Remove r) {
    Holder h = binders.remove(r.key);
    if (h != null) {
      if (h.binder.isBinderAlive()) {
        h.binder.unlinkToDeath(h.death, 0);
      }
    }
  }

  /**
   * Pairs a binder with a particular key.
   *
   * <p>The pairing will succeed if there is no other entry paired with that key and the binder is
   * not dead.
   */
  private void doPublish(final SpeakEasyProtocol.Publish p) {
    if (binders.containsKey(p.key)) {
      p.resultReceiver.send(
          Activity.RESULT_OK,
          SpeakEasyProtocol.PublishResult.asBundle(
              p.key, false, String.format("'%s': already in use", p.key)));
      return;
    }

    final String key = p.key;
    IBinder.DeathRecipient death =
        new IBinder.DeathRecipient() {
          @Override
          public void binderDied() {
            binderDeathCallback.binderDeath(key, p.value);
          }
        };

    try {
      p.value.linkToDeath(death, 0);
    } catch (Exception e) {
      Log.w(TAG, "Super early death of: " + key, e);
      binderDeathCallback.binderDeath(key, p.value);
      p.resultReceiver.send(
          Activity.RESULT_OK,
          SpeakEasyProtocol.PublishResult.asBundle(
              p.key, false, String.format("'%s': already dead", p.key)));
      return;
    }

    Holder h = new Holder(p.value, death);
    binders.put(p.key, h);
    p.resultReceiver.send(
        Activity.RESULT_OK, SpeakEasyProtocol.PublishResult.asBundle(p.key, true, "published"));
  }

  /** Notifies listeners when a binder has died. */
  interface BinderDeathCallback {
    public void binderDeath(String key, IBinder dead);
  }
}
