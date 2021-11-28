package com.FaceLab.FaceLab.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.FaceLab.FaceLab.R
import com.FaceLab.FaceLab.databinding.ActivityResultBinding
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ResultActivity : AppCompatActivity() {
    private var mAniFabOpen: Animation? = null
    private var mAniFabClose: Animation? = null
    private var isFabOpen = false
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //floating action button 애니메이션 설정
        mAniFabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        mAniFabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        val intent = intent
        val imageUri = Uri.parse(intent.extras!!.getString("imageUri"))

        try {
            val bmp = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            binding.resultIvFace.setImageBitmap(bmp)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        //분석 결과 나타내기
        binding.resultTvAge.text = getIntent().getStringExtra("sexAge")
        binding.resultTvEmotion.text = getIntent().getStringExtra("emotion")
        binding.resultTvContent.text = getIntent().getStringExtra("result")
        val output2 = getIntent().getFloatArrayExtra("output")

        //차트 부분
        val chart = findViewById<View>(R.id.result_chart) as HorizontalBarChart
        setChart(chart, output2)

        binding.resultFabMenu.setOnClickListener { toggleFab() }
        binding.resultFabShare.setOnClickListener { resultShare() }
        binding.resultFabInstagram.setOnClickListener { shareInstagram() }
        binding.resultFabTwitter.setOnClickListener { shareTwitter() }
        binding.resultFabDownload.setOnClickListener { download() }
    }

    private fun resultShare() {
        toggleFab()
        try {
            val bitmap = getBitmapFromView(
                binding.resultScroll,
                binding.resultScroll.getChildAt(0).height,
                binding.resultScroll.getChildAt(0).width
            )
            val share = Intent(Intent.ACTION_SEND)
            share.type = "image/*"
            share.putExtra(Intent.EXTRA_STREAM, getImageUri(applicationContext, bitmap))
            startActivity(Intent.createChooser(share, "공유하기"))
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun shareInstagram() {
        toggleFab()
        try {
            val bitmap = getBitmapFromView(
                binding.resultScroll,
                binding.resultScroll.getChildAt(0).height,
                binding.resultScroll.getChildAt(0).width
            )
            val share = Intent(Intent.ACTION_SEND)
            share.type = "image/*"
            share.putExtra(Intent.EXTRA_STREAM, getImageUri(applicationContext, bitmap))
            share.setPackage("com.instagram.android")
            startActivity(share)
        } catch (e: ActivityNotFoundException) {
            val marketLaunch = Intent(Intent.ACTION_VIEW)
            marketLaunch.data = Uri.parse("market://details?id=com.instagram.android")
            startActivity(marketLaunch)
        }
    }

    private fun shareTwitter() {
        toggleFab()
        try {
            val bitmap = getBitmapFromView(
                binding.resultScroll,
                binding.resultScroll.getChildAt(0).height,
                binding.resultScroll.getChildAt(0).width
            )
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, "얼굴분석결과는 다음과 같습니다 -AI 얼굴분석 앱 [Face Lab]")
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_STREAM, getImageUri(applicationContext, bitmap))
            intent.type = "image/*"
            intent.setPackage("com.twitter.android")
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val marketLaunch = Intent(Intent.ACTION_VIEW)
            marketLaunch.data = Uri.parse("market://details?id=com.twitter.android")
            startActivity(marketLaunch)
        }
    }

    private fun download() {
        toggleFab()
        val bitmap = getBitmapFromView(
            binding.resultScroll,
            binding.resultScroll.getChildAt(0).height,
            binding.resultScroll.getChildAt(0).width
        )
        saveImage(bitmap)
    }

    private fun setChart(chart: HorizontalBarChart, data: FloatArray?) {
        val barEntryList = ArrayList<BarEntry>()
        barEntryList.add(BarEntry(0f, data!![0] * 100))
        barEntryList.add(BarEntry(1f, data[1] * 100))
        barEntryList.add(BarEntry(2f, data[2] * 100))
        barEntryList.add(BarEntry(3f, data[3] * 100))
        barEntryList.add(BarEntry(4f, data[4] * 100))
        barEntryList.add(BarEntry(5f, data[5] * 100))
        barEntryList.add(BarEntry(6f, data[6] * 100))
        val barDataSet = BarDataSet(barEntryList, "Feelings")
        val barData = BarData(barDataSet)
        barDataSet.setColors(
            Color.rgb(242, 208, 242),
            Color.rgb(181, 156, 217),
            Color.rgb(32, 79, 140),
            Color.rgb(29, 123, 163),
            Color.rgb(12, 53, 89),
            Color.rgb(181, 156, 217),
            Color.rgb(242, 208, 242)
        )
        chart.data = barData
        chart.setTouchEnabled(false)
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        val feeling = arrayOf("Anger", "Disgust", "Fear", "Happy", "Sad", "Surprise", "Neutral")
        val formatter = IndexAxisValueFormatter(feeling)
        xAxis.granularity = 1f
        xAxis.valueFormatter = formatter
        chart.setFitBars(true)
        chart.setBackgroundColor(Color.WHITE)
        barData.barWidth = 0.8f
        chart.invalidate()
    }

    private fun toggleFab() {
        if (isFabOpen) {
            binding.resultFabMenu.setImageResource(R.drawable.ic_more)
            binding.resultFabShare.startAnimation(mAniFabClose)
            binding.resultFabInstagram.startAnimation(mAniFabClose)
            binding.resultFabTwitter.startAnimation(mAniFabClose)
            binding.resultFabDownload.startAnimation(mAniFabClose)
            binding.resultFabShare.isClickable = false
            binding.resultFabInstagram.isClickable = false
            binding.resultFabTwitter.isClickable = false
            binding.resultFabDownload.isClickable = false
            isFabOpen = false
        } else {
            binding.resultFabMenu.setImageResource(R.drawable.ic_close)
            binding.resultFabShare.startAnimation(mAniFabOpen)
            binding.resultFabInstagram.startAnimation(mAniFabOpen)
            binding.resultFabTwitter.startAnimation(mAniFabOpen)
            binding.resultFabDownload.startAnimation(mAniFabOpen)
            binding.resultFabShare.isClickable = true
            binding.resultFabInstagram.isClickable = true
            binding.resultFabTwitter.isClickable = true
            binding.resultFabDownload.isClickable = true
            isFabOpen = true
        }
    }

    private fun getBitmapFromView(view: View?, height: Int, width: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view!!.background
        if (bgDrawable != null) bgDrawable.draw(canvas) else canvas.drawRGB(35, 7, 77)
        view.draw(canvas)
        return bitmap
    }

    private fun getImageUri(context: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    private fun saveImage(finalBitmap: Bitmap) {
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val myDir = File("$root/FaceLab")
        myDir.mkdirs()

        @SuppressLint("SimpleDateFormat")
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fileName = "FaceLab-$timeStamp.jpg"
        val file = File(myDir, fileName)
        file.delete()

        try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            /*media broadcasting*/
            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
            out.flush()
            out.close()
            Toast.makeText(this, "이미지 저장 완료", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "이미지 저장 실패", Toast.LENGTH_LONG).show()
        }
    }
}