package com.FaceLab.FaceLab.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.FaceLab.FaceLab.R

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
    }

    fun customOnClick(v: View) {
        if (v.id == R.id.info_btn_back) finish()
    }
}