package com.lcj.sb.account.switcher.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lcj.sb.account.switcher.BaseAdapter
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.model.DungeonElementModel
import com.lcj.sb.account.switcher.utils.IconUtils

class DungeonElementAdapter(activity: Activity) : BaseAdapter<DungeonElementModel, DungeonElementAdapter.ViewHolder>(activity) {
    private var mCallback: ((selectedItem: DungeonElementModel, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(mInflater.inflate(R.layout.item_dungeon_element, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = mDataList[position]
        val iconResId = IconUtils.getInstance(activity).getDungeonElementResId(currentItem.index)

        holder.imageView.setImageResource(iconResId)
        holder.textView.text = currentItem.title
        holder.itemView.setOnClickListener {
            mCallback?.let { it(currentItem, position) }
        }
    }

    fun setCallback(callback: (selectedItem: DungeonElementModel, position: Int) -> Unit) {
        mCallback = callback
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.icon_iv)
        var textView: TextView = itemView.findViewById(R.id.text_tv)
    }
}