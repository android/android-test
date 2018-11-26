/*
 * Copyright (C) 2016 The Android Open Source Project
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

package androidx.test.rule;

import static androidx.test.internal.util.Checks.checkArgument;
import static androidx.test.internal.util.Checks.checkNotNull;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import android.util.Log;
import androidx.test.annotation.Beta;
import java.util.Properties;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A {@code TestRule} to forward network traffic to a specific port. By default all traffic is
 * forwarded to default address {@code #DEFAULT_PROXY_HOST}:{@code #DEFAULT_PROXY_PORT} unless
 * otherwise specified
 *
 * <p>Note: Traffic forwarding will only apply to the current process under test.
 *
 * <p><b>This API is currently in beta.</b>
 *
 * @hide
 */
@Beta
public class PortForwardingRule implements TestRule {

  private static final String TAG = "PortForwardingRule";

  public static final int MIN_PORT = 1024;
  public static final int MAX_PORT = 65535;

  @VisibleForTesting static final int DEFAULT_PROXY_PORT = 8080;
  @VisibleForTesting static final String DEFAULT_PROXY_HOST = "127.0.0.1";
  @VisibleForTesting static final String HTTP_HOST_PROPERTY = "http.proxyHost";
  @VisibleForTesting static final String HTTPS_HOST_PROPERTY = "https.proxyHost";
  @VisibleForTesting static final String HTTP_PORT_PROPERTY = "http.proxyPort";
  @VisibleForTesting static final String HTTPS_PORT_PROPERTY = "https.proxyPort";

  @VisibleForTesting final String proxyHost;
  @VisibleForTesting final int proxyPort;
  @VisibleForTesting Properties prop;

  private Properties backUpProp;

  /** @hide */
  public static class Builder {

    private String proxyHost = DEFAULT_PROXY_HOST;
    private int proxyPort = DEFAULT_PROXY_PORT;
    private Properties prop = System.getProperties();

    /**
     * Builder to set a specific host address to forward the network traffic to.
     *
     * @param proxyHost The host address to which the network traffic is forwarded during tests.
     */
    public Builder withProxyHost(@NonNull String proxyHost) {
      this.proxyHost = checkNotNull(proxyHost);
      return this;
    }

    /**
     * Builder to set a specific port number to forward the network traffic to.
     *
     * @param proxyPort The port number to which the network traffic is forwarded during tests.
     */
    public Builder withProxyPort(int proxyPort) {
      checkArgument(
          proxyPort >= MIN_PORT && proxyPort <= MAX_PORT,
          "%d is used as a proxy port, must in range [%d, %d]",
          proxyPort,
          MIN_PORT,
          MAX_PORT);
      this.proxyPort = proxyPort;
      return this;
    }

    /**
     * Builder which allows to pass a {@link Properties} object for testing. This will help to avoid
     * the system properties being affected by tests.
     *
     * @param properties A pre-constructed properties object for testing.
     */
    public Builder withProperties(@NonNull Properties properties) {
      prop = checkNotNull(properties);
      return this;
    }

    public PortForwardingRule build() {
      return new PortForwardingRule(this);
    }
  }

  private PortForwardingRule(Builder builder) {
    this(builder.proxyHost, builder.proxyPort, builder.prop);
  }

  protected PortForwardingRule(int proxyPort) {
    this(DEFAULT_PROXY_HOST, proxyPort, System.getProperties());
  }

  @VisibleForTesting
  PortForwardingRule(String proxyHost, int proxyPort, @NonNull Properties properties) {
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
    prop = checkNotNull(properties);
    backUpProp = new Properties();
    backUpProperties();
  }

  protected static int getDefaultPort() {
    return DEFAULT_PROXY_PORT;
  }

  /**
   * Override this method to execute any code that should run before port forwarding. This method is
   * called before each test method, including any method annotated with <a
   * href="http://junit.sourceforge.net/javadoc/org/junit/Before.html"><code>Before</code></a>.
   */
  protected void beforePortForwarding() {
    // empty by default
  }

  /**
   * Override this method to execute any code that should run after port forwarding is set up, but
   * before any test code is run including any method annotated with <a
   * href="http://junit.sourceforge.net/javadoc/org/junit/Before.html"><code>Before</code></a>.
   */
  protected void afterPortForwarding() {
    // empty by default
  }

  /**
   * Override this method to execute any code that should run before port forwarding is restored.
   * This method is called after each test method, including any method annotated with <a
   * href="http://junit.sourceforge.net/javadoc/org/junit/After.html"><code>After</code></a>.
   */
  protected void beforeRestoreForwarding() {
    // empty by default
  }

  /**
   * Override this method to execute any code that should run after port forwarding is restored.
   * This method is called after each test method, including any method annotated with <a
   * href="http://junit.sourceforge.net/javadoc/org/junit/After.html"><code>After</code></a>.
   */
  protected void afterRestoreForwarding() {
    // empty by default
  }

  /**
   * Set the port forwarding system properties (Note: for the process under test only). This method
   * will also back up the existing system properties for later to restore.
   */
  private void setPortForwarding() {
    beforePortForwarding();
    prop.setProperty(HTTP_HOST_PROPERTY, proxyHost);
    prop.setProperty(HTTPS_HOST_PROPERTY, proxyHost);
    prop.setProperty(HTTP_PORT_PROPERTY, String.valueOf(proxyPort));
    prop.setProperty(HTTPS_PORT_PROPERTY, String.valueOf(proxyPort));
    afterPortForwarding();
  }

  /** Restore the system properties backed up (Note: for the process under test only) */
  private void restorePortForwarding() {
    try {
      beforeRestoreForwarding();
    } finally {
      restoreOneProperty(prop, backUpProp, HTTP_HOST_PROPERTY);
      restoreOneProperty(prop, backUpProp, HTTPS_HOST_PROPERTY);
      restoreOneProperty(prop, backUpProp, HTTP_PORT_PROPERTY);
      restoreOneProperty(prop, backUpProp, HTTPS_PORT_PROPERTY);
      afterRestoreForwarding();
    }
  }

  private void backUpProperties() {
    if (prop.getProperty(HTTP_HOST_PROPERTY) != null) {
      backUpProp.setProperty(HTTP_HOST_PROPERTY, prop.getProperty(HTTP_HOST_PROPERTY));
    }
    if (prop.getProperty(HTTPS_HOST_PROPERTY) != null) {
      backUpProp.setProperty(HTTPS_HOST_PROPERTY, prop.getProperty(HTTPS_HOST_PROPERTY));
    }
    if (prop.getProperty(HTTP_PORT_PROPERTY) != null) {
      backUpProp.setProperty(HTTP_PORT_PROPERTY, prop.getProperty(HTTP_PORT_PROPERTY));
    }
    if (prop.getProperty(HTTPS_PORT_PROPERTY) != null) {
      backUpProp.setProperty(HTTPS_PORT_PROPERTY, prop.getProperty(HTTPS_PORT_PROPERTY));
    }
  }

  private void restoreOneProperty(Properties prop, Properties backUpProp, String key) {
    if (backUpProp.getProperty(key) != null) {
      prop.setProperty(key, backUpProp.getProperty(key));
    } else {
      prop.remove(key);
    }
  }

  @Override
  public Statement apply(final Statement base, Description description) {
    return new PortForwardingStatement(base);
  }

  /** {@link Statement} that set/restore port forwarding before/after the execution of the test. */
  private class PortForwardingStatement extends Statement {

    private final Statement base;

    public PortForwardingStatement(Statement base) {
      this.base = base;
    }

    @Override
    public void evaluate() throws Throwable {
      try {
        setPortForwarding();
        Log.i(
            TAG,
            String.format(
                "The current process traffic is forwarded to %s:%d", proxyHost, proxyPort));
        base.evaluate();
      } finally {
        restorePortForwarding();
        Log.i(TAG, "Current process traffic forwarding is cancelled");
      }
    }
  }
}
