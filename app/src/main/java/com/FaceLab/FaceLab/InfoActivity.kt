package com.FaceLab.FaceLab

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
    }

    fun customOnClick(v: View) {
        if (v.id == R.id.info_btn_back) finish()
    }
}