package com.video_call.ViewModel;

import androidx.annotation.NonNull;

public final class WebRtcControls {

  public static final WebRtcControls NONE = new WebRtcControls();
  public static final WebRtcControls PIP  = new WebRtcControls(false, false, false, false, true, CallState.NONE );

  private final boolean           isRemoteVideoEnabled;
  private final boolean           isLocalVideoEnabled;
  private final boolean           isMoreThanOneCameraAvailable;
  private final boolean           isBluetoothAvailable;
  private final boolean           isInPipMode;
  private final CallState         callState;

  private WebRtcControls() {
    this(false, false, false, false, false, CallState.NONE );
  }

  public WebRtcControls(boolean isLocalVideoEnabled,
                        boolean isRemoteVideoEnabled,
                        boolean isMoreThanOneCameraAvailable,
                        boolean isBluetoothAvailable,
                        boolean isInPipMode,
                        @NonNull CallState callState
  )
  {
    this.isLocalVideoEnabled          = isLocalVideoEnabled;
    this.isRemoteVideoEnabled         = isRemoteVideoEnabled;
    this.isBluetoothAvailable         = isBluetoothAvailable;
    this.isMoreThanOneCameraAvailable = isMoreThanOneCameraAvailable;
    this.isInPipMode                  = isInPipMode;
    this.callState                    = callState;
   }

  public boolean displayEndCall() {
    return isOngoing();
  }

  public boolean displayMuteAudio() {
    return isOngoing();
  }

  public boolean displayVideoToggle() {
    return isOngoing();
  }

  public boolean displayAudioToggle() {
    return isOngoing(); //&& (!isLocalVideoEnabled || isBluetoothAvailable);
  }

  public boolean displayCameraToggle() {
    return isOngoing() && isLocalVideoEnabled && isMoreThanOneCameraAvailable;
  }

  public boolean displayAnswerWithAudio() {
    return isIncoming();
  }

  public boolean displayIncomingCallButtons() {
    return isIncoming();
  }

  boolean enableHandsetInAudioToggle() {
    return !isLocalVideoEnabled;
  }

  boolean enableHeadsetInAudioToggle() {
    return isBluetoothAvailable;
  }

  public boolean isFadeOutEnabled() {
    return isOngoing() && isRemoteVideoEnabled;
  }

  public boolean displaySmallOngoingCallButtons() {
    return isOngoing() && displayAudioToggle() && displayCameraToggle();
  }

  public boolean displayLargeOngoingCallButtons() {
    return isOngoing() && !(displayAudioToggle() && displayCameraToggle());
  }

  public boolean displayTopViews() {
    return !isInPipMode;
  }


  private boolean isOngoing() {
    return callState == CallState.ONGOING;
  }

  private boolean isIncoming() {
    return callState == CallState.INCOMING;
  }
  public enum CallState {
    NONE,
    INCOMING,
    ONGOING
  }
}