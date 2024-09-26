package com.quintus.labs.smarthome.ShareDataPreference;

import android.content.Context;

public class DataLocalManager {
    private static DataLocalManager instance;
    private MySharePreferences mySharedPreferences;

    public static void init(Context context){
        instance = new DataLocalManager();
        instance.mySharedPreferences = new MySharePreferences(context);
    }

    public static DataLocalManager getInstance(){
        if(instance == null){
            instance = new DataLocalManager();
        }
        return instance;
    }

    public static void setDataToTopic(String topic, String data){
        DataLocalManager.getInstance().mySharedPreferences.putString(topic, data);
    }

    public static String  getDataFromTopic(String topic){
        return DataLocalManager.getInstance().mySharedPreferences.getStringValue(topic);
    }

    public static void removeDataOfTopic(String topic){
        DataLocalManager.getInstance().mySharedPreferences.remove(topic);
    }

    public static void clearAllData(){
        DataLocalManager.getInstance().mySharedPreferences.clear();
    }
}
