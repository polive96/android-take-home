/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;

import org.apache.cordova.engine.SystemWebViewEngine;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class for interacting with a Cordova webview. Manages plugins, events, and a CordovaWebViewEngine.
 * Class uses two-phase initialization. You must call init() before calling any other methods.
 */
public class CordovaServiceWebViewImpl extends CordovaWebViewImpl implements CordovaServiceWebView {

    public static final String TAG = "CordovaServiceWebViewImpl";

    protected final CordovaWebViewEngine engine;
    private CordovaServiceInterface cordova;

    public static CordovaWebViewEngine createEngine(Context context, CordovaPreferences preferences) {
        String className = SystemWebViewEngine.class.getCanonicalName();
        try {
            Class<?> webViewClass = Class.forName(className);
            Constructor<?> constructor = webViewClass.getConstructor(Context.class, CordovaPreferences.class);
            return (SystemWebViewEngine) constructor.newInstance(context, preferences);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create webview. ", e);
        }
    }

    public CordovaServiceWebViewImpl(CordovaWebViewEngine cordovaWebViewEngine) {
        super(cordovaWebViewEngine);
        this.engine = cordovaWebViewEngine;
    }

    // Convenience method for when creating programmatically (not from Config.xml).
    public void init(CordovaInterface cordova) {
        init(cordova, new ArrayList<>(), new CordovaPreferences());
    }

    @SuppressLint("Assert")
    @Override
    public void init(CordovaServiceInterface cordova, List<PluginEntry> pluginEntries, CordovaPreferences preferences) {
        if (this.cordova != null) {
            throw new IllegalStateException();
        }
        this.cordova = cordova;
        this.preferences = preferences;
        pluginManager = new PluginManager(this, cordova, pluginEntries);
        resourceApi = new CordovaResourceApi(engine.getView().getContext(), pluginManager);
        nativeToJsMessageQueue = new NativeToJsMessageQueue();
        nativeToJsMessageQueue.addBridgeMode(new NativeToJsMessageQueue.NoOpBridgeMode());
        nativeToJsMessageQueue.addBridgeMode(new NativeToJsMessageQueue.LoadUrlBridgeMode(engine, (CordovaInterface) cordova));

        if (preferences.getBoolean("DisallowOverscroll", false)) {
            engine.getView().setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        engine.init(this, cordova, engineClient, resourceApi, pluginManager, nativeToJsMessageQueue);
        // This isn't enforced by the compiler, so assert here.
        assert engine.getView() instanceof CordovaWebViewEngine.EngineView;

        pluginManager.addService(CoreAndroid.PLUGIN_NAME, "org.apache.cordova.CoreAndroid");
        pluginManager.init();

    }

    @Override
    public boolean isInitialized() {
        return cordova != null;
    }

    @Override
    public void loadUrlIntoView(final String url, boolean recreatePlugins) {
        LOG.d(TAG, ">>> loadUrl(" + url + ")");
        if (url.equals("about:blank") || url.startsWith("javascript:")) {
            engine.loadUrl(url, false);
            return;
        }

        recreatePlugins = recreatePlugins || (loadedUrl == null);

        if (recreatePlugins) {
            // Don't re-initialize on first load.
            if (loadedUrl != null) {
                appPlugin = null;
                pluginManager.init();
            }
            loadedUrl = url;
        }

        // Create a timeout timer for loadUrl
        final int currentLoadUrlTimeout = loadUrlTimeout;
        final int loadUrlTimeoutValue = preferences.getInteger("LoadUrlTimeoutValue", 20000);

        // Timeout error method
        final Runnable loadError = new Runnable() {
            public void run() {
                stopLoading();
                LOG.e(TAG, "CordovaWebView: TIMEOUT ERROR!");

                // Handle other errors by passing them to the webview in JS
                JSONObject data = new JSONObject();
                try {
                    data.put("errorCode", -6);
                    data.put("description", "The connection to the server was unsuccessful.");
                    data.put("url", url);
                } catch (JSONException e) {
                    // Will never happen.
                }
                pluginManager.postMessage("onReceivedError", data);
            }
        };

        // Timeout timer method
        final Runnable timeoutCheck = new Runnable() {
            public void run() {
                try {
                    synchronized (this) {
                        wait(loadUrlTimeoutValue);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // If timeout, then stop loading and handle error
                if (loadUrlTimeout == currentLoadUrlTimeout) {
                    Handler handler = new Handler();
                    handler.post(loadError);
                }
            }
        };

        final boolean _recreatePlugins = recreatePlugins;
        new Thread(() -> {
            if (loadUrlTimeoutValue > 0) {
                cordova.getThreadPool().execute(timeoutCheck);
            }
            engine.loadUrl(url, _recreatePlugins);
        }).run();
    }


    private void sendJavascriptEvent(String event) {
        if (appPlugin == null) {
            appPlugin = (CoreAndroid)pluginManager.getPlugin(CoreAndroid.PLUGIN_NAME);
        }

        if (appPlugin == null) {
            LOG.w(TAG, "Unable to fire event without existing plugin");
            return;
        }
        appPlugin.fireJavascriptEvent(event);
    }


    protected class EngineClient implements CordovaWebViewEngine.Client {
        @Override
        public void clearLoadTimeoutTimer() {
            loadUrlTimeout++;
        }

        @Override
        public void onPageStarted(String newUrl) {
            LOG.d(TAG, "onPageDidNavigate(" + newUrl + ")");
            boundKeyCodes.clear();
            pluginManager.onReset();
            pluginManager.postMessage("onPageStarted", newUrl);
        }

        @Override
        public void onReceivedError(int errorCode, String description, String failingUrl) {
            clearLoadTimeoutTimer();
            JSONObject data = new JSONObject();
            try {
                data.put("errorCode", errorCode);
                data.put("description", description);
                data.put("url", failingUrl);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pluginManager.postMessage("onReceivedError", data);
        }

        @Override
        public void onPageFinishedLoading(String url) {
            LOG.d(TAG, "onPageFinished(" + url + ")");

            clearLoadTimeoutTimer();

            // Broadcast message that page has loaded
            pluginManager.postMessage("onPageFinished", url);

            // Make app visible after 2 sec in case there was a JS error and Cordova JS never initialized correctly
            if (engine.getView().getVisibility() != View.VISIBLE) {
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(2000);
                            /*
                            Handler handler = new Handler();
                            handler.post(new Runnable() {
                                public void run() {
                                    pluginManager.postMessage("spinner", "stop");
                                }
                            });
                            */
                        } catch (InterruptedException e) {
                        }
                    }
                });
                t.start();
            }

            // Shutdown if blank loaded
            if (url.equals("about:blank")) {
                pluginManager.postMessage("exit", null);
            }
        }

        @Override
        public Boolean onDispatchKeyEvent(KeyEvent event) {
            int keyCode = event.getKeyCode();
            boolean isBackButton = keyCode == KeyEvent.KEYCODE_BACK;
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (isBackButton && mCustomView != null) {
                    return true;
                } else if (boundKeyCodes.contains(keyCode)) {
                    return true;
                } else if (isBackButton) {
                    return engine.canGoBack();
                }
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                if (isBackButton && mCustomView != null) {
                    hideCustomView();
                    return true;
                } else if (boundKeyCodes.contains(keyCode)) {
                    String eventName = null;
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_VOLUME_DOWN:
                            eventName = "volumedownbutton";
                            break;
                        case KeyEvent.KEYCODE_VOLUME_UP:
                            eventName = "volumeupbutton";
                            break;
                        case KeyEvent.KEYCODE_SEARCH:
                            eventName = "searchbutton";
                            break;
                        case KeyEvent.KEYCODE_MENU:
                            eventName = "menubutton";
                            break;
                        case KeyEvent.KEYCODE_BACK:
                            eventName = "backbutton";
                            break;
                    }
                    if (eventName != null) {
                        sendJavascriptEvent(eventName);
                        return true;
                    }
                } else if (isBackButton) {
                    return engine.goBack();
                }
            }
            return null;
        }

        @Override
        public boolean onNavigationAttempt(String url) {
            // Give plugins the chance to handle the url
            if (pluginManager.onOverrideUrlLoading(url)) {
                return true;
            } else if (pluginManager.shouldAllowNavigation(url)) {
                return false;
            } else if (pluginManager.shouldOpenExternalUrl(url)) {
                showWebPage(url, true, false, null);
                return true;
            }
            LOG.w(TAG, "Blocked (possibly sub-frame) navigation to non-allowed URL: " + url);
            return true;
        }
    }
}
