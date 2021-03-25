package com.video_call.widget;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.util.Consumer;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.video_call.Engine.CallEngine;
import com.video_call.NotificationPayloadData;
import com.video_call.R;
import com.video_call.ViewModel.WebRtcControls;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import de.hdodenhof.circleimageview.CircleImageView;

import static io.agora.rtc.RtcEngine.CreateRendererView;


public class WebRtcCallView2 extends FrameLayout {

  private static final long                  TRANSITION_DURATION_MILLIS          = 250;
  private static final int                   SMALL_ONGOING_CALL_BUTTON_MARGIN_DP = 8;
  private static final int                   LARGE_ONGOING_CALL_BUTTON_MARGIN_DP = 16;

  public static final int FADE_OUT_DELAY = 5000;


  private SurfaceView           localRenderer;
  private ImageView audioToggle;
  private ImageView        videoToggle;
  private AppCompatImageView call_screen_large_local_video_off_avatar;
  NotificationPayloadData recipient;

  private ImageView micToggle;
  private FrameLayout                     largeLocalRenderContainer;
  private FrameLayout                     localRenderPipFrame;
  private FrameLayout                     smallLocalRenderContainer;
  private FrameLayout                     remoteRenderContainer;
  private TextView                      recipientName;
  private TextView                      status;
  private ConstraintLayout              parent;
  private CircleImageView avatar;
  private ImageView                     avatarCard;
  private ControlsListener              controlsListener;
  private String                   recipientId;
   private ImageView                     answer;
  private ImageView                     cameraDirectionToggle;
  private PictureInPictureGestureHelper pictureInPictureGestureHelper;
  private ImageView                     hangup;
  private View                          answerWithAudio;
  private View                          answerWithAudioLabel;
  private View                          ongoingFooterGradient;
  private boolean mIsVideoEnabled=true;
  private boolean mIsSpeakerphoneEnabled=true;
  private boolean mIsMicEnabled=true;

  private final Set<View> incomingCallViews    = new HashSet<>();
  private final Set<View> topViews             = new HashSet<>();
  private final Set<View> visibleViewSet       = new HashSet<>();
  private final Set<View> adjustableMarginsSet = new HashSet<>();
   private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
   private WebRtcControls controls        = WebRtcControls.NONE;
  private final Runnable       fadeOutRunnable = new Runnable() {
    @Override
    public void run() {
      if (WebRtcCallView2.this.isAttachedToWindow() && controls.isFadeOutEnabled())
        WebRtcCallView2.this.fadeOutControls();
    }
  };
   public WebRtcCallView2(@NonNull Context context) {
    this(context, null);
  }
  public WebRtcCallView2(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    LayoutInflater.from(context).inflate(R.layout.webrtc_call_vieww, this, true);
  }

  @SuppressWarnings("CodeBlock2Expr")
  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();


    call_screen_large_local_video_off_avatar               = findViewById(R.id.call_screen_large_local_video_off_avatar);
    audioToggle               = findViewById(R.id.call_screen_speaker_toggle);
    videoToggle               = findViewById(R.id.call_screen_video_toggle);
    micToggle                 = findViewById(R.id.call_screen_audio_mic_toggle);
    localRenderPipFrame       = findViewById(R.id.call_screen_pip);
    largeLocalRenderContainer = findViewById(R.id.call_screen_large_local_renderer_holder);
    smallLocalRenderContainer = findViewById(R.id.call_screen_small_local_renderer_holder);
    remoteRenderContainer     = findViewById(R.id.call_screen_remote_renderer_holder);
    recipientName             = findViewById(R.id.call_screen_recipient_name);
    status                    = findViewById(R.id.call_screen_status);
    parent                    = findViewById(R.id.call_screen);
    avatar                    = findViewById(R.id.call_screen_recipient_avatar);
    avatarCard                = findViewById(R.id.call_screen_recipient_avatar_call_card);
    answer                    = findViewById(R.id.call_screen_answer_call);
    cameraDirectionToggle     = findViewById(R.id.call_screen_camera_direction_toggle);
    hangup                    = findViewById(R.id.call_screen_end_call);
    answerWithAudio           = findViewById(R.id.call_screen_answer_with_audio);
    answerWithAudioLabel      = findViewById(R.id.call_screen_answer_with_audio_label);
    ongoingFooterGradient     = findViewById(R.id.call_screen_ongoing_footer_gradient);

    View      topGradient            = findViewById(R.id.call_screen_header_gradient);
    View      downCaret              = findViewById(R.id.call_screen_down_arrow);
    View      decline                = findViewById(R.id.call_screen_decline_call);
    View      answerLabel            = findViewById(R.id.call_screen_answer_call_label);
    View      declineLabel           = findViewById(R.id.call_screen_decline_call_label);
    View      incomingFooterGradient = findViewById(R.id.call_screen_incoming_footer_gradient);
    Guideline statusBarGuideline     = findViewById(R.id.call_screen_status_bar_guideline);

    topViews.add(status);
    topViews.add(topGradient);
    topViews.add(recipientName);

    incomingCallViews.add(answer);
    incomingCallViews.add(answerLabel);
    incomingCallViews.add(decline);
    incomingCallViews.add(declineLabel);
    incomingCallViews.add(incomingFooterGradient);

    adjustableMarginsSet.add(micToggle);
   // adjustableMarginsSet.add(cameraDirectionToggle);
    adjustableMarginsSet.add(videoToggle);
    adjustableMarginsSet.add(audioToggle);


   /* audioToggle.setOnAudioOutputChangedListener(outputMode -> {
      runIfNonNull(controlsListener, listener -> listener.onAudioOutputChanged(outputMode));
    });
*/
    videoToggle.setSelected(!mIsVideoEnabled);
    videoToggle.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mIsVideoEnabled) {
          mIsVideoEnabled = false;
          videoToggle.setSelected(true);
         // callEngine.disableVideo();
        } else {
          mIsVideoEnabled = true;
          videoToggle.setSelected(false);
//callEngine.enableVideo();
        }
        runIfNonNull(controlsListener, new Consumer<ControlsListener>() {
          @Override
          public void accept(ControlsListener listener) {
            listener.onVideoChanged(mIsVideoEnabled);
          }
        });

      }
    });
    micToggle.setSelected(!mIsMicEnabled);
    micToggle.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mIsMicEnabled) {
          mIsMicEnabled = false;
          micToggle.setSelected(true);
        } else {
          mIsMicEnabled = true;
          micToggle.setSelected(false);
        }
        runIfNonNull(controlsListener, new Consumer<ControlsListener>() {
          @Override
          public void accept(ControlsListener listener) {
            listener.onMicChanged(mIsMicEnabled);
          }
        });
      }
    });
    audioToggle.setSelected(!mIsSpeakerphoneEnabled);
    audioToggle.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mIsSpeakerphoneEnabled) {
          mIsSpeakerphoneEnabled = false;
          audioToggle.setSelected(true);
         } else {
           mIsSpeakerphoneEnabled = true;
          audioToggle.setSelected(false);
        }
        runIfNonNull(controlsListener, new Consumer<ControlsListener>() {
          @Override
          public void accept(ControlsListener listener) {
            listener.onSpeakerphone(mIsSpeakerphoneEnabled);
          }
        });
      }
    });

    cameraDirectionToggle.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
       // callEngine.switchCamera();
        runIfNonNull(controlsListener, ControlsListener::onCameraDirectionChanged);
      }
    });

    hangup.setOnClickListener(v -> runIfNonNull(controlsListener, ControlsListener::onEndCallPressed));
    decline.setOnClickListener(v -> runIfNonNull(controlsListener, ControlsListener::onDenyCallPressed));

    downCaret.setOnClickListener(v -> runIfNonNull(controlsListener, ControlsListener::onDownCaretPressed));
    answer.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        runIfNonNull(controlsListener, ControlsListener::onAcceptCallPressed);
      }
    });

    answerWithAudio.setOnClickListener(v -> runIfNonNull(controlsListener, ControlsListener::onAcceptCallWithVoiceOnlyPressed));

    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        WebRtcCallView2.this.toggleControls(true);
      }
    });
    avatar.setOnClickListener(v -> toggleControls(true));

    pictureInPictureGestureHelper = PictureInPictureGestureHelper.applyTo(localRenderPipFrame);

   int statusBarHeight =getStatusBarHeight(this);
    statusBarGuideline.setGuidelineBegin(statusBarHeight);

   }



   public void videoToggle(){
     mIsVideoEnabled = false;
     videoToggle.setSelected(true);

   }
  public static int getStatusBarHeight(@NonNull View view) {
    int result = 0;
    int resourceId = view.getResources().getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
      result = view.getResources().getDimensionPixelSize(resourceId);
    }
    return result;
  }
  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (controls.isFadeOutEnabled()) {
      scheduleFadeOut();
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    cancelFadeOut();
  }


  public void setControlsListener(@Nullable ControlsListener controlsListener) {
    this.controlsListener = controlsListener;
  }



  public void setEngine(CallEngine callEngine){
   // this.callEngine=callEngine;

  }
  public void setRemoteVideoEnabled(boolean isRemoteVideoEnabled) {
    if (isRemoteVideoEnabled) {
      remoteRenderContainer.setVisibility(View.VISIBLE);
    } else {
      remoteRenderContainer.setVisibility(View.GONE);
    }
  }

  public void setLocalRenderer(@Nullable SurfaceView surfaceViewRenderer) {
     this.localRenderer=surfaceViewRenderer;
    setRenderer(largeLocalRenderContainer, surfaceViewRenderer);
    setRenderer(smallLocalRenderContainer, surfaceViewRenderer);

  /*  localRenderer = surfaceViewRenderer;


   if (surfaceViewRenderer==null){
     new Handler(Looper.getMainLooper()).post(new Runnable() {
       @Override
       public void run() {
         Log.d("UI thread", "I am the UI thread");
         localRenderer = CreateRendererView(getContext());
         localRenderer.setZOrderMediaOverlay(true);

       }
     });
   }
     setRenderer(largeLocalRenderContainer, localRenderer);
    setRenderer(smallLocalRenderContainer, localRenderer);
     if (callEngine!=null){
      callEngine.enableVideo();
      callEngine.setupLocalVideoFeed(localRenderer);
    }

   */


  }

  public void setRemoteRenderer(@Nullable SurfaceView remoteRenderer) {
    setRenderer(remoteRenderContainer, remoteRenderer);
  }

  public void set(CallEngine callEngine){
 //   this.callEngine=  callEngine;

  }
  public CallEngine getCallEngine(){
   return  CallEngine.getInstance(getContext());

  }
  public void setLocalRenderState(WebRtcLocalRenderState localRenderState, SurfaceView surfaceViewRenderer) {
   ///videoToggle.setChecked(localRenderState != WebRtcLocalRenderState.GONE, false);
    switch (localRenderState) {
      case GONE:
        Log.d("localRenderState","GONE");
        localRenderPipFrame.setVisibility(View.GONE);
        largeLocalRenderContainer.setVisibility(View.GONE);
        setRenderer(largeLocalRenderContainer, null);
        setRenderer(smallLocalRenderContainer, null);
        call_screen_large_local_video_off_avatar.setVisibility(View.GONE);
        break;
      case LARGE:
        Log.d("localRenderState","LARGE");
        localRenderPipFrame.setVisibility(View.GONE);
        largeLocalRenderContainer.setVisibility(View.VISIBLE);
        call_screen_large_local_video_off_avatar.setVisibility(View.GONE);
        if (largeLocalRenderContainer.getChildCount() == 0) {
          setRenderer(largeLocalRenderContainer, surfaceViewRenderer);
        }
        break;
      case SMALL:
        Log.d("localRenderState","SMALL");
        call_screen_large_local_video_off_avatar.setVisibility(View.GONE);
        localRenderPipFrame.setVisibility(View.VISIBLE);
        largeLocalRenderContainer.setVisibility(View.GONE);
        if (smallLocalRenderContainer.getChildCount() == 0) {
          setRenderer(smallLocalRenderContainer, surfaceViewRenderer);
        }
      case LARGE_NO_VIDEO:
        Log.d("localRenderState","LARGE_NO_VIDEO");
        localRenderPipFrame.setVisibility(View.GONE);
        largeLocalRenderContainer.setVisibility(View.GONE);
        smallLocalRenderContainer.removeAllViews();
        call_screen_large_local_video_off_avatar.setVisibility(View.VISIBLE);
       /* Glide.with(getContext().getApplicationContext())
                .load(recipient.getUrl())
                .transform(new CenterCrop(), new BlurTransformation(getContext(), 0.25f, BlurTransformation.MAX_RADIUS))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(call_screen_large_local_video_off_avatar);*/

         break;
    }
  }

 /* public void setCameraDirection(@NonNull CameraState.Direction cameraDirection) {
    this.cameraDirection = cameraDirection;

    if (localRenderer != null) {
      localRenderer.setMirror(cameraDirection == CameraState.Direction.FRONT);
    }
  }
*/

  public void setLocalRenderState(WebRtcLocalRenderState localRenderState) {
    ///videoToggle.setChecked(localRenderState != WebRtcLocalRenderState.GONE, false);
    switch (localRenderState) {
      case GONE:
        Log.d("localRenderState","GONE");
        localRenderPipFrame.setVisibility(View.GONE);
        largeLocalRenderContainer.setVisibility(View.GONE);
        setRenderer(largeLocalRenderContainer, null);
        setRenderer(smallLocalRenderContainer, null);
        call_screen_large_local_video_off_avatar.setVisibility(View.GONE);
        break;
      case LARGE:
        Log.d("localRenderState","LARGE");
        localRenderPipFrame.setVisibility(View.GONE);
        largeLocalRenderContainer.setVisibility(View.VISIBLE);
        call_screen_large_local_video_off_avatar.setVisibility(View.GONE);
        if (largeLocalRenderContainer.getChildCount() == 0) {
          setRenderer(largeLocalRenderContainer, localRenderer);
        }
        break;
      case SMALL:
        Log.d("localRenderState","SMALL");
        call_screen_large_local_video_off_avatar.setVisibility(View.GONE);
        localRenderPipFrame.setVisibility(View.VISIBLE);
        largeLocalRenderContainer.setVisibility(View.GONE);
        if (smallLocalRenderContainer.getChildCount() == 0) {
          setRenderer(smallLocalRenderContainer, localRenderer);
        }
      case LARGE_NO_VIDEO:
        Log.d("localRenderState","LARGE_NO_VIDEO");
        localRenderPipFrame.setVisibility(View.GONE);
        largeLocalRenderContainer.setVisibility(View.GONE);
        smallLocalRenderContainer.removeAllViews();
        call_screen_large_local_video_off_avatar.setVisibility(View.VISIBLE);
       /* Glide.with(getContext().getApplicationContext())
                .load(recipient.getUrl())
                .transform(new CenterCrop(), new BlurTransformation(getContext(), 0.25f, BlurTransformation.MAX_RADIUS))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(call_screen_large_local_video_off_avatar);*/

        break;
    }
  }

  /* public void setCameraDirection(@NonNull CameraState.Direction cameraDirection) {
     this.cameraDirection = cameraDirection;

     if (localRenderer != null) {
       localRenderer.setMirror(cameraDirection == CameraState.Direction.FRONT);
     }
   }
 */
  public void setRecipient(@NonNull NotificationPayloadData recipient) {
    this.recipient=recipient;
  /*  if (recipient.getChannelName() == recipientId) {
      return;
    }*/
    recipientId = recipient.getChannelName();
    recipientName.setText(recipient.getName());
//     Glide.with(getContext().getApplicationContext()).load(recipient.getImage()).into(avatar);
     /*if (recipient.getReceiverUid()!=null){
      Chat.callManager().listenIncomingCall(recipient.getReceiverUid(), new ListenerCall() {
        @Override
        public void Stat(BaseMessage baseMessage) {

        }

        @Override
        public void Busy() {

        }

        @Override
        public void onRinging() {

        }

        @Override
        public void EndRinging(String state) {

        }

        @Override
        public void CallState(String state) {
          Log.d("CallState10",state);

        }
      });
    }*/

  }

  public void showCallCard(boolean showCallCard) {
    avatarCard.setVisibility(showCallCard ? VISIBLE : GONE);
    avatar.setVisibility(showCallCard ? GONE : VISIBLE);
   // avatarCard.setVisibility(showCallCard ? VISIBLE : GONE);
   // avatar.setVisibility(showCallCard ? GONE : VISIBLE);
  }

  public void setStatus(@NonNull String status) {
    this.status.setText(status);
  }

 /* public void setStatusFromHangupType(@NonNull HangupMessage.Type hangupType) {
    switch (hangupType) {
      case NORMAL:
      case NEED_PERMISSION:
        status.setText(R.string.RedPhone_ending_call);
        break;
      case ACCEPTED:
        status.setText(R.string.WebRtcCallActivity__answered_on_a_linked_device);
        break;
      case DECLINED:
        status.setText(R.string.WebRtcCallActivity__declined_on_a_linked_device);
        break;
      case BUSY:
        status.setText(R.string.WebRtcCallActivity__busy_on_a_linked_device);
        break;
      default:
        throw new IllegalStateException("Unknown hangup type: " + hangupType);
    }
  }
*/
  public void setWebRtcControls(WebRtcControls webRtcControls) {
    Set<View> lastVisibleSet = new HashSet<>(visibleViewSet);

    visibleViewSet.clear();

    if (webRtcControls.displayTopViews()) {
      visibleViewSet.addAll(topViews);
    }

    if (webRtcControls.displayIncomingCallButtons()) {
      visibleViewSet.addAll(incomingCallViews);

    //  status.setText(R.string.WebRtcCallView__signal_voice_call);
     // answer.setImageDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.webrtc_call_screen_answer));
    }

    if (webRtcControls.displayAnswerWithAudio()) {
      visibleViewSet.add(answerWithAudio);
      visibleViewSet.add(answerWithAudioLabel);

    //  status.setText(R.string.WebRtcCallView__signal_video_call);
     // answer.setImageDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.webrtc_call_screen_answer_with_video));
    }

    if (webRtcControls.displayAudioToggle()) {
      visibleViewSet.add(audioToggle);

    /*  audioToggle.setControlAvailability(webRtcControls.enableHandsetInAudioToggle(),
                                         webRtcControls.enableHeadsetInAudioToggle());

      audioToggle.setAudioOutput(webRtcControls.getAudioOutput(), false);*/
    }

    if (webRtcControls.displayCameraToggle()) {
      visibleViewSet.add(cameraDirectionToggle);
    }

    if (webRtcControls.displayEndCall()) {
      visibleViewSet.add(hangup);
      visibleViewSet.add(ongoingFooterGradient);
    }

    if (webRtcControls.displayMuteAudio()) {
      visibleViewSet.add(micToggle);
    }

    if (webRtcControls.displayVideoToggle()) {
      visibleViewSet.add(videoToggle);
    }

    if (webRtcControls.displaySmallOngoingCallButtons()) {
      updateButtonStateForSmallButtons();
    } else if (webRtcControls.displayLargeOngoingCallButtons()) {
      updateButtonStateForLargeButtons();
    }

    if (webRtcControls.isFadeOutEnabled()) {
      if (!controls.isFadeOutEnabled()) {
        scheduleFadeOut();
      }
    } else {
      cancelFadeOut();
    }

    controls = webRtcControls;

    if (!visibleViewSet.equals(lastVisibleSet) || !controls.isFadeOutEnabled()) {
      fadeInNewUiState(lastVisibleSet, webRtcControls.displaySmallOngoingCallButtons());
      post(new Runnable() {
        @Override
        public void run() {
          pictureInPictureGestureHelper.setVerticalBoundaries(status.getBottom(), videoToggle.getTop());
        }
      });
    }
  }
  public void setWebRtcControls() {
    Set<View> lastVisibleSet = new HashSet<>(visibleViewSet);

    visibleViewSet.clear();
    visibleViewSet.addAll(topViews);
   // visibleViewSet.addAll(incomingCallViews);
    visibleViewSet.add(audioToggle);
    visibleViewSet.add(cameraDirectionToggle);
    visibleViewSet.add(hangup);
    visibleViewSet.add(ongoingFooterGradient);
    visibleViewSet.add(micToggle);
    visibleViewSet.add(videoToggle);


   /* visibleViewSet.add(answerWithAudio);
    visibleViewSet.add(answerWithAudioLabel);
*/











    if (true) {
      if (true) {
        scheduleFadeOut();
      }
    } else {
      cancelFadeOut();
    }


    if (!visibleViewSet.equals(lastVisibleSet)) {
      fadeInNewUiState(lastVisibleSet,true);
      post(new Runnable() {
        @Override
        public void run() {
          pictureInPictureGestureHelper.setVerticalBoundaries(status.getBottom(), videoToggle.getTop());
        }
      });
    }
  }
  public @NonNull View getVideoTooltipTarget() {
    return videoToggle;
  }

  private void toggleControls() {
    if (controls.isFadeOutEnabled() && status.getVisibility() == VISIBLE) {
      fadeOutControls();
    } else {
      fadeInControls();
    }
  }

  private void toggleControls(boolean view) {
    if ( status.getVisibility() == VISIBLE) {
      fadeOutControls();
    } else {
      fadeInControls();
    }
  }

  private void fadeOutControls() {
    fadeControls(ConstraintSet.GONE);
    controlsListener.onControlsFadeOut();
    pictureInPictureGestureHelper.clearVerticalBoundaries();
  }

  private void fadeInControls() {
    fadeControls(ConstraintSet.VISIBLE);
    pictureInPictureGestureHelper.setVerticalBoundaries(status.getBottom(), videoToggle.getTop());
    scheduleFadeOut();
  }

  private void fadeControls(int visibility) {
    Transition transition = new AutoTransition().setDuration(TRANSITION_DURATION_MILLIS);

    TransitionManager.beginDelayedTransition(parent, transition);

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(parent);

    for (View view : visibleViewSet) {
      constraintSet.setVisibility(view.getId(), visibility);
    }

    constraintSet.applyTo(parent);
  }

  private void fadeInNewUiState(@NonNull Set<View> previouslyVisibleViewSet, boolean useSmallMargins) {
    Transition transition = new AutoTransition().setDuration(TRANSITION_DURATION_MILLIS);

    TransitionManager.beginDelayedTransition(parent, transition);

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(parent);

    for (View view : SetUtil.difference(previouslyVisibleViewSet, visibleViewSet)) {
      constraintSet.setVisibility(view.getId(), ConstraintSet.GONE);
    }

    for (View view : visibleViewSet) {
      constraintSet.setVisibility(view.getId(), ConstraintSet.VISIBLE);

      if (adjustableMarginsSet.contains(view)) {
        constraintSet.setMargin(view.getId(),
                                ConstraintSet.END,
                               dpToPx(useSmallMargins ? SMALL_ONGOING_CALL_BUTTON_MARGIN_DP
                                                                : LARGE_ONGOING_CALL_BUTTON_MARGIN_DP));
      }
    }

    constraintSet.applyTo(parent);
  }
  public static int dpToPx(int dp) {
    return Math.round(dp * Resources.getSystem().getDisplayMetrics().density);
  }
  private void scheduleFadeOut() {
    cancelFadeOut();

    if (getHandler() == null) return;
    getHandler().postDelayed(fadeOutRunnable, FADE_OUT_DELAY);
  }

  private void cancelFadeOut() {
    if (getHandler() == null) return;
    getHandler().removeCallbacks(fadeOutRunnable);
  }

  private static void runIfNonNull(@Nullable ControlsListener controlsListener, @NonNull Consumer<ControlsListener> controlsListenerConsumer) {
    if (controlsListener != null) {
      controlsListenerConsumer.accept(controlsListener);
    }
  }

  private static void setRenderer(@NonNull ViewGroup container, @Nullable View renderer) {
    if (renderer == null) {
      container.removeAllViews();
      return;
    }

    ViewParent parent = renderer.getParent();
    if (parent != null && parent != container) {
      ((ViewGroup) parent).removeAllViews();
    }

    if (parent == container) {
      return;
    }

    container.addView(renderer);
  }



  private void updateButtonStateForLargeButtons() {
    //cameraDirectionToggle.setImageResource(R.drawable.webrtc_call_screen_camera_toggle);
   // hangup.setImageResource(R.drawable.webrtc_call_screen_hangup);
   // micToggle.setBackgroundResource(R.drawable.webrtc_call_screen_mic_toggle);
   // videoToggle.setBackgroundResource(R.drawable.webrtc_call_screen_video_toggle);
   // audioToggle.setImageResource(R.drawable.webrtc_call_screen_speaker_toggle);
  }

  private void updateButtonStateForSmallButtons() {
    //   cameraDirectionToggle.setImageResource(R.drawable.webrtc_call_screen_camera_toggle_small);
    //  hangup.setImageResource(R.drawable.webrtc_call_screen_hangup_small);
    //  micToggle.setBackgroundResource(R.drawable.webrtc_call_screen_mic_toggle_small);
    // videoToggle.setBackgroundResource(R.drawable.webrtc_call_screen_video_toggle_small);
    // audioToggle.setImageResource(R.drawable.webrtc_call_screen_speaker_toggle_small);
  }



  public interface ControlsListener {
    void onControlsFadeOut();
     void onVideoChanged(boolean isVideoEnabled);
    void onMicChanged(boolean isMicEnabled);
    void onSpeakerphone(boolean isSpeakeEnabled);
    void onCameraDirectionChanged();
    void onEndCallPressed();
    void onDenyCallPressed();
    void onAcceptCallWithVoiceOnlyPressed();
    void onAcceptCallPressed();
    void onDownCaretPressed();
  }
}