package com.lcj.sb.account.switcher.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import com.lcj.sb.account.switcher.BaseApplication
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.adapter.SettingsPagerAdapter
import com.lcj.sb.account.switcher.databinding.FragmentSettingsBinding
import com.lcj.sb.account.switcher.utils.Configs

class SettingsFragment : BaseFragment() {
    private lateinit var mBinding: FragmentSettingsBinding
    private lateinit var mTabTitleArray: Array<String>

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
        mTabTitleArray = arrayOf(resources.getString(R.string.settings_tab_local_backup_title), resources.getString(R.string.settings_tab_remote_backup_title))

        mBinding.settingsPager.adapter = SettingsPagerAdapter(this)
        TabLayoutMediator(mBinding.settingsTabLayout, mBinding.settingsPager) { tab, position ->
            tab.text = mTabTitleArray[position]
        }.attach()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        BaseApplication.setCurrentScreen(mActivity, Configs.SCREEN_SETTINGS, LOG_TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}