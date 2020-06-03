package com.lcj.sb.account.switcher.http.model

import java.io.Serializable

data class MonsterModel(var id: Long = 0,
                        var number: Long = 0,
                        var name_jp: String? = null,
                        var name_tw: String? = null,
                        var element: Long = 0,
                        var type_a: Long = 0,
                        var type_b: Long = 0,
                        var type_c: Long = 0,
                        var rarity: Long = 0,
                        var before_number: Long = 0,
                        var after_number: Long = 0) : Serializable {
}