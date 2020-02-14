package com.lcj.sb.account.switcher

import android.app.Activity
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.database.entity.DungeonParty

abstract class BaseAdapter<T : RecyclerView.ViewHolder>(val activity: Activity) : RecyclerView.Adapter<T>() {
    protected val mInflater: LayoutInflater = LayoutInflater.from(activity)

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

    interface LocalSyncListListener {
        fun onItemClick(account: Account)
        fun onIconClick(account: Account)
        fun onDeleteClick(account: Account)
        fun onUploadClick(account: Account)
    }
}