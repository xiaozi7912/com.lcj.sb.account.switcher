package com.lcj.sb.account.switcher.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.databinding.ItemLocalSyncListBinding
import java.text.SimpleDateFormat

class LocalSyncListAdapter(activity: Activity) : RecyclerView.Adapter<LocalSyncListAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(activity)
    private var dataList: List<Account> = emptyList()
    private var mListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemLocalSyncListBinding.inflate(mInflater, parent, false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.binding.accountIconIv.setImageResource(when (item.lang) {
            Account.Language.JP.ordinal -> R.drawable.ic_launcher_jp_p
            Account.Language.TW.ordinal -> R.drawable.ic_launcher_tw_p
            else -> R.drawable.ic_launcher_jp_p
        })
        holder.binding.accountAliasTv.text = item.alias
        holder.binding.accountPathTv.text = item.folder.substring(item.folder.lastIndexOf("/") + 1)
        holder.binding.accountUpdateTimeTv.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.updateTime)

        holder.binding.root.setOnClickListener { mListener?.onItemClick(holder, item) }
        holder.binding.accountIconIv.setOnClickListener { mListener?.onIconClick(item) }
        holder.binding.accountDeleteBtn.setOnClickListener { mListener?.onDeleteClick(item) }
        holder.binding.accountMoreBtn.setOnClickListener { mListener?.onMoreClick(holder, item) }
    }

    fun update(dataList: List<Account>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }

    class ViewHolder(val binding: ItemLocalSyncListBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.accountPathTv.visibility = View.INVISIBLE
            binding.accountMoreBtn.visibility = View.GONE
        }
    }

    interface OnClickListener {
        fun onItemClick(holder: ViewHolder, account: Account)

        fun onIconClick(account: Account)

        fun onDeleteClick(account: Account)

        fun onMoreClick(holder: ViewHolder, account: Account)
    }
}