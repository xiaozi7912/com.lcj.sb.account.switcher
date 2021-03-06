package com.lcj.sb.account.switcher

import android.app.Activity
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.database.entity.DungeonParty
import com.lcj.sb.account.switcher.database.entity.GoogleDriveItem

abstract class BaseAdapter<DT, VT : RecyclerView.ViewHolder>(val activity: Activity) : RecyclerView.Adapter<VT>() {
    protected val LOG_TAG = javaClass.simpleName;
    protected val mInflater: LayoutInflater = LayoutInflater.from(activity)

    protected var mDataList: List<DT> = emptyList()

    override fun getItemCount(): Int {
        return mDataList.size
    }

    fun update(dataList: List<DT>) {
        mDataList = dataList
        notifyDataSetChanged()
    }

    interface BaseListener {
        fun onItemClick(account: Account)
    }

    interface AccountListListener : BaseListener {
        fun onDeleteClick(account: Account)
        fun onEditAliasClick(account: Account)
        fun onSaveClick(account: Account)
        fun onLoadGameClick(account: Account)
        fun onMoreClick(account: Account)
    }

    interface PartyListListener {
        fun onDeleteClick(item: DungeonParty)
    }

    interface LocalSyncListListener : BaseListener {
        fun onIconClick(account: Account)
        fun onDeleteClick(account: Account)
        fun onUploadClick(account: Account)
    }

    interface RemoteSyncListListener {
        fun onItemClick(entity: GoogleDriveItem)
        fun onDeleteClick(entity: GoogleDriveItem)
        fun onDownloadClick(entity: GoogleDriveItem)
    }
}