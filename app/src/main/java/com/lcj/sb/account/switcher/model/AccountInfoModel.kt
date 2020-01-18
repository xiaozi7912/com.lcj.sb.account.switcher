package com.lcj.sb.account.switcher.model

import android.content.Context
import androidx.databinding.BaseObservable
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.database.entity.DungeonParty
import com.lcj.sb.account.switcher.utils.Configs

class AccountInfoModel(var account: Account) : BaseObservable() {
    private val LOG_TAG = javaClass.simpleName

    private var mSelectedLevelList = arrayListOf<Int>()
    private var mSelectedElementList = arrayListOf<Int>()
    var filterText: String = ""

    init {
        for (model in Configs.DUNGEON_LEVEL_LIST) {
            mSelectedLevelList.add(model.index)
        }

        for (model in Configs.DUNGEON_ELEMENT_LIST) {
            mSelectedElementList.add(model.index)
        }
    }

    fun updateLevelList(dataList: ArrayList<Int>) {
        mSelectedLevelList = dataList
        notifyChange()
    }

    fun updateElementList(dataList: ArrayList<Int>) {
        mSelectedElementList = dataList
        notifyChange()
    }

    fun onFilterClick(context: Context, callback: (List<DungeonParty>?) -> Unit) {
        Thread {
            BaseDatabase.getInstance(context).dungeonPartyDAO()
                    .getFilterPartyList(account.id, mSelectedLevelList, mSelectedElementList, String.format("%%%s%%", filterText)).let {
                        callback(it)
                    }
        }.start()
    }
}