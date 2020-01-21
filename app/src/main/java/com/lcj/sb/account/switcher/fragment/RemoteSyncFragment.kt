package com.lcj.sb.account.switcher.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.lcj.sb.account.switcher.BaseApplication
import com.lcj.sb.account.switcher.databinding.FragmentRemoteBackupBinding
import com.lcj.sb.account.switcher.utils.Configs

class RemoteSyncFragment : BaseFragment() {
    private lateinit var mBinding: FragmentRemoteBackupBinding

    private lateinit var mSignInClient: GoogleSignInClient

    companion object {
        fun newInstance(): RemoteSyncFragment {
            return RemoteSyncFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentRemoteBackupBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.signInButton.setOnClickListener {
            startActivityForResult(mSignInClient.signInIntent, Configs.REQUEST_CODE_GOOGLE_SIGN_IN)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        BaseApplication.analytics.setCurrentScreen(mActivity, Configs.SCREEN_REMOTE_BACKUP, LOG_TAG)
        initGoogleSignIn()
        checkLastSignedInAccount()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Configs.REQUEST_CODE_GOOGLE_SIGN_IN -> handleSignInResult(data)
            else -> {
            }
        }
    }

    private fun initGoogleSignIn() {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE_APPDATA))
                .build().let { mSignInClient = GoogleSignIn.getClient(activity!!, it) }
    }

    private fun checkLastSignedInAccount(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(mActivity)
        updateAccountUI(account)
        return (account != null)
    }

    private fun handleSignInResult(result: Intent?) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener { account ->
                    updateAccountUI(account)
                }
                .addOnFailureListener { err ->
                    err.printStackTrace()
                }
    }

    private fun updateAccountUI(account: GoogleSignInAccount?) {
        if (account != null) {
            if (checkGrantedScopes(account, arrayOf(DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_FILE))) {
                mBinding.settingsSignInAccountTv.text = account.displayName
            } else {
                mBinding.settingsSignInAccountTv.text = "權限不足，請重新登入"
            }
        } else {
            mBinding.settingsSignInAccountTv.text = "尚未綁定Google帳號"
        }
    }

    private fun checkGrantedScopes(account: GoogleSignInAccount, scopes: Array<String>): Boolean {
        val scopeSize = scopes.size
        var count = 0

        account.grantedScopes.forEach {
            if (scopes.contains(it.scopeUri)) count++
        }
        return (count == scopeSize)
    }
}