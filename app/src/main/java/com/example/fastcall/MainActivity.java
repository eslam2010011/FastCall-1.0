package com.example.fastcall;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.video_call.CallConfig;
import com.video_call.CallFast;
import com.video_call.Engine.CallState;
import com.video_call.Engine.CallType;
import com.video_call.NotificationPayloadData;
import com.video_call.util.NotificationHelper;
import com.video_call.util.PermissionUtil;

public class MainActivity extends AppCompatActivity {
 Button button1,button2;

    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1=findViewById(R.id.video1);

        button2=findViewById(R.id.video2);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionUtil.checkSelfPermission(MainActivity.this,Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) &&PermissionUtil.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
                    NotificationPayloadData notificationPayloadData=   new NotificationPayloadData();
                    notificationPayloadData.setName("Eslam");
                    notificationPayloadData.setChannelName("Eslam");
                    notificationPayloadData.setImage("Eslam");
                    notificationPayloadData.setCallState(CallState.ONGOING);
                    notificationPayloadData.setCallType(CallType.VIDEO);
                    new CallFast.CallActivityBuilder(notificationPayloadData)
                            .show(MainActivity.this);
                    CallFast.getCallConfig().setOnEndCallListener(new CallConfig.CallListener.OnCallListener() {
                        @Override
                        public void OnEnd(boolean OnEnd, long callDurationInMillis, AppCompatActivity appCompatActivity) {
                            Toast.makeText(MainActivity.this,"EndV3"+callDurationInMillis+"",Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void OnRinging(boolean isFinishTime) {
                            Toast.makeText(MainActivity.this,"EndV3"+"",Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void OnANSWER() {
                            Toast.makeText(MainActivity.this,"OnANSWER"+"",Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void OnDENY() {
                            Toast.makeText(MainActivity.this,"OnDENY"+"",Toast.LENGTH_LONG).show();

                        }
                    });
                }

            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (PermissionUtil.checkSelfPermission(MainActivity.this,Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) &&PermissionUtil.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
                    NotificationPayloadData notificationPayloadData=new NotificationPayloadData();
                    notificationPayloadData.setCallState(CallState.INCOMING);
                    notificationPayloadData.setCallType(CallType.VIDEO);
                    notificationPayloadData.setChannelName(CallType.VIDEO);
                    notificationPayloadData.setName("Eslam Mostafa");
                    NotificationManager  manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    Notification notification= new NotificationHelper(MainActivity.this)
                            .createIncomingCallNotification(notificationPayloadData)
                            .build();
                    manager.notify(1, notification);



                }

            }


        });


    }
}