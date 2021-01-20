package com.FaceLab.FaceLab;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class TestActivity extends AppCompatActivity {
    private static final int TEST_START = 3;
    private AdView mAdView;
    Bitmap bmp;
    byte[] byteArray;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        MobileAds.initialize(this, getString(R.string.admob_app_id));
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // 광고가 제대로 로드 되는지 테스트 하기 위한 코드입니다.

        mAdView.setAdListener(new AdListener() {

            @Override

            public void onAdLoaded() {

                // Code to be executed when an ad finishes loading.

                // 광고가 문제 없이 로드시 출력됩니다.

                Log.d("@@@", "onAdLoaded");

            }



            @Override

            public void onAdFailedToLoad(int errorCode) {

                // Code to be executed when an ad request fails.

                // 광고 로드에 문제가 있을시 출력됩니다.

                Log.d("@@@", "onAdFailedToLoad " + errorCode);

            }



            @Override

            public void onAdOpened() {

                // Code to be executed when an ad opens an overlay that

                // covers the screen.

            }



            @Override

            public void onAdClicked() {

                // Code to be executed when the user clicks on an ad.

            }



            @Override

            public void onAdLeftApplication() {

                // Code to be executed when the user has left the app.

            }



            @Override

            public void onAdClosed() {

                // Code to be executed when the user is about to return

                // to the app after tapping on an ad.

            }

        });





        byteArray = getIntent().getByteArrayExtra("image");
        bmp = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
        bmp = getRoundedCornerBitmap(bmp);
        ((ImageView) findViewById(R.id.testImage)).setImageBitmap(bmp);

        findViewById(R.id.btnTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestActivity.this,realMain.class);
                intent.putExtra("image",byteArray);
                startActivity(intent);
            }
        });
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 40;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    protected void test(Bitmap bmp){
        //resize the bitmap image
        int cx=64, cy=64;
        Bitmap bitmap = Bitmap.createScaledBitmap(bmp, cx, cy, false);
        //read pixel values from the loaded image
        int[] pixels = new int[cx*cy];
        bitmap.getPixels(pixels, 0, cx, 0, 0, cx, cy);
        //이미지를 효율적으로 사용하기 위해서 byte배열로 변환
        ByteBuffer input_img = getInputImage(pixels, cx, cy);

        Interpreter tf_lite = getTfliteInterpreter("face_age2.tflite");

        float[][] output = new float[1][19];
        tf_lite.run(input_img, output);

        //get the maximum value from the prediction
        int max_label = 0;
        String what;

        for (int i=0; i < 19; i++){
            if (output[0][i] > output[0][max_label]) {
                max_label = i;
            }

        }

        switch(max_label){
            case 0:
                what = "영유아"; break;
            case 1:
                what = "남자어린이"; break;
            case 2:
                what = "여자어린이"; break;
            case 3:
                what = "남자초딩"; break;
            case 4:
                what = "여자초딩"; break;
            case 5:
                what = "남자중딩"; break;
            case 6:
                what = "여자중딩"; break;
            case 7:
                what = "남자고딩"; break;
            case 8:
                what = "여자고딩"; break;
            case 9:
                what = "20대남자"; break;
            case 10:
                what = "20대여자"; break;
            case 11:
                what = "30대남자"; break;
            case 12:
                what = "30대여자"; break;
            case 13:
                what = "40대남자"; break;
            case 14:
                what = "40대여자"; break;
            case 15:
                what = "50대남자"; break;
            case 16:
                what = "50대여자"; break;
            case 17:
                what = "60대이상 남자"; break;
            default:
                what = "60대이상 여자"; break;
        }

        CheckTypesTask task = new CheckTypesTask();
        task.execute();

        Intent intent = new Intent(TestActivity.this,realMain.class);
        intent.putExtra("result",what);
        intent.putExtra("image",byteArray);
        startActivity(intent);
    }
    //이미지를 byte배열로 변환하는 함수
    private ByteBuffer getInputImage(int[] pixels, int cx, int cy) {
        ByteBuffer input_img = ByteBuffer.allocateDirect(cx * cy * 3 * 4); // multiply 4 since float is 4 bytes
        input_img.order(ByteOrder.nativeOrder()); // 현 시스템이 가지는 바이트 순서로 맞춰야함.

        for (int i = 0; i < cx * cy; i++) {
            int pixel = pixels[i];        // ARGB : ff4e2a2a

            input_img.putFloat(((pixel >> 16) & 0xff) / (float) 255); // R
            input_img.putFloat(((pixel >> 8) & 0xff) / (float) 255); // G
            input_img.putFloat(((pixel >> 0) & 0xff) / (float) 255); // B
        }

        return input_img;
    }

    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(TestActivity.this, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 모델을 읽어오는 함수로, 텐서플로 라이트 홈페이지에 있다.
    // MappedByteBuffer 바이트 버퍼를 Interpreter 객체에 전달하면 모델 해석을 할 수 있다.
    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    class CheckTypesTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog asyncDialog = new ProgressDialog(TestActivity.this);
        @Override
        protected void onPreExecute() {


            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("Detecting..");

            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                for (int i = 0; i < 5; i++) {
                    asyncDialog.setProgress(i * 30);
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            asyncDialog.dismiss();
            super.onPostExecute(result);
            Intent intent = new Intent(getApplicationContext(), realMain.class);
            startActivity(intent);

        }
    }

}