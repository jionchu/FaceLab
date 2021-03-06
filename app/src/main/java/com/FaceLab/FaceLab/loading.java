package com.FaceLab.FaceLab;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.io.File;
import java.io.IOException;

public class loading extends AppCompatActivity {
    private File tempFile;
    private Boolean isPermission = true;
    private static final int FROM_CAMERA = 2;
    private static final int FROM_ALBUM = 1;
    private String imageFilePath;
    private Uri photoUri;
    final Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        tedPermission();

        Button info = (Button)findViewById(R.id.information);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), information.class);
                startActivity(intent);
            }
        });


        //album 버튼 눌렀을 때 실행
        findViewById(R.id.album).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 권한 허용에 동의하지 않았을 경우 토스트를 띄웁니다.
                if(isPermission) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    View v = LayoutInflater.from(context).inflate(R.layout.rest_dialog,null,false);
                    builder.setView(v);

                    final AlertDialog dialog = builder.create();

                    dialog.show();

                    v.findViewById(R.id.button324).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();

                            //앨범에서 이미지 선택하기
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);

                            //onActivityResult 실행 - 선택한 이미지를 앱 화면에 띄우기
                            startActivityForResult(intent, FROM_ALBUM);
                            //RsESULT_LOAD_IMAGE : REQUEST CODE - 호출하는 액티비티가 여러개일 경우 구분하기 위해 사용
                        }
                    });
                }
                else Toast.makeText(view.getContext()
                        , "사진 및 파일을 저장하기 위하여 접근 권한이 필요합니다."
                        , Toast.LENGTH_LONG).show();
            }
        });

        //camera 버튼 눌렸을 때 실행 : 스크린 캡쳐/그냥 사각형
        findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                View v = LayoutInflater.from(context).inflate(R.layout.rest_dialog,null,false);
                builder.setView(v);

                final AlertDialog dialog = builder.create();

                dialog.show();

                v.findViewById(R.id.button324).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        sendTakePhotoIntent();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            Bitmap bmp = null;
            if(resultCode != RESULT_OK){
                Toast.makeText(this, "이미지 처리 오류", Toast.LENGTH_SHORT).show();

                if (tempFile != null) {
                    if (tempFile.exists()) {
                        if (tempFile.delete()) {
                            Log.e("TAG", tempFile.getAbsoluteFile() + "삭제 성공");
                            tempFile = null;
                        }
                    }
                }
                return;
            }
            //앨범에서 이미지를 선택한 경우
            if (requestCode == FROM_ALBUM && resultCode == RESULT_OK && null != data) {
                //이미지를 uri로 저장
                photoUri = data.getData();
                photoUri = cropImage(photoUri);
            }
            //카메라로 사진을 찍은 경우
            if (requestCode == FROM_CAMERA && resultCode == RESULT_OK) {

                //이미지 파일을 bitmap으로 저장
                bmp = BitmapFactory.decodeFile(imageFilePath);
                //ExifInterface : 이미지가 가지고 있는 정보집합. 여기에선 이미지의 회전값을 알아내기 위해 사용
                ExifInterface exif = null;

                try {
                    //이미지 정보 불러오기
                    exif = new ExifInterface(imageFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                int exifOrientation;
                int exifDegree;

                if (exif != null) {
                    //이미지의 회전값 계산
                    exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    //이미지의 회전값에 따라 어느 정도의 degree만큼 회전시킬지 계산
                    exifDegree = exifOrientationToDegrees(exifOrientation);
                } else {
                    exifDegree = 0;
                }
                bmp = rotate(bmp, exifDegree);
                photoUri = getImageUri(this,bmp);
                photoUri = cropImage(photoUri);
            }

            //Uri to Bitmap
            try {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            float scale = (float) (1024/(float)bmp.getWidth());
            int image_w = (int) (bmp.getWidth() * scale);
            int image_h = (int) (bmp.getHeight() * scale);
            Bitmap resize = Bitmap.createScaledBitmap(bmp, image_w, image_h, true);
            resize.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            Intent intent = new Intent(loading.this,TestActivity.class);
            intent.putExtra("image",byteArray);
            startActivity(intent);

            tempFile = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*권한요청*/
    private void tedPermission() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                isPermission = true;
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                isPermission = false;
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("사진 및 파일을 저장하기 위하여 접근 권한이 필요합니다.")
                .setDeniedMessage("[설정] > [권한]에서 권한을 허용할 수 있습니다.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();
    }

    //Uri from Bitmap
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    //이미지의 회전 각도 계산
    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    //이미지가 사용자가 찍은 방향대로 화면에 보이도록 회전시킴
    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    //camera button click 시 실행
    private void sendTakePhotoIntent() {
        //카메라로 사진 촬영
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;

            try {
                //촬영한 이미지를 저장하기 위한 파일을 생성한다.
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            //이미지 파일이 정상적으로 생성된 경우
            if (photoFile != null) {
                //사진을 uri로 저장
                photoUri = FileProvider.getUriForFile(this, "com.example.provider", photoFile);
                //intent로 이미지 전달하기
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                //onActivityResult 실행
                startActivityForResult(takePictureIntent, FROM_CAMERA);
            }
        }
    }

    //이미지 파일 생성
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,      /* prefix */
                ".jpg",         /* suffix */
                storageDir          /* directory */
        );
        imageFilePath = image.getAbsolutePath();
        Log.d("TAG", "createImageFile : " + image.getAbsolutePath());

        return image;
    }

    private Uri cropImage(Uri photoUri) {
        Log.d("Tag", "tempFile:"+ tempFile);

        if(tempFile == null) { // 갤러리에서 이미지 로드 시, 저장할 파일이 없으므로 createImageFile함.
            try {
                tempFile = createImageFile();
            } catch (IOException e) {
                Toast.makeText(this, "이미지 처리 오류", Toast.LENGTH_SHORT).show();
                finish();
                e.printStackTrace();
            }
        }

        Uri savingUri = FileProvider.getUriForFile(this, "com.example.provider", tempFile);
        Crop.of(photoUri, savingUri).asSquare().start(this);
        return savingUri;
    }



}
