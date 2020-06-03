package com.lcj.sb.account.switcher.adapter

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lcj.sb.account.switcher.BaseAdapter
import com.lcj.sb.account.switcher.BuildConfig
import com.lcj.sb.account.switcher.databinding.ItemMonsterListBinding
import com.lcj.sb.account.switcher.http.model.MonsterModel
import com.squareup.picasso.Picasso

class MonsterListAdapter(activity: Activity) : BaseAdapter<MonsterListAdapter.ViewHolder>(activity) {
    private var mDataList: List<MonsterModel> = emptyList()

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMonsterListBinding.inflate(mInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mDataList[position]
        holder.updateView(item)
    }

    fun update(dataList: List<MonsterModel>) {
        mDataList = dataList;
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemMonsterListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun updateView(item: MonsterModel) {
            val iconUrl = String.format("%s/images/ic_summon_%d.png", BuildConfig.API_BASE_URL, item.number)

            Picasso.get().load(iconUrl).into(binding.iconView)
            binding.titleView.text = item.name_jp
        }
    }
}