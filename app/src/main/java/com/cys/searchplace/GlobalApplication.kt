package com.cys.searchplace

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this,"63fe06a5d8d4139190944f590cee0cae")
    }
}