package com.video_call.Audio;

import com.video_call.R;

public class ImAudioEx implements ImAudio {
    @Override
    public int getRingingSound() {
        return R.raw.incoming_call;
    }
    @Override
    public int getInIncomingSound() {
        return R.raw.incoming_call;
    }
    @Override
    public int getDisconnectedSound() {
        return R.raw.end_call;
    }
}
