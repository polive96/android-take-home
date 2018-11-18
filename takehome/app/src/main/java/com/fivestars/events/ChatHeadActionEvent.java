package com.fivestars.events;

public class ChatHeadActionEvent {

    public static final String ACTION_TYPE_MINIMIZE = "minimize";
    public static final String ACTION_TYPE_CLOSE = "close";

    private String action = "";

    public ChatHeadActionEvent(String action) {
        this.setAction(action);
    }

    public String getAction() {
        return action;
    }

    private void setAction(String action) {
        this.action = action;
    }
}
