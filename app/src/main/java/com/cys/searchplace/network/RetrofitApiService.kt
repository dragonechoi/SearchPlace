package com.cys.searchplace.network

import com.cys.searchplace.model.KakaoSearchPlaceResponse
import com.cys.searchplace.model.NidUserInfoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface RetrofitApiService {
    //네아로 사용자정보 API
    @GET("/v1/nid/me")
    fun getNidUserInfo(@Header("Authorization") authorization:String) : Call<NidUserInfoResponse>

    //카카오 키워드 장소 검색 API
    @Headers("Authorization: KakaoAK 5376e645cd4366e765ef8f13b3d88540")
    @GET("/v2/local/search/keyword.json")
    fun searchPlace(@Query("query") query:String,@Query("y") latitude:String,@Query("x") longitude:String):Call<KakaoSearchPlaceResponse>

}