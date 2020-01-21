package com.lcj.sb.account.switcher.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.lcj.sb.account.switcher.BuildConfig
import com.lcj.sb.account.switcher.fragment.LocalSyncFragment
import com.lcj.sb.account.switcher.fragment.RemoteSyncFragment

class SettingsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return if (BuildConfig.DEBUG) 2 else 1
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LocalSyncFragment.newInstance()
            1 -> RemoteSyncFragment.newInstance()
            else -> Fragment()
        }
    }
}