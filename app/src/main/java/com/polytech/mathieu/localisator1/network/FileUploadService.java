package com.polytech.mathieu.localisator1.network;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by shafiq on 03/01/2017.
 */
public interface FileUploadService {
    @Multipart
    @POST("upload")
    Call<ResponseBody> upload(@Query("id") String uuid,
                              @Query("nbCluster") int nbCluster,
                              @Part MultipartBody.Part file);
}