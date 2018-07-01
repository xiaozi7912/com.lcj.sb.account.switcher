package com.lcj.sb.account.switcher

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.lcj.sb.account.switcher.fragment.SBJPFragment
import com.lcj.sb.account.switcher.fragment.SBTWFragment
import com.lcj.sb.account.switcher.utils.AccountInfoManager

class MainActivity : BaseActivity() {
    var mTabJPButton: Button? = null
    var mTabTWButton: Button? = null

    val TAB_BUTTON_IDS = intArrayOf(R.id.main_tab_jp_button, R.id.main_tab_tw_button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AccountInfoManager.getInstance().readAccountInfoFile()

        initView()
        initSelectedTab()
    }

    override fun onResume() {
        super.onResume()
        Log.i(LOG_TAG, "onResume")
    }

    override fun onStop() {
        super.onStop()
        AccountInfoManager.getInstance().writeAccountInfoFile()
    }

    override fun initView() {
        super.initView()
        mTabJPButton = findViewById(R.id.main_tab_jp_button)
        mTabTWButton = findViewById(R.id.main_tab_tw_button)

        mTabJPButton?.setOnClickListener(onClickListener)
        mTabTWButton?.setOnClickListener(onClickListener)
    }

    fun initSelectedTab() {
        Log.i(LOG_TAG, "initSelectedTab")
        var selectedTab = AccountInfoManager.getInstance().currentTab
        when (selectedTab) {
            AccountInfoManager.TAB_TYPE_JP -> mTabJPButton?.performClick()
            AccountInfoManager.TAB_TYPE_TW -> mTabTWButton?.performClick()
        }
    }

    fun setTabButtonStatusAsDefault() {
        Log.i(LOG_TAG, "setTabButtonStatusAsDefault")
        for (viewId in TAB_BUTTON_IDS) {
            findViewById<Button>(viewId).isActivated = false
        }
    }

    fun updateTabButtonStatus(viewId: Int, activated: Boolean) {
        findViewById<Button>(viewId).isActivated = activated
    }

    fun onTabJPButtonClick() {
        Log.i(LOG_TAG, "onTabJPButtonClick")
        var ft = fragmentManager.beginTransaction()

        ft.replace(R.id.main_frame_layout, SBJPFragment.newInstance())
        ft.commit()

        AccountInfoManager.getInstance().currentTab = AccountInfoManager.TAB_TYPE_JP
    }

    fun onTabTWButtonClick() {
        Log.i(LOG_TAG, "onTabTWButtonClick")
        var ft = fragmentManager.beginTransaction()

        ft.replace(R.id.main_frame_layout, SBTWFragment.newInstance())
        ft.commit()

        AccountInfoManager.getInstance().currentTab = AccountInfoManager.TAB_TYPE_TW
    }

    var onClickListener = View.OnClickListener({ v ->
        setTabButtonStatusAsDefault()

        when (v.id) {
            R.id.main_tab_jp_button -> onTabJPButtonClick()
            R.id.main_tab_tw_button -> onTabTWButtonClick()
        }

        updateTabButtonStatus(v.id, true)
    })
}
