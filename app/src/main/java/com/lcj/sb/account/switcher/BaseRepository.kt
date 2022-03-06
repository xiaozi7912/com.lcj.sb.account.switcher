package com.lcj.sb.account.switcher

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.preference.PreferenceManager
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.database.entity.GoogleDriveItem
import com.lcj.sb.account.switcher.utils.Configs

open class BaseRepository(val activity: Activity) {
    protected val LOG_TAG = javaClass.simpleName
    protected val mHandler = Handler()

    interface BaseCallback {
        fun onError(message: String)
    }

    interface UploadCallback : BaseCallback {
        fun onInitial(fileName: String)
        fun onUploadStarted(progress: Int)
        fun inProgress(progress: Int)
        fun onComplete(progress: Int)
        fun onSuccess()
    }

    interface FetchCallback : BaseCallback {
        fun onInitial()
        fun onSuccess(dataList: ArrayList<GoogleDriveItem>)
    }

    interface DownloadCallback : BaseCallback {
        fun onInitial()
        fun inProgress(progress: Int)
        fun onComplete(progress: Int)
        fun onUnzip()
        fun onSuccess()
    }

    interface DeleteCallback : BaseCallback {
        fun onInitial()
        fun onSuccess()
    }
}