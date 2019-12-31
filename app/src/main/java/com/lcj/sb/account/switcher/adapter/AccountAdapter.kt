package com.lcj.sb.account.switcher.adapter

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.databinding.ItemAccountListBinding
import java.text.SimpleDateFormat

class AccountAdapter(private val activity: Activity)
    : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(activity)
    private var dataList: List<Account> = emptyList()
    private var mItemClickCallback: ((ViewHolder, Account) -> Unit)? = null
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
        holder.binding.accountPathTv.visibility = View.INVISIBLE

        if (currentItem.selected) {
            val iconResId = if (currentItem.lang == Account.Language.JP.ordinal) R.drawable.ic_launcher_jp_p else R.mipmap.icon_launcher_tw_p
            holder.binding.accountIconIv.setImageResource(iconResId)
            holder.binding.accountAliasTv.setTextColor(Color.RED)
        } else {
            val iconResId = if (currentItem.lang == Account.Language.JP.ordinal) R.drawable.ic_launcher_jp_n else R.mipmap.icon_launcher_tw_n
            holder.binding.accountIconIv.setImageResource(iconResId)
            holder.binding.accountAliasTv.setTextColor(Color.BLACK)
        }

        holder.binding.root.setOnClickListener {
            currentItem.selected = true
            Thread {
                BaseDatabase.getInstance(activity)
                        .accountDAO().deselectAllAccount(currentItem.lang)
                BaseDatabase.getInstance(activity)
                        .accountDAO().updateAccount(currentItem)
            }.start()
            mItemClickCallback?.let { it(holder, currentItem) }
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

    fun setItemClick(callback: (ViewHolder, Account) -> Unit) {
        mItemClickCallback = callback
    }

    fun setSaveButtonClick(callback: (ViewHolder, Account) -> Unit) {
        mSaveButtonClickCallback = callback
    }

    fun setLoadButtonClick(callback: (ViewHolder, Account) -> Unit) {
        mLoadButtonClickCallback = callback
    }

    class ViewHolder(val binding: ItemAccountListBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.accountSaveBtn.visibility = View.GONE
            binding.accountLoadBtn.visibility = View.GONE
        }
    }
}