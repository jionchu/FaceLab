package com.FaceLab.FaceLab.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.FaceLab.FaceLab.databinding.ActivityInfoBinding

class InfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.infoBtnBack.setOnClickListener { finish() }
    }
}