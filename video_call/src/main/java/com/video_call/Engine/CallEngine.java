package com.video_call.Engine;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import com.video_call.FastCall;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class CallEngine {

    public enum CallType {
        VOICE,
        VIDEO
    }
    private static final String LOG_TAG = "VC_ENGINE";
    private static CallEngine instance = null;
    Context context;

    EventHandler2 eventHandler2;

    public static CallEngine getInstance(Context context) {
        if (instance == null) {
            instance = new CallEngine(context);
        }
        return instance;
    }

    private RtcEngine mRtcEngine;
    private EventHandler handler = null;
    private final Lock mutex = new ReentrantLock(true);
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onRejoinChannelSuccess(String s, int i, int i1) {
            super.onRejoinChannelSuccess(s, i, i1);
        }

        @Override
        public void onLeaveChannel(RtcStats rtcStats) {
            super.onLeaveChannel(rtcStats);
            Log.d("totalDuration", rtcStats.totalDuration + "");
            mutex.lock();
            if (eventHandler2 != null) eventHandler2.onLeaveChannel(rtcStats);
            mutex.unlock();
        }

        @Override
        public void onConnectionStateChanged(int i, int i1) {
            super.onConnectionStateChanged(i, i1);
            mutex.lock();
            if (eventHandler2 != null) eventHandler2.onConnectionStateChanged(i, i1);
            mutex.unlock();

        }


        @Override
        public void onConnectionLost() {
            super.onConnectionLost();
            mutex.lock();
            if (eventHandler2 != null) eventHandler2.onConnectionLost();
            mutex.unlock();
        }

        @Override
        public void onLastmileQuality(int i) {
            super.onLastmileQuality(i);

        }

        @Override
        public void onNetworkQuality(int i, int i1, int i2) {
            super.onNetworkQuality(i, i1, i2);
        }

        @Override
        public void onUserOffline(final int uid, final int reason) {
            mutex.lock();
            if (handler != null) handler.onUserOffline(uid, reason);
            mutex.unlock();
        }

        @Override
        public void onUserMuteVideo(final int uid, final boolean toggle) { // Tutorial Step 10
            mutex.lock();
            if (handler != null) handler.onUserMuteVideo(uid, toggle);
            if (eventHandler2 != null) eventHandler2.onUserMuteVideo(uid, toggle);
            mutex.unlock();
        }

        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            mutex.lock();
            if (handler != null) handler.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
            if (eventHandler2 != null)
                eventHandler2.onFirstRemoteVideoDecoded(uid, width, height, elapsed);

            mutex.unlock();

        }

        @Override
        public void onLocalVideoStateChanged(int i, int i1) {
            super.onLocalVideoStateChanged(i, i1);
        }


        @Override
        public void onUserEnableVideo(int i, boolean b) {
            super.onUserEnableVideo(i, b);
        }

        @Override
        public void onUserEnableLocalVideo(int i, boolean b) {
            super.onUserEnableLocalVideo(i, b);
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            mutex.lock();
            if (handler != null) handler.onUserJoined(uid, elapsed);
            if (eventHandler2 != null) eventHandler2.onUserJoined(uid, elapsed);
            mutex.unlock();
        }
    };

    public RtcEngine getmRtcEngine() {
        return this.mRtcEngine;
    }

    public void setEventHandler2(EventHandler2 eventHandler2) {
        this.eventHandler2 = eventHandler2;

    }

    private CallEngine(Context context) {
        this.context = context;
        try {
            mRtcEngine = RtcEngine.create(context, FastCall.getKey(), mRtcEventHandler);
            Log.e(LOG_TAG, FastCall.getKey());
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
        setupSession();
        // mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
    }

    private void setupSession() {
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_640x480, VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    public void setupLocalVideoFeed(FrameLayout videoContainer) {
        // setup the container for the local user
        SurfaceView videoSurface = RtcEngine.CreateRendererView(context);
        videoSurface.setZOrderMediaOverlay(true);
        videoContainer.addView(videoSurface);
        mRtcEngine.setupLocalVideo(new VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    public void setupLocalVideoFeed(SurfaceView videoContainer) {
        mRtcEngine.setupLocalVideo(new VideoCanvas(videoContainer, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }


    public void setupRemoteVideoStream(FrameLayout videoContainer, int uid) {
        SurfaceView videoSurface = RtcEngine.CreateRendererView(context);
        videoContainer.addView(videoSurface);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_FIT, uid));
        mRtcEngine.setRemoteSubscribeFallbackOption(io.agora.rtc.Constants.STREAM_FALLBACK_OPTION_AUDIO_ONLY);

    }

    public void setupRemoteVideoStream(SurfaceView videoSurface, int uid) {
        mRtcEngine.setupRemoteVideo(new VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        mRtcEngine.setRemoteSubscribeFallbackOption(io.agora.rtc.Constants.STREAM_FALLBACK_OPTION_AUDIO_ONLY);


    }

    public void joinChannel(String channelID, EventHandler handler) {
        mutex.lock();
        this.handler = handler;
        mutex.unlock();
        mRtcEngine.joinChannel(null, channelID, "", 0);
        unmuteMic();
        useEarpiece();
    }

    public void joinChannel(String channelID) {
        mRtcEngine.joinChannel(null, channelID, "", 0);
       // unmuteMic();
        useEarpiece();
    }

    public void eventHandler(EventHandler handler) {
        mutex.lock();
        this.handler = handler;
        mutex.unlock();
    }

    public void leaveChannel() {
        mutex.lock();
        handler = null;
        mutex.unlock();
        mRtcEngine.leaveChannel();
    }

    public void muteMic(boolean mute) {
        mRtcEngine.muteLocalAudioStream(mute);
    }

    public void unmuteMic() {
        mRtcEngine.muteLocalAudioStream(false);
    }

    public void setAudioEnable(boolean Enable) {
        mRtcEngine.muteLocalAudioStream(Enable);
    }

    public void useSpeaker() {
        if (mRtcEngine.isSpeakerphoneEnabled()) {
            mRtcEngine.setEnableSpeakerphone(false);

        } else {
            mRtcEngine.setEnableSpeakerphone(true);

        }
    }

    public void setSpeakerEnable(boolean Enable) {
        mRtcEngine.setEnableSpeakerphone(false);

    }

    public void useEarpiece() {
        mRtcEngine.setEnableSpeakerphone(false);
    }

    public void onVideoMute(boolean onvideomute) {
        mRtcEngine.muteLocalVideoStream(onvideomute);
    }

    public void switchCamera() {
        mRtcEngine.switchCamera();

    }

    public void enableVideo() {
        mRtcEngine.enableVideo();

    }

    public void disableVideo() {
        mRtcEngine.disableVideo();

    }

    public void destroy() {
        RtcEngine.destroy();

    }

    public static abstract class EventHandler {
        public abstract void onUserOffline(final int uid, final int reason);

        public abstract void onUserJoined(int uid, int elapsed);

        public abstract void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed);

        public abstract void onUserMuteVideo(int uid, boolean toggle);

    }

    public interface EventHandler2 {
        public void onUserOffline(final int uid, final int reason);

        public void onUserJoined(int uid, int elapsed);

        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed);

        public void onUserMuteVideo(int uid, boolean toggle);

        public void onLeaveChannel(IRtcEngineEventHandler.RtcStats rtcStats);

        public void onConnectionLost();

        public void onConnectionStateChanged(int state, int reason);


    }


}