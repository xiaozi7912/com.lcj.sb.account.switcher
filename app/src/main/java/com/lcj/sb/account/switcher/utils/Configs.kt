package com.lcj.sb.account.switcher.utils

import android.os.Environment
import com.lcj.sb.account.switcher.model.DungeonElementModel
import com.lcj.sb.account.switcher.model.DungeonLevelModel

/**
 * Created by Larry on 2018-06-19.
 */
class Configs {
    companion object {
        val PATH_EXTERNAL_STORAGE: String = Environment.getExternalStorageDirectory().absolutePath
        val PATH_APP_DATA: String = String.format("%s/%s", PATH_EXTERNAL_STORAGE, "Android/data")

        const val PREFIX_NAME_SB_JP: String = "jp.gungho.bm"
        const val PREFIX_NAME_SB_TW: String = "com.ghg.sb"
        const val NAME_ACCOUNT_INFO_FILE: String = "account.json"

        const val PREF_KEY_FIRST_RUN = "first_run"
        const val PREF_KEY_LANGUAGE = "lang"

        const val URL_WEB_SITE_JP = "https://sb.gungho.jp/member/"
        const val URL_WEB_SITE_TW = "http://www.gungho-gamania.com/SB/Bulletins/Bulletin.aspx"
        const val URL_APK_JP = "https://lcj.page.link/7Yoh"

        const val SCREEN_SB_JP = "SCREEN_SB_JP"
        const val SCREEN_SB_TW = "SCREEN_SB_TW"
        const val SCREEN_SETTINGS = "SCREEN_SETTINGS"
        const val SCREEN_LOCAL_BACKUP = "SCREEN_LOCAL_BACKUP"
        const val SCREEN_REMOTE_BACKUP = "SCREEN_REMOTE_BACKUP"

        const val REQUEST_CODE_GOOGLE_SIGN_IN: Int = 1001
        const val INTENT_KEY_ACCOUNT = "ACCOUNT"

        val DUNGEON_LEVEL_LIST: ArrayList<DungeonLevelModel> = arrayListOf(
                DungeonLevelModel(2, "冥"), DungeonLevelModel(1, "神"), DungeonLevelModel(0, "滅"),
                DungeonLevelModel(3, "塔"), DungeonLevelModel(4, "魔窟")
        )
        val DUNGEON_ELEMENT_LIST: ArrayList<DungeonElementModel> = arrayListOf(
                DungeonElementModel(0, "火"), DungeonElementModel(1, "水"), DungeonElementModel(2, "木"),
                DungeonElementModel(3, "光"), DungeonElementModel(4, "暗"))
    }
}