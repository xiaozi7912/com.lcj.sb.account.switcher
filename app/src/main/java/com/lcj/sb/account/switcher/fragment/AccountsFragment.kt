package com.lcj.sb.account.switcher.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import com.lcj.sb.account.switcher.BaseFragment
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.adapter.AccountsPagerAdapter
import com.lcj.sb.account.switcher.databinding.FragmentAccountsBinding

class AccountsFragment : BaseFragment() {
    private lateinit var mBinding: FragmentAccountsBinding
    private lateinit var mTabTitleArray: Array<String>

    companion object {
        fun newInstance(): AccountsFragment {
            return AccountsFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentAccountsBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mTabTitleArray = arrayOf(resources.getString(R.string.main_drawer_item_sb_j), resources.getString(R.string.main_drawer_item_sb_t))

        mBinding.settingsPager.adapter = AccountsPagerAdapter(this)
        TabLayoutMediator(mBinding.settingsTabLayout, mBinding.settingsPager) { tab, position ->
            tab.text = mTabTitleArray[position]
        }.attach()
    }
}