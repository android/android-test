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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.runner.JUnitCore.runClasses;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

/** Tests for {@link PortForwardingRule} */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class PortForwardingRuleTest {

  @Test
  public void ruleBuilderTest() {
    int proxyPort = 9090;
    String proxyHost = "192.168.1.5";
    Properties properties = new Properties(System.getProperties());
    PortForwardingRule portForwardingRule =
        new PortForwardingRule.Builder()
            .withProxyPort(proxyPort)
            .withProxyHost(proxyHost)
            .withProperties(properties)
            .build();
    assertEquals(portForwardingRule.proxyHost, proxyHost);
    assertEquals(portForwardingRule.proxyPort, proxyPort);
    assertEquals(portForwardingRule.prop, properties);
  }

  public static class ExecutionOrderTest {

    private static final Properties TEST_PROPERTIES = new Properties(System.getProperties());

    private static StringBuilder log = new StringBuilder();

    @Rule
    public PortForwardingRule portForwardingRule =
        new PortForwardingRule(
            PortForwardingRule.DEFAULT_PROXY_HOST,
            PortForwardingRule.DEFAULT_PROXY_PORT,
            TEST_PROPERTIES) {
          @Override
          protected void beforePortForwarding() {
            log.append("beforePortForwarding ");
          }

          @Override
          protected void afterPortForwarding() {
            log.append("afterPortForwarding ");
          }

          @Override
          protected void beforeRestoreForwarding() {
            log.append("beforeRestoreForwarding ");
          }

          @Override
          protected void afterRestoreForwarding() {
            log.append("afterRestoreForwarding ");
          }
        };

    @Before
    public void before() {
      log.append("before ");
    }

    @Test
    public void fails() {
      log.append("test ");
      fail("This is execution order test");
    }

    @After
    public void after() {
      log.append("after ");
    }
  }

  @Test
  public void executionOrderTest() {
    Result result = runClasses(ExecutionOrderTest.class);
    assertEquals(result.getFailureCount(), 1);
    assertEquals(result.getFailures().get(0).getMessage(), "This is execution order test");
    assertEquals(
        ExecutionOrderTest.log.toString(),
        "beforePortForwarding "
            + "afterPortForwarding before test after beforeRestoreForwarding afterRestoreForwarding ");
  }

  public static class CheckPropBeforeAndAfterPortForwardingTest {

    private static final String SET_PROXY_HOST = "127.0.0.1";
    private static final int SET_PROXY_PORT = 9090;
    private static final Properties TEST_PROPERTIES = new Properties(System.getProperties());
    private static final String ORIGINAL_HTTP_HOST =
        TEST_PROPERTIES.getProperty(PortForwardingRule.HTTP_HOST_PROPERTY);
    private static final String ORIGINAL_HTTP_PORT =
        TEST_PROPERTIES.getProperty(PortForwardingRule.HTTP_PORT_PROPERTY);
    private static final String ORIGINAL_HTTPS_HOST =
        TEST_PROPERTIES.getProperty(PortForwardingRule.HTTPS_HOST_PROPERTY);
    private static final String ORIGINAL_HTTPS_PORT =
        TEST_PROPERTIES.getProperty(PortForwardingRule.HTTPS_PORT_PROPERTY);

    @Rule
    public PortForwardingRule portForwardingRule =
        new PortForwardingRule(SET_PROXY_HOST, SET_PROXY_PORT, TEST_PROPERTIES) {
          @Override
          protected void beforePortForwarding() {
            verifyOriginalProperties(prop);
          }

          @Override
          protected void afterPortForwarding() {
            verifySetProperties(prop);
          }

          @Override
          protected void beforeRestoreForwarding() {
            verifySetProperties(prop);
          }

          @Override
          protected void afterRestoreForwarding() {
            verifyOriginalProperties(prop);
          }
        };

    @Test
    public void test() {}

    private static void verifyOriginalProperties(Properties prop) {
      assertEquals(prop.getProperty(PortForwardingRule.HTTP_HOST_PROPERTY), ORIGINAL_HTTP_HOST);
      assertEquals(prop.getProperty(PortForwardingRule.HTTPS_HOST_PROPERTY), ORIGINAL_HTTPS_HOST);
      assertEquals(prop.getProperty(PortForwardingRule.HTTP_PORT_PROPERTY), ORIGINAL_HTTP_PORT);
      assertEquals(prop.getProperty(PortForwardingRule.HTTPS_PORT_PROPERTY), ORIGINAL_HTTPS_PORT);
    }

    private static void verifySetProperties(Properties prop) {
      assertEquals(prop.getProperty(PortForwardingRule.HTTP_HOST_PROPERTY), SET_PROXY_HOST);
      assertEquals(prop.getProperty(PortForwardingRule.HTTPS_HOST_PROPERTY), SET_PROXY_HOST);
      assertEquals(
          prop.getProperty(PortForwardingRule.HTTP_PORT_PROPERTY), String.valueOf(SET_PROXY_PORT));
      assertEquals(
          prop.getProperty(PortForwardingRule.HTTPS_PORT_PROPERTY), String.valueOf(SET_PROXY_PORT));
    }
  }

  @Test
  public void checkPropBeforeAndAfterPortForwardingTest() {
    Result result = runClasses(CheckPropBeforeAndAfterPortForwardingTest.class);
    assertEquals(result.getFailureCount(), 0);
  }
}
