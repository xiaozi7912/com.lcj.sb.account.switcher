package com.lcj.sb.account.switcher.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.fragment.AccountFragment

class AccountsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AccountFragment.newInstance(Account.Language.JP)
            1 -> AccountFragment.newInstance(Account.Language.TW)
            else -> Fragment()
        }
    }
}