package com.FaceLab.FaceLab

import android.app.Application
import android.content.SharedPreferences

/**
 * Created by jionchu on 2021-01-28
 */
class ApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()
        if (sSharedPreferences == null) {
            sSharedPreferences = applicationContext.getSharedPreferences(TAG, MODE_PRIVATE)
        }
    }

    companion object {
        var sSharedPreferences: SharedPreferences? = null

        // SharedPreferences 키 값
        var TAG = "FACE_LAB"
    }
}