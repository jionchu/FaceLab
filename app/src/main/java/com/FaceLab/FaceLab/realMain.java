package com.FaceLab.FaceLab;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;

import org.tensorflow.lite.Interpreter;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class realMain extends AppCompatActivity implements View.OnClickListener{

    static String ageNsex = ""; //<성별>+<나이>
    static String emotion = ""; //<감정>
    String age [][]  = {

            {"아가", "대한민국의 미래", "국보급", "사랑스러운 아가", "갓난아기"},
            {"어린이", "멋쟁이", "개구쟁이","사고뭉치", "장난꾸러기", "꿈나무", "자라나는 새싹"},
            {"초등학생", "반장", "똑똑이", "범생이", "아역배우", "부반장", "전교일등"},
            {"중학생", "예비중학생", "반장", "부반장", "전교일등", "아이돌연습생", "사춘기", "질풍노도"},
            {"고등학생", "고삼", "수능 일주일 전", "수능만점자", "정시생", "수시생", "전교일등", "아이돌연습생"},
            {"알바생", "대학생", "새내기", "인턴", "사회 초년생", "신입사원", "인생의 황금기", "새내기", "복학생", "휴학생", "청춘"},
            {"직장인", "딩크족", "금수저", "팀장님", "대리님", "주니어", "경력3년차", "이직준비생"},
            {"x세대", "사장님", "우아한 40대", "카리스마", "애기엄마", "커리어우먼", "직장인"}, //40대여
            {"x세대", "아재", "사장님", "카리스마", "애기아빠", "직장인"}, //40대남
            {"베이비붐세대", "선생님", "어머니", "미중년"}, //50대여
            {"베이비붐세대", "선생님", "아버지", "미중년"}, //50대남
            {"베이비붐세대", "어르신", "할머니", "시니어", "노년기"}, //60대여
            {"베이비붐세대", "어르신", "할아버지", "시니어", "노년기"} //60대남
    };

    Context mContext;
    FloatingActionButton fab_1, fab_2, fab_3, fab_4, fab_5;
    Animation fab_open, fab_close;
    boolean isfabopen = false;
    ImageView image;
    ScrollView scroll;
    TextView sextype;
    TextView feeling;
    TextView result;
    TextView txt;

    ArrayList<BarEntry> BARENTRY;
    ArrayList<String> BarEntryLabels;
    BarDataSet barDataSet;
    BarData BARDATA;



        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_main);
        mContext = getApplicationContext();

        Intent intent = getIntent();
        byte[] byteArray = intent.getByteArrayExtra("image");
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
        bmp = getRoundedCornerBitmap(bmp);
        ((ImageView) findViewById(R.id.real_image)).setImageBitmap(bmp);
        //change img into grayscale
        Bitmap graybmp = androidGrayScale(bmp);

        sextype = findViewById(R.id.sextype);
        feeling = findViewById(R.id.emotion);
        result = findViewById(R.id.text2);
        scroll = findViewById(R.id.scroll);
        image = (ImageView) findViewById(R.id.real_image);
        final ScrollView scroll = (ScrollView) findViewById(R.id.scroll);
        txt = findViewById(R.id.txt);

        //여기서 시작!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //resize the bitmap image
        Bitmap bitmap1 = Bitmap.createScaledBitmap(graybmp, 64, 64, false);
        Bitmap bitmap2 = Bitmap.createScaledBitmap(graybmp, 48, 48, false);
        //read pixel values from the loaded image
        int[] pixels1 = new int[64*64];
        int[] pixels2 = new int[48*48];
        bitmap1.getPixels(pixels1, 0, 64, 0, 0, 64, 64);
        bitmap2.getPixels(pixels2, 0, 48, 0, 0, 48, 48);


        //이미지를 효율적으로 사용하기 위해서 byte배열로 변환
        ByteBuffer input_img1 = getInputImageRGB(pixels1, 64, 64);
        ByteBuffer input_img2 = getInputImage(pixels2, 48, 48);

        Interpreter age_lite = getTfliteInterpreter("face_age2.tflite");
        Interpreter emo_lite = getTfliteInterpreter("facial_exp_model.tflite");

        float[][] output1 = new float[1][19];
        float[][] output2 = new float[1][7];
        age_lite.run(input_img1, output1);
        emo_lite.run(input_img2, output2);

        sextype.setText(printAgenSex(output1));
        feeling.setText(printFeeling(output2));
        result.setText(printResult(output2));

        image.setImageBitmap(bmp);

        //차트 부분
        HorizontalBarChart chart = (HorizontalBarChart) findViewById(R.id.chart2);
        BARENTRY = new ArrayList<>();
        BARENTRY.add(new BarEntry(0f,output2[0][0]*100));
        BARENTRY.add(new BarEntry(1f, output2[0][1]*100));
        BARENTRY.add(new BarEntry(2f, output2[0][2]*100));
        BARENTRY.add(new BarEntry(3f, output2[0][3]*100));
        BARENTRY.add(new BarEntry(4f, output2[0][4]*100));
        BARENTRY.add(new BarEntry(5f, output2[0][5]*100));
        BARENTRY.add(new BarEntry(6f,output2[0][6]*100));
        barDataSet = new BarDataSet(BARENTRY, "Feelings");
        BARDATA = new BarData(barDataSet);
        barDataSet.setColors(new int[]{Color.rgb(242,208,242),Color.rgb(181,156,217),Color.rgb(32,79,140),Color.rgb(29,123,163), Color.rgb(12,53,89), Color.rgb(181,156,217), Color.rgb(242,208,242)});
        //        barDataSet.setColors(new int[]{Color.rgb(242,208,242),Color.rgb(181,156,217),Color.rgb(32,79,140),Color.rgb(29,123,163), Color.rgb(12,53,89), Color.rgb(9,91,112), Color.rgb(8,66,60)});
        chart.setData(BARDATA);
        chart.setTouchEnabled(false);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        final String[] feeling = new String[]{"Anger", "Disgust", "Fear", "Happy", "Sad", "Surprise","Neutral"};
        IndexAxisValueFormatter formatter = new IndexAxisValueFormatter(feeling);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);
        chart.setFitBars(true);
        chart.setBackgroundColor(Color.WHITE);
        BARDATA.setBarWidth(0.8f);
        chart.invalidate();

        //floating button 애니메이션 설정
        fab_open = AnimationUtils.loadAnimation(mContext, R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(mContext, R.anim.fab_close);
        fab_1 = findViewById(R.id.fab1);
        fab_2 = findViewById(R.id.fab2);
        fab_3 = findViewById(R.id.fab3);
        fab_4 = findViewById(R.id.fab4);
        fab_5 = findViewById(R.id.fab5);

        fab_1.setOnClickListener(this);
        fab_2.setOnClickListener(this);
        fab_3.setOnClickListener(this);
        fab_4.setOnClickListener(this);
        fab_5.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab1:
                toggleFab();
                break;

            case R.id.fab2:                 //facebook
                toggleFab();
                try{
                    Bitmap bitmap = getBitmapFromView(scroll, scroll.getChildAt(0).getHeight(), scroll.getChildAt(0).getWidth());
//                    SharePhoto photo = new SharePhoto.Builder()
//                            .setBitmap(bitmap)
//                            .build();
//                    SharePhotoContent content = new SharePhotoContent.Builder()
//                            .addPhoto(photo)
//                            .build();
//
//                    ShareDialog shareDialog = new ShareDialog(this);
//                    shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/*");
                    share.putExtra(Intent.EXTRA_STREAM, getImageUri(getApplicationContext(),bitmap));
                    startActivity(Intent.createChooser(share, "공유하기"));

                    this.finish();
                    break;


                }
                catch(ActivityNotFoundException e){

                }


            case R.id.fab3:
                toggleFab();
                try{
                    Bitmap bitmap = getBitmapFromView(scroll, scroll.getChildAt(0).getHeight(), scroll.getChildAt(0).getWidth());
                    Intent share = new Intent (Intent.ACTION_SEND);
                    share.setType("image/*");
                    share.putExtra(Intent.EXTRA_STREAM, getImageUri(getApplicationContext(),bitmap));
                    share.setPackage("com.instagram.android");
                    startActivity(share);
                    this.finish();
                    break;
                }catch(ActivityNotFoundException e){
                    Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                    marketLaunch.setData(Uri.parse("market://details?id=com.instagram.android"));
                    startActivity(marketLaunch);
                }

            case R.id.fab4:
                toggleFab();
                try{
                    Bitmap bitmap = getBitmapFromView(scroll, scroll.getChildAt(0).getHeight(), scroll.getChildAt(0).getWidth());
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, "얼굴분석결과는 다음과 같습니다 -AI 얼굴분석 앱 [Face Lab]");//value부분에는 트위터 입력화면에 들어가는 디폴트 문구
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_STREAM, getImageUri(getApplicationContext(),bitmap));//사진추가
                    intent.setType("image/*");
                    intent.setPackage("com.twitter.android");//트위터 앱과 연결함.
                    startActivity(intent);
                    this.finish();
                    break;
                } catch(ActivityNotFoundException e){//트위터 앱이 없을때 자동으로 플레이스토어 설치화면으로 이동.
                    Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                    marketLaunch.setData(Uri.parse("market://details?id=com.twitter.android"));
                    startActivity(marketLaunch);
                }

            case R.id.fab5:                 //DOWNLOAD
                Bitmap bitmap = getBitmapFromView(scroll, scroll.getChildAt(0).getHeight(), scroll.getChildAt(0).getWidth());
                saveImage(bitmap);
                Toast.makeText(this,"저장 완료",Toast.LENGTH_LONG).show();
                this.finish();
                break;
        }

    }

    private void toggleFab() {

        if (isfabopen) {
            fab_1.setImageResource(R.drawable.out);
            fab_2.startAnimation(fab_close);
            fab_3.startAnimation(fab_close);
            fab_4.startAnimation(fab_close);
            fab_5.startAnimation(fab_close);
            fab_1.setClickable(false);
            fab_2.setClickable(false);
            fab_3.setClickable(false);
            fab_4.setClickable(false);
            fab_5.setClickable(false);
            isfabopen = false;

        } else {
            fab_1.setImageResource(R.drawable.more);
            fab_2.startAnimation(fab_open);
            fab_3.startAnimation(fab_open);
            fab_4.startAnimation(fab_open);
            fab_5.startAnimation(fab_open);
            fab_1.setClickable(true);
            fab_2.setClickable(true);
            fab_3.setClickable(true);
            fab_4.setClickable(true);
            fab_5.setClickable(true);
            isfabopen = true;
        }

    }

    private Bitmap getBitmapFromView(View view, int height, int width) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawRGB(35,7,77);
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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = "FaceLab"+ timeStamp +".jpg";
        File file = new File(myDir, fname);
        if (file.exists())
            txt.setText("maybe");
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            /**media broadcasting**/
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(realMain.this, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    //Grayscale의 이미지를 받아옴
    private ByteBuffer getInputImage(int[] pixels, int cx, int cy) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(cx * cy * 1 *  4);
        byteBuffer.order(ByteOrder.nativeOrder());
       for (int pixel : pixels) {
            float rChannel = (pixel >> 16) & 0xFF;
            float gChannel = (pixel >> 8) & 0xFF;
            float bChannel = (pixel) & 0xFF;
            float pixelValue = (rChannel + gChannel + bChannel) / 3 / 255.f;
            byteBuffer.putFloat(pixelValue);
        }
        return byteBuffer;
    }
    //RGB의 이미지를 받아옴
    private ByteBuffer getInputImageRGB(int[] pixels, int cx, int cy) {
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





    private String printAgenSex(float[][] output) {
        //get the maximum value from the prediction
        int max_label = 0;
        String what;
        int num;


        for (int i=0; i < 19; i++){
            if (output[0][i] > output[0][max_label]) {
                max_label = i;
            }

        }

        switch(max_label) {
            case 0:
                what = "영유아";
                num = (int) (Math.random() * age[0].length);
                ageNsex = age[0][num];
                break;
            case 1:
                what = "남자 어린이";
                num = (int) (Math.random() * age[1].length);
                ageNsex = "남자 " + age[1][num];
                break;
            case 2:
                what = "여자 어린이";
                num = (int) (Math.random() * age[1].length);
                ageNsex = "여자 " + age[1][num];
                break;
            case 3:
                what = "남자 초등학생";
                num = (int) (Math.random() * age[2].length);
                ageNsex = "남자 " + age[2][num];
                break;
            case 4:
                what = "여자 초등학생";
                num = (int) (Math.random() * age[2].length);
                ageNsex = "여자 " + age[2][num];
                break;
            case 5:
                what = "남자 중학생";
                num = (int) (Math.random() * age[3].length);
                ageNsex = "남자 " + age[3][num];
                break;
            case 6:
                what = "여자 중학생";
                num = (int) (Math.random() * age[3].length);
                ageNsex = "여자 " + age[3][num];
                break;
            case 7:
                what = "남자 고등학생";
                num = (int) (Math.random() * age[4].length);
                ageNsex = "남자 " + age[4][num];
                break;
            case 8:
                what = "여자 고등학생";
                num = (int) (Math.random() * age[4].length);
                ageNsex = "여자 " + age[4][num];
                break;
            case 9:
                what = "남자 20대";
                num = (int) (Math.random() * age[5].length);
                ageNsex = "남자 " + age[5][num];
                break;
            case 10:
                what = "여자 20대";
                num = (int) (Math.random() * age[5].length);
                ageNsex = "여자 " + age[5][num];
                break;
            case 11:
                what = "남자 30대";
                num = (int) (Math.random() * age[6].length);
                ageNsex = "남자 " + age[6][num];
                break;
            case 12:
                what = "여자 30대";
                num = (int) (Math.random() * age[6].length);
                ageNsex = "여자 " + age[6][num];
                break;
            case 13:
                what = "남자 40대";
                num = (int) (Math.random() * age[8].length);
                ageNsex = "남자 " + age[8][num];
                break;
            case 14:
                what = "여자 40대";
                num = (int) (Math.random() * age[7].length);
                ageNsex = "여자 " + age[7][num];
                break;
            case 15:
                what = "남자 50대";
                num = (int) (Math.random() * age[10].length);
                ageNsex = "남자 " + age[10][num];
                break;
            case 16:
                what = "여자 50대";
                num = (int) (Math.random() * age[9].length);
                ageNsex = "여자 " + age[9][num];
                break;
            case 17:
                what = "남자 60대이상";
                num = (int) (Math.random() * age[12].length);
                ageNsex = "남자 " + age[12][num];
                break;
            default:
                what = "여자 60대이상";
                num = (int) (Math.random() * age[11].length);
                ageNsex = "여자 " + age[11][num];
                break;
        }
        return what;

    }


    private String printFeeling(float[][] output) {
        //get the maximum value from the prediction
        int max_label = 0;
        String what;

        for (int i=0; i < 7; i++){
            if (output[0][i] > output[0][max_label]) {
                max_label = i;
            }

        }

        switch(max_label){
            case 0:
                emotion = "화난";
                what = "화남";
                break;
            case 1:
                emotion = "경멸하는";
                what = "경멸함";
                break;
            case 2:
                emotion = "두려워하는";
                what = "두려움";
                break;
            case 3:
                emotion = "행복해하는";
                what = "행복함";
                break;
            case 4:
                emotion = "슬퍼하는";
                what = "슬퍼함";
                break;
            case 5:
                emotion = "놀라는";
                what = "놀람";
                break;
            case 6:
                emotion = "무표정인";
                what = "무표정";
                break;
            default:
                emotion = "행복해하는";
                what = "행복함";
                break; //혹시나해서 남겨둠
        }
        what = emotion;
        return what;
    }

    private String printResult(float[][] output) {
        //get the maximum value from the prediction
        int max_label = 0;
        String what;

        what = emotion +" "+ ageNsex + "의 얼굴";

        return what;
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

    private Bitmap androidGrayScale(final Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorMatrixFilter);
        canvas.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

}
