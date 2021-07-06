package com.lcj.sb.account.switcher.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.lcj.sb.account.switcher.BaseApplication
import com.lcj.sb.account.switcher.BaseFragment
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.databinding.FragmentSettingsBinding
import com.lcj.sb.account.switcher.utils.Configs

class SettingsFragment : BaseFragment() {
    private lateinit var mBinding: FragmentSettingsBinding
    private lateinit var mSignInClient: GoogleSignInClient

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentSettingsBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.signInButton.setOnClickListener {
            startActivityForResult(mSignInClient.signInIntent, Configs.REQUEST_CODE_GOOGLE_SIGN_IN)
        }
        mBinding.signOutButton.setOnClickListener {
            mSignInClient.signOut().addOnCompleteListener {
                updateUI(GoogleSignIn.getLastSignedInAccount(mActivity))
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initSignInClient()
        updateUI(GoogleSignIn.getLastSignedInAccount(mActivity))
    }

    override fun onResume() {
        super.onResume()
        BaseApplication.setCurrentScreen(mActivity, Configs.SCREEN_SETTINGS, LOG_TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Configs.REQUEST_CODE_GOOGLE_SIGN_IN -> handleSignInResult(data)
            else -> {
            }
        }
    }

    private fun initSignInClient() {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE_APPDATA))
                .build().let { mSignInClient = GoogleSignIn.getClient(activity!!, it) }
    }

    private fun handleSignInResult(result: Intent?) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener { account ->
                    updateUI(account)
                }
                .addOnFailureListener { err ->
                    err.printStackTrace()
                }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        account?.let {
            Glide.with(mActivity).load(account.photoUrl).into(mBinding.settingsGoogleAvatar)
            mBinding.signInButton.visibility = View.GONE
            mBinding.signOutButton.visibility = View.VISIBLE

            if (checkGrantedScopes(account, arrayOf(DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_FILE))) {
                mBinding.settingsSignInAccountTv.text = account.displayName
            } else {
                mBinding.settingsSignInAccountTv.text = "權限不足，請重新登入"
            }
        } ?: run {
            Glide.with(mActivity).load(R.drawable.round_account_box_black_24).into(mBinding.settingsGoogleAvatar)
            mBinding.signInButton.visibility = View.VISIBLE
            mBinding.signOutButton.visibility = View.GONE
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