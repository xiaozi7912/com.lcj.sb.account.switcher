package com.lcj.sb.account.switcher.model

import com.google.gson.annotations.SerializedName

data class RemoteConfigModel(
        @SerializedName("jp_code") val versionCodeJP: Long,
        @SerializedName("tw_code") val versionCodeTW: Long)