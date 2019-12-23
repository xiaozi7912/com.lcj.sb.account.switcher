package com.lcj.sb.account.switcher.adapter

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.databinding.ItemAccountListBinding
import java.text.SimpleDateFormat

class AccountAdapter(private val activity: Activity, private var dataList: List<Account>)
    : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(activity)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemAccountListBinding.inflate(mInflater, parent, false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataList[position]
        holder.binding.accountAliasTv.text = currentItem.alias
        holder.binding.accountPathTv.text = currentItem.folder.substring(currentItem.folder.lastIndexOf("/") + 1)
        holder.binding.accountUpdateTimeTv.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentItem.updateTime)
        if (currentItem.selected) {
            holder.binding.root.setBackgroundColor(Color.RED)
        } else {
            holder.binding.root.setBackgroundColor(Color.TRANSPARENT)
        }

        holder.binding.accountLoadBtn.setOnClickListener {
            currentItem.selected = true
            Thread {
                BaseDatabase.getInstance(activity)
                        .getAccountDao().deselectAllAccount()
                BaseDatabase.getInstance(activity)
                        .getAccountDao().updateAccount(currentItem)
            }.start()
        }
    }

    fun update(dataList: List<Account>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemAccountListBinding) : RecyclerView.ViewHolder(binding.root)
}