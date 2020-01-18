package com.lcj.sb.account.switcher.utils

import android.content.Context
import com.lcj.sb.account.switcher.R

class IconUtils(val context: Context) {
    private var LOG_TAG = javaClass.simpleName

    companion object {
        private var instance: IconUtils? = null

        fun getInstance(context: Context): IconUtils {
            if (instance == null) instance = IconUtils(context)
            return instance!!
        }
    }

    fun getDungeonResId(iconName: String): Int {
        return context.resources.getIdentifier(iconName, "drawable", context.packageName)
    }

    fun getDungeonLevelResId(index: Int): Int {
        return when (index) {
            0 -> 0
            1 -> R.drawable.ic_dungeon_level_1
            2 -> R.drawable.ic_dungeon_level_2
            3 -> R.drawable.ic_dungeon_level_3
            4 -> R.drawable.ic_dungeon_level_4
            else -> 0
        }
    }

    fun getDungeonElementResId(index: Int): Int {
        return when (index) {
            0 -> R.drawable.ic_element_1_p
            1 -> R.drawable.ic_element_2_p
            2 -> R.drawable.ic_element_3_p
            3 -> R.drawable.ic_element_4_p
            4 -> R.drawable.ic_element_5_p
            else -> 0
        }
    }
}