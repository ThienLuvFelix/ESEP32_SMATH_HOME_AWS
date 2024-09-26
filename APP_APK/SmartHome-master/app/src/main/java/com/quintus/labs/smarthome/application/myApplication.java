package com.quintus.labs.smarthome.application;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;

import com.quintus.labs.smarthome.AwsIoTManager.AwsIoTManager;
import com.quintus.labs.smarthome.ShareDataPreference.DataLocalManager;

public class myApplication extends Application {
    private AwsIoTManager awsIoTManager = AwsIoTManager.getInstance();
    public  static  final String channel_id = "channel_service";
    @Override
    public void onCreate() {
        super.onCreate();
        DataLocalManager.init(getApplicationContext());
        awsIoTManager.connect(getApplicationContext());
        createNotify();

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Intent intent = new Intent(this, myApplication.class);
        stopService(intent);
    }

    private void createNotify() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channel_id,
                    "Channel Service",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if(manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

}
