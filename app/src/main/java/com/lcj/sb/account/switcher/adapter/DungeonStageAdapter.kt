package com.lcj.sb.account.switcher.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.model.DungeonStageModel
import com.lcj.sb.account.switcher.utils.IconUtils

class DungeonStageAdapter(private val activity: Activity, private val dataList: ArrayList<DungeonStageModel>) : RecyclerView.Adapter<DungeonStageAdapter.ViewHolder>() {
    private val mInflater = LayoutInflater.from(activity)
    private var mCallback: ((selectedItem: DungeonStageModel, position: Int) -> Unit)? = null

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(mInflater.inflate(R.layout.item_dungeon_stage, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataList[position]
        val resourceId = IconUtils.getInstance(activity).getDungeonResId(currentItem.icon!!)

        holder.imageView.setImageResource(resourceId)
        holder.eventTitleTextView.text = currentItem.event_title
        holder.stageTitleTextView.text = String.format("%s - %s", currentItem.title, currentItem.monster_name)
        holder.itemView.setOnClickListener {
            mCallback?.let { it(currentItem, position) }
        }
    }

    fun setCallback(callback: (selectedItem: DungeonStageModel, position: Int) -> Unit) {
        mCallback = callback
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.icon_iv)
        var eventTitleTextView: TextView = itemView.findViewById(R.id.event_title_text)
        var stageTitleTextView: TextView = itemView.findViewById(R.id.stage_title_text)
    }
}