package com.video_call.Service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.video_call.Audio.ImAudioEx;
import com.video_call.CallConfig;
import com.video_call.Engine.CallEngine;
import com.video_call.Engine.CallType;
import com.video_call.FastCall;
import com.video_call.Notification.INotificationEx;
import com.video_call.NotificationPayloadData;
import com.video_call.ViewModel.WebRtcViewModel;
 import com.video_call.util.CallDataHelper;
import com.video_call.util.RingManager;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.video.ViEAndroidGLES20;

import static io.agora.rtc.RtcEngine.CreateRendererView;

public class WebRtcCallService extends Service implements CallEngine.EventHandler2 {

    public static final String ACTION_OUTGOING_CALL = "CALL_OUTGOING";
    public static final String ACTION_DENY_CALL = "DENY_CALL";
    public static final String ACTION_LOCAL_HANGUP = "LOCAL_HANGUP";
    public static final String ACTION_SET_MUTE_AUDIO = "SET_MUTE_AUDIO";
    public static final String ACTION_FLIP_CAMERA = "FLIP_CAMERA";
    public static final String EXTRA_REMOTE_PEER = "remote_peer";
    public static final String EXTRA_ENABLE = "enable_value";
    public static final String ACTION_SET_ENABLE_VIDEO = "SET_ENABLE_VIDEO";
    public static final String ACTION_ENDED_TIMEOUT = "ENDED_TIMEOUT";
    public static final String EXTRA_MUTE = "mute_value";
    public static final String ACTION_ACCEPT_CALL = "ACCEPT_CALL";
    public static final String EXTRA_ANSWER_WITH_VIDEO = "enable_video";
    public static final String ACTION_SET_AUDIO_SPEAKER                   = "SET_AUDIO_SPEAKER";
    public static final String EXTRA_SPEAKER                    = "audio_speaker";


    public static final String ANSWER_ACTION = WebRtcCallService.class.getCanonicalName() + ".ANSWER_ACTION";
    public static final String DENY_ACTION = WebRtcCallService.class.getCanonicalName() + ".DENY_ACTION";
    public static final String END_CALL_ACTION = WebRtcCallService.class.getCanonicalName() + ".END_CALL_ACTION";


    private final Handler mainThreadHandler = new Handler();
    private boolean microphoneEnabled = true;
    private boolean remoteVideoEnabled = false;
    private boolean bluetoothAvailable = false;
    private boolean enableVideoOnCreate = true;
    private boolean isRemoteVideoOffer = false;
    private boolean acceptWithVideo = true;

    private long callConnectedTime = -1;
    private static final int SERVICE_ID = 786;

    private final ExecutorService serviceExecutor = Executors.newSingleThreadExecutor();
    Notification notification;
    RingManager playRinging;
    @Nullable
    private CallEngine callManager;
    @Nullable
    private SurfaceView localRenderer;
    @Nullable
    private SurfaceView remoteRenderer;
    private NotificationManager manager;
    CountDownTimer countDownTime = new CountDownTimer(FastCall.getCallConfig().getTimeEnd(), 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            Log.d("onTickTime", millisUntilFinished + "");

        }

        @Override
        public void onFinish() {
            CallConfig.CallListener.OnCallListener listener = FastCall.getCallConfig().getOnEndCallClickListener();
            if (listener != null) {
                listener.OnRinging(true);
            }
            handleEndedTimeout_C1();
        }
    };
    public static WebRtcCallService rtcService;

    public static WebRtcCallService getInstance() {
        if (rtcService == null) {
            rtcService = new WebRtcCallService();
            return rtcService;
        }
        return rtcService;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeResources();
        playRinging = RingManager.getInstance(WebRtcCallService.this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initializeResources() {
        this.callManager = CallEngine.getInstance(this);
        this.callManager.setEventHandler2(this);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) return START_NOT_STICKY;
        serviceExecutor.execute(() -> {
            if (intent.getAction().equals(ACTION_OUTGOING_CALL)) handleOutgoingCall(intent);
            else if (intent.getAction().equals(ACTION_SET_ENABLE_VIDEO))
                handleSetEnableVideo(intent);
            else if (intent.getAction().equals(ACTION_FLIP_CAMERA))
                handleSetCameraFlip(intent);
            else if (intent.getAction().equals(ACTION_ENDED_TIMEOUT))
                handleEndedTimeout();
            else if (intent.getAction().equals(ACTION_SET_MUTE_AUDIO))
                handleSetMuteAudio(intent);
            else if (intent.getAction().equals(ACTION_ACCEPT_CALL))
                handleAcceptCall(intent);
            else if (intent.getAction().equals(ACTION_DENY_CALL))
                handleDenyCall(intent);
            else if (intent.getAction().equals(ACTION_SET_AUDIO_SPEAKER))
                handleSetSpeakerAudio(intent);

        });

        return START_NOT_STICKY;
    }

    private void handleOutgoingCall(Intent intent) {
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (FastCall.getCallConfig().getiNotification() != null) {
            notification = FastCall.getCallConfig().getiNotification().getNotificationInGoing();
            manager.notify(SERVICE_ID, notification);
            startForeground(SERVICE_ID, notification);
        } else {
            notification = new INotificationEx().getNotificationInGoing();
            manager.notify(SERVICE_ID, notification);
            startForeground(SERVICE_ID, notification);
        }
        NotificationPayloadData remotePeer = getRemotePeer(intent);
        if (CallConfig.getImAudio() == null) {
            playRinging.playRinging(CallEngine.CallType.VIDEO, new ImAudioEx());
        } else {
            playRinging.playRinging(CallEngine.CallType.VIDEO, CallConfig.getImAudio());
        }
        initializeVideo();
        boolean isVideo = remotePeer.getCallType().equals(CallType.VIDEO);
        if (callManager != null) {
            Log.d(ACTION_OUTGOING_CALL, remotePeer.getChannelName());
            callManager.joinChannel(remotePeer.getChannelName());
        }
        acceptWithVideo = isVideo;
        if (callManager != null || isVideo) {
            callManager.enableVideo();
        }
        sendMessage(WebRtcViewModel.State.CALL_OUTGOING, CallDataHelper.getCallData(getSharedPreferences("Call", 0)), acceptWithVideo, remoteVideoEnabled, bluetoothAvailable, microphoneEnabled, isRemoteVideoOffer, callManager);
        if (isVideo) {
            Intent intent1 = new Intent(WebRtcCallService.this, WebRtcCallService.class);
            intent1.setAction(WebRtcCallService.ACTION_SET_ENABLE_VIDEO);
            intent1.putExtra(WebRtcCallService.EXTRA_ENABLE, true);
            startService(intent1);
        }

        countDownTime.start();
    }

    private void handleSetEnableVideo(Intent intent) {
        boolean enable = intent.getBooleanExtra(EXTRA_ENABLE, false);
        enableVideoOnCreate = enable;
        if (enable) {
            if (callManager != null) {
                callManager.enableVideo();
                callManager.onVideoMute(false);
            }
        } else {
            if (callManager != null) {
                callManager.disableVideo();
                callManager.onVideoMute(true);
            }
        }
        if (remoteVideoEnabled) {
            sendMessage(WebRtcViewModel.State.CALL_CONNECTED, getRemotePeer(intent), enable, remoteVideoEnabled, bluetoothAvailable, microphoneEnabled, isRemoteVideoOffer, callManager);
        } else {
            sendMessage(WebRtcViewModel.State.CALL_OUTGOING, getRemotePeer(intent), enable, remoteVideoEnabled, bluetoothAvailable, microphoneEnabled, isRemoteVideoOffer, callManager);
        }
        // sendMessage(viewModelStateFor(activePeer), activePeer, localCameraState, remoteVideoEnabled, bluetoothAvailable, microphoneEnabled, isRemoteVideoOffer);
    }

    private void initializeVideo() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d("UI thread", "I am the UI thread");
                if (callManager != null) {
                    localRenderer = CreateRendererView(WebRtcCallService.this);
                    localRenderer.setZOrderMediaOverlay(true);
                    remoteRenderer = CreateRendererView(WebRtcCallService.this);
                    remoteRenderer.setZOrderMediaOverlay(true);
                }
            }
        });


    }

    private void handleSetCameraFlip(Intent intent) {
        if (callManager != null) {
            callManager.switchCamera();
        }
        if (remoteVideoEnabled) {
            sendMessage(WebRtcViewModel.State.CALL_CONNECTED, getRemotePeer(intent), getRemotePeer(intent).getCallType().equals(CallType.VIDEO), remoteVideoEnabled, bluetoothAvailable, microphoneEnabled, isRemoteVideoOffer, callManager);
        } else {
            sendMessage(WebRtcViewModel.State.CALL_OUTGOING, getRemotePeer(intent), getRemotePeer(intent).getCallType().equals(CallType.VIDEO), remoteVideoEnabled, bluetoothAvailable, microphoneEnabled, isRemoteVideoOffer, callManager);
        }
    }

    private void handleSetMuteAudio(Intent intent) {
        boolean muted = intent.getBooleanExtra(EXTRA_MUTE, false);
        microphoneEnabled = !muted;
        if (callManager != null) {
            callManager.setAudioEnable(muted);
        }
        // if (remoteVideoEnabled) {
        //   sendMessage(WebRtcViewModel.State.CALL_CONNECTED, getRemotePeer(intent), getRemotePeer(intent).getCallType().equals(CallType.VIDEO), remoteVideoEnabled, bluetoothAvailable, microphoneEnabled, isRemoteVideoOffer, callManager);
        // } else {
        //  sendMessage(WebRtcViewModel.State.CALL_OUTGOING, getRemotePeer(intent), getRemotePeer(intent).getCallType().equals(CallType.VIDEO), remoteVideoEnabled, bluetoothAvailable, microphoneEnabled, isRemoteVideoOffer, callManager);
        // }
    }

    private void handleAcceptCall(Intent intent) {
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (FastCall.getCallConfig().getiNotification() != null) {
            notification = FastCall.getCallConfig().getiNotification().getNotificationInGoing();
            manager.notify(SERVICE_ID, notification);
            startForeground(SERVICE_ID, notification);
        } else {
            notification = new INotificationEx().getNotificationInGoing();
            manager.notify(SERVICE_ID, notification);
            startForeground(SERVICE_ID, notification);
        }
        NotificationPayloadData callCome = getRemotePeer(intent);
        initializeVideo();
        boolean isVideo = callCome.getCallType().equals(CallType.VIDEO);
        if (callManager != null) {
            callManager.joinChannel(callCome.getChannelName());
        }
        handleSetEnableVideo(new Intent().putExtra(EXTRA_ENABLE, isVideo));
        callConnectedTime = System.currentTimeMillis();
        sendMessage(WebRtcViewModel.State.CALL_CONNECTED, callCome, isVideo, remoteVideoEnabled, bluetoothAvailable, microphoneEnabled, isRemoteVideoOffer, callManager);
    }

    private void handleSetSpeakerAudio(Intent intent) {
        NotificationPayloadData callCome=getRemotePeer(intent);
        boolean      isSpeaker    = intent.getBooleanExtra(EXTRA_SPEAKER, false);
        boolean isvideo= callCome.getCallType().equals(CallType.VIDEO);
        if (callManager != null) {
            if (callManager.getmRtcEngine().isSpeakerphoneEnabled()){
                callManager.setSpeakerEnable(true);
            }else {
                callManager.setSpeakerEnable(false);
            }

        }
       // sendMessage(WebRtcViewModel.State.CALL_CONNECTED , getRemotePeer(intent), isvideo,remoteVideoEnabled, bluetoothAvailable, microphoneEnabled, isRemoteVideoOffer,callManager);
    }




    private void handleDenyCall(Intent intent) {
        NotificationPayloadData callCome = getRemotePeer(intent);
        sendMessage(WebRtcViewModel.State.CALL_DISCONNECTED, callCome, false, remoteVideoEnabled, bluetoothAvailable, microphoneEnabled, isRemoteVideoOffer, callManager);
        terminate();
    }

    private void handleEndedTimeout() {
        RingManager.getInstance(WebRtcCallService.this).stop();
        NotificationPayloadData remotePeer = CallDataHelper.getCallData(getSharedPreferences("Call", 0));
        sendMessage(WebRtcViewModel.State.CALL_DISCONNECTED, remotePeer, false, remoteVideoEnabled, bluetoothAvailable, microphoneEnabled, isRemoteVideoOffer, callManager);
        terminate();
    }

    private void handleEndedTimeout_C1() {
        Intent intent = new Intent(this, WebRtcCallService.class);
        intent.setAction(WebRtcCallService.ACTION_ENDED_TIMEOUT);
        startService(intent);
        //  delayedFinish();
    }

    //انا ولا حلتي ولا معايا
    private synchronized void terminate() {
        if (callManager != null) {
            callManager.disableVideo();
            callManager.leaveChannel();
            playRinging.stop();
            countDownTime.cancel();
        }
        stopSelf();
        stopForeground(true);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(SERVICE_ID);
    }

    public void handleNotificationAction(Context context, Intent intent) {
        NotificationPayloadData notificationPayloadData = (NotificationPayloadData) intent.getSerializableExtra(WebRtcCallService.EXTRA_REMOTE_PEER);
        if (DENY_ACTION.equals(intent.getAction())) {
            CallConfig.CallListener.OnCallListener listener = FastCall.getCallConfig().getOnEndCallClickListener();
            if (listener != null) {
                listener.OnDENY();
            }
            Intent intent30 = new Intent(this, WebRtcCallService.class);
            intent30.setAction(WebRtcCallService.ACTION_DENY_CALL);
            intent30.putExtra(WebRtcCallService.EXTRA_REMOTE_PEER, notificationPayloadData);
            context.startService(intent30);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(1);
        } else if (ANSWER_ACTION.equals(intent.getAction())) {
            CallConfig.CallListener.OnCallListener listener = FastCall.getCallConfig().getOnEndCallClickListener();
            if (listener != null) {
                listener.OnANSWER();
            }
            Intent intent1 = new Intent(context, FastCall.getCallConfig().getCallActivity());
            intent1.putExtra(WebRtcCallService.EXTRA_REMOTE_PEER, notificationPayloadData);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            // intent.putExtra(StringContract.IntentStrings.CAll,(Call)baseMessage);
            boolean isvideo = false;
            if (notificationPayloadData != null) {
                isvideo = notificationPayloadData.getCallType().equals(CallType.VIDEO);
            }
            if (isvideo) {
                Intent intent12 = new Intent(context, WebRtcCallService.class);
                intent12.setAction(WebRtcCallService.ACTION_ACCEPT_CALL);
                intent12.putExtra(WebRtcCallService.EXTRA_ANSWER_WITH_VIDEO, true);
                intent12.putExtra(WebRtcCallService.EXTRA_REMOTE_PEER, notificationPayloadData);
                context.startService(intent12);
            } else {
                Intent intent32 = new Intent(context, WebRtcCallService.class);
                intent32.setAction(WebRtcCallService.ACTION_ACCEPT_CALL);
                intent32.putExtra(WebRtcCallService.EXTRA_REMOTE_PEER, notificationPayloadData);
                intent32.putExtra(WebRtcCallService.EXTRA_ANSWER_WITH_VIDEO, false);
                context.startService(intent32);
                //handleAnswerWithAudio();
            }
            context.startActivity(intent1);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(1);
        }
    }


    public static SurfaceView CreateRendererView(Context var0) {
        Object var1;
        if (ViEAndroidGLES20.IsSupported(var0)) {
            var1 = new ViEAndroidGLES20(var0);
        } else {
            var1 = new SurfaceView(var0);
        }
        ((SurfaceView) var1).setVisibility(View.VISIBLE);
        return (SurfaceView) var1;
    }

    public NotificationPayloadData getRemotePeer(Intent intent) {
        NotificationPayloadData remotePeer = (NotificationPayloadData) intent.getSerializableExtra(WebRtcCallService.EXTRA_REMOTE_PEER);
        if (remotePeer != null) {
            CallDataHelper.saveCallData(remotePeer, getSharedPreferences("Call", 0));
            return remotePeer;
        } else {
            return CallDataHelper.getCallData(getSharedPreferences("Call", 0));
        }
    }


    @Override
    public void onUserOffline(int uid, int reason) {

    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        countDownTime.cancel();
        playRinging.stop();
        callConnectedTime = System.currentTimeMillis();
        sendMessage(WebRtcViewModel.State.CALL_CONNECTED, CallDataHelper.getCallData(getSharedPreferences("Call", 0)), CallDataHelper.getCallData(getSharedPreferences("Call", 0)).getCallType().equals("Video"), true, bluetoothAvailable, microphoneEnabled, isRemoteVideoOffer, callManager);
    }


    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                remoteVideoEnabled = true;
                if (callManager != null) {
                    callManager.setupRemoteVideoStream(remoteRenderer, uid);
                }
            }
        });

    }

    @Override
    public void onUserMuteVideo(int uid, boolean toggle) {
        //  sendMessage(WebRtcViewModel.State.CALL_CONNECTED, getRemotePeer(intent), getRemotePeer(intent).getCallType().equals(CallType.VIDEO), true, bluetoothAvailable, microphoneEnabled, isRemoteVideoOffer, callManager);


    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats rtcStats) {
        CallConfig.CallListener.OnCallListener listener = FastCall.getCallConfig().getOnEndCallClickListener();
        if (listener != null) {
            listener.OnEnd(true, rtcStats.totalDuration, null);
        }

    }

    @Override
    public void onConnectionLost() {

    }

    @Override
    public void onConnectionStateChanged(int state, int reason) {

    }


    private void sendMessage(@NonNull WebRtcViewModel.State state,
                             @NonNull NotificationPayloadData remotePeer,
                             boolean remoteVideoEnabled,
                             boolean bluetoothAvailable,
                             boolean microphoneEnabled,
                             boolean isRemoteVideoOffer) {


        EventBus.getDefault().postSticky(new WebRtcViewModel(state,
                remotePeer,
                "",
                localRenderer,
                remoteRenderer,
                remoteVideoEnabled,
                bluetoothAvailable,
                microphoneEnabled,
                isRemoteVideoOffer,
                callConnectedTime));
    }

    private void sendMessage(@NonNull WebRtcViewModel.State state,
                             @NonNull NotificationPayloadData remotePeer,
                             boolean isLocalVideoEnabled,
                             boolean remoteVideoEnabled,
                             boolean bluetoothAvailable,
                             boolean microphoneEnabled,
                             boolean isRemoteVideoOffer, CallEngine callEngine) {


        EventBus.getDefault().postSticky(new WebRtcViewModel(state,
                remotePeer,
                "",
                localRenderer,
                remoteRenderer,
                isLocalVideoEnabled,
                remoteVideoEnabled,
                bluetoothAvailable,
                microphoneEnabled,
                isRemoteVideoOffer,
                callConnectedTime, callEngine));
    }

    private void sendMessage(@NonNull WebRtcViewModel.State state,
                             @NonNull NotificationPayloadData remotePeer,
                             @NonNull String identityKey,
                             boolean remoteVideoEnabled,
                             boolean bluetoothAvailable,
                             boolean microphoneEnabled,
                             boolean isRemoteVideoOffer) {
        EventBus.getDefault().postSticky(new WebRtcViewModel(state,
                remotePeer,
                identityKey,
                localRenderer,
                remoteRenderer,
                remoteVideoEnabled,
                bluetoothAvailable,
                microphoneEnabled,
                isRemoteVideoOffer,
                callConnectedTime));
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public WebRtcCallService getService() {
            // Return this instance of LocalService so clients can call public methods
            return WebRtcCallService.this;
        }
    }
}
