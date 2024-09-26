package com.quintus.labs.smarthome.AwsIoTManager;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Regions;
import com.quintus.labs.smarthome.MyInterface.AwsIotMessageCallback;
import com.quintus.labs.smarthome.ShareDataPreference.DataLocalManager;

import java.nio.charset.StandardCharsets;

public class AwsIoTManager {
    private AwsIotMessageCallback awsIotMessageCallback;
    static final String LOG_TAG = "AWS_MANAGER";
    private static AwsIoTManager instance;
    private static final String CUSTOMER_SPECIFIC_ENDPOINT = "a3t2umusntklie-ats.iot.us-east-1.amazonaws.com";
    private static final String COGNITO_POOL_ID = "us-east-1:451d5ea5-7fd9-4691-a3b6-7c74cac9090d";
    private static final Regions MY_REGION = Regions.US_EAST_1;
    private static AWSIotMqttManager mqttManager;
    private static CognitoCachingCredentialsProvider credentialsProvider;
    private static String clientId = "8bb71518-91b5-4c17-a3ab-feb7a4848866";
    private AwsIoTManager() {
        // Private constructor to prevent instantiation from outside
    }

    public static synchronized AwsIoTManager getInstance() {
        if (instance == null) {
            instance = new AwsIoTManager();
        }
        return instance;
    }

    public void connect(Context context){
        credentialsProvider = new CognitoCachingCredentialsProvider(
                context, // context
                COGNITO_POOL_ID, // Identity Pool ID
                MY_REGION // Region
        );

        // MQTT Client
        mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_ENDPOINT);
        try {
            mqttManager.connect(credentialsProvider, new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(AWSIotMqttClientStatus status, Throwable throwable) {
                    Log.d(LOG_TAG, "Status = " + String.valueOf(status));
                    if (status == AWSIotMqttClientStatus.Connecting) {
                        Log.e(LOG_TAG, "Connecting...");

                    } else if (status == AWSIotMqttClientStatus.Connected) {
                        Log.e(LOG_TAG, "Connected");

                    } else if (status == AWSIotMqttClientStatus.Reconnecting) {
                        if (throwable != null) {
                            Log.e(LOG_TAG, "Connection error.", throwable);
                        }
                        Log.e(LOG_TAG, "Reconnecting");
                    } else if (status == AWSIotMqttClientStatus.ConnectionLost) {
                        if (throwable != null) {
                            Log.e(LOG_TAG, "Connection error.", throwable);
                            throwable.printStackTrace();
                        }
                        Log.e(LOG_TAG, "Disconnected");
                    } else {
                        Log.e(LOG_TAG, "Disconnected");
                    }
                }
            });
        }
        catch (Exception e){
            Log.e("HI", "Connection error.", e);
        }
    }

    public void sendMessageToAwsIot(String topic, String message) {
        try {
            mqttManager.publishString(message, topic, AWSIotMqttQos.QOS0);
        } catch (Exception e) {
            Log.e("HI", "Publish error.", e);
        }
    }

    public void subscribeTopicFromAwsIot(String topic){
        try {
           mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0, new AWSIotMqttNewMessageCallback() {
               @Override
               public void onMessageArrived(String topic, byte[] data) {

                   String message = new String(data, StandardCharsets.UTF_8);
                   DataLocalManager.setDataToTopic(topic, message);
                   Log.e(LOG_TAG,message);
               }
           });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void DisconnectAWSIoT(){
        mqttManager.disconnect();
    }
}
