package com.lcj.sb.account.switcher.repository

import com.lcj.sb.account.switcher.database.entity.DungeonParty

interface IPartyListListener {
    fun onDeleteClick(item: DungeonParty)
}