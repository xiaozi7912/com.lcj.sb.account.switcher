package com.lcj.sb.account.switcher.http.model

import java.io.Serializable

data class DungeonModel(var id: Long = 0,
                        var number: Long = 0,
                        var event_jp: String? = null,
                        var event_tw: String? = null,
                        var title_jp: String? = null,
                        var title_tw: String? = null,
                        var element: Long = 0,
                        var type_a: Long = 0,
                        var floor: Long = 0) : Serializable {
}