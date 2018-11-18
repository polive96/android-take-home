package com.fivestars.communication;

import org.apache.cordova.events.JavascriptActionEvent;
import org.greenrobot.eventbus.EventBus;

public class JsInterface {

    //function to show a toast in the webview
    public void sendAlert(String msg) {
        EventBus.getDefault().post(
            new JavascriptActionEvent(
                    JavascriptActionEvent.ACTION_TYPE_ALERT,
                    new String[] { msg }
                    )
        );
    }

    //function to change the background color
    //of the webview randomly
    public void setRandomBackgroundColor() {
        EventBus.getDefault().post(
            new JavascriptActionEvent(
                    JavascriptActionEvent.ACTION_TYPE_CHANGE_BACKGROUND,
                    new String[] { }
            )
        );
    }

    //function to toggle the visibility
    // of the cordova logo in the webview
    public void toggleLogo() {
        EventBus.getDefault().post(
            new JavascriptActionEvent(
                    JavascriptActionEvent.ACTION_TYPE_TOGGLE_LOGO,
                    new String[] { }
            )
        );
    }

}
