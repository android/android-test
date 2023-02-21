/*
 * Copyright (C) 2015 The Android Open Source Project
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

package androidx.test.espresso.web.bridge;

import static androidx.test.internal.util.Checks.checkNotNull;

import androidx.concurrent.futures.ResolvableFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * A mechanism to get results out of a Javascript context and into a Java context.
 *
 * <p>Users can get instances of this class via JavaScriptBridge.makeConduit(). Each conduit can be
 * used once (and only once) to transmit results. Before evaluating javascript via a loadUrl call
 * the caller should call wrapScriptInConduit with the script to be evaluated. The script is wrapped
 * up into an additional handler statement which forwards the result of the script to the
 * JavaScriptBridge object. After calling loadUrl the caller can use the getResult method to get a
 * Future which will contain the result of the javascript execution.
 */
public final class Conduit {
  private final String bridgeName;
  private final String errorMethod;
  private final String successMethod;
  private final String token;
  private final ResolvableFuture<String> jsResult;

  private Conduit(Builder builder) {
    this.bridgeName = checkNotNull(builder.bridgeName);
    this.errorMethod = checkNotNull(builder.errorMethod);
    this.successMethod = checkNotNull(builder.successMethod);
    this.token = checkNotNull(builder.token);
    this.jsResult = checkNotNull(builder.jsResult);
  }

  /**
   * Takes Javascript code and wraps it within a statement that will pipe the results of
   * evaluation to the ListenableFuture this conduit holds.
   */
  public String wrapScriptInConduit(String script) {
    checkNotNull(script);
    return wrapScriptInConduit(new StringBuilder(script)).toString();
  }

  /**
   * Wraps a script within additional javascript code that will allow the function to
   * return its results back thru this conduit.
   *
   * @param script the buffer holding the script, it will be modified in place.
   * @return the StringBuilder passed in.
   */
  public StringBuilder wrapScriptInConduit(StringBuilder script) {
    String preamble = "try{" +
        "window." + bridgeName + "." + successMethod + "('" + token + "', ";
    script.insert(0, preamble)
        .append(");")
        .append("}catch(e){")
        .append("window.").append(bridgeName).append(".").append(errorMethod)
        .append("('").append(token).append("', 'error!');}");
    return script;
  }

  /**
   * The future that will be resolved when the Javascript evaluation completes.
   */
  public ListenableFuture<String> getResult() {
    return jsResult;
  }

  /** Allows JavascriptBoundBridge to set the result of javascript execution. */
  ResolvableFuture<String> internalGetResult() {
    return jsResult;
  }

  String getToken() {
    return token;
  }

  static class Builder {
    private String bridgeName;
    private String errorMethod;
    private String successMethod;
    private String token;
    private ResolvableFuture<String> jsResult;

    @CanIgnoreReturnValue
    public Builder withBridgeName(String bridgeName) {
      this.bridgeName = checkNotNull(bridgeName);
      return this;
    }

    @CanIgnoreReturnValue
    public Builder withErrorMethod(String errorMethod) {
      this.errorMethod = checkNotNull(errorMethod);
      return this;
    }

    @CanIgnoreReturnValue
    public Builder withSuccessMethod(String successMethod) {
      this.successMethod = checkNotNull(successMethod);
      return this;
    }

    @CanIgnoreReturnValue
    public Builder withToken(String token) {
      this.token = checkNotNull(token);
      return this;
    }

    @CanIgnoreReturnValue
    public Builder withJsResult(ResolvableFuture<String> jsResult) {
      this.jsResult = checkNotNull(jsResult);
      return this;
    }

    public Conduit build() {
      return new Conduit(this);
    }
  }
}
