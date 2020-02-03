package com.lcj.sb.account.switcher.activity

import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.AdRequest
import com.lcj.sb.account.switcher.BaseActivity
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.databinding.ActivityLocalSyncListBinding
import com.lcj.sb.account.switcher.utils.Configs

class LocalSyncListActivity : BaseActivity() {
    private lateinit var mBinding: ActivityLocalSyncListBinding

    private lateinit var mDetector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(mActivity, R.layout.activity_local_sync_list)

        setSupportActionBar(mBinding.toolBar)
        mDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                Log.d(LOG_TAG, "onFling: $e1 $e2")
                Log.d(LOG_TAG, "onFling: $velocityX $velocityY")
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })

        initView()
        reloadAd()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun initView() {
        Log.v(LOG_TAG, "Language : ${intent.getIntExtra(Configs.INTENT_KEY_LANGUAGE, Account.Language.JP.ordinal)}")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun reloadAd() {
        val adRequest = AdRequest.Builder().build()
        mBinding.adView.loadAd(adRequest)
    }
}