package com.lcj.sb.account.switcher.model

data class MonsterFilterModel(
        var elements: ArrayList<Int> = arrayListOf(),
        var typeAs: ArrayList<Int> = arrayListOf(),
        var typeBs: ArrayList<Int> = arrayListOf())