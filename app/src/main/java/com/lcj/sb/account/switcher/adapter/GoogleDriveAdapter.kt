package com.lcj.sb.account.switcher.adapter

import android.app.Activity
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lcj.sb.account.switcher.BaseAdapter
import com.lcj.sb.account.switcher.database.entity.GoogleDriveItem
import com.lcj.sb.account.switcher.databinding.ItemGoogleDriveBinding

class GoogleDriveAdapter(activity: Activity) : BaseAdapter<GoogleDriveAdapter.ViewHolder>(activity) {
    private var dataList: List<GoogleDriveItem> = emptyList()

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemGoogleDriveBinding.inflate(mInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
    }

    fun update(dataList: List<GoogleDriveItem>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemGoogleDriveBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
        }
    }
}