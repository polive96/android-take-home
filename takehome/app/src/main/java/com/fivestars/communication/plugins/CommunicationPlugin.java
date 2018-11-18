package com.fivestars.communication.plugins;

import android.widget.Toast;

import com.fivestars.events.ChatHeadActionEvent;
import com.fivestars.R;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class communicates action
 * coming up from the javascript layer
 * to the native interface
 */
public class CommunicationPlugin extends CordovaPlugin {

    private static final String ACTION_TYPE_ALERT = "alert";
    private static final String ACTION_TYPE_MINIMIZE = "minimize";
    private static final String ACTION_TYPE_CLOSE = "close";

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        switch(action) {
            case ACTION_TYPE_ALERT:
                alert(args.getString(0));
                callbackContext.success();
                return true;
            case ACTION_TYPE_MINIMIZE:
                minimizeChatHead();
                callbackContext.success();
                return true;
            case ACTION_TYPE_CLOSE:
                closeChatHead();
                callbackContext.success();
                return true;
            default:
                callbackContext.error(webView.getContext().getString(R.string.error_no_js_action));
        }
        return false;
    }

    //function to show a native toast message
    private void alert(String message) {
        if (message != null && message.length() > 0) {
            Toast.makeText(webView.getContext(), message, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(webView.getContext(), "Expected one non-empty string argument.", Toast.LENGTH_LONG).show();
        }
    }

    private void minimizeChatHead() {
        EventBus.getDefault().post(new ChatHeadActionEvent(ChatHeadActionEvent.ACTION_TYPE_MINIMIZE));
    }

    private void closeChatHead() {
        EventBus.getDefault().post(new ChatHeadActionEvent(ChatHeadActionEvent.ACTION_TYPE_CLOSE));
    }
}
