package com.inspirati.iotcompanion.model;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceManager {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor spEditor;
    Context context;

    int MODE = 0;

    private static final String PREFERENCE = "IoTCompanion";

    private static final String TAG = "##PREFMANAGER##";

    public SharedPreferenceManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREFERENCE, MODE);
        spEditor = sharedPreferences.edit();
    }

    public void setMyKey(String myKey,String myValue) {
        spEditor.putString(myKey,myValue);
        spEditor.commit();
    }

    public String getMyKey(String myKey) {
        final String myVal = sharedPreferences.getString(myKey,"");
        return myVal;
    }

}


