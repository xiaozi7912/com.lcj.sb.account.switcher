package com.lcj.sb.account.switcher.model

import android.content.Context
import android.widget.Button
import androidx.databinding.BaseObservable
import androidx.databinding.BindingAdapter
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.database.entity.DungeonParty
import java.util.*

class AccountInfoModel(var account: Account) : BaseObservable() {
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

    fun onFilterClick(context: Context, callback: (List<DungeonParty>?) -> Unit) {
        val dungeonTypes = ArrayList<Int>()
        val elementTypes = ArrayList<Int>()

        for (i in dungeonTypeActArray.indices) {
            if (dungeonTypeActArray[i]) {
                dungeonTypes.add(i)
            }
        }

        for (i in elementTypeActArray.indices) {
            if (elementTypeActArray[i]) {
                elementTypes.add(i)
            }
        }

        Thread {
            BaseDatabase.getInstance(context).dungeonPartyDAO()
                    .getFilterPartyList(account.id, dungeonTypes, elementTypes, String.format("%%%s%%", filterText)).let {
                        callback(it)
                    }
        }.start()
    }
}