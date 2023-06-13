package com.cys.searchplace.activities

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.cys.searchplace.R
import com.cys.searchplace.databinding.ActivitySignUpBinding
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //툴바를 액션바로 설정
        setSupportActionBar(binding.toolbar)
        //액션바에 업버튼 만들기
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_action_arrow_back)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.btnSignup.setOnClickListener { clickSignUp() }
    }//onCreate method..

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    private fun clickSignUp(){
        //Firebase Firestore DB에 사용자 정보 저장하기

        var email:String= binding.etEmail.text.toString()
        var password:String= binding.etPassword.text.toString()
        var passwordConfirm:String= binding.etPasswordConfirm.text.toString()

        //유효성 검사 - 패스워드와 패스워드 확인이 맞는지 검사
        if(password != passwordConfirm){
            AlertDialog.Builder(this).setMessage("패스워드확인에 문제가 있습니다. 다시 확인해주시기 바랍니다.").create().show()
            binding.etPasswordConfirm.selectAll()
            return
        }

        //Firestore DB instance 얻어오기
        val db= FirebaseFirestore.getInstance()

        //저장할 값(이메일,비밀번호)을 HashMap으로 저장
        val user:MutableMap<String, String> = mutableMapOf()
        user.put("email", email)
        user["password"] = password

        //Collection 명은 "emailUsers" 로 지정 [ RDBMS 의 테이블명 같은 역할 ]
        //혹시 중복된 email을 가진 회원정보가 있을 수 있으니 확인..
        db.collection("emailUsers")
            .whereEqualTo("email", email)
            .get().addOnSuccessListener {

                //같은 값을 가진 Document가 있다면..사이즈가 0개 이상일 것이므로
                if(it.documents.size>0){
                    AlertDialog.Builder(this).setMessage("중복된 이메일이 있습니다. 다시 확인하시기 바랍니다.").show()
                    binding.etEmail.requestFocus()
                    binding.etEmail.selectAll()
                }else{
                    //랜덤하게 만들어지는 document명을 회원 id값으로 사용할 예정
                    db.collection("emailUsers").add(user).addOnSuccessListener {
                        AlertDialog.Builder(this)
                            .setMessage("축하합니다.\n회원가입이 완료되었습니다.")
                            .setPositiveButton("확인", object : DialogInterface.OnClickListener {
                                override fun onClick(p0: DialogInterface?, p1: Int) {
                                    finish()
                                }
                            }).create().show()
                    }////
                }
            }
    }

}