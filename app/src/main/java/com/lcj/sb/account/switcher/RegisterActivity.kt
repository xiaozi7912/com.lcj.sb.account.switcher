package com.lcj.sb.account.switcher

import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : BaseActivity() {
    private lateinit var mEmailInputEditText: TextInputEditText
    private lateinit var mPasswordInputEditText: TextInputEditText
    private lateinit var mRegisterButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

    }

    override fun initView() {
        mEmailInputEditText = findViewById(R.id.account_edit_text)
        mPasswordInputEditText = findViewById(R.id.password_edit_text)
        mRegisterButton = findViewById(R.id.register_button)

        mRegisterButton.setOnClickListener {
            val inputEmail = mEmailInputEditText.text.toString()
            val inputPassword = mPasswordInputEditText.text.toString()
            Log.v(LOG_TAG, "inputEmail : $inputEmail")
            Log.v(LOG_TAG, "inputPassword : $inputPassword")

            if (inputEmail.isNotEmpty() && inputPassword.isNotEmpty()) {
                mAuth.createUserWithEmailAndPassword(inputEmail, inputPassword)
                        .addOnCompleteListener(mActivity) { task ->
                            if (task.isSuccessful) {
                                Log.d(LOG_TAG, "createUserWithEmail:success")
                                val user = mAuth.currentUser
                                user?.sendEmailVerification()
                            } else {
                                Log.w(LOG_TAG, "createUserWithEmail:failure", task.exception)
                            }
                        }
            } else {

            }
        }
    }

    override fun reloadAd() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}