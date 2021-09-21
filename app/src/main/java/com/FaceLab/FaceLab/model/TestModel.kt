package com.FaceLab.FaceLab.model

import android.app.Activity
import android.graphics.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Created by jionchu on 2021-01-28
 */
class TestModel(private val activity: Activity, private val bmp: Bitmap) {
    var ageSex = "" //<성별>+<나이>
        private set
    var emotion = "" //<감정>
        private set
    var result //<감정>+<성별>+<나이>
            : String? = null
        private set
    lateinit var output: FloatArray
        private set

    fun runModel() {
        //change img into grayscale
        val grayBmp = setGrayScale(bmp)

        //resize the bitmap image
        val bitmap1 = Bitmap.createScaledBitmap(grayBmp, 64, 64, false)
        val bitmap2 = Bitmap.createScaledBitmap(grayBmp, 48, 48, false)
        //read pixel values from the loaded image
        val pixels1 = IntArray(64 * 64)
        val pixels2 = IntArray(48 * 48)
        bitmap1.getPixels(pixels1, 0, 64, 0, 0, 64, 64)
        bitmap2.getPixels(pixels2, 0, 48, 0, 0, 48, 48)

        //이미지를 효율적으로 사용하기 위해서 byte 배열로 변환
        val inputImg1 = getInputImageRGB(pixels1, 64, 64)
        val inputImg2 = getInputImage(pixels2, 48, 48)
        val ageLite = getTfliteInterpreter("face_age2.tflite")
        val emoLite = getTfliteInterpreter("facial_exp_model.tflite")
        val output1 = Array(1) { FloatArray(19) }
        val output2 = Array(1) { FloatArray(7) }
        ageLite!!.run(inputImg1, output1)
        emoLite!!.run(inputImg2, output2)
        output = output2[0]
        setAgeSex(output1)
        setEmotion(output2[0])
        setResult()
    }

    private fun setGrayScale(bmpOriginal: Bitmap): Bitmap {
        val height: Int = bmpOriginal.height
        val width: Int = bmpOriginal.width
        val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmpGrayscale)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val colorMatrixFilter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = colorMatrixFilter
        canvas.drawBitmap(bmpOriginal, 0f, 0f, paint)
        return bmpGrayscale
    }

    private fun getTfliteInterpreter(modelPath: String): Interpreter? {
        try {
            return Interpreter(loadModelFile(activity, modelPath))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    //Grayscale의 이미지를 받아옴
    private fun getInputImage(pixels: IntArray, cx: Int, cy: Int): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(cx * cy * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        for (pixel in pixels) {
            val rChannel = (pixel shr 16 and 0xFF.toFloat().toInt()).toFloat()
            val gChannel = (pixel shr 8 and 0xFF.toFloat().toInt()).toFloat()
            val bChannel = (pixel and 0xFF.toFloat().toInt()).toFloat()
            val pixelValue = (rChannel + gChannel + bChannel) / 3 / 255f
            byteBuffer.putFloat(pixelValue)
        }
        return byteBuffer
    }

    //RGB의 이미지를 받아옴
    private fun getInputImageRGB(pixels: IntArray, cx: Int, cy: Int): ByteBuffer {
        val input_img = ByteBuffer.allocateDirect(cx * cy * 3 * 4) // multiply 4 since float is 4 bytes
        input_img.order(ByteOrder.nativeOrder()) // 현 시스템이 가지는 바이트 순서로 맞춰야함.
        for (i in 0 until cx * cy) {
            val pixel = pixels[i] // ARGB : ff4e2a2a
            input_img.putFloat((pixel shr 16 and 0xff) / 255.toFloat()) // R
            input_img.putFloat((pixel shr 8 and 0xff) / 255.toFloat()) // G
            input_img.putFloat((pixel and 0xff) / 255.toFloat()) // B
        }
        return input_img
    }

    // 모델을 읽어오는 함수로, 텐서플로 라이트 홈페이지에 있습니다.
    // MappedByteBuffer 바이트 버퍼를 Interpreter 객체에 전달하면 모델 해석을 할 수 있습니다.
    @Throws(IOException::class)
    private fun loadModelFile(activity: Activity, modelPath: String): MappedByteBuffer {
        val fileDescriptor = activity.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun setAgeSex(output: Array<FloatArray>) {
        val ageSet = arrayOf(arrayOf("아가", "대한민국의 미래", "국보급", "사랑스러운 아가", "갓난아기"), arrayOf("어린이", "멋쟁이", "개구쟁이", "사고뭉치", "장난꾸러기", "꿈나무", "자라나는 새싹"), arrayOf("초등학생", "반장", "똑똑이", "범생이", "아역배우", "부반장", "전교일등"), arrayOf("중학생", "예비중학생", "반장", "부반장", "전교일등", "아이돌연습생", "사춘기", "질풍노도"), arrayOf("고등학생", "고삼", "수능 일주일 전", "수능만점자", "정시생", "수시생", "전교일등", "아이돌연습생"), arrayOf("알바생", "대학생", "새내기", "인턴", "사회 초년생", "신입사원", "인생의 황금기", "새내기", "복학생", "휴학생", "청춘"), arrayOf("직장인", "딩크족", "금수저", "팀장님", "대리님", "주니어", "경력3년차", "이직준비생"), arrayOf("x세대", "사장님", "우아한 40대", "카리스마", "애기엄마", "커리어우먼", "직장인"), arrayOf("x세대", "아재", "사장님", "카리스마", "애기아빠", "직장인"), arrayOf("베이비붐세대", "선생님", "어머니", "미중년"), arrayOf("베이비붐세대", "선생님", "아버지", "미중년"), arrayOf("베이비붐세대", "어르신", "할머니", "시니어", "노년기"), arrayOf("베이비붐세대", "어르신", "할아버지", "시니어", "노년기"))

        //get the maximum value from the prediction
        var max_label = 0
        for (i in 0..18) {
            if (output[0][i] > output[0][max_label]) {
                max_label = i
            }
        }
        val num: Int
        when (max_label) {
            0 -> {
                ageSex = "영유아"
                num = (Math.random() * ageSet[0].size).toInt()
                result = ageSet[0][num]
            }
            1 -> {
                ageSex = "남자 어린이"
                num = (Math.random() * ageSet[1].size).toInt()
                result = "남자 " + ageSet[1][num]
            }
            2 -> {
                ageSex = "여자 어린이"
                num = (Math.random() * ageSet[1].size).toInt()
                result = "여자 " + ageSet[1][num]
            }
            3 -> {
                ageSex = "남자 초등학생"
                num = (Math.random() * ageSet[2].size).toInt()
                result = "남자 " + ageSet[2][num]
            }
            4 -> {
                ageSex = "여자 초등학생"
                num = (Math.random() * ageSet[2].size).toInt()
                result = "여자 " + ageSet[2][num]
            }
            5 -> {
                ageSex = "남자 중학생"
                num = (Math.random() * ageSet[3].size).toInt()
                result = "남자 " + ageSet[3][num]
            }
            6 -> {
                ageSex = "여자 중학생"
                num = (Math.random() * ageSet[3].size).toInt()
                result = "여자 " + ageSet[3][num]
            }
            7 -> {
                ageSex = "남자 고등학생"
                num = (Math.random() * ageSet[4].size).toInt()
                result = "남자 " + ageSet[4][num]
            }
            8 -> {
                ageSex = "여자 고등학생"
                num = (Math.random() * ageSet[4].size).toInt()
                result = "여자 " + ageSet[4][num]
            }
            9 -> {
                ageSex = "남자 20대"
                num = (Math.random() * ageSet[5].size).toInt()
                result = "남자 " + ageSet[5][num]
            }
            10 -> {
                ageSex = "여자 20대"
                num = (Math.random() * ageSet[5].size).toInt()
                result = "여자 " + ageSet[5][num]
            }
            11 -> {
                ageSex = "남자 30대"
                num = (Math.random() * ageSet[6].size).toInt()
                result = "남자 " + ageSet[6][num]
            }
            12 -> {
                ageSex = "여자 30대"
                num = (Math.random() * ageSet[6].size).toInt()
                result = "여자 " + ageSet[6][num]
            }
            13 -> {
                ageSex = "남자 40대"
                num = (Math.random() * ageSet[8].size).toInt()
                result = "남자 " + ageSet[8][num]
            }
            14 -> {
                ageSex = "여자 40대"
                num = (Math.random() * ageSet[7].size).toInt()
                result = "여자 " + ageSet[7][num]
            }
            15 -> {
                ageSex = "남자 50대"
                num = (Math.random() * ageSet[10].size).toInt()
                result = "남자 " + ageSet[10][num]
            }
            16 -> {
                ageSex = "여자 50대"
                num = (Math.random() * ageSet[9].size).toInt()
                result = "여자 " + ageSet[9][num]
            }
            17 -> {
                ageSex = "남자 60대이상"
                num = (Math.random() * ageSet[12].size).toInt()
                result = "남자 " + ageSet[12][num]
            }
            18 -> {
                ageSex = "여자 60대이상"
                num = (Math.random() * ageSet[11].size).toInt()
                result = "여자 " + ageSet[11][num]
            }
        }
    }

    private fun setEmotion(output: FloatArray) {
        val emotionSet = arrayOf("화난", "경멸하는", "두려워하는", "행복해하는", "슬퍼하는", "놀라는", "무표정인")

        //get the maximum value from the prediction
        var maxLabel = 0
        for (i in 0..6) {
            if (output[i] > output[maxLabel]) {
                maxLabel = i
            }
        }
        emotion = emotionSet[maxLabel]
    }

    private fun setResult() {
        result = emotion + " " + result + "의 얼굴"
    }
}