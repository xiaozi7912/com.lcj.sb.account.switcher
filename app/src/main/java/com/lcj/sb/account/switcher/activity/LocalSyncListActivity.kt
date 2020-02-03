package com.lcj.sb.account.switcher.activity

import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.AdRequest
import com.lcj.sb.account.switcher.BaseActivity
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.databinding.ActivityLocalSyncListBinding
import com.lcj.sb.account.switcher.utils.Configs

class LocalSyncListActivity : BaseActivity() {
    private lateinit var mBinding: ActivityLocalSyncListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(mActivity, R.layout.activity_local_sync_list)

        setSupportActionBar(mBinding.toolBar)
        initView()
        reloadAd()
    }

    override fun initView() {
        Log.v(LOG_TAG, "Language : ${intent.getIntExtra(Configs.INTENT_KEY_LANGUAGE, Account.Language.JP.ordinal)}")
    }

    override fun reloadAd() {
        val adRequest = AdRequest.Builder().build()
        mBinding.adView.loadAd(adRequest)
    }
}