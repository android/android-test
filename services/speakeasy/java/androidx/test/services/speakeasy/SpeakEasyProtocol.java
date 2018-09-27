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

import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ResultReceiver;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * SpeakEasyProtocol abstracts away sending commands / interpreting responses from speakeasy via
 * bundles.
 *
 * <p>SpeakEasy allows the registration, query, removal of IBinders from the shell user to android
 * apps.
 *
 * <p>This bypasses the Android platform's typical dependency and lifecycle management of Services
 * and IPC. Using Service objects defined in your Android manifest is the proper way to do IPC in
 * Android and these mechanisms should only be used in test.
 *
 * <p>The dependencies of this class should be kept to a minimum and it should remain possible to
 * use this class outside of an apk.
 */
public final class SpeakEasyProtocol {
  private static final String TAG = SpeakEasyProtocol.class.getName();

  public final int type;

  public static final int PUBLISH_TYPE = 0;
  public static final int PUBLISH_RESULT_TYPE = 1;
  public static final int REMOVE_TYPE = 2;
  public static final int FIND_TYPE = 3;
  public static final int FIND_RESULT_TYPE = 4;

  /** Set based on type. */
  public final Publish publish;

  public final PublishResult publishResult;
  public final Remove remove;
  public final Find find;
  public final FindResult findResult;

  private static final String TYPE_KEY = "sep_type";
  private static final Method GET_IBINDER;
  private static final Method PUT_IBINDER;

  private SpeakEasyProtocol(Publish p) {
    this.type = PUBLISH_TYPE;
    this.publish = p;
    this.publishResult = null;
    this.remove = null;
    this.find = null;
    this.findResult = null;
  }

  @Override
  public String toString() {
    return String.format(
        "SpeakEasyProtocol{ type: %d, publish: %s, publishResult: %s, remove: %s, find: %s,"
            + "findResult: %s }",
        type, publish, publishResult, remove, find, findResult);
  }

  private SpeakEasyProtocol(PublishResult pr) {
    this.type = PUBLISH_RESULT_TYPE;
    this.publish = null;
    this.publishResult = pr;
    this.remove = null;
    this.find = null;
    this.findResult = null;
  }

  private SpeakEasyProtocol(Remove r) {
    this.type = REMOVE_TYPE;
    this.publish = null;
    this.publishResult = null;
    this.remove = r;
    this.find = null;
    this.findResult = null;
  }

  private SpeakEasyProtocol(Find f) {
    this.type = FIND_TYPE;
    this.publish = null;
    this.publishResult = null;
    this.remove = null;
    this.find = f;
    this.findResult = null;
  }

  private SpeakEasyProtocol(FindResult fr) {
    this.type = FIND_RESULT_TYPE;
    this.publish = null;
    this.publishResult = null;
    this.remove = null;
    this.find = null;
    this.findResult = fr;
  }

  /**
   * Decodes a bundle into a SpeakEasyProtocol object.
   *
   * @param b a Bundle (nullable)
   * @return A SpeakEasyProtocol - or null if invalid.
   */
  public static SpeakEasyProtocol fromBundle(Bundle b) {
    if (null == b) {
      Log.w(TAG, "Null bundle");
      return null;
    }
    switch (b.getInt(TYPE_KEY, -1)) {
      case PUBLISH_TYPE:
        return Publish.fromBundle(b);
      case PUBLISH_RESULT_TYPE:
        return PublishResult.fromBundle(b);
      case REMOVE_TYPE:
        return Remove.fromBundle(b);
      case FIND_TYPE:
        return Find.fromBundle(b);
      case FIND_RESULT_TYPE:
        return FindResult.fromBundle(b);
      default:
        Log.w(TAG, "Invalid/missing sep_type: " + b.getInt(TYPE_KEY, -1));
        return null;
    }
  }

  /** Represents a publish command to speakeasy. */
  public static final class Publish {
    /** The key to publish this IBinder under. */
    public final String key;

    /** The IBinder to publish. */
    public final IBinder value;

    /** A ResultReceiver to handle the response or failure of publishing. */
    public final ResultReceiver resultReceiver;

    private static final String KEY_KEY = "sep_pub_key";
    private static final String IBINDER_KEY = "sep_pub_ib";
    private static final String RESULT_KEY = "sep_pub_rr";

    private Publish(String key, IBinder value, ResultReceiver resultReceiver) {
      this.key = key;
      this.value = value;
      this.resultReceiver = resultReceiver;
    }

    @Override
    public String toString() {
      return String.format(
          "Publish: {key: %s, value: %s, resultReceiver: %s}", key, value, resultReceiver);
    }

    private static SpeakEasyProtocol fromBundle(Bundle b) {
      Publish p =
          new Publish(
              b.getString(KEY_KEY),
              getBinder(b, IBINDER_KEY),
              (ResultReceiver) b.getParcelable(RESULT_KEY));
      if (null == p.key) {
        Log.w(TAG, String.format("'%s': not set", KEY_KEY));
        return null;
      }
      if (null == p.value) {
        Log.w(TAG, String.format("'%s': not set", IBINDER_KEY));
        return null;
      }
      if (null == p.resultReceiver) {
        Log.w(TAG, String.format("'%s': not set", RESULT_KEY));
        return null;
      }
      return new SpeakEasyProtocol(p);
    }

    /** Builds a publish command into a bundle. */
    public static Bundle asBundle(String key, IBinder ib, ResultReceiver rr) {
      Bundle b = new Bundle();
      b.putInt(TYPE_KEY, PUBLISH_TYPE);
      b.putString(KEY_KEY, checkNotNull(key));
      putBinder(b, IBINDER_KEY, checkNotNull(ib));
      b.putParcelable(RESULT_KEY, marshableReceiver(checkNotNull(rr)));
      return b;
    }
  }

  /** Represents a publish response from speakeasy. */
  public static final class PublishResult {
    /** The key that this message is about. */
    public final String key;

    /** Whether or not the IBinder was published. */
    public final boolean published;

    /** An error message if publishing failed. */
    public final String error;

    private static final String KEY_KEY = "sep_pr_key";
    private static final String PUBLISHED_KEY = "sep_pr_published";
    private static final String ERROR_KEY = "sep_pr_err";

    private PublishResult(String key, boolean published, String error) {
      this.key = key;
      this.published = published;
      this.error = error;
    }

    @Override
    public String toString() {
      return String.format(
          "PublishResult: {key: %s, published: %s, error: %s}", key, published, error);
    }

    /**
     * Encodes a publish result into a bundle.
     *
     * @param published if the IBinder has been published.
     * @param error a message to tell the caller about how broken things are.
     * @return a bundle
     * @throws NullPointerException if not published and error is null.
     */
    public static Bundle asBundle(String key, boolean published, String error) {
      Bundle b = new Bundle();
      b.putInt(TYPE_KEY, PUBLISH_RESULT_TYPE);
      checkNotNull(key);
      b.putString(KEY_KEY, key);

      if (!published) {
        checkNotNull(error);
        b.putString(ERROR_KEY, error);
      }
      b.putBoolean(PUBLISHED_KEY, published);
      return b;
    }

    private static SpeakEasyProtocol fromBundle(Bundle b) {
      PublishResult pr =
          new PublishResult(
              b.getString(KEY_KEY), b.getBoolean(PUBLISHED_KEY), b.getString(ERROR_KEY));
      if (null == pr.key) {
        Log.w(TAG, String.format("'%s': not set", KEY_KEY));
        return null;
      }
      return new SpeakEasyProtocol(pr);
    }
  }

  /** Represents a Find request to SpeakEasy. */
  public static class Find {
    /** The key to search for. */
    public final String key;

    /** A ResultReceiver to be called with the search results. */
    public final ResultReceiver resultReceiver;

    private static final String KEY_KEY = "sep_find_key";
    private static final String RESULT_KEY = "sep_find_rr";

    private Find(String key, ResultReceiver resultReceiver) {
      this.key = key;
      this.resultReceiver = resultReceiver;
    }

    @Override
    public String toString() {
      return String.format("Find: {key: %s, resultReceiver: %s}", key, resultReceiver);
    }

    private static SpeakEasyProtocol fromBundle(Bundle b) {
      Find f = new Find(b.getString(KEY_KEY), (ResultReceiver) b.getParcelable(RESULT_KEY));
      if (null == f.key) {
        Log.w(TAG, String.format("'%s': not set", KEY_KEY));
        return null;
      }
      if (null == f.resultReceiver) {
        Log.w(TAG, String.format("'%s': not set", RESULT_KEY));
        return null;
      }
      return new SpeakEasyProtocol(f);
    }

    /**
     * Encodes a find request into a bundle.
     *
     * @param key the key to search for.
     * @param rr the ResultReceiver to send the results to.
     * @return a bundle
     * @throws NullPointerException if you do not provide the right parameters.
     */
    public static Bundle asBundle(String key, ResultReceiver rr) {
      Bundle b = new Bundle();
      b.putInt(TYPE_KEY, FIND_TYPE);
      b.putString(KEY_KEY, checkNotNull(key));
      b.putParcelable(RESULT_KEY, marshableReceiver(checkNotNull(rr)));
      return b;
    }
  }

  /** The result of a find operation on SpeakEasy. */
  public static class FindResult {
    /** Whether or not the IBinder was found. */
    public final Boolean found;

    /** The IBinder which was found. */
    public final IBinder binder;

    /** An error that caused the search to fail. */
    public final String error;

    private static final String FOUND_KEY = "sep_fr_found";
    private static final String BINDER_KEY = "sep_fr_binder";
    private static final String ERROR_KEY = "sep_fr_error";

    private FindResult(boolean found, IBinder binder, String error) {
      this.found = found;
      this.binder = binder;
      this.error = error;
    }

    @Override
    public String toString() {
      return String.format("FindResult: {found: %s, binder: %s, error: %s}", found, binder, error);
    }

    private static SpeakEasyProtocol fromBundle(Bundle b) {
      FindResult fr =
          new FindResult(
              b.getBoolean(FOUND_KEY, false), getBinder(b, BINDER_KEY), b.getString(ERROR_KEY));
      return new SpeakEasyProtocol(fr);
    }

    /**
     * Encodes the result of a find operation into a bundle.
     *
     * @param found whether or not the IBinder was found
     * @param binder the located IBinder
     * @param error the problem finding the thing.
     * @return A bundle that can be converted into a SpeakEasyProtocol
     * @throws NullPointerException if a IBinder is not provide for a successful find or an error is
     *     not provided on a failure.
     */
    public static Bundle asBundle(boolean found, IBinder binder, String error) {
      Bundle b = new Bundle();
      b.putInt(TYPE_KEY, FIND_RESULT_TYPE);
      b.putBoolean(FOUND_KEY, found);
      if (!found) {
        b.putString(ERROR_KEY, checkNotNull(error));
        return b;
      }
      putBinder(b, BINDER_KEY, checkNotNull(binder));
      return b;
    }
  }

  /** Indicates a request to remove a IBinder from SpeakEasy. */
  public static class Remove {
    /** The key to remove. */
    public final String key;

    private static final String KEY_KEY = "sep_rm_key";

    public Remove(String key) {
      this.key = key;
    }

    @Override
    public String toString() {
      return String.format("Remove: {key: %s}", key);
    }

    /**
     * Encodes a remove request into a bundle.
     *
     * @param key the Key representing the IBinder to remove
     * @return a bundle representing the command.
     * @throws NullPointerException if key is null.
     */
    public static Bundle asBundle(String key) {
      Bundle b = new Bundle();
      b.putInt(TYPE_KEY, REMOVE_TYPE);
      b.putString(KEY_KEY, checkNotNull(key));
      return b;
    }

    private static SpeakEasyProtocol fromBundle(Bundle b) {
      Remove r = new Remove(b.getString(KEY_KEY));
      if (null == r.key) {
        Log.w(TAG, String.format("'%s': not set", KEY_KEY));
        return null;
      }
      return new SpeakEasyProtocol(r);
    }
  }

  private static <T> T checkNotNull(T val) {
    if (null == val) {
      throw new NullPointerException();
    }
    return val;
  }

  /** Strips the custom subclass out of the ResultReceiver so you can ship between packages. */
  private static ResultReceiver marshableReceiver(ResultReceiver r) {
    if (r.getClass().equals(ResultReceiver.class)) {
      return r;
    }
    Parcel p = Parcel.obtain();
    try {
      r.writeToParcel(p, 0);
      p.setDataPosition(0);
      return ResultReceiver.CREATOR.createFromParcel(p);
    } finally {
      p.recycle();
    }
  }

  static {
    Method getIBinder = null;
    Method putIBinder = null;
    if (Build.VERSION.SDK_INT < 18) {
      try {
        getIBinder = Bundle.class.getMethod("getIBinder", String.class);
        putIBinder = Bundle.class.getMethod("putIBinder", String.class, IBinder.class);
      } catch (NoSuchMethodException nsme) {
        Log.e(TAG, "Cannot find methods for IBinders on bundle object", nsme);
        throw new RuntimeException(nsme);
      }
    }
    GET_IBINDER = getIBinder;
    PUT_IBINDER = putIBinder;
  }

  /** Gets an IBinder from a bundle safely. */
  private static IBinder getBinder(Bundle b, String key) {
    if (null != GET_IBINDER) {
      try {
        return (IBinder) GET_IBINDER.invoke(b, key);
      } catch (InvocationTargetException | IllegalAccessException ex) {
        throw new RuntimeException(ex);
      }
    }
    return b.getBinder(key);
  }

  /** Puts an IBinder in a bundle safely. */
  private static void putBinder(Bundle b, String key, IBinder val) {
    if (null != PUT_IBINDER) {
      try {
        PUT_IBINDER.invoke(b, key, val);
        return;
      } catch (InvocationTargetException | IllegalAccessException ex) {
        throw new RuntimeException(ex);
      }
    }
    b.putBinder(key, val);
  }
}
