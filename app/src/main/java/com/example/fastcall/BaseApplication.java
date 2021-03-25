package com.example.fastcall;

import android.app.Application;
import android.app.Notification;

import com.video_call.CallConfig;
import com.video_call.CallFast;
import com.video_call.Notification.INotification;
import com.video_call.util.NotificationHelper;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CallFast.init(this,getString(R.string.agora_app_id),new CallConfig().setRingingTimeEnd(30000).setiNotification(new INotification() {
            @Override
            public Notification getNotificationInGoing() {
                return   new NotificationHelper(CallFast.getContext())
                        .getNotification1("Eslam MOstafa", "Tap to return")
                        .build();
            }

            @Override
            public Notification getNotificationInIncoming() {
                return null;
            }
        }));
    }
}
