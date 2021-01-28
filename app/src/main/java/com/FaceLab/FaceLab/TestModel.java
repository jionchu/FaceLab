package com.FaceLab.FaceLab;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by jionchu on 2021-01-28
 */
public class TestModel {

    private Activity activity;
    private Bitmap bmp;
    private String ageSex = ""; //<성별>+<나이>
    private String emotion = ""; //<감정>
    private String result; //<감정>+<성별>+<나이>
    private float[] output;

    public TestModel(Activity activity, Bitmap bmp) {
        this.bmp = bmp;
        this.activity = activity;
    }

    public void runModel() {
        //change img into grayscale
        Bitmap grayBmp = setGrayScale(bmp);

        //resize the bitmap image
        Bitmap bitmap1 = Bitmap.createScaledBitmap(grayBmp, 64, 64, false);
        Bitmap bitmap2 = Bitmap.createScaledBitmap(grayBmp, 48, 48, false);
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
        assert age_lite != null;
        assert emo_lite != null;
        age_lite.run(input_img1, output1);
        emo_lite.run(input_img2, output2);

        output = output2[0];
        setAgeSex(output1);
        setEmotion(output2[0]);
        setResult();
    }

    public float[] getOutput() {
        return output;
    }

    public String getAgeSex() {
        return ageSex;
    }

    public String getEmotion() {
        return emotion;
    }

    public String getResult() {
        return result;
    }

    private Bitmap setGrayScale(final Bitmap bmpOriginal) {
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

    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(activity, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //Grayscale의 이미지를 받아옴
    private ByteBuffer getInputImage(int[] pixels, int cx, int cy) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(cx * cy * 4);
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
            input_img.putFloat(((pixel) & 0xff) / (float) 255); // B
        }

        return input_img;
    }

    // 모델을 읽어오는 함수로, 텐서플로 라이트 홈페이지에 있습니다.
    // MappedByteBuffer 바이트 버퍼를 Interpreter 객체에 전달하면 모델 해석을 할 수 있습니다.
    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void setAgeSex(float[][] output) {
        String[][] ageSet = {
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

        //get the maximum value from the prediction
        int max_label = 0;
        for (int i=0; i < 19; i++){
            if (output[0][i] > output[0][max_label]) {
                max_label = i;
            }
        }

        int num;
        switch(max_label) {
            case 0:
                ageSex = "영유아";
                num = (int) (Math.random() * ageSet[0].length);
                result = ageSet[0][num];
                break;
            case 1:
                ageSex = "남자 어린이";
                num = (int) (Math.random() * ageSet[1].length);
                result = "남자 " + ageSet[1][num];
                break;
            case 2:
                ageSex = "여자 어린이";
                num = (int) (Math.random() * ageSet[1].length);
                result = "여자 " + ageSet[1][num];
                break;
            case 3:
                ageSex = "남자 초등학생";
                num = (int) (Math.random() * ageSet[2].length);
                result = "남자 " + ageSet[2][num];
                break;
            case 4:
                ageSex = "여자 초등학생";
                num = (int) (Math.random() * ageSet[2].length);
                result = "여자 " + ageSet[2][num];
                break;
            case 5:
                ageSex = "남자 중학생";
                num = (int) (Math.random() * ageSet[3].length);
                result = "남자 " + ageSet[3][num];
                break;
            case 6:
                ageSex = "여자 중학생";
                num = (int) (Math.random() * ageSet[3].length);
                result = "여자 " + ageSet[3][num];
                break;
            case 7:
                ageSex = "남자 고등학생";
                num = (int) (Math.random() * ageSet[4].length);
                result = "남자 " + ageSet[4][num];
                break;
            case 8:
                ageSex = "여자 고등학생";
                num = (int) (Math.random() * ageSet[4].length);
                result = "여자 " + ageSet[4][num];
                break;
            case 9:
                ageSex = "남자 20대";
                num = (int) (Math.random() * ageSet[5].length);
                result = "남자 " + ageSet[5][num];
                break;
            case 10:
                ageSex = "여자 20대";
                num = (int) (Math.random() * ageSet[5].length);
                result = "여자 " + ageSet[5][num];
                break;
            case 11:
                ageSex = "남자 30대";
                num = (int) (Math.random() * ageSet[6].length);
                result = "남자 " + ageSet[6][num];
                break;
            case 12:
                ageSex = "여자 30대";
                num = (int) (Math.random() * ageSet[6].length);
                result = "여자 " + ageSet[6][num];
                break;
            case 13:
                ageSex = "남자 40대";
                num = (int) (Math.random() * ageSet[8].length);
                result = "남자 " + ageSet[8][num];
                break;
            case 14:
                ageSex = "여자 40대";
                num = (int) (Math.random() * ageSet[7].length);
                result = "여자 " + ageSet[7][num];
                break;
            case 15:
                ageSex = "남자 50대";
                num = (int) (Math.random() * ageSet[10].length);
                result = "남자 " + ageSet[10][num];
                break;
            case 16:
                ageSex = "여자 50대";
                num = (int) (Math.random() * ageSet[9].length);
                result = "여자 " + ageSet[9][num];
                break;
            case 17:
                ageSex = "남자 60대이상";
                num = (int) (Math.random() * ageSet[12].length);
                result = "남자 " + ageSet[12][num];
                break;
            default:
                ageSex = "여자 60대이상";
                num = (int) (Math.random() * ageSet[11].length);
                result = "여자 " + ageSet[11][num];
                break;
        }
    }

    private void setEmotion(float[] output) {
        String[] emotionSet = {"화난", "경멸하는", "두려워하는", "행복해하는", "슬퍼하는", "놀라는", "무표정인"};

        //get the maximum value from the prediction
        int max_label = 0;
        for (int i=0; i < 7; i++){
            if (output[i] > output[max_label]) {
                max_label = i;
            }
        }
        emotion = emotionSet[max_label];
    }

    private void setResult() {
        result = emotion +" "+ result + "의 얼굴";
    }

}
