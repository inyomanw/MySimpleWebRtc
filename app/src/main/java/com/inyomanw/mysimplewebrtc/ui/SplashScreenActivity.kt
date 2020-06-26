package com.inyomanw.mysimplewebrtc.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.inyomanw.mysimplewebrtc.NetworkConfig
import com.inyomanw.mysimplewebrtc.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        NetworkConfig().getService()
            .getBanners().enqueue(object : Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {

                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {

                }

            })
        Handler().postDelayed(
            {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            },
            1000
        )
//        tv_text.setOnClickListener {
//            startActivity(Intent(this, MainActivity::class.java))
//        }
    }
}
