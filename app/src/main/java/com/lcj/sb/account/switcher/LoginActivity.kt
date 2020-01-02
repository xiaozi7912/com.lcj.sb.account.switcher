package com.lcj.sb.account.switcher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : BaseActivity() {
    private lateinit var mEmailInputEditText: TextInputEditText
    private lateinit var mPasswordInputEditText: TextInputEditText
    private lateinit var mLoginButton: Button
    private lateinit var mRegisterButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    override fun initView() {
        mEmailInputEditText = findViewById(R.id.account_edit_text)
        mPasswordInputEditText = findViewById(R.id.password_edit_text)
        mLoginButton = findViewById(R.id.login_button)
        mRegisterButton = findViewById(R.id.register_button)

        mLoginButton.setOnClickListener {
            val inputEmail = mEmailInputEditText.text.toString()
            val inputPassword = mPasswordInputEditText.text.toString()
            Log.v(LOG_TAG, "inputEmail : $inputEmail")
            Log.v(LOG_TAG, "inputPassword : $inputPassword")

            if (inputEmail.isNotEmpty() && inputPassword.isNotEmpty()) {
                mAuth.signInWithEmailAndPassword(inputEmail, inputPassword)
                        .addOnCompleteListener(mActivity) { task ->
                            if (task.isSuccessful) {
                                val user = mAuth.currentUser
                                Log.d(LOG_TAG, "createUserWithEmail:success")
                                Log.d(LOG_TAG, "isEmailVerified : ${user?.isEmailVerified}")
                                val intent = Intent(mActivity, MainActivity::class.java)
                                startActivity(intent)
                            } else {
                                Log.w(LOG_TAG, "createUserWithEmail:failure", task.exception)
                            }
                        }
            } else {

            }
        }

        mRegisterButton.setOnClickListener {
            val intent = Intent(mActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}