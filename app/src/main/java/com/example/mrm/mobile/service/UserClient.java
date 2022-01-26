package com.example.mrm.mobile.service;

import com.example.mrm.mobile.model.Login;
import com.example.mrm.mobile.model.User;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UserClient {

    @POST("/api/authentication/login")
    @Headers({ "Content-Type: application/json;charset=UTF-8"})
    Call<User> login(@Body Login login);
}
