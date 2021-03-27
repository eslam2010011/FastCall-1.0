# FastCall-1.0
 
 FastCall Library is a powerful library for doing any type of Call (video and audio Call like messenger) in Android applications .
 
 
 ![stack Overflow](https://firebasestorage.googleapis.com/v0/b/opayapp-1135d.appspot.com/o/3ad8f6c9-cb7c-432d-9b6c-382d263e8a24.jfif?alt=media&token=baef21a9-6449-4840-b707-10a9531839b4)
 ![stack Overflow](https://firebasestorage.googleapis.com/v0/b/opayapp-1135d.appspot.com/o/8272a997-5658-4a9b-9829-672e0b3ba705.jfif?alt=media&token=c347e497-f581-4591-a79b-a48a2751b175)

 
 # Step 1
 
 
 
 ```html

  implementation 'androidx.preference:preference:1.1.1'
  implementation 'androidx.lifecycle:lifecycle-process:2.3.0'
  implementation 'androidx.lifecycle:lifecycle-extensions:2.2.0'
  implementation 'de.hdodenhof:circleimageview:2.1.0'
  implementation 'com.github.bumptech.glide:glide:3.8.0'
  implementation 'io.agora.rtc:full-sdk:3.0.1'
 implementation 'org.greenrobot:eventbus:3.0.0'
implementation 'com.google.code.gson:gson:2.8.6'

```

# Step 2
 ```html
    implementation 'com.github.eslam2010011:FastCall-1.0:1.9'
```

# Step 3

 ```html
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
		//https://www.agora.io/en/
        FastCall.init(this,"api_key_agora",new CallConfig().setRingingTimeEnd(30000).setiNotification(new INotification() {
            @Override
            public Notification getNotificationInGoing() {
                return   new NotificationHelper(FastCall.getContext())
                        .getNotification1(getString(R.string.app_name)+" Call", "Tap to return")
                        .build();
            }

            @Override
            public Notification getNotificationInIncoming() {
                return null;
            }
        }));
    }
}

```

# Step 4

 ```html
 
  if (PermissionUtil.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) &&PermissionUtil.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
            NotificationPayloadData notificationPayloadData=   new NotificationPayloadData();
            notificationPayloadData.setName("user_name");
            notificationPayloadData.setChannelName("Channel_Name");
            notificationPayloadData.setImage("url_image");
            notificationPayloadData.setCallState(CallState.ONGOING);  //CallState => ONGOING or INCOMING
            notificationPayloadData.setCallType(CallType.VIDEO); CallType=> VIDEO of VOICE
            new FastCall.CallActivityBuilder(notificationPayloadData)
                    .show(MainActivity.this);
            FastCall.getCallConfig().setOnEndCallListener(new CallConfig.CallListener.OnCallListener() {
                @Override
                public void OnEnd(boolean OnEnd, long callDurationInMillis, AppCompatActivity appCompatActivity) {
                    Toast.makeText(MainActivity.this,"OnEnd"+callDurationInMillis+"",Toast.LENGTH_LONG).show();
                }

                @Override
                public void OnRinging(boolean isFinishTime) {
                    Toast.makeText(MainActivity.this,"OnRinging"+"",Toast.LENGTH_LONG).show();
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
		
	
	
```	
		
  # Step 4 (INCOMING Call )

 ```html
 
 
 
 public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size()>0){
		 String type = remoteMessage.getData().get("TYPE");
		 if (type.equals("call")){
		 
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

 
