package org.apache.cordova;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;

//cordova webview factory class
public class CordovaWebViewFactory {

    //method to generate a webview
    @SuppressWarnings({"deprecation", "ResourceType"})
    public static CordovaWebView getWebView(Context context, String initUrl) {
        // Set the initial url to load
        if (initUrl==null) {
            //else launch fivestars if none provided
            initUrl = "http://www.fivestars.com";
        }
        //init the webview
        CordovaPreferences preferences = new CordovaPreferences();
        CordovaWebView webview = new CordovaWebViewImpl(CordovaWebViewImpl.createEngine(context, preferences));
        webview.getView().setId(100);
        webview.getView().setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        if (preferences.contains("BackgroundColor")) {
            try {
                int backgroundColor = preferences.getInteger("BackgroundColor", Color.BLACK);
                // Background of activity:
                webview.getView().setBackgroundColor(backgroundColor);
            }
            catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        CordovaInterfaceImpl cordovaInterface = new CordovaInterfaceImpl(new CordovaActivity()) {
            @Override
            public Object onMessage(String id, Object data) {
                return data;
            }
        };
        webview.getView().requestFocusFromTouch();
        //init the plugin interface
        ArrayList<PluginEntry> pluginEntries;
        ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse(context);
        pluginEntries = parser.getPluginEntries();
        if (!webview.isInitialized()) {
            webview.init(cordovaInterface, pluginEntries, preferences);
        }
        cordovaInterface.onCordovaInit(webview.getPluginManager());
        //load the initial url
        webview.loadUrl(initUrl);
        return webview;
    }

}
