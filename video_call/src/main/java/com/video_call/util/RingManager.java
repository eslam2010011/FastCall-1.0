package com.video_call.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;

import com.video_call.Audio.ImAudio;
import com.video_call.Engine.CallEngine;

import java.io.IOException;


public final class RingManager {
    private static RingManager ringManager;
    private AudioManager audioManager;
    private MediaPlayer phoneRingPlayer;
    private MediaPlayer phoneHangupPlayer;
    private Context context;
    private Vibrator v;

    public static RingManager getInstance(Context context) {
        if (ringManager == null) {
            ringManager = new RingManager(context);
        }

        return ringManager;
    }

    private RingManager(Context context) {
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }


    public synchronized void playDisconnect(CallEngine.CallType callType,ImAudio imAudio) {

        if (phoneRingPlayer != null) {
            phoneRingPlayer.release();
        }
        phoneRingPlayer = new MediaPlayer();
        phoneRingPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        phoneRingPlayer.setLooping(true);
        String packageName = context.getPackageName();
        Uri dataUri = Uri.parse("android.resource://" + packageName + "/" + imAudio.getDisconnectedSound());
        try {
            phoneRingPlayer.setDataSource(context, dataUri);
            phoneRingPlayer.prepare();
            phoneRingPlayer.start();

        } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
            // Logger.error(TAG, e.getMessage());
        }


    }

    public synchronized void playRinging(CallEngine.CallType callType , ImAudio imAudio) {
        stop();
        phoneRingPlayer = MediaPlayer.create(context,imAudio.getRingingSound());
        phoneRingPlayer.setLooping(true);
        if (phoneRingPlayer != null && !phoneRingPlayer.isPlaying()) {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setSpeakerphoneOn(callType == CallEngine.CallType.VIDEO);
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE, 0);
            phoneRingPlayer.start();

        }
    }



    public void setSpeakerPhoneOn(boolean speakerOn) {
        audioManager.setSpeakerphoneOn(speakerOn);
    }

    public synchronized void stop() {
        if (phoneRingPlayer != null) {
            if (phoneRingPlayer.isPlaying()) {
                phoneRingPlayer.stop();
            }
            phoneRingPlayer.release();
            phoneRingPlayer = null;
        }
        stopVibrate();
    }

    private void vibrate() {
        long[] pattern = {500, 300, 500};
        if (v != null) {
            v.vibrate(pattern, 0);
        }
    }

    private void stopVibrate() {
        if (v != null) {
            v.cancel();
        }
    }

    private void calculateVolume() {
        int ringVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        int maxRingVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        int maxMusicVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        final int calculatedVolume = ringVolume * maxMusicVolume / maxRingVolume;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, calculatedVolume, AudioManager.FLAG_PLAY_SOUND);
    }


}