package com.FaceLab.FaceLab.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.FaceLab.FaceLab.ApplicationClass
import com.FaceLab.FaceLab.R
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.soundcloud.android.crop.Crop
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var tempFile: File? = null
    private var isPermission = true
    private var imageFilePath: String? = null
    private var photoUri: Uri? = null
    private var mDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tedPermission()
        setDialog()
    }

    fun customOnClick(v: View) {
        when (v.id) {
            R.id.main_iv_info -> {
                val intent = Intent(this, InfoActivity::class.java)
                startActivity(intent)
            }
            R.id.main_tv_album -> {
                if (isPermission) {
                    mDialog!!.show()
                    val editor: Editor = ApplicationClass.sSharedPreferences!!.edit()
                    editor.putString("imageType", "album")
                    editor.apply()
                } else  // 권한 허용에 동의하지 않았을 경우 토스트를 띄웁니다.
                    Toast.makeText(this, "사진 및 파일을 저장하기 위하여 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show()
            }
            R.id.main_tv_camera -> {
                val editor: Editor = ApplicationClass.sSharedPreferences!!.edit()
                editor.putString("imageType", "camera")
                editor.apply()
            }
        }
    }

    private fun setDialog() {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_crop, null, false)
        builder.setView(view)
        mDialog = builder.create()

        view.findViewById<View>(R.id.dialog_btn_cancel).setOnClickListener {
            mDialog!!.dismiss()
        }

        view.findViewById<View>(R.id.dialog_btn_confirm).setOnClickListener {
            val imageType: String = ApplicationClass.sSharedPreferences?.getString("imageType", "album").toString()
            if (imageType == "album") {
                //앨범에서 이미지 선택하기
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = MediaStore.Images.Media.CONTENT_TYPE

                //onActivityResult 실행 - 선택한 이미지를 앱 화면에 띄우기
                startActivityForResult(intent, FROM_ALBUM)
                //RESULT_LOAD_IMAGE : REQUEST CODE - 호출하는 액티비티가 여러개일 경우 구분하기 위해 사용
            } else if (imageType == "camera") {
                sendTakePhotoIntent()
            }
            mDialog!!.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var bmp: Bitmap? = null
        //앨범에서 이미지를 선택한 경우
        if (requestCode == FROM_ALBUM && resultCode == RESULT_OK && data != null) {
            //이미지를 uri로 저장
            photoUri = data.data
            bmp = rotate(MediaStore.Images.Media.getBitmap(contentResolver, photoUri), absolutelyPath(photoUri!!))
            photoUri = getImageUri(this, bmp)
            photoUri = cropImage(photoUri)
        }
        //카메라로 사진을 찍은 경우
        else if (requestCode == FROM_CAMERA && resultCode == RESULT_OK) {
            //이미지 파일을 bitmap으로 저장
            bmp = rotate(BitmapFactory.decodeFile(imageFilePath), imageFilePath!!)
            photoUri = getImageUri(this, bmp)
            photoUri = cropImage(photoUri)
        }
        //이미지 crop 완료
        else if (resultCode == RESULT_OK) {
            val intent = Intent(this, TestActivity::class.java)
            intent.putExtra("imageUri", photoUri.toString())
            startActivity(intent)
            tempFile = null
        }
        else if (resultCode != RESULT_OK) {
            if (tempFile != null) {
                if (tempFile!!.exists()) {
                    if (tempFile!!.delete()) {
                        Log.e("TAG", tempFile!!.absoluteFile.toString() + "삭제 성공")
                        tempFile = null
                    }
                }
            }
        }
    }

    // 절대경로 변환
    fun absolutelyPath(path: Uri): String {

        var proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        var c: Cursor = contentResolver.query(path, proj, null, null, null)!!
        var index = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        c.moveToFirst()

        var result = c.getString(index)

        return result
    }

    //권한요청
    private fun tedPermission() {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                isPermission = true
            }

            override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {
                isPermission = false
            }
        }
        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("사진 및 파일을 저장하기 위하여 접근 권한이 필요합니다.")
                .setDeniedMessage("[설정] > [권한]에서 권한을 허용할 수 있습니다.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check()
    }

    //Uri from Bitmap
    private fun getImageUri(inContext: Context, inImage: Bitmap?): Uri {
        val bytes = ByteArrayOutputStream()
        inImage!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    //이미지의 회전 각도 계산
    private fun exifOrientationToDegrees(exifOrientation: Int): Int {
        return when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    //이미지가 사용자가 찍은 방향대로 화면에 보이도록 회전시킴
    private fun rotate(bitmap: Bitmap?, imagePath: String): Bitmap {
        //ExifInterface : 이미지가 가지고 있는 정보집합. 여기에선 이미지의 회전값을 알아내기 위해 사용
        var exif: ExifInterface? = null
        try {
            //이미지 정보 불러오기
            exif = ExifInterface(imagePath)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val exifOrientation: Int
        val exifDegree: Int
        if (exif != null) {
            //이미지의 회전값 계산
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            //이미지의 회전값에 따라 어느 정도의 degree만큼 회전시킬지 계산
            exifDegree = exifOrientationToDegrees(exifOrientation)
        } else {
            exifDegree = 0
        }

        val matrix = Matrix()
        matrix.postRotate(exifDegree.toFloat())
        return Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    //camera button click 시 실행
    private fun sendTakePhotoIntent() {
        //카메라로 사진 촬영
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                //촬영한 이미지를 저장하기 위한 파일을 생성한다.
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
            }

            //이미지 파일이 정상적으로 생성된 경우
            if (photoFile != null) {
                //사진을 uri로 저장
                photoUri = FileProvider.getUriForFile(this, "com.FaceLab.provider", photoFile)
                //intent로 이미지 전달하기
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                //onActivityResult 실행
                startActivityForResult(takePictureIntent, FROM_CAMERA)
            }
        }
    }

    //이미지 파일 생성
    @Throws(IOException::class)
    private fun createImageFile(): File {
        @SuppressLint("SimpleDateFormat") val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "TEST_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",  /* suffix */
                storageDir /* directory */
        )
        imageFilePath = image.absolutePath
        Log.d("TAG", "createImageFile : $imageFilePath")
        return image
    }

    private fun cropImage(photoUri: Uri?): Uri {
        Log.d("Tag", "tempFile:$tempFile")
        if (tempFile == null) { // 갤러리에서 이미지 로드 시, 저장할 파일이 없으므로 createImageFile함.
            try {
                tempFile = createImageFile()
            } catch (e: IOException) {
                Toast.makeText(this, "이미지 처리 오류", Toast.LENGTH_SHORT).show()
                finish()
                e.printStackTrace()
            }
        }
        val savingUri = FileProvider.getUriForFile(this, "com.FaceLab.provider", tempFile!!)
        Crop.of(photoUri, savingUri).asSquare().start(this)
        return savingUri
    }

    companion object {
        private const val FROM_ALBUM = 1
        private const val FROM_CAMERA = 2
    }
}