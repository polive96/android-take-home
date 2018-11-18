package com.fivestars.takehome;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.fivestars.communication.JsInterface;
import com.fivestars.events.ChatHeadActionEvent;
import com.fivestars.utils.Constants;

import org.apache.cordova.CordovaServiceWebView;
import org.apache.cordova.CordovaServiceWebViewFactory;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class ChatHeadService extends Service {

    //ui elements to manipulate
    private LinearLayout mWebViewContainer;
    protected View mUiView;
    //chat head movement class
    private ChatHeadMovement mChatHeadMovement;
    //Js Interface class
    private JsInterface mJsInterface;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //setup the webview
        CordovaServiceWebView webView = CordovaServiceWebViewFactory.createWebView(this, intent.getStringExtra(Constants.BUNDLE_LAUNCH_URL));
        //show the webview
        mWebViewContainer.addView(webView.getView());
        //setup Js Interface
        mJsInterface = new JsInterface(webView);

        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();
        //Inflate the chat head layout we created
        mUiView = LayoutInflater.from(this).inflate(R.layout.chat_head_layout, null);
        //setup eventbus
        EventBus.getDefault().register(this);
        //get a reference to the chat window container
        mWebViewContainer = mUiView.findViewById(R.id.webview_container);
        //setup the movement of the chat head
        mChatHeadMovement = new ChatHeadMovement(this, mUiView, mWebViewContainer);
        //setup alert button
        Button alertButton  = mUiView.findViewById(R.id.alert_button);
        alertButton.setOnClickListener((View v) -> {
            mJsInterface.sendAlert("This is a message from Android to the Webview!");
        });
        //Set the close button.
        ImageView closeButton = mUiView.findViewById(R.id.close_btn);
        closeButton.setOnClickListener((View v) -> {
            stopSelf();
        });
        //Drag and move chat head using user's touch action.
        ImageView mChatHeadImage = mUiView.findViewById(R.id.chat_head_profile_iv);
        mChatHeadImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mChatHeadMovement.handleAction(event);
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //destroy eventbus
        EventBus.getDefault().unregister(this);
        //remove view that was created
        try {
            ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).removeView(mUiView);
        } catch (NullPointerException e) { e.printStackTrace(); }
    }

    @Subscribe
    public void onMessageEvent(ChatHeadActionEvent event) {
        switch (event.getAction()) {
            case ChatHeadActionEvent.ACTION_TYPE_MINIMIZE:
                mChatHeadMovement.minimizeChatHead();
                break;
            case ChatHeadActionEvent.ACTION_TYPE_CLOSE:
                this.stopSelf();
                break;
            default:
        }
    }

}