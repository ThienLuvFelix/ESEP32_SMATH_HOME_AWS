package com.quintus.labs.smarthome.ShareDataPreference;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharePreferences {

    private static final String MY_SHARED_PREFERENCES = "MY_SHARED_PREFERENCES";
    private Context mContext;

    public MySharePreferences(Context context) {
        this.mContext = context;
    }

    public void putString(String key,String value){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(MY_SHARED_PREFERENCES,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getStringValue(String key){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(MY_SHARED_PREFERENCES,Context.MODE_PRIVATE);
        return sharedPreferences.getString(key,"");
    }

    public void remove(String key) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(MY_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    public void clear() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(MY_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
