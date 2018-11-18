package com.fivestars.communication;

import org.apache.cordova.events.JavascriptActionEvent;
import org.greenrobot.eventbus.EventBus;

public class JsInterface {

    public void sendAlert(String msg) {
        EventBus.getDefault().post(
                new JavascriptActionEvent(
                        JavascriptActionEvent.ACTION_TYPE_ALERT,
                        new String[] { msg }
                        )
        );
    }

    public void setRandomBackgroundColor() {
        EventBus.getDefault().post(
                new JavascriptActionEvent(
                        JavascriptActionEvent.ACTION_TYPE_CHANGE_BACKGROUND,
                        new String[] { }
                )
        );
    }

}
