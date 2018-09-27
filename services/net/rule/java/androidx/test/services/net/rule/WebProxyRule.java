/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Lice`nse is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.services.net.rule;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import androidx.test.InstrumentationRegistry;
import androidx.test.annotation.Beta;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.idling.net.UriIdlingResource;
import androidx.test.rule.PortForwardingRule;
import com.google.android.apps.common.testing.services.net.UrlNotifications;
import com.google.android.apps.common.testing.services.net.UrlNotificationsCallback;
import com.google.android.apps.common.testing.util.BackdoorTestUtil;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * {@code WebProxyRule} extends the {@link PortForwardingRule} to perform extra operations in
 * addition to port forwarding. This rule will setup and start devproxy via {@link
 * BackdoorTestUtil#sendUrlNotifications} and register for {@link UriIdlingResource} before port
 * forwarding. After test execution, the rule will perform all the necessary clean up by reverting
 * port forwarding, shutting down the devproxy and unregistering the {@link UriIdlingResource}.
 *
 * <p>This {@code Rule} coordinates with {@link UriIdlingResource} to monitor when resources become
 * reliably idle. Especially, in the case when an action triggers multiple rounds of network
 * requests/responses, the resource is only reliably idle after all responses are received and after
 * a timeout elapsed. The default timeout length is 500ms unless otherwise specified.
 *
 * <p><b>This API is currently in beta.</b>
 */
@Beta
public class WebProxyRule extends PortForwardingRule {

  private static final String TAG = "WebProxyRule";

  public static final int MIN_PORT = 1024;
  public static final int MAX_PORT = 65535;

  @VisibleForTesting static final int DEFAULT_PROXY_PORT = PortForwardingRule.getDefaultPort();
  @VisibleForTesting static final long DEFAULT_TIMEOUT_MS = 500;

  private final int proxyPort;
  private final long idleTimeoutMs;
  private final CountingResourceCallback callback;
  private boolean isProxyHealthy;
  private UriIdlingResource uriIdlingResource;

  /**
   * A Builder which enables users to optionally choose a specific port number to set up the proxy,
   * specify a timeout length, and ignore patterns. By default, the port number is {@code 8080}, the
   * timeout length is {@value DEFAULT_TIMEOUT_MS}ms and there is no ignore pattern.
   */
  public static class Builder {

    private int proxyPort = DEFAULT_PROXY_PORT;
    private long idleTimeoutMs = DEFAULT_TIMEOUT_MS;
    private List<Pattern> ignorePatterns = Collections.emptyList();

    /**
     * Builder which enables users to choose a specific port number to set up the proxy.
     *
     * @param port The port number on which the proxy will listen.
     */
    public Builder withProxyPort(int port) {
      checkArgument(
          port >= MIN_PORT && port <= MAX_PORT,
          "%d is used as a proxy port, must in range [%d, %d]",
          port,
          MIN_PORT,
          MAX_PORT);
      proxyPort = port;
      return this;
    }

    /**
     * Builder which enables users to specify a timeout length.
     *
     * @param timeoutMs The timeout length to set {@link UriIdlingResource} to idle after all
     *     network responses are received.
     */
    public Builder withTimeoutMs(long timeoutMs) {
      checkArgument(timeoutMs > 0, "%d is used as timeout length, must be > 0", timeoutMs);
      idleTimeoutMs = timeoutMs;
      return this;
    }

    /**
     * Builder which enables users to specify ignore patterns.
     *
     * @param patterns The list of patterns that are ignored by {@link UriIdlingResource}.
     */
    public Builder withIgnorePatterns(@NonNull List<Pattern> patterns) {
      ignorePatterns = checkNotNull(patterns);
      return this;
    }

    public WebProxyRule build() {
      return new WebProxyRule(this);
    }
  }

  private WebProxyRule(Builder builder) {
    this(builder.proxyPort, builder.idleTimeoutMs, builder.ignorePatterns);
  }

  @VisibleForTesting
  WebProxyRule(int proxyPort, long idleTimeoutMs, List<Pattern> ignorePatterns) {
    super(proxyPort);
    this.proxyPort = proxyPort;
    this.idleTimeoutMs = idleTimeoutMs;
    uriIdlingResource =
        new UriIdlingResource(this.getClass().getSimpleName() + "Resource", this.idleTimeoutMs);
    for (Pattern ignorePattern : ignorePatterns) {
      uriIdlingResource.ignoreUri(ignorePattern);
    }
    callback = new CountingResourceCallback();
  }

  @Override
  protected void beforePortForwarding() {
    // proxy setup (start and configure proxy)
    try {
      int sendStatus =
          BackdoorTestUtil.sendUrlNotifications(
              InstrumentationRegistry.getTargetContext(), proxyPort, callback);
      checkState(
          UrlNotifications.BAD_ARGUMENTS != sendStatus,
          "Bad arguments to call sendUrlNotifications!");
      checkState(
          UrlNotifications.OTHER != sendStatus,
          "Other errors happened when calling sendUrlNotifications");
      isProxyHealthy = true;
    } catch (IllegalStateException ise) {
      isProxyHealthy = false;
      Log.e(TAG, "error happened when calling sendUrlNotifications", ise);
    } finally {
      checkState(isProxyHealthy, "Proxy is not setup successfully");
    }

    // TODO(b/29522060): find better way to sync against proxy startUp, because currently
    // there is no good way to detect if the NotificationProxy is actually up and running
    // so sleep is needed.
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Log.e(TAG, "error happens when sleeping in beforePortForwarding", e);
    }

    // register for idlingResource
    checkState(
        Espresso.registerIdlingResources(uriIdlingResource),
        "Espresso register idling resource failed");
  }

  @Override
  protected void afterRestoreForwarding() {
    // unregister for idlingResource if registered
    if (Espresso.getIdlingResources().contains(uriIdlingResource)) {
      checkState(
          Espresso.unregisterIdlingResources(uriIdlingResource),
          "Espresso unregister idling resource failed");
    }

    // clean up proxy if setup successfully formerly
    checkState(isProxyHealthy, "Proxy had fatal error before");
    int stopStatus =
        BackdoorTestUtil.stopUrlNotifications(
            InstrumentationRegistry.getTargetContext(), proxyPort, callback);
    checkState(
        UrlNotifications.BAD_ARGUMENTS != stopStatus,
        "Bad arguments to call stopUrlNotifications!");
    checkState(
        UrlNotifications.OTHER != stopStatus,
        "Other errors happened when calling stopUrlNotifications");
  }

  private class CountingResourceCallback extends UrlNotificationsCallback.Stub {

    @Override
    public void onRequestStarted(String url) {
      Log.i(TAG, "Received request for url: " + url);
      uriIdlingResource.beginLoad(url);
    }

    @Override
    public void onResponseReceived(String url) {
      Log.i(TAG, "Received response for url: " + url);
      uriIdlingResource.endLoad(url);
    }

    @Override
    public void fatalProxyError(String why) {
      // TODO(b/30096187): get a handle on the current running test and fail it.
      Log.e(TAG, "fatalProxyError: " + why);
      isProxyHealthy = false;
    }
  }
}
