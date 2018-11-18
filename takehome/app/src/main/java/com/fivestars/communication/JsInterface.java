package com.fivestars.communication;

import org.apache.cordova.CordovaServiceWebView;

public class JsInterface {

    private CordovaServiceWebView mWebView;

    public JsInterface(CordovaServiceWebView webView) {
        mWebView = webView;
    }

    public void sendAlert(String msg) {
        mWebView.loadUrl("javascript:showJsAlert('"
                + msg
                + "', null, 'ALERT', 'DONE')");
    }

    public void setRandomBackgroundColor() {

    }

}
