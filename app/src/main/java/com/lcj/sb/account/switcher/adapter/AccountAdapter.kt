package com.lcj.sb.account.switcher.adapter

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.databinding.ItemAccountListBinding
import java.text.SimpleDateFormat

class AccountAdapter(activity: Activity) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(activity)
    private var dataList: List<Account> = emptyList()
    private var mOnClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemAccountListBinding.inflate(mInflater, parent, false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.binding.accountAliasTv.text = item.alias
        holder.binding.accountPathTv.text = item.folder.substring(item.folder.lastIndexOf("/") + 1)
        holder.binding.accountUpdateTimeTv.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.updateTime)

        val iconResId = if (item.selected) {
            holder.binding.accountAliasTv.setTextColor(Color.parseColor("#ffffffff"))
            when (item.lang) {
                Account.Language.JP.ordinal -> R.drawable.ic_launcher_jp_p
                Account.Language.TW.ordinal -> R.drawable.ic_launcher_tw_p
                else -> R.drawable.ic_launcher_jp_p
            }
        } else {
            holder.binding.accountAliasTv.setTextColor(Color.parseColor("#80ffffff"))
            when (item.lang) {
                Account.Language.JP.ordinal -> R.drawable.ic_launcher_jp_n
                Account.Language.TW.ordinal -> R.drawable.ic_launcher_tw_n
                else -> R.drawable.ic_launcher_jp_p
            }
        }
        holder.binding.accountIconIv.setImageResource(iconResId)

        holder.binding.root.setOnClickListener {
            mOnClickListener?.onItemClick(holder, item)
        }
        holder.binding.accountMoreBtn.setOnClickListener {
            mOnClickListener?.onMoreClick(holder, item)
        }
    }

    fun update(dataList: List<Account>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    fun setOnClickListener(listener: OnClickListener) {
        mOnClickListener = listener
    }

    class ViewHolder(val binding: ItemAccountListBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.accountPathTv.visibility = View.INVISIBLE
        }
    }

    interface OnClickListener {
        fun onItemClick(holder: ViewHolder, account: Account)

        fun onMoreClick(holder: ViewHolder, account: Account)
    }
}