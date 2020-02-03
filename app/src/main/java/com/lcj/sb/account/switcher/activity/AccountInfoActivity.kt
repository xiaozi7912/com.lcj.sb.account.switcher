package com.lcj.sb.account.switcher.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.AdRequest
import com.lcj.sb.account.switcher.BaseActivity
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.databinding.ActivityAccountInfoBinding
import com.lcj.sb.account.switcher.fragment.PartyFragment
import com.lcj.sb.account.switcher.utils.Configs

class AccountInfoActivity : BaseActivity() {
    private lateinit var mBinding: ActivityAccountInfoBinding
    private var mAccount: Account? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(mActivity, R.layout.activity_account_info)

        setSupportActionBar(mBinding.toolBar)
        initView()
        reloadAd()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun initView() {
        mAccount = intent?.extras?.get(Configs.INTENT_KEY_ACCOUNT) as Account?
        supportActionBar?.title = mAccount?.alias
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        showPartyFragment()
    }

    override fun reloadAd() {
        val adRequest = AdRequest.Builder().build()
        mBinding.adView.loadAd(adRequest)
    }

    private fun showPartyFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_layout, PartyFragment.newInstance(mAccount!!))
            commit()
        }
    }
}