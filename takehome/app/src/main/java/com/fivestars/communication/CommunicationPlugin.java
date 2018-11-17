package com.fivestars.communication;

import android.widget.Toast;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class shows a toast message using text that
 * is coming up from the javascript layer
 */
public class CommunicationPlugin extends CordovaPlugin {

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolAlert")) {
            String message = args.getString(0);
            this.coolAlert(message, callbackContext);
            return true;
        }
        return false;
    }

    //function to show a native toast message
    private void coolAlert(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            Toast.makeText(webView.getContext(), message, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(webView.getContext(), "Expected one non-empty string argument.", Toast.LENGTH_LONG).show();
        }
    }
}
