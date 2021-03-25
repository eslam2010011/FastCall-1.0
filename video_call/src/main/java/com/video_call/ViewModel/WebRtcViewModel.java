package com.video_call.ViewModel;

import android.view.SurfaceView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.video_call.Engine.CallEngine;
import com.video_call.NotificationPayloadData;



public class WebRtcViewModel {

    public enum State {
        // Normal states
        CALL_INCOMING,
        CALL_OUTGOING,
        CALL_CONNECTED,

        CALL_RINGING,
        CALL_BUSY,
        CALL_DISCONNECTED,
        CALL_DISCONNECTED_End,
        CALL_NEEDS_PERMISSION,

        // Error states
        NETWORK_FAILURE,
        RECIPIENT_UNAVAILABLE,
        NO_SUCH_USER,
        UNTRUSTED_IDENTITY,

        // Multiring Hangup States
        CALL_ACCEPTED_ELSEWHERE,
        CALL_DECLINED_ELSEWHERE,
        CALL_ONGOING_ELSEWHERE
    }


    private final @NonNull
    State state;
    private final @NonNull
    NotificationPayloadData recipient;
    private final @Nullable
    String identityKey;

    private final boolean remoteVideoEnabled;

    private final boolean isBluetoothAvailable;
    private final boolean isMicrophoneEnabled;
    private final boolean isRemoteVideoOffer;
    private boolean isLocalVideoEnabled;

    private final SurfaceView localRenderer;
    private final SurfaceView remoteRenderer;
    private final long callConnectedTime;
    CallEngine callEngine;

    public WebRtcViewModel(@NonNull State state,
                           @NonNull NotificationPayloadData recipient,
                           @NonNull SurfaceView localRenderer,
                           @NonNull SurfaceView remoteRenderer,
                           boolean remoteVideoEnabled,
                           boolean isBluetoothAvailable,
                           boolean isMicrophoneEnabled,
                           boolean isRemoteVideoOffer,
                           long callConnectedTime) {
        this(state,
                recipient,
                null,
                localRenderer,
                remoteRenderer,
                remoteVideoEnabled,
                isBluetoothAvailable,
                isMicrophoneEnabled,
                isRemoteVideoOffer,
                callConnectedTime);
    }

    public WebRtcViewModel(@NonNull State state,
                           @NonNull NotificationPayloadData recipient,
                           @Nullable String identityKey,
                           @NonNull SurfaceView localRenderer,
                           @NonNull SurfaceView remoteRenderer,
                           boolean remoteVideoEnabled,
                           boolean isBluetoothAvailable,
                           boolean isMicrophoneEnabled,
                           boolean isRemoteVideoOffer,
                           long callConnectedTime) {
        this.state = state;
        this.recipient = recipient;
        this.localRenderer = localRenderer;
        this.remoteRenderer = remoteRenderer;
        this.identityKey = identityKey;
        this.remoteVideoEnabled = remoteVideoEnabled;
        this.isBluetoothAvailable = isBluetoothAvailable;
        this.isMicrophoneEnabled = isMicrophoneEnabled;
        this.isRemoteVideoOffer = isRemoteVideoOffer;
        this.callConnectedTime = callConnectedTime;
    }

    public WebRtcViewModel(@NonNull State state,
                           @NonNull NotificationPayloadData recipient,
                           @Nullable String identityKey,
                           @NonNull SurfaceView localRenderer,
                           @NonNull SurfaceView remoteRenderer,
                           boolean isLocalVideoEnabled,
                           boolean remoteVideoEnabled,
                           boolean isBluetoothAvailable,
                           boolean isMicrophoneEnabled,
                           boolean isRemoteVideoOffer,
                           long callConnectedTime, CallEngine callEngine) {
        this.state = state;
        this.recipient = recipient;
        this.localRenderer = localRenderer;
        this.remoteRenderer = remoteRenderer;
        this.isLocalVideoEnabled = isLocalVideoEnabled;
        this.identityKey = identityKey;
        this.remoteVideoEnabled = remoteVideoEnabled;
        this.isBluetoothAvailable = isBluetoothAvailable;
        this.isMicrophoneEnabled = isMicrophoneEnabled;
        this.isRemoteVideoOffer = isRemoteVideoOffer;
        this.callConnectedTime = callConnectedTime;
        this.callEngine = callEngine;
    }

    public @NonNull
    State getState() {
        return state;
    }

    public @NonNull
    NotificationPayloadData getRecipient() {
        return recipient;
    }


    public @Nullable
    String getIdentityKey() {
        return identityKey;
    }

    public boolean isRemoteVideoEnabled() {
        return remoteVideoEnabled;
    }

    public boolean isBluetoothAvailable() {
        return isBluetoothAvailable;
    }

    public boolean isMicrophoneEnabled() {
        return isMicrophoneEnabled;
    }

    public boolean isRemoteVideoOffer() {
        return isRemoteVideoOffer;
    }

    public SurfaceView getLocalRenderer() {
        return localRenderer;
    }

    public SurfaceView getRemoteRenderer() {
        return remoteRenderer;
    }

    public long getCallConnectedTime() {
        return callConnectedTime;
    }

    public CallEngine getCallEngine() {
        return callEngine;
    }

    public void setCallEngine(CallEngine callEngine) {
        this.callEngine = callEngine;
    }

    public boolean isLocalVideoEnabled() {
        return isLocalVideoEnabled;
    }

    public void setLocalVideoEnabled(boolean localVideoEnabled) {
        isLocalVideoEnabled = localVideoEnabled;
    }

    public @NonNull
    String toString() {
        return "[State: " + state +
                ", recipient: " + recipient +
                ", identity: " + identityKey +
                ", remoteVideo: " + remoteVideoEnabled +
                ", localVideo: " + isLocalVideoEnabled +
                ", isRemoteVideoOffer: " + isRemoteVideoOffer +
                ", callConnectedTime: " + callConnectedTime +
                "]";
    }
}