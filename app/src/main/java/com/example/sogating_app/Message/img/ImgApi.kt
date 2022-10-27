package com.example.sogating_app.Message.img

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

public interface ImgApi {
    @POST("/img_server.php")
    @FormUrlEncoded
    fun getScanResult(@Field("data") data: String) : Call<ResponseData>

    companion object { // static 처럼 공유객체로 사용가능함. 모든 인스턴스가 공유하는 객체로서 동작함.
        private const val BASE_URL = "http://192.168.219.103" // 주소

        fun create(): ImgApi {


            val gson : Gson =   GsonBuilder().setLenient().create();

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
//                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ImgApi::class.java)
        }
    }
}

