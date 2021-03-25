package com.video_call.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.video_call.NotificationPayloadData;
 import com.video_call.Service.WebRtcCallService;
import com.video_call.ViewModel.WebRtcCallViewModel;
import com.video_call.ViewModel.WebRtcViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashSet;

import io.agora.rtc.RtcEngine;


abstract public class BaseRtcActivity2 extends AppCompatActivity {
    private WebRtcCallService mService;
    public boolean mBound = false;
    private static final int STANDARD_DELAY_FINISH = 1000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     }



    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
       // Intent intent = new Intent(this, WebRtcCallService.class);
       // bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }



    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            WebRtcCallService.LocalBinder binder = (WebRtcCallService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
           // if (mService.getRtcEngine() == null)
           //     mService.initializeAgoraEngine();
           // onRtcServiceConnected(mService.getRtcEngine());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };




    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }
    @Override
    protected void onPause() {

        super.onPause();
    }
    @Override
    protected void onStop() {
         super.onStop();
      //  unbindService(mConnection);
      //  mBound = false;
        EventBus.getDefault().unregister(this);

    }



    abstract public void onRtcServiceConnected(RtcEngine rtcEngine);

    public void onCallEnded(long callTime) {
        //Implement in client if required
    }

    public void onActiveSpeaker(int uid) {
        //Implement in client if required
    }

    public void onAudioVolumeIndication(int uid, int totalVolume) {
        //Implement in client if required
    }

    public void onRemoteUserVideoMuted(int uid, boolean muted) {
        //Implement in client if required
    }

    public void onRemoteUserLeft(int uid) {
        //Implement in client if required
    }

    public void setupRemoteVideo(int uid) {
        //Implement in client if required
    }

    public void onRtcError(String error) {
        //Implement in client if required
    }

    public void onUserJoined(int uid) {
        //Implement in client if required
    }




    public void handleFlipCamera() {
        Intent intent = new Intent(this, WebRtcCallService.class);
        intent.setAction(WebRtcCallService.ACTION_FLIP_CAMERA);
        startService(intent);
    }
    public void handleEndedTimeout() {
        Intent intent = new Intent(this, WebRtcCallService.class);
        intent.setAction(WebRtcCallService.ACTION_ENDED_TIMEOUT);
        startService(intent);
        EventBus.getDefault().removeStickyEvent(WebRtcViewModel.class);
    }
    public void handleSetMuteAudio(boolean enabled) {
        Intent intent = new Intent(this, WebRtcCallService.class);
        intent.setAction(WebRtcCallService.ACTION_SET_MUTE_AUDIO);
        intent.putExtra(WebRtcCallService.EXTRA_MUTE, enabled);
        startService(intent);
    }




}