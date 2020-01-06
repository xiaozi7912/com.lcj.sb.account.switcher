package com.lcj.sb.account.switcher.adapter

import android.app.Activity
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lcj.sb.account.switcher.R
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

        when (item.dungeonType) {
            0 -> R.drawable.ic_dungeon_type_1_p
            1 -> R.drawable.ic_dungeon_type_2_p
            2 -> R.drawable.ic_dungeon_type_3_p
            3 -> R.drawable.ic_dungeon_type_4_p
            4 -> R.drawable.ic_dungeon_type_5_p
            else -> R.drawable.ic_dungeon_type_1_p
        }.let {
            holder.binding.dungeonTypeImg.setImageResource(it)
        }

        when (item.elementType) {
            0 -> R.drawable.ic_element_1_p
            1 -> R.drawable.ic_element_2_p
            2 -> R.drawable.ic_element_3_p
            3 -> R.drawable.ic_element_4_p
            4 -> R.drawable.ic_element_5_p
            else -> R.drawable.ic_element_1_p
        }.let {
            holder.binding.elementTypeImg.setImageResource(it)
        }

        holder.binding.title.text = item.title
        holder.binding.partyImage.setImageURI(Uri.parse(item.imagePath))
    }

    fun update(dataList: List<DungeonParty>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemDungeonPartyBinding) : RecyclerView.ViewHolder(binding.root)
}