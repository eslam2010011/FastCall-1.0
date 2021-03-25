package com.video_call.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.Gson;
 import com.video_call.NotificationPayloadData;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class CallDataHelper {

    static Gson gson = new Gson();

    public synchronized static NotificationPayloadData getCallData(SharedPreferences sp) {

        return gson.fromJson(sp.getString("call_data", "{state:{videoCallState:-1}}"), NotificationPayloadData.class);
    }

    public synchronized static void saveCallData(NotificationPayloadData callData, SharedPreferences sp) {
        sp.edit().putString("call_data", gson.toJson(callData, NotificationPayloadData.class)).commit();
    }

    public synchronized static void notifyAgoraCallStatus(Context context) {
        Intent intent = new Intent("refresh_video_call_status");

        // You can also include some extra data.
        intent.putExtra("action", "refresh");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}