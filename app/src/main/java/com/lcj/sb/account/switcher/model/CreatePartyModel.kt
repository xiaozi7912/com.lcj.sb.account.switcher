package com.lcj.sb.account.switcher.model

import android.widget.Button
import androidx.databinding.BaseObservable
import androidx.databinding.BindingAdapter

class CreatePartyModel : BaseObservable() {
    var dungeonTypeActArray = arrayOf(false, false, false, false, false)
    var elementTypeActArray = arrayOf(false, false, false, false, false)
    var title: String = ""

    companion object {
        @JvmStatic
        @BindingAdapter("activated")
        fun isActivated(view: Button, flag: Boolean) {
            view.isActivated = flag
        }
    }

    fun onDungeonTypeClick(index: Int) {
        for (i in dungeonTypeActArray.indices) {
            dungeonTypeActArray[i] = (i == index)
        }
        notifyChange()
    }

    fun onElementTypeClick(index: Int) {
        for (i in elementTypeActArray.indices) {
            elementTypeActArray[i] = (i == index)
        }
        notifyChange()
    }

    fun getSelectedDungeonType(): Int {
        var result = -1
        for (i in dungeonTypeActArray.indices) {
            if (dungeonTypeActArray[i]) result = i
        }
        return result
    }

    fun getSelectedElementType(): Int {
        var result = -1
        for (i in elementTypeActArray.indices) {
            if (elementTypeActArray[i]) result = i
        }
        return result
    }
}