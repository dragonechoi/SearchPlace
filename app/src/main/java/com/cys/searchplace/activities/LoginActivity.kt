package com.cys.searchplace.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cys.searchplace.databinding.ActivityLoginBinding
import com.cys.searchplace.model.NidUserInfoResponse
import com.cys.searchplace.model.UserAccount
import com.cys.searchplace.network.RetrofitApiService
import com.cys.searchplace.network.RetrofitHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.mrhi2023.tpkaosearchapp.G
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    val binding: ActivityLoginBinding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //둘러보기 버튼 클릭으로 로그인없이 Main 화면으로 이동
        binding.tvGo.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        //회원가입 버튼 클릭
        binding.tvSignup.setOnClickListener {
            //회원가입화면으로 전환
            startActivity(Intent(this, SignupActivity::class.java))
        }

        //이메일 로그인 버튼 클릭
        binding.layoutEmail.setOnClickListener {
            //이메일 로그인 화면으로 전환
            startActivity(Intent(this, EmailSigninActivity::class.java))
        }

        //간편로그인 버튼들 클릭
        binding.ivLoginKakao.setOnClickListener { clickedLoginKakao() }
        binding.ivLoginGoogle.setOnClickListener { clickedLoginGoogle() }
        binding.ivLoginNaver.setOnClickListener { clickedLoginNaver() }

        //카카오 키해시 값 얻어오기
        val keyHash: String = Utility.getKeyHash(this)
        Log.i("keyHash", keyHash)

    }//onCreate method..

    private fun clickedLoginKakao() {

        // 카카오 로그인 공통 callback 함수
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (token != null) {
                Toast.makeText(this, "카카오 로그인 성공", Toast.LENGTH_SHORT).show()

                //사용자 정보 요청 [ 1.회원번호, 2.이메일주소 ]
                UserApiClient.instance.me { user, error ->
                    if (user != null) {
                        var id: String = user.id.toString()
                        var email: String = user.kakaoAccount?.email ?: ""  //혹시 null이면 이메일의 기본값은 ""

                        Toast.makeText(this, "$email", Toast.LENGTH_SHORT).show()
                        G.userAccount = UserAccount(id, email)

                        // main 화면으로 이동
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }

            } else {
                Toast.makeText(this, "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }

        //카카오톡이 설치되어 있으면 카톡으로 로그인, 아니면 카카오계정 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }

    }

    private fun clickedLoginGoogle() {
        val signInOptions: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        val intent: Intent = GoogleSignIn.getClient(this, signInOptions).signInIntent
        resultLauncher.launch(intent)
    }

    val resultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult?) {
                val intent: Intent? = result?.data

                try {
                    val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(intent)
                    val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)

                    if (account != null) {
                        val id: String = account.id.toString()
                        val email: String = account.email ?: ""

                        Toast.makeText(this@LoginActivity, email, Toast.LENGTH_SHORT).show()
                        G.userAccount = UserAccount(id, email)

                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "로그인 취소", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: ApiException) {
                    // Handle Google Sign-In API exceptions
                    Toast.makeText(this@LoginActivity, "로그인 취소", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    // Handle other exceptions
                    Toast.makeText(this@LoginActivity, "시스템 에러: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })

    fun clickedLoginNaver() {
        NaverIdLoginSDK.initialize(this,"Yz9G6fm6iocquCO6LiZj","gNDPKjyejW","발자취")

        // 네이버 로그인
        NaverIdLoginSDK.authenticate(this, object : OAuthLoginCallback{
            override fun onError(errorCode: Int, message: String) {
                Toast.makeText(this@LoginActivity, "error : $message", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(httpStatus: Int, message: String) {
                Toast.makeText(this@LoginActivity, "로그인 실패 : $message", Toast.LENGTH_SHORT).show()
            }

            override fun onSuccess() {
                Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()

                // 사용자 정보를 가져오는 REST API를 작업할때 접속토큰이 필요함.
                val accessToken:String? = NaverIdLoginSDK.getAccessToken()
                // 토큰값 확인
                Log.i("token", accessToken!!)

                // 레트로핏으로 사용자정보 API 가져오기
                val retrofit= RetrofitHelper.getRetrofitInstance("https://openapi.naver.com")
                retrofit.create(RetrofitApiService::class.java).getNidUserInfo("Bearer $accessToken").enqueue(object :
                    Callback<NidUserInfoResponse> {
                    override fun onResponse(
                        call: Call<NidUserInfoResponse>,
                        response: Response<NidUserInfoResponse>
                    ) {
                        val userInfoResponse: NidUserInfoResponse?= response.body()
                        val id:String= userInfoResponse?.response?.id ?: ""
                        val email:String= userInfoResponse?.response?.email ?: ""

                        Toast.makeText(this@LoginActivity, "$email", Toast.LENGTH_SHORT).show()
                        G.userAccount= UserAccount(id, email)

                        // main 화면으로 이동
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }

                    override fun onFailure(call: Call<NidUserInfoResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "회원정보 불러오기 실패 : ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })

            }
        })

    }

}