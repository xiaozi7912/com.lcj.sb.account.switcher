package com.lcj.sb.account.switcher.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lcj.sb.account.switcher.BaseAdapter
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.model.DungeonStageModel
import com.lcj.sb.account.switcher.utils.IconUtils

class DungeonStageAdapter(activity: Activity) : BaseAdapter<DungeonStageModel, DungeonStageAdapter.ViewHolder>(activity) {
    private var mCallback: ((selectedItem: DungeonStageModel, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(mInflater.inflate(R.layout.item_dungeon_stage, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = mDataList[position]
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