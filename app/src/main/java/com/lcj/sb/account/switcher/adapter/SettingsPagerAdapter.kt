package com.lcj.sb.account.switcher.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.lcj.sb.account.switcher.fragment.LocalBackupFragment
import com.lcj.sb.account.switcher.fragment.RemoteBackupFragment

class SettingsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LocalBackupFragment.newInstance()
            1 -> RemoteBackupFragment.newInstance()
            else -> Fragment()
        }
    }
}