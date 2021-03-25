package com.video_call.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;



public class VoIPActionsReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (WebRtcCallService.getInstance() != null) {
			WebRtcCallService.getInstance().handleNotificationAction(context,intent);
		}
	}
}