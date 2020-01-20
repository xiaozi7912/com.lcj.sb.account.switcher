package com.lcj.sb.account.switcher.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.lcj.sb.account.switcher.databinding.FragmentSettingsBinding
import com.lcj.sb.account.switcher.utils.Configs

class SettingsFragment : BaseFragment() {
    private lateinit var mBinding: FragmentSettingsBinding

    private val REQUEST_CODE_SIGN_IN = 1001

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
            if (!checkLastSignedInAccount()) {
                startActivityForResult(mSignInClient.signInIntent, REQUEST_CODE_SIGN_IN)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        BaseApplication.analytics.setCurrentScreen(mActivity, Configs.SCREEN_NAME_SETTINGS, LOG_TAG)
        initGoogleSignIn()
        checkLastSignedInAccount()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SIGN_IN -> handleSignInResult(data)
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
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    Log.e(LOG_TAG, "Unable to sign in.", e)
                }
    }

    private fun updateAccountUI(account: GoogleSignInAccount?) {
        mBinding.settingsSignInAccountTv.text = account?.displayName
        Log.d(LOG_TAG, "Signed in as " + account?.email)
    }
}