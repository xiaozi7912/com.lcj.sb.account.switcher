package com.lcj.sb.account.switcher.adapter

import android.app.Activity
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lcj.sb.account.switcher.BaseAdapter
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.database.entity.GoogleDriveItem
import com.lcj.sb.account.switcher.databinding.ItemGoogleDriveBinding
import java.text.SimpleDateFormat
import java.util.*

class GoogleDriveAdapter(activity: Activity) : BaseAdapter<GoogleDriveItem, GoogleDriveAdapter.ViewHolder>(activity) {
    private var mListener: RemoteSyncListListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemGoogleDriveBinding.inflate(mInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mDataList[position]
        holder.updateView(item)
        holder.binding.root.setOnClickListener { mListener?.onItemClick(item) }
        holder.binding.itemDeleteBtn.setOnClickListener { mListener?.onDeleteClick(item) }
        holder.binding.itemDownloadBtn.setOnClickListener { mListener?.onDownloadClick(item) }
    }

    fun setListener(listener: RemoteSyncListListener) {
        mListener = listener
    }

    class ViewHolder(val binding: ItemGoogleDriveBinding) : RecyclerView.ViewHolder(binding.root) {
        fun updateView(item: GoogleDriveItem) {
            binding.itemIcon.setImageResource(when (item.lang) {
                Account.Language.JP -> R.drawable.ic_launcher_jp_p
                Account.Language.TW -> R.drawable.ic_launcher_tw_p
            })
            binding.itemId.text = item.id
            binding.itemName.text = item.name
            binding.itemModifiedTime.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN).format(item.modifiedTime)
        }
    }
}