package com.lcj.sb.account.switcher.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lcj.sb.account.switcher.database.entity.DungeonParty
import com.lcj.sb.account.switcher.databinding.ItemDungeonPartyBinding

class PartyAdapter(activity: Activity) : RecyclerView.Adapter<PartyAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(activity)
    private var dataList: List<DungeonParty> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemDungeonPartyBinding.inflate(mInflater, parent, false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.binding.title.text = item.title
    }

    fun update(dataList: List<DungeonParty>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemDungeonPartyBinding) : RecyclerView.ViewHolder(binding.root)
}