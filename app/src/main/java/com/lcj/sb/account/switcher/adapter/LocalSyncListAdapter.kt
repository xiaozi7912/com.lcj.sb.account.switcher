package com.lcj.sb.account.switcher.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lcj.sb.account.switcher.BaseAdapter
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.databinding.ItemLocalSyncListBinding
import java.text.SimpleDateFormat
import java.util.*

class LocalSyncListAdapter(activity: Activity) : BaseAdapter<LocalSyncListAdapter.ViewHolder>(activity) {
    private var dataList: List<Account> = emptyList()
    private var mListener: LocalSyncListListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemLocalSyncListBinding.inflate(mInflater, parent, false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.updateView(item)
        holder.binding.root.setOnClickListener { mListener?.onItemClick(item) }
        holder.binding.accountIconIv.setOnClickListener { mListener?.onIconClick(item) }
        holder.binding.accountDeleteBtn.setOnClickListener { mListener?.onDeleteClick(item) }
        holder.binding.itemUploadBtn.setOnClickListener { mListener?.onUploadClick(item) }
    }

    fun update(dataList: List<Account>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    fun setOnClickListener(listener: LocalSyncListListener) {
        mListener = listener
    }

    class ViewHolder(val binding: ItemLocalSyncListBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.accountDeleteBtn.visibility = View.GONE
        }

        fun updateView(item: Account) {
            binding.accountIconIv.setImageResource(when (item.lang) {
                Account.Language.JP.ordinal -> R.drawable.ic_launcher_jp_p
                Account.Language.TW.ordinal -> R.drawable.ic_launcher_tw_p
                else -> R.drawable.ic_launcher_jp_p
            })
            binding.accountAliasTv.text = item.alias
            binding.accountPathTv.text = item.folder.substring(item.folder.lastIndexOf("/") + 1)
            binding.accountUpdateTimeTv.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN).format(item.updateTime)
        }
    }
}