package com.lcj.sb.account.switcher

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.database.entity.DungeonParty

abstract class BaseAdapter<T : RecyclerView.ViewHolder>(val activity: Activity) : RecyclerView.Adapter<T>() {
    interface AccountListListener {
        fun onItemClick(account: Account)
        fun onDeleteClick(account: Account)
        fun onEditAliasClick(account: Account)
        fun onSaveClick(account: Account)
        fun onLoadGameClick(account: Account)
        fun onMoreClick(account: Account)
    }

    interface PartyListListener {
        fun onDeleteClick(item: DungeonParty)
    }
}