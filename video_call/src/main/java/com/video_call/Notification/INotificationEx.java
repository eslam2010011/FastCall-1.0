package com.video_call.Notification;

import android.app.Notification;

import com.video_call.FastCall;
import com.video_call.util.NotificationHelper;
public class INotificationEx implements INotification {
    @Override
    public Notification getNotificationInGoing() {
        return  new NotificationHelper(FastCall.getContext())
                .getNotification1("call on going", "Tap to return")
                .build();
    }
    @Override
    public Notification getNotificationInIncoming() {
        return null;
    }
}
