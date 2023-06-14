package com.cys.searchplace.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.kakao.sdk.common.util.Utility

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var keyHash = Utility.getKeyHash(this)
        Log.i("keyHash",keyHash)
        //setContentView(R.layout.activity_splash) //테마를 이용하여 화면을 구현할 것임

        // 단순하게 1.5초 후에 로그인 화면(LoginActivity)로 전환
//        Handler(Looper.getMainLooper()).postDelayed( object:Runnable{
//            override fun run() {
//
//            }
//        } ,1500)

        // lambda 표기로 축약
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity( Intent(this, LoginActivity::class.java) )
            finish()
        },1500)

    }
}
