package com.lcj.sb.account.switcher.adapter

import android.app.Activity
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lcj.sb.account.switcher.database.entity.DungeonParty
import com.lcj.sb.account.switcher.databinding.ItemDungeonPartyBinding
import com.lcj.sb.account.switcher.utils.IconUtils

class PartyAdapter(val activity: Activity) : RecyclerView.Adapter<PartyAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(activity)
    private var dataList: List<DungeonParty> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemDungeonPartyBinding.inflate(mInflater, parent, false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataList[position]
        val dungeonResId = IconUtils.getInstance(activity).getDungeonResId(currentItem.iconName!!)
        val levelResId = IconUtils.getInstance(activity).getDungeonLevelResId(currentItem.dungeonType)
        val elementResId = IconUtils.getInstance(activity).getDungeonElementResId(currentItem.elementType)

        holder.binding.dungeonIconIv.setImageResource(dungeonResId)
        holder.binding.elementTypeImg.setImageResource(elementResId)
        holder.binding.title.text = String.format("%s - %s", currentItem.title, currentItem.monsterName)
        holder.binding.partyImage.setImageURI(Uri.parse(currentItem.imagePath))
    }

    fun update(dataList: List<DungeonParty>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemDungeonPartyBinding) : RecyclerView.ViewHolder(binding.root)
}