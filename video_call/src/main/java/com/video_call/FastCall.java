package com.video_call;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import com.video_call.Engine.CallState;
import com.video_call.Engine.CallType;
import com.video_call.Service.WebRtcCallService;
import com.video_call.ui.CallActivity;
import com.video_call.util.CallDataHelper;

public class FastCall {

    private static Context context;
    private static CallConfig callConfig;
    private static String key;

    /**
     * @param context    this  context
     * @param key        this api key agora
     * @param callConfig
     */
    public static void init(Context context, String key, CallConfig callConfig) {
        FastCall.context = context;
        FastCall.key = key;
        FastCall.callConfig = callConfig;
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        FastCall.context = context;
    }

    public static String getKey() {
        return key;
    }

    public static CallConfig getCallConfig() {
        return callConfig;
    }

    public static class CallActivityBuilder {
        private NotificationPayloadData notificationPayloadData;

        public CallActivityBuilder(NotificationPayloadData notificationPayloadData) {
            this.notificationPayloadData = notificationPayloadData;
        }

        public void show(Context context) {
            CallDataHelper.saveCallData(notificationPayloadData, context.getSharedPreferences("Call", 0));
            Intent intent = new Intent(context, CallActivity.class);
            intent.putExtra("Call", notificationPayloadData);
            context.startActivity(intent);
            if (notificationPayloadData.getCallState().equals(CallState.ONGOING)) {
                Intent intent2 = new Intent(context, WebRtcCallService.class);
                intent2.setAction(WebRtcCallService.ACTION_OUTGOING_CALL);
                intent2.putExtra(WebRtcCallService.EXTRA_REMOTE_PEER, notificationPayloadData);
                context.startService(intent2);
            }


        }


    }

}
