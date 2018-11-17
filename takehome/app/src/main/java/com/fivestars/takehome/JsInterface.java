package com.fivestars.takehome;

import org.apache.cordova.CordovaWebView;

public class JsInterface {

    private CordovaWebView mWebView;

    public JsInterface(CordovaWebView webView) {
        mWebView = webView;
    }

    public void sendAlert(String msg) {
        mWebView.loadUrl("javascript:showJsAlert('"
                + msg
                + "', null, 'ALERT', 'DONE')");
    }



}
