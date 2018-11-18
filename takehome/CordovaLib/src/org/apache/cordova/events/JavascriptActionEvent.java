package org.apache.cordova.events;

public class JavascriptActionEvent {

    public static final String ACTION_TYPE_CHANGE_BACKGROUND = "actionChangeBackground";
    public static final String ACTION_TYPE_ALERT = "actionShowAlert";

    private String action = "";
    private String[] arguments = null;

    public JavascriptActionEvent(String action, String[] arguments) {
        this.setAction(action);
        this.setArguments(arguments);
    }

    public String getAction() {
        return action;
    }

    private void setAction(String action) {
        this.action = action;
    }

    public String[] getArguments() {
        return arguments;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }
}
