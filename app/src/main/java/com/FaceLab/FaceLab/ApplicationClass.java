package com.FaceLab.FaceLab;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jionchu on 2021-01-28
 */
public class ApplicationClass extends Application {
    public static SharedPreferences sSharedPreferences = null;

    // SharedPreferences 키 값
    public static String TAG = "FACE_LAB";

    @Override
    public void onCreate() {
        super.onCreate();

        if (sSharedPreferences == null) {
            sSharedPreferences = getApplicationContext().getSharedPreferences(TAG, Context.MODE_PRIVATE);
        }
    }
}
