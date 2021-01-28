package com.FaceLab.FaceLab;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.FaceLab.FaceLab.TestActivity.getRoundedCornerBitmap;

public class ResultActivity extends AppCompatActivity {

    private FloatingActionButton mFabMenu, mFabShare, mFabInsta, mFabTwitter, mFabDownload;
    private Animation mAniFabOpen, mAniFabClose;
    private boolean isFabOpen = false;
    private ScrollView mScrollView;
    private TextView mTvRights;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        ImageView ivFace = findViewById(R.id.result_iv_face);
        TextView tvSexAge = findViewById(R.id.result_tv_age);
        TextView tvEmotion = findViewById(R.id.result_tv_emotion);
        TextView tvResult = findViewById(R.id.result_tv_content);
        mScrollView = findViewById(R.id.result_scroll);
        mTvRights = findViewById(R.id.result_tv_rights);
        mFabMenu = findViewById(R.id.result_fab_menu);
        mFabShare = findViewById(R.id.result_fab_share);
        mFabInsta = findViewById(R.id.result_fab_instagram);
        mFabTwitter = findViewById(R.id.result_fab_twitter);
        mFabDownload = findViewById(R.id.result_fab_download);

        //floating action button 애니메이션 설정
        mAniFabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        mAniFabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close);

        Intent intent = getIntent();
        byte[] byteArray = intent.getByteArrayExtra("image");
        assert byteArray != null;
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        bmp = getRoundedCornerBitmap(bmp);
        ivFace.setImageBitmap(bmp);

        //분석 결과 나타내기
        tvSexAge.setText(getIntent().getStringExtra("sexAge"));
        tvEmotion.setText(getIntent().getStringExtra("emotion"));
        tvResult.setText(getIntent().getStringExtra("result"));
        float[] output2 = getIntent().getFloatArrayExtra("output");

        //차트 부분
        HorizontalBarChart chart = (HorizontalBarChart) findViewById(R.id.result_chart);
        assert output2 != null;
        setChart(chart, output2);
    }

    public void customOnClick(View view) {
        switch (view.getId()) {
            case R.id.result_fab_menu:
                toggleFab();
                break;
            case R.id.result_fab_share: //공유하기
                toggleFab();
                try {
                    Bitmap bitmap = getBitmapFromView(mScrollView, mScrollView.getChildAt(0).getHeight(), mScrollView.getChildAt(0).getWidth());

                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/*");
                    share.putExtra(Intent.EXTRA_STREAM, getImageUri(getApplicationContext(), bitmap));
                    startActivity(Intent.createChooser(share, "공유하기"));

                    finish();
                    break;
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            case R.id.result_fab_instagram:
                toggleFab();
                try {
                    Bitmap bitmap = getBitmapFromView(mScrollView, mScrollView.getChildAt(0).getHeight(), mScrollView.getChildAt(0).getWidth());
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/*");
                    share.putExtra(Intent.EXTRA_STREAM, getImageUri(getApplicationContext(), bitmap));
                    share.setPackage("com.instagram.android");
                    startActivity(share);
                    finish();
                    break;
                } catch (ActivityNotFoundException e) {
                    Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                    marketLaunch.setData(Uri.parse("market://details?id=com.instagram.android"));
                    startActivity(marketLaunch);
                }
            case R.id.result_fab_twitter:
                toggleFab();
                try {
                    Bitmap bitmap = getBitmapFromView(mScrollView, mScrollView.getChildAt(0).getHeight(), mScrollView.getChildAt(0).getWidth());
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, "얼굴분석결과는 다음과 같습니다 -AI 얼굴분석 앱 [Face Lab]");//value부분에는 트위터 입력화면에 들어가는 디폴트 문구
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_STREAM, getImageUri(getApplicationContext(), bitmap));//사진추가
                    intent.setType("image/*");
                    intent.setPackage("com.twitter.android");//트위터 앱과 연결함.
                    startActivity(intent);
                    finish();
                    break;
                } catch (ActivityNotFoundException e) {//트위터 앱이 없을때 자동으로 플레이스토어 설치화면으로 이동.
                    Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                    marketLaunch.setData(Uri.parse("market://details?id=com.twitter.android"));
                    startActivity(marketLaunch);
                }
            case R.id.result_fab_download:                 //DOWNLOAD
                Bitmap bitmap = getBitmapFromView(mScrollView, mScrollView.getChildAt(0).getHeight(), mScrollView.getChildAt(0).getWidth());
                saveImage(bitmap);
                Toast.makeText(this, "저장 완료", Toast.LENGTH_LONG).show();
                finish();
                break;
        }

    }

    private void setChart(HorizontalBarChart chart, float[] data) {
        ArrayList<BarEntry> barEntryList = new ArrayList<>();
        barEntryList.add(new BarEntry(0f, data[0] * 100));
        barEntryList.add(new BarEntry(1f, data[1] * 100));
        barEntryList.add(new BarEntry(2f, data[2] * 100));
        barEntryList.add(new BarEntry(3f, data[3] * 100));
        barEntryList.add(new BarEntry(4f, data[4] * 100));
        barEntryList.add(new BarEntry(5f, data[5] * 100));
        barEntryList.add(new BarEntry(6f, data[6] * 100));
        BarDataSet barDataSet = new BarDataSet(barEntryList, "Feelings");
        BarData barData = new BarData(barDataSet);
        barDataSet.setColors(Color.rgb(242, 208, 242), Color.rgb(181, 156, 217), Color.rgb(32, 79, 140), Color.rgb(29, 123, 163), Color.rgb(12, 53, 89), Color.rgb(181, 156, 217), Color.rgb(242, 208, 242));
        chart.setData(barData);
        chart.setTouchEnabled(false);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        final String[] feeling = {"Anger", "Disgust", "Fear", "Happy", "Sad", "Surprise", "Neutral"};
        IndexAxisValueFormatter formatter = new IndexAxisValueFormatter(feeling);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);
        chart.setFitBars(true);
        chart.setBackgroundColor(Color.WHITE);
        barData.setBarWidth(0.8f);
        chart.invalidate();
    }

    private void toggleFab() {
        if (isFabOpen) {
            mFabMenu.setImageResource(R.drawable.out);
            mFabShare.startAnimation(mAniFabClose);
            mFabInsta.startAnimation(mAniFabClose);
            mFabTwitter.startAnimation(mAniFabClose);
            mFabDownload.startAnimation(mAniFabClose);
            mFabMenu.setClickable(false);
            mFabShare.setClickable(false);
            mFabInsta.setClickable(false);
            mFabTwitter.setClickable(false);
            mFabDownload.setClickable(false);
            isFabOpen = false;
        } else {
            mFabMenu.setImageResource(R.drawable.more);
            mFabShare.startAnimation(mAniFabOpen);
            mFabInsta.startAnimation(mAniFabOpen);
            mFabTwitter.startAnimation(mAniFabOpen);
            mFabDownload.startAnimation(mAniFabOpen);
            mFabMenu.setClickable(true);
            mFabShare.setClickable(true);
            mFabInsta.setClickable(true);
            mFabTwitter.setClickable(true);
            mFabDownload.setClickable(true);
            isFabOpen = true;
        }
    }

    private Bitmap getBitmapFromView(View view, int height, int width) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawRGB(35, 7, 77);
        view.draw(canvas);
        return bitmap;
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void saveImage(Bitmap finalBitmap) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root+"/FaceLab");
        myDir.mkdirs();
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = "FaceLab"+ timeStamp +".jpg";
        File file = new File(myDir, fname);
        if (file.exists())
            mTvRights.setText("maybe");
        file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            /*media broadcasting*/
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
