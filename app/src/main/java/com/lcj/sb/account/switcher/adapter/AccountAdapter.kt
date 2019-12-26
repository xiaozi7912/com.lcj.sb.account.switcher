package com.lcj.sb.account.switcher.adapter

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.databinding.ItemAccountListBinding
import java.text.SimpleDateFormat

class AccountAdapter(private val activity: Activity)
    : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(activity)
    private var dataList: List<Account> = emptyList()
    private var mSaveButtonClickCallback: ((ViewHolder, Account) -> Unit)? = null
    private var mLoadButtonClickCallback: ((ViewHolder, Account) -> Unit)? = null

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
            holder.binding.accountSaveBtn.visibility = View.VISIBLE
            holder.binding.accountLoadBtn.visibility = View.VISIBLE
        } else {
            holder.binding.root.setBackgroundColor(Color.TRANSPARENT)
            holder.binding.accountSaveBtn.visibility = View.INVISIBLE
            holder.binding.accountLoadBtn.visibility = View.INVISIBLE
        }

        holder.binding.root.setOnClickListener {
            currentItem.selected = true
            Thread {
                BaseDatabase.getInstance(activity)
                        .accountDAO().deselectAllAccount(currentItem.lang)
                BaseDatabase.getInstance(activity)
                        .accountDAO().updateAccount(currentItem)
            }.start()
        }
        holder.binding.accountSaveBtn.setOnClickListener {
            mSaveButtonClickCallback?.let { it(holder, currentItem) }
        }
        holder.binding.accountLoadBtn.setOnClickListener {
            mLoadButtonClickCallback?.let { it(holder, currentItem) }
        }
    }

    fun update(dataList: List<Account>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    fun setSaveButtonClick(callback: (ViewHolder, Account) -> Unit) {
        mSaveButtonClickCallback = callback
    }

    fun setLoadButtonClick(callback: (ViewHolder, Account) -> Unit) {
        mLoadButtonClickCallback = callback
    }

    class ViewHolder(val binding: ItemAccountListBinding) : RecyclerView.ViewHolder(binding.root)
}