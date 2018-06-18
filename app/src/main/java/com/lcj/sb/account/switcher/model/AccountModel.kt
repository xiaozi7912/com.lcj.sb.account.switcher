package com.lcj.sb.account.switcher.model

/**
 * Created by Larry on 2018-06-18.
 */
class AccountModel() {
    open var folderName: String? = null
    open var folderPath: String? = null

    constructor(folderName: String, folderPath: String) : this() {
        this.folderName = folderName
        this.folderPath = folderPath
    }
}