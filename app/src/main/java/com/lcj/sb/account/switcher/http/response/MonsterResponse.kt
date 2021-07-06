package com.lcj.sb.account.switcher.http.response

import com.lcj.sb.account.switcher.http.model.MonsterModel

class MonsterResponse : BaseResponse() {
    data class ResultModel(
            var data: List<MonsterModel>? = null,
            var current_page: Int = 0,
            var last_page: Int = 0,
            var first_page_url: String? = null,
            var last_page_url: String? = null,
            var next_page_url: String? = null)

    lateinit var result: ResultModel
}