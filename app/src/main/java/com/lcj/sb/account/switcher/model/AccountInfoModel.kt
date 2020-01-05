package com.lcj.sb.account.switcher.model

import android.util.Log
import android.widget.Button
import androidx.databinding.BaseObservable
import androidx.databinding.BindingAdapter

class AccountInfoModel : BaseObservable() {
    private val LOG_TAG = javaClass.simpleName

    var dungeonTypeActArray = arrayOf(true, true, true, true, true)
    var elementTypeActArray = arrayOf(true, true, true, true, true)
    var filterText: String = ""

    companion object {
        @JvmStatic
        @BindingAdapter("activated")
        fun isActivated(view: Button, flag: Boolean) {
            view.isActivated = flag
        }
    }

    fun onDungeonTypeClick(index: Int) {
        dungeonTypeActArray[index] = !dungeonTypeActArray[index]
        notifyChange()
    }

    fun onElementTypeClick(index: Int) {
        elementTypeActArray[index] = !elementTypeActArray[index]
        notifyChange()
    }

    fun onFilterClick() {
        Log.i(LOG_TAG, "onFilterClick")
    }
}