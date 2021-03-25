package com.video_call;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.video_call.Audio.ImAudio;
import com.video_call.Notification.INotification;
import com.video_call.ui.CallActivity;

public class CallConfig {
    private CallListener.OnCallListener OnCallListener;

    public static String AgoraAppId;
    public static int RingingTimeEnd = 30000;
    public static ImAudio imAudio_;
    public INotification iNotification;
    public static Class CallActivity = com.video_call.ui.CallActivity.class;

    public int getTimeEnd() {
        return RingingTimeEnd;
    }

    public CallConfig setTimeEnd(int timeEnd) {
        RingingTimeEnd = timeEnd;
        return this;
    }

    public static ImAudio getImAudio() {
        return imAudio_;
    }

    public CallConfig setImAudio(ImAudio imAudio) {
        imAudio_ = imAudio;
        return this;
    }


    public static int getRingingTimeEnd() {
        return RingingTimeEnd;
    }


    public CallConfig setRingingTimeEnd(int ringingTimeEnd) {
        RingingTimeEnd = ringingTimeEnd;
        return this;
    }

    public Class getCallActivity() {
        return CallActivity;
    }

    public CallConfig setCallActivity(Class callActivity) {
        CallActivity = callActivity;
        return this;
    }

    public INotification getiNotification() {
        return iNotification;
    }

    public CallConfig setiNotification(INotification iNotification) {
        this.iNotification = iNotification;
        return this;
    }

    public interface CallListener {
        interface OnCallListener {
            void OnEnd(boolean OnEnd, long callDurationInMillis, AppCompatActivity appCompatActivity);

            void OnRinging(boolean isFinishTime);

            void OnANSWER();

            void OnDENY();


        }


    }

    /**
     * listen on end call click event
     *
     * @param OnCallListener - listener
     */
    public CallConfig setOnEndCallListener(@NonNull CallListener.OnCallListener OnCallListener) {
        this.OnCallListener = OnCallListener;
        return this;
    }

    /**
     * get end call listener
     *
     * @return - listener
     */
    @Nullable
    public CallListener.OnCallListener getOnEndCallClickListener() {
        return OnCallListener;
    }
}
