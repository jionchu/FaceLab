package com.FaceLab.FaceLab.ui

import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.FaceLab.FaceLab.databinding.ActivityTestBinding
import com.FaceLab.FaceLab.model.TestModel
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import java.io.IOException

class TestActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    private var byteArray: ByteArray? = null
    private var bmp: Bitmap? = null
    private lateinit var binding: ActivityTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) { }
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        // 광고가 제대로 로드 되는지 테스트 하기 위한 코드입니다.
        binding.adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                // 광고가 문제 없이 로드시 출력됩니다.
                Log.d("@@@", "onAdLoaded")
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }

        imageUri = Uri.parse(intent.extras!!.getString("imageUri"))
        try {
            bmp = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            binding.testIvFace.setImageBitmap(bmp)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        binding.testClAnalysis.setOnClickListener { startResultActivity() }
    }

    fun startResultActivity() {
        val model = TestModel(this, bmp!!)
        model.runModel()
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("imageUri", imageUri.toString())
        intent.putExtra("image", byteArray)
        intent.putExtra("output", model.output)
        intent.putExtra("sexAge", model.ageSex)
        intent.putExtra("emotion", model.emotion)
        intent.putExtra("result", model.result)
        startActivity(intent)
        finish()
    }
}