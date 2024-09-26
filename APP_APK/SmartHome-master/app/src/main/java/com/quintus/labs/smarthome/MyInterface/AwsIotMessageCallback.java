package com.quintus.labs.smarthome.MyInterface;

public interface AwsIotMessageCallback {
    void onMessageReceived(final String topic, final String message);
}
