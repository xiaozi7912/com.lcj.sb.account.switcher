package com.lcj.sb.account.switcher.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.AdRequest
import com.google.gson.Gson
import com.lcj.sb.account.switcher.BaseActivity
import com.lcj.sb.account.switcher.BuildConfig
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.databinding.ActivityMonsterDetailBinding
import com.lcj.sb.account.switcher.http.model.MonsterModel
import com.lcj.sb.account.switcher.utils.Configs
import com.squareup.picasso.Picasso

class MonsterDetailActivity : BaseActivity() {
    private lateinit var mBinding: ActivityMonsterDetailBinding
    private lateinit var mMonsterItem: MonsterModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_monster_detail)
        val strMonsterJson = intent.getStringExtra(Configs.INTENT_KEY_MONSTER_MODEL)
        mMonsterItem = Gson().fromJson(strMonsterJson, MonsterModel::class.java)

        setSupportActionBar(mBinding.toolBar)
        initView()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun initView() {
        val iconUrl = String.format("%s/images/ic_summon_%d.png", BuildConfig.API_BASE_URL, mMonsterItem.number)

        Picasso.get().load(iconUrl).into(mBinding.monsterIconImage)
        Picasso.get().load(Configs.ELEMENT_ICON_LIST[mMonsterItem.element]).into(mBinding.monsterElementImage)
        Picasso.get().load(Configs.MONSTER_TYPE_A_ICON_LIST[mMonsterItem.type_a]).into(mBinding.monsterTypeAImage)
        if (mMonsterItem.type_b == 0) {
            mBinding.monsterTypeBImage.setImageResource(0)
        } else {
            Picasso.get().load(Configs.MONSTER_TYPE_B_ICON_LIST[mMonsterItem.type_b]).into(mBinding.monsterTypeBImage)
        }
        mBinding.monsterRatingView.updateView(mMonsterItem.rarity)
        mBinding.monsterNameText.text = mMonsterItem.name_jp
    }

    override fun initAdMob() {
        val adRequest = AdRequest.Builder().build()
        mBinding.adView.loadAd(adRequest)
    }
}