package com.icandothisallday2020.ex80retrofitfileupload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.net.URI;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    ImageView iv;
    String imgPath;//업로드할 이미지의 절대경로

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv=findViewById(R.id.iv);

        //외부저장소 접근에 대한 동적퍼미션
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED)
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 100:
                if(grantResults[0]==PackageManager.PERMISSION_DENIED){
                    Toast.makeText(this, "Denied--Can't Use Camera App", Toast.LENGTH_SHORT).show();
                    finish();
                }

                break;
        }
    }

    public void selectBtn(View view) {
        //사진을 선택할 수 있도록  [외부저장소 퍼미션 필수]
        // 사진앱 실행
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//MIME 타입 설정 image/audio/video  ||   image/jpg...
        startActivityForResult(intent,10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode==10){
            //선택된 이미지의 uri 를 가지고 돌아온 Intent 객체에게 이미지 get
            Uri uri=data.getData();
            if(uri != null){
                Glide.with(this).load(uri).into(iv);//가져온 이미지

                // 선택된 이미지를 서버로 전송 [Retrofit library 사용]
                //※서버에 전송하려면 파일의 uri 가 아닌 파일의 실제경로(절대주소) 필요
                //*uri(식별값):uri 가 가지고있는 DB(SQLite 로 되어있음) 안의  "Content://MediaStore/IMAGES/123456(식별번호)" 를
                // → 절대주소:/storage/emulated/0/Pictures/xxx.jpg 로 바꿔야함

                //uri-->절대경로로 바꾸기
                imgPath=getRealPathFromUri(uri);
                new AlertDialog.Builder(this).setMessage(imgPath).show();//create()없이 show()만해도 자동 create()까지 됨
            }
        }
    }//onActivityResult...

    //Uri -- > 절대경로로 바꿔서 리턴시켜주는 메소드
    String getRealPathFromUri(Uri uri){
        String[] proj= {MediaStore.Images.Media.DATA};//uri 가 가지고 있는 DB 안 data 칸에 절대경로가 있음
        CursorLoader loader= new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor= loader.loadInBackground();
        int column_index= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result= cursor.getString(column_index);
        cursor.close();
        return  result;
    }

    public void uploadBtn(View view) {
        Retrofit retrofit=RetrofitHelper.newRetrofit();
        RetrofitService service=retrofit.create(RetrofitService.class);
        //서버에 보낼 파일의 MultipartBody.Part 객체 생성
        File file=new File(imgPath);//경로에 해당하는 File 객체
        //이 파일객체를 서버에 보내기 위한 포장작업을 해주는 객체
        RequestBody body=RequestBody.create(MediaType.parse("image/*"),file);
        MultipartBody.Part filePart=MultipartBody.Part.createFormData("img",file.getName(),body);//img : 식별자--php 에서 사용
        //└['식별자 (key)',파일명,요청객체] 모두 가지고 있음
        Call<String> call=service.uploadFile(filePart);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    String s=response.body();
                    new AlertDialog.Builder(MainActivity.this).setMessage(s).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                new AlertDialog.Builder(MainActivity.this).setMessage("FAIL:"+t.getMessage()).show();

            }
        });
    }
}//Main
