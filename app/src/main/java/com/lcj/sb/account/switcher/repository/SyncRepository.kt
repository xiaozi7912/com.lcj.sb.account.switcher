package com.lcj.sb.account.switcher.repository

import android.app.Activity
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.lcj.sb.account.switcher.BaseRepository
import com.lcj.sb.account.switcher.BuildConfig
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.utils.ZipManager
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class SyncRepository(activity: Activity) : BaseRepository(activity) {
    companion object {
        fun getInstance(activity: Activity): SyncRepository {
            return SyncRepository(activity)
        }
    }

    fun upload(account: Account, listener: UploadListener) {
        GoogleSignIn.getLastSignedInAccount(activity)?.let { signedInAccount ->
            Thread {
                val folderPath = account.folder
                val filesPath = String.format("%s/%s", folderPath, "files")
                val filesFile: File? = File(filesPath)
                val folderName = account.folder.substring(account.folder.lastIndexOf("/") + 1)
                val hashZipFile = hashMapOf(
                        "name" to "${folderName}.zip",
                        "path" to "${activity.externalCacheDir?.absolutePath}/${folderName}.zip")
                val fileList = ArrayList<String>()

                listener.onInitial(hashZipFile["name"]!!)
                filesFile?.listFiles()?.forEach { file -> fileList.add(file.absolutePath) }
                if (fileList.size > 0) {
                    ZipManager.zip(fileList, hashZipFile["path"]!!)

                    val credential = GoogleAccountCredential.usingOAuth2(activity, setOf(DriveScopes.DRIVE_FILE)).apply {
                        selectedAccount = signedInAccount.account
                    }
                    val service = Drive.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            GsonFactory(), credential).build()
                    try {
                        val qFolder = service.files().list()
                                .setQ("name='${BuildConfig.APPLICATION_ID}'")
                                .execute()
                        val qFile = service.files().list()
                                .setQ("name='${hashZipFile["name"]}'")
                                .execute()
                        val folderFile = if (qFolder.files.size == 0) {
                            service.files().create(com.google.api.services.drive.model.File().apply {
                                name = BuildConfig.APPLICATION_ID
                                mimeType = "application/vnd.google-apps.folder"
                            }).execute()
                        } else {
                            qFolder.files.first()
                        }

                        service.files().create(com.google.api.services.drive.model.File().apply {
                            parents = Collections.singletonList(folderFile.id)
                            name = hashZipFile["name"]
                        }, FileContent("application/zip", File(hashZipFile["path"]!!))).apply {
                            mediaHttpUploader.chunkSize = (1 * 1024 * 1024)
                            mediaHttpUploader.setProgressListener {
                                when (it.uploadState) {
                                    MediaHttpUploader.UploadState.INITIATION_STARTED -> {
                                        listener.onUploadStarted(0)
                                    }
                                    MediaHttpUploader.UploadState.INITIATION_COMPLETE -> {
                                    }
                                    MediaHttpUploader.UploadState.MEDIA_IN_PROGRESS -> {
                                        val percent = it.progress * 100
                                        Log.v(LOG_TAG, "percent : $percent")
                                        listener.onUploadInProgress(percent.toInt())
                                    }
                                    MediaHttpUploader.UploadState.MEDIA_COMPLETE -> {
                                        listener.onUploadOnComplete(100)
                                    }
                                    else -> {
                                    }
                                }
                            }
                        }.execute()

                        qFile.files.forEach {
                            service.files().delete(it.id).execute()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        listener.onError(e.localizedMessage)
                    }
                }
                listener.onSuccess()
            }.start()
        } ?: run {
            listener.onError(activity.getString(R.string.no_google_account_association))
        }
    }
}