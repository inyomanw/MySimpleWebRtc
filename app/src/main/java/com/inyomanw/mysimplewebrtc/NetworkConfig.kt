package com.inyomanw.mysimplewebrtc

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class NetworkConfig {
    // set interceptor
    fun getInterceptor() : OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        return  okHttpClient
    }
    private val tutoreURL = "https://s-core.tutore.id/mobile/"
    private val semutUrl = "http://semut.baggrek.com:3005/mobile/"
    fun getRetrofit() : Retrofit {
        return Retrofit.Builder()
            .baseUrl(tutoreURL)
            .client(getInterceptor())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    fun getService() = getRetrofit().create(Users::class.java)
}
interface Users {
    @GET("api/v1/user_device")
    fun getUsers(): Call<Any>

    @GET("api/v1/banners")
    fun getBanners(
        @Query("page") page: Int? = 1,
        @Query("per_page") perPage: Int? = 5
    ): Call<Any>
}