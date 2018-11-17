package com.fivestars.takehome;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.fivestars.communication.JsInterface;
import com.fivestars.utils.Constants;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewFactory;
public class ChatHeadService extends Service {

    //WindowManager refs
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams params;
    //ui elements to manipulate
    private View mChatHeadView;
    private LinearLayout mWebViewContainer;
    //Js Interface class
    private JsInterface mJsInterface;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //setup the webview
        CordovaWebView webView = CordovaWebViewFactory.getWebView(this, intent.getStringExtra(Constants.BUNDLE_LAUNCH_URL));
        //show the webview
        mWebViewContainer.addView(webView.getView());
        //setup Js Interface
        mJsInterface = new JsInterface(webView);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();
        //Inflate the chat head layout we created
        mChatHeadView = LayoutInflater.from(this).inflate(R.layout.chat_head_layout, null);
        //get a reference to the chat window container
        mWebViewContainer = mChatHeadView.findViewById(R.id.webview_container);
        //setup alert button
        Button alertButton  = mChatHeadView.findViewById(R.id.alert_button);
        alertButton.setOnClickListener((View v) -> {
            mJsInterface.sendAlert("This is a message from Android to the Webview!");
        });
        //specify the chat window layout
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        //Specify the chat head position
        params.gravity = Gravity.TOP | Gravity.START;        //Initially view will be added to top-left corner
        params.x = 0;
        params.y = 100;
        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mChatHeadView, params);
        //Set the close button.
        ImageView closeButton = mChatHeadView.findViewById(R.id.close_btn);
        closeButton.setOnClickListener((View v) -> {
            stopSelf();
        });
        //Drag and move chat head using user's touch action.
        ImageView chatHeadImage = mChatHeadView.findViewById(R.id.chat_head_profile_iv);
        chatHeadImage.setOnTouchListener(new View.OnTouchListener() {
            private int lastAction, initialX, initialY, currentTouchX, currentTouchY = 0;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        lastAction = event.getAction();
                        return true;
                    case MotionEvent.ACTION_UP:
                        //heck if the previous action was ACTION_DOWN
                        //to identify if the user clicked the view or not.
                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            if (mWebViewContainer.getVisibility() == View.VISIBLE) {
                                //hide the webview
                                mWebViewContainer.setVisibility(View.GONE);
                                //move icon back to last spot when clicked
                                params = new WindowManager.LayoutParams(
                                        WindowManager.LayoutParams.WRAP_CONTENT,
                                        WindowManager.LayoutParams.WRAP_CONTENT,
                                        WindowManager.LayoutParams.TYPE_PHONE,
                                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                                        PixelFormat.TRANSLUCENT);

                                params.x = currentTouchX;
                                params.y = currentTouchY;
                            } else {
                                //Open the webview
                                mWebViewContainer.setVisibility(View.VISIBLE);
                                //move view to give room for chat.
                                params = new WindowManager.LayoutParams(
                                        WindowManager.LayoutParams.MATCH_PARENT,
                                        WindowManager.LayoutParams.WRAP_CONTENT,
                                        WindowManager.LayoutParams.TYPE_PHONE,
                                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                                        PixelFormat.TRANSLUCENT);
                                params.x = 0;
                                params.y = 0;
                            }
                            //set the gravity on the window
                            params.gravity = Gravity.TOP | Gravity.LEFT;
                            //Update the layout with new X & Y coordinate
                            mWindowManager.updateViewLayout(mChatHeadView, params);
                            lastAction = event.getAction();
                        }
                        lastAction = event.getAction();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        currentTouchX  = params.x;
                        currentTouchY = params.y;
                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mChatHeadView, params);
                        lastAction = event.getAction();
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatHeadView != null) mWindowManager.removeView(mChatHeadView);
    }
}