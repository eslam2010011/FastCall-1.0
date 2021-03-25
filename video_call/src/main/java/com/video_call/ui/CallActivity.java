package com.video_call.ui;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.util.Rational;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.video_call.R;
import com.video_call.Service.WebRtcCallService;
import com.video_call.ViewModel.WebRtcCallViewModel;
import com.video_call.ViewModel.WebRtcViewModel;
import com.video_call.util.EllapsedTimeFormatter;
import com.video_call.widget.WebRtcCallView2;
import com.video_call.widget.WebRtcCallView3;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.agora.rtc.RtcEngine;


public class CallActivity extends BaseRtcActivity2 {
    private static final String TAG = "CallActivity";
    private WebRtcCallView3 callScreen;
    private WebRtcCallViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        work();

    }

    public void work() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.webrtc_call_activityw);
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        initializeResources();
        initializeViewModel();

    }


    private void initializeResources() {
        callScreen = findViewById(R.id.callScreen);
        callScreen.setControlsListener(new ControlsListener());
    }

    private void initializeViewModel() {
        viewModel = ViewModelProviders.of(this).get(WebRtcCallViewModel.class);
        viewModel.getRemoteVideoEnabled().observe(this, callScreen::setRemoteVideoEnabled);
        viewModel.getMicrophoneEnabled().observe(this, callScreen::setMicEnabled);
        viewModel.getLocalRenderState().observe(this, callScreen::setLocalRenderState);
        viewModel.getWebRtcControls().observe(this, webRtcControls -> callScreen.setWebRtcControls(webRtcControls));
        viewModel.getWebRtcControls().observe(this, callScreen::setWebRtcControls);
        viewModel.getCallTime().observe(this, this::handleCallTime);
        viewModel.displaySquareCallCard().observe(this, callScreen::showCallCard);

    }

    private final class ControlsListener implements WebRtcCallView3.ControlsListener {
        @Override
        public void onControlsFadeOut() {

        }

        @Override
        public void onVideoChanged(boolean isVideoEnabled) {
          /*if (WebRtcCallService.getInstance()!=null){
              WebRtcCallService.getInstance().switchCamera();
          }*/
            // handleSetMuteVideo(!isVideoEnabled);
            Intent intent = new Intent(CallActivity.this, WebRtcCallService.class);
            intent.setAction(WebRtcCallService.ACTION_SET_ENABLE_VIDEO);
            intent.putExtra(WebRtcCallService.EXTRA_ENABLE, isVideoEnabled);
            startService(intent);
        }

        @Override
        public void onMicChanged(boolean isMicEnabled) {
            handleSetMuteAudio(!isMicEnabled);
        }

        @Override
        public void onSpeakerphone(boolean isSpeakeEnabled) {
            // handleSetAudioSpeaker(isSpeakeEnabled);
        }

        @Override
        public void onCameraDirectionChanged() {
            handleFlipCamera();
        }

        @Override
        public void onEndCallPressed() {
            handleEndedTimeout();
            Intent intent = new Intent(CallActivity.this, WebRtcCallService.class);
            stopService(intent);
            finish();
        }

        @Override
        public void onDenyCallPressed() {
        }

        @Override
        public void onAcceptCallWithVoiceOnlyPressed() {

        }

        @Override
        public void onAcceptCallPressed() {

        }

        @Override
        public void onDownCaretPressed() {

        }

        @Override
        public void onHeartActionClicked(ImageView heartIcon) {

        }


    }

    private void handleSetAudioSpeaker(boolean enableSpeakerIfAvailable) {
        Intent intent = new Intent(this, WebRtcCallService.class);
        intent.setAction(WebRtcCallService.ACTION_SET_AUDIO_SPEAKER);
        intent.putExtra(WebRtcCallService.EXTRA_SPEAKER, enableSpeakerIfAvailable);
        startService(intent);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(final WebRtcViewModel event) {
        Log.i(TAG, "Got message from service: " + event);
        viewModel.setRecipient(event.getRecipient());
        callScreen.setRecipient(event.getRecipient());
        callScreen.set(event.getCallEngine());
        switch (event.getState()) {
            case CALL_CONNECTED:         //countDownTime.cancel(); //handleCallConnected(event);                                                break;
            case NETWORK_FAILURE:        // handleServerFailure(event);                                                break;
            case CALL_RINGING:
                handleCallRinging(event);
                break;
            case CALL_DISCONNECTED:
                handleTerminate(event);
                break;
            case CALL_DISCONNECTED_End:
                break;
            case CALL_ACCEPTED_ELSEWHERE:// handleTerminate(event.getRecipient(), HangupMessage.Type.ACCEPTED);        break;
            case CALL_DECLINED_ELSEWHERE:
                handleTerminate();
                break;
            case CALL_ONGOING_ELSEWHERE://  handleTerminate(event.getRecipient(), HangupMessage.Type.BUSY);            break;
            case CALL_NEEDS_PERMISSION:  // handleTerminate(event.getRecipient(), HangupMessage.Type.NEED_PERMISSION); break;
            case NO_SUCH_USER:           // handleNoSuchUser(event);                                                   break;
            case RECIPIENT_UNAVAILABLE:  // handleRecipientUnavailable(event);                                         break;
            case CALL_INCOMING:
                handleIncomingCall(event);
                break;
            case CALL_OUTGOING:
                handleOutgoingCall(event);
                break;
            case CALL_BUSY:               //handleCallBusy(event);                                                     break;
            case UNTRUSTED_IDENTITY:     // handleUntrustedIdentity(event);                                            break;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callScreen.setLocalRenderer(event.getLocalRenderer());

            }
        });

        callScreen.setRemoteRenderer(event.getRemoteRenderer());
        viewModel.updateFromWebRtcViewModel(event, event.isLocalVideoEnabled());
    }

    private void handleOutgoingCall(WebRtcViewModel webRtcViewModel) {
        // handleEndedTimeout();

        callScreen.setStatus(getString(R.string.WebRtcCallActivity__calling));
    }

    private void handleCallTime(long callTime) {
        EllapsedTimeFormatter ellapsedTimeFormatter = EllapsedTimeFormatter.fromDurationMillis(callTime);
        if (ellapsedTimeFormatter == null) {
            return;
        }
        callScreen.setStatus(getString(R.string.WebRtcCallActivity__signal_s, ellapsedTimeFormatter.toString()));
    }


    private boolean isSystemPipEnabledAndAvailable() {
        return Build.VERSION.SDK_INT >= 26 &&
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE);
    }

    private boolean isInPipMode() {
        return isSystemPipEnabledAndAvailable() && isInPictureInPictureMode();
    }

    private void handleIncomingCall(@NonNull WebRtcViewModel event) {
        callScreen.setRecipient(event.getRecipient());
    }

    private void handleCallRinging(@NonNull WebRtcViewModel event) {
        callScreen.setRecipient(event.getRecipient());
        // callScreen.setStatus("ringing");
    }

    private void handleTerminate(WebRtcViewModel event) {
        callScreen.setStatus("Ending call");


        EventBus.getDefault().removeStickyEvent(WebRtcViewModel.class);

    }

    private void handleTerminate() {

        EventBus.getDefault().removeStickyEvent(WebRtcViewModel.class);

    }


    public void handleTerminate2() {

        EventBus.getDefault().removeStickyEvent(WebRtcViewModel.class);

    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: ");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRtcServiceConnected(RtcEngine rtcEngine) {

    }


}
