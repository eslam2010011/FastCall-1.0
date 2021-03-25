package com.video_call.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.video_call.Audio.ImAudioEx;
import com.video_call.CallConfig;
 import com.video_call.Engine.CallEngine;
import com.video_call.FastCall;
import com.video_call.NotificationPayloadData;
import com.video_call.R;
 import com.video_call.Service.VoIPActionsReceiver;
import com.video_call.Service.WebRtcCallService;
import com.video_call.ui.CallActivity;


/**
 * Helper class to manage notification channels, and create notifications.
 */
public class NotificationHelper extends ContextWrapper {
    private  NotificationManager manager;
    public static final String CALL_STATUS_CHANNEL = "CALL_STATUS_CHANNEL";
    public static final String SIGNALING_SERVICE_CHANNEL = "CALL_STATUS_CHANNEL";

    /**
     * Registers notification channels, which can be used later by individual notifications.
     *
     * @param ctx The application context
     */
    public NotificationHelper(Context ctx) {
        super(ctx);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                getManager().getNotificationChannel(CALL_STATUS_CHANNEL) == null) {

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();
            NotificationChannel chan1 = new NotificationChannel(CALL_STATUS_CHANNEL,
                "End", NotificationManager.IMPORTANCE_LOW);
            chan1.setLightColor(Color.GREEN);
            Uri soundUri;
            if (CallConfig.getImAudio() == null) {
                soundUri = Uri.parse(
                        "android.resource://" +
                                FastCall.getContext().getApplicationContext().getPackageName() +
                                "/" +
                                R.raw.video_chat_incoming_call);
            } else {

                soundUri = Uri.parse(
                        "android.resource://" +
                                FastCall.getContext().getApplicationContext().getPackageName() +
                                "/" +
                                CallConfig.getImAudio().getInIncomingSound());

            }
            chan1.setSound(soundUri, audioAttributes);
            chan1.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            chan1.enableVibration(true);
            chan1.enableLights(false);
            chan1.setBypassDnd(true);
            getManager().createNotificationChannel(chan1);
        }

    }

    /**
     * Get a notification of type 1
     * <p>
     * Provide the builder rather than the notification it's self as useful for making notification
     * changes.
     *
     * @param title the title of the notification
     * @param body  the body text for the notification
     * @return the builder as it keeps a reference to the notification (since API 24)
     */
    public NotificationCompat.Builder getNotification1(String title, String body) {
        Intent endCallIntent = new Intent(this, WebRtcCallService.class);
        endCallIntent.setAction(WebRtcCallService.ACTION_ENDED_TIMEOUT);
        PendingIntent endCallPendingIntent =
                PendingIntent.getService(this, 0, endCallIntent, 0);
        Intent resultIntent = new Intent(this, CallActivity.class);
        resultIntent.putExtra(WebRtcCallService.EXTRA_REMOTE_PEER,CallDataHelper.getCallData(getBaseContext().getSharedPreferences("Call",0)));
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
         stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Builder(getApplicationContext(), CALL_STATUS_CHANNEL)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(getSmallIcon())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setWhen(0)
                .setColor(getResources().getColor(R.color.colorPrimary))
                 .setOngoing(true)
                .setContentIntent(resultPendingIntent)
                .addAction(0, "End Call", endCallPendingIntent)
                .setAutoCancel(true);
    }



    public NotificationCompat.Builder createIncomingCallNotification(NotificationPayloadData payloadData) {
        CallDataHelper.saveCallData(payloadData,getBaseContext().getSharedPreferences("Call",0));
        Intent endCallIntent = new Intent(this, WebRtcCallService.class);
        Intent resultIntent = new Intent(this, CallActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
       // stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0 /* Request code */, resultIntent,
                        PendingIntent.FLAG_ONE_SHOT);

        Uri soundUri;
        if (CallConfig.getImAudio() == null) {
             soundUri = Uri.parse(
                    "android.resource://" +
                            FastCall.getContext().getApplicationContext().getPackageName() +
                            "/" +
                            R.raw.video_chat_incoming_call);
        } else {

             soundUri = Uri.parse(
                    "android.resource://" +
                            FastCall.getContext().getApplicationContext().getPackageName() +
                            "/" +
                            CallConfig.getImAudio().getInIncomingSound());

         }

           return new NotificationCompat.Builder(getApplicationContext(), CALL_STATUS_CHANNEL)
                .setContentTitle("Incoming call")
                .setContentText(payloadData.getName() + " is calling")
                .setSmallIcon(getSmallIcon())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setWhen(0)
                .setSound(soundUri)
                .addAction(getCallAnswerAction(getBaseContext(),payloadData,R.drawable.btn_call_voice_accept,"ANSWERED"))
                .addAction(getCallDeclineAction(getBaseContext(),payloadData,R.drawable.btn_call_end,"REJECTED"))
                .setOngoing(true)
                .setContentIntent(resultPendingIntent)
                 .setAutoCancel(true);
    }



    public static NotificationCompat.Action getCallAnswerAction(Context context, NotificationPayloadData notificationPayloadData, int iconResId, String titleResId) {
        Intent answerPendingIntent = new Intent(context, VoIPActionsReceiver.class);
        answerPendingIntent.setAction(WebRtcCallService.ANSWER_ACTION);
        answerPendingIntent.putExtra("Call",notificationPayloadData);
         PendingIntent endPendingIntent = PendingIntent.getBroadcast(context, 0, answerPendingIntent, PendingIntent.FLAG_CANCEL_CURRENT);
         CharSequence answerTitle = titleResId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            answerTitle = new SpannableString(answerTitle);
            ((SpannableString) answerTitle).setSpan(new ForegroundColorSpan(0xFF00AA00), 0, answerTitle.length(), 0);
        }
        return new NotificationCompat.Action(iconResId, answerTitle, endPendingIntent);
    }
    public static NotificationCompat.Action getCallDeclineAction(Context context,NotificationPayloadData notificationPayloadData, int iconResId, String titleResId) {
        Intent endIntent = new Intent(context, VoIPActionsReceiver.class);
        endIntent.setAction(WebRtcCallService.DENY_ACTION);
        endIntent.putExtra("Call",notificationPayloadData);
         PendingIntent endPendingIntent = PendingIntent.getBroadcast(context, 0, endIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        CharSequence endTitle = titleResId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            endTitle = new SpannableString(endTitle);
            ((SpannableString) endTitle).setSpan(new ForegroundColorSpan(0xFFF44336), 0, endTitle.length(), 0);
        }
         return new NotificationCompat.Action(iconResId, endTitle, endPendingIntent);
    }


    /**
     * Send a notification.
     *
     * @param id           The ID of the notification
     * @param notification The notification object
     */
    public void notify(int id, Notification notification) {
        getManager().notify(id, notification);
    }

    /**
     * Get the small icon for this app
     *
     * @return The small icon resource id
     */
    private int getSmallIcon() {
        return R.drawable.btn_call_voice_accept;
    }

    /**
     * Get the notification manager.
     * <p>
     * Utility method as this helper works with it a lot.
     *
     * @return The system service NotificationManager
     */
    public NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }
}