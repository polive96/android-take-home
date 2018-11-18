package org.apache.cordova;

import android.app.Service;
import android.graphics.Color;
import android.view.WindowManager;

import java.util.ArrayList;

//cordova service based webview factory class
public final class CordovaServiceWebViewFactory {

    //method to generate a webview
    @SuppressWarnings({"deprecation", "ResourceType"})
    public static CordovaServiceWebView createWebView(Service service, String initUrl) {
        // Set the initial url to load
        if (initUrl==null) {
            //else launch fivestars if none provided
            initUrl = "http://www.fivestars.com";
        }
        //init the webview
        CordovaPreferences preferences = new CordovaPreferences();
        CordovaServiceWebView webview = new CordovaServiceWebViewImpl(CordovaServiceWebViewImpl.createEngine(service.getBaseContext(), preferences));
        //set the id of the view
        webview.getView().setId((int)(Math.random() * 500 + 100));
        //set the intial layout
        webview.getView().setLayoutParams(new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT));
        //set the background color
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
        //set the implementation
        CordovaServiceInterfaceImpl cordovaInterface = new CordovaServiceInterfaceImpl(service) {
            @Override
            public Object onMessage(String id, Object data) {
                return data;
            }
        };
        webview.getView().requestFocusFromTouch();
        //init the plugin interface
        ArrayList<PluginEntry> pluginEntries;
        ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse(service.getBaseContext());
        pluginEntries = parser.getPluginEntries();
        //init webview
        if (!webview.isInitialized()) {
            webview.init(cordovaInterface, pluginEntries, preferences);
        }
        //load the initial url
        webview.loadUrl(initUrl);
        return webview;
    }

}
