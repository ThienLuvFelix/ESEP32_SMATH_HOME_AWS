package com.quintus.labs.smarthome.MyService;

import static com.quintus.labs.smarthome.application.myApplication.channel_id;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.quintus.labs.smarthome.AwsIoTManager.AwsIoTManager;
import com.quintus.labs.smarthome.R;
import com.quintus.labs.smarthome.ui.activity.MainActivity;


public class AWS_IoT_Service extends Service {
    private AwsIoTManager awsIoTManager = AwsIoTManager.getInstance();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("SV", "Start....");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        awsIoTManager.subscribeTopicFromAwsIot("$aws/things/ESP32_THING_4LED/shadow/get/accepted");
        //awsIoTManager.subscribeTopicFromAwsIot("$aws/things/ESP32_THING_4LED/shadow/update/accepted");
        Intent intentClick = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, channel_id)
                .setContentTitle("Hi")
                .setContentText("Alo")
                .setSmallIcon(R.drawable.logo)
                //.setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        awsIoTManager.DisconnectAWSIoT();
    }
}
