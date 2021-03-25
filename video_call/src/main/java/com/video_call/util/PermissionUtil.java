package com.video_call.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.video_call.CallFast;

public class PermissionUtil {
    public static boolean checkSelfPermission( Activity activity,String permission, int requestCode) {
         if (ContextCompat.checkSelfPermission(activity,
                permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }
}
