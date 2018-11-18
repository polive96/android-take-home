package com.fivestars.chathead;

import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;


public class ChatHeadMovement {

    //layout type
    private int mLayoutType;
    //WindowManager refs
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams params;
    //containers used
    private View mMainContainer;
    private LinearLayout mWebContainer;
    //layout variables
    private int lastAction, initialX, initialY, currentTouchX, currentTouchY = 0;
    private float initialTouchX, initialTouchY;

    public ChatHeadMovement(Service service, View mainContainer, LinearLayout webContainer) {
        mMainContainer = mainContainer;
        mWebContainer = webContainer;
        //specify the chat window layout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            mLayoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                mLayoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        //Specify the chat head position
        params.gravity = Gravity.TOP | Gravity.START;        //Initially view will be added to top-left corner
        params.x = 0;
        params.y = 100;
        //Add the view to the window
        mWindowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(mMainContainer, params);
    }

    public void handleAction(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                handleUpAction();
                break;
            case MotionEvent.ACTION_DOWN:
                handleDownAction(event);
                break;
            case MotionEvent.ACTION_MOVE:
                handleMoveAction(event);
        }
    }

    private void handleDownAction(MotionEvent event) {
        //remember the initial position.
        initialX = params.x;
        initialY = params.y;
        //get the touch location
        initialTouchX = event.getRawX();
        initialTouchY = event.getRawY();
        lastAction = event.getAction();
    }

    private void handleUpAction() {
        //heck if the previous action was ACTION_DOWN
        //to identify if the user clicked the view or not.
        if (lastAction == MotionEvent.ACTION_DOWN) {
            if (mWebContainer.getVisibility() == View.VISIBLE) {
                minimizeChatHead();
            } else {
                maximizeChatHead();
            }
        }
    }

    private void handleMoveAction(MotionEvent event) {
        if (mWebContainer.getVisibility() == View.GONE) {
            //Calculate the X and Y coordinates of the view.
            params.x = initialX + (int) (event.getRawX() - initialTouchX);
            params.y = initialY + (int) (event.getRawY() - initialTouchY);
            currentTouchX = params.x;
            currentTouchY = params.y;
            //Update the layout with new X & Y coordinate
            mWindowManager.updateViewLayout(mMainContainer, params);
        }
        lastAction = event.getAction();
    }

    public void minimizeChatHead() {
        //hide the webview
        mWebContainer.setVisibility(View.GONE);
        //move icon back to last spot when clicked
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                mLayoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.x = currentTouchX;
        params.y = currentTouchY;
        //set the gravity on the window
        params.gravity = Gravity.TOP | Gravity.LEFT;
        //Update the layout with new X & Y coordinate
        mWindowManager.updateViewLayout(mMainContainer, params);
        lastAction = MotionEvent.ACTION_UP;
    }

    public void maximizeChatHead() {
        //Open the webview
        mWebContainer.setVisibility(View.VISIBLE);
        //move view to give room for chat.
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                mLayoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.x = 0;
        params.y = 0;
        //set the gravity on the window
        params.gravity = Gravity.TOP | Gravity.LEFT;
        //Update the layout with new X & Y coordinate
        mWindowManager.updateViewLayout(mMainContainer, params);
        lastAction = MotionEvent.ACTION_UP;
    }
}