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
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link JavaScriptBridgeInstaller} for aosp-browser based WebViews (default on API level 18 and
 * lower).
 */
final class AndroidJavaScriptBridgeInstaller {
  private static final JavaScriptBoundBridge boundBridge = new JavaScriptBoundBridge();
  private static final String WEB_CORE_CLAZZ = "android.webkit.WebViewCore";
  private static final String WEB_CORE_HANDLER = "sWebCoreHandler";
  private static final String JAVASCRIPT_INTERFACES = "mJavascriptInterfaces";
  private static final String CALLBACK_PROXY_CLAZZ = "android.webkit.CallbackProxy";
  private static final String SET_WEB_CHROME_CLIENT_METHOD = "setWebChromeClient";
  private static final String CALLBACK_PROXY_FIELD = "mCallbackProxy";

  public JavaScriptBoundBridge install()
      throws JavaScriptBridgeInstallException {
    try {
      Class<?> webCoreClazz = Class.forName(WEB_CORE_CLAZZ);
      Field webCoreHandlerField = webCoreClazz.getDeclaredField(WEB_CORE_HANDLER);
      Field javascriptInterfacesField = webCoreClazz.getDeclaredField(JAVASCRIPT_INTERFACES);
      Field callbackProxyField = null;
      Method setWebChromeClientMethod = null;
      if (Build.VERSION.SDK_INT < 13) {
        callbackProxyField = webCoreClazz.getDeclaredField(CALLBACK_PROXY_FIELD);
        Class<?> callbackProxyClazz = Class.forName(CALLBACK_PROXY_CLAZZ);
        setWebChromeClientMethod = callbackProxyClazz.getDeclaredMethod(
            SET_WEB_CHROME_CLIENT_METHOD, WebChromeClient.class);
        callbackProxyField.setAccessible(true);
        setWebChromeClientMethod.setAccessible(true);
      }

      webCoreHandlerField.setAccessible(true);
      javascriptInterfacesField.setAccessible(true);
      Handler webCoreHandler = null;
      synchronized (webCoreClazz) {
        webCoreHandler = (Handler) webCoreHandlerField.get(null);
        if (null != webCoreHandler) {
          Log.w(JavaScriptBridge.TAG, "Initializing late - some webviews may be unbridged.");
        }
      }

      if (null == webCoreHandler) {
        // TODO(b/227119444): should the constructed instance be used?
        WebView unused = new WebView(getInstrumentation().getTargetContext());
        while (null == webCoreHandler) {
          synchronized (webCoreClazz) {
            webCoreHandler = (Handler) webCoreHandlerField.get(null);
          }
        }
      }


      Handler instrumentedHandler = new WebCoreHandlerSpy(webCoreHandler,
          javascriptInterfacesField, callbackProxyField, setWebChromeClientMethod);
      synchronized (webCoreClazz) {
        webCoreHandlerField.set(null, instrumentedHandler);
      }

    } catch (ClassNotFoundException cnfe) {
      throw new JavaScriptBridgeInstallException(cnfe);
    } catch (NoSuchFieldException nsfe) {
      throw new JavaScriptBridgeInstallException(nsfe);
    } catch (NoSuchMethodException nsme) {
      throw new JavaScriptBridgeInstallException(nsme);
    } catch (IllegalAccessException iae) {
      throw new JavaScriptBridgeInstallException(iae);
    }
    Log.i(JavaScriptBridge.TAG, "Initialized web view bridging for android WebView.");
    return boundBridge;
  }

  private static final class WebCoreHandlerSpy extends Handler {
    private final Handler realHandler;
    private final Field javascriptInterfacesField;
    private final Field callbackProxyField;
    private final Method setWebViewClientMethod;

    private WebCoreHandlerSpy(Handler realHandler, Field javascriptInterfacesField,
        Field callbackProxyField, Method setWebViewClientMethod) {
      super(realHandler.getLooper());
      this.realHandler = checkNotNull(realHandler);
      this.javascriptInterfacesField = checkNotNull(javascriptInterfacesField);
      // nullables.
      this.callbackProxyField = callbackProxyField;
      this.setWebViewClientMethod = setWebViewClientMethod;
    }

    // Override this method to detect when new WebViewCore's are being initialized
    // We do the injection of the JavaScriptInterfaces field here to ensure that
    // subwindows get our javascript bridge variable.
    @Override
    public boolean sendMessageAtTime(Message message, long delayMillis) {
      // 0 is the initialize message.
      if (message.what == 0) {
        // and it's sent on main.
        try {
          @SuppressWarnings("unchecked")
          Map<Object, Object> jsInterfaces =
              (Map<Object, Object>) javascriptInterfacesField.get(message.obj);
          if (null == jsInterfaces) {
            jsInterfaces = new HashMap<>();
            javascriptInterfacesField.set(message.obj, jsInterfaces);
          }
          jsInterfaces.put(JavaScriptBridge.JS_BRIDGE_NAME, boundBridge);
          if (Build.VERSION.SDK_INT < 13) {
            // progress is not reported unless a webchromeclient is installed on the webview.
            // Knowing progress helps write code that doesn't run while we're reloading.
            // since this code is running in the constructor of WebView - lets install a
            // a webchromeclient immedately. It doesn't need to do anything - just its presence
            // will propagate progress.
            Object callbackProxy = callbackProxyField.get(message.obj);
            setWebViewClientMethod.invoke(callbackProxy, new WebChromeClient());
          }
        } catch (IllegalAccessException iae) {
          Log.e(JavaScriptBridge.TAG, "Couldn't initialize js bridge in webview!", iae);
        } catch (InvocationTargetException ite) {
          Log.e(JavaScriptBridge.TAG, "Couldn't initialize js bridge in webview!", ite);
        }
      }
      return super.sendMessageAtTime(message, delayMillis);
    }

    @Override
    public void handleMessage(Message message) {
      realHandler.handleMessage(message);
    }
  }
}

