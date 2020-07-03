package com.icandothisallday2020.ex80retrofitfileupload;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RetrofitService {
    //파일전송에 사용되는 annotation [@Multipart] & [@Part]
    //MultipartBody.Part : ['식별자','파일명',파일을 가지고있는 요청객체를 가진 객체]
    @Multipart
    @POST("/Retrofit/fileUpload.php")
    Call<String> uploadFile(@Part MultipartBody.Part filePart);
}
