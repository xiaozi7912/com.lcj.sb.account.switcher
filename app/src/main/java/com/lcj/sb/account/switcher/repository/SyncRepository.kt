package com.lcj.sb.account.switcher.repository

import android.app.Activity
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.media.MediaHttpDownloader
import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.lcj.sb.account.switcher.BaseRepository
import com.lcj.sb.account.switcher.BuildConfig
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.database.entity.GoogleDriveItem
import com.lcj.sb.account.switcher.utils.Configs
import com.lcj.sb.account.switcher.utils.ZipManager
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Collections

class SyncRepository(activity: Activity) : BaseRepository(activity) {
    companion object {
        private const val CHUNK_SIZE = (0.5 * 1024 * 1024).toInt()
        fun getInstance(activity: Activity): SyncRepository {
            return SyncRepository(activity)
        }
    }

    fun upload(account: Account, callback: UploadCallback) {
        checkSignedInAccount({ signedIn ->
            CompletableFromAction.fromAction {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startZipFiles(signedIn, account, callback)
                } else {
                    startLegacyZipFiles(signedIn, account, callback)
                }
            }
                .subscribeOn(Schedulers.io())
                .subscribe { }.let { }
        }, { callback.onError(it) })
    }

    fun fetch(callback: FetchCallback) {
        callback.onInitial()
        checkSignedInAccount({ signedIn ->
            Thread {
                val service = getDriveService(signedIn)
                try {
                    val folder = service.files().list()
                        .setQ("name='${BuildConfig.APPLICATION_ID}' and mimeType='application/vnd.google-apps.folder' and trashed=false")
                        .setFields("files(id,name,modifiedTime,parents,mimeType)")
                        .execute().files.first().apply {
                        }

                    val dataList = arrayListOf<GoogleDriveItem>()
                    val files = service.files().list()
                        .setQ("'${folder.id}' in parents and mimeType='application/zip' and trashed=false")
                        .setFields("files(id,name,modifiedTime,parents,mimeType)")
                        .setOrderBy("name")
                        .execute().files

                    files.forEach { file ->
                        with(file.name) {
                            when {
                                contains(Configs.PREFIX_NAME_SB_JP) -> Account.Language.JP
                                contains(Configs.PREFIX_NAME_SB_TW) -> Account.Language.TW
                                else -> Account.Language.JP
                            }
                        }.let { lang ->
                            dataList.add(GoogleDriveItem(file.id, file.name, file.modifiedTime.value, lang))
                        }
                    }
                    mHandler.post { callback.onSuccess(dataList) }
                } catch (e: NoSuchElementException) {
                    e.printStackTrace()
                    mHandler.post { callback.onError(e.localizedMessage ?: "") }
                } catch (e: Exception) {
                    e.printStackTrace()
                    mHandler.post { callback.onError(e.localizedMessage ?: "") }
                }
            }.start()
        }, { callback.onError(it) })
    }

    fun download(entity: GoogleDriveItem, callback: DownloadCallback) {
        callback.onInitial()
        checkSignedInAccount({ signedIn ->
            val d = CompletableFromAction.fromAction {
                val service = getDriveService(signedIn)
                try {
                    val zipFile = File("${activity.externalCacheDir?.absolutePath}/${entity.name}")
                    FileOutputStream(zipFile).use { inputStream ->
                        service.files().get(entity.id).apply {
                            mediaHttpDownloader.setProgressListener {
                                when (it.downloadState) {
                                    MediaHttpDownloader.DownloadState.NOT_STARTED -> {
                                    }

                                    MediaHttpDownloader.DownloadState.MEDIA_IN_PROGRESS -> {
                                        val percent = it.progress * 100
                                        callback.inProgress(percent.toInt())
                                    }

                                    MediaHttpDownloader.DownloadState.MEDIA_COMPLETE -> {
                                        callback.onComplete(100)
                                    }

                                    else -> {
                                    }
                                }
                            }
                        }.executeMediaAndDownloadTo(inputStream)
                    }
                    mHandler.post { callback.onUnzip() }

                    val folderName = entity.name.substring(0, entity.name.lastIndexOf("."))
                    val docDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    val appDir = File(docDir, BuildConfig.APPLICATION_ID)
                    val accountDir = File(appDir, folderName)

                    if (!accountDir.mkdirs()) Log.e(LOG_TAG, "create dir error.")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            accountDir.listFiles()?.let { list ->
                                list.map { file -> file.delete() }
                            }
                            ZipManager.unZip(zipFile.absolutePath, appDir.absolutePath)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        ZipManager.unZip(zipFile.absolutePath, appDir.absolutePath)
                    }

                    val currentTime = System.currentTimeMillis()
                    BaseDatabase.getInstance(activity).accountDAO()
                        .insert(
                            Account(
                                alias = folderName, folder = accountDir.absolutePath, lang = entity.lang.ordinal,
                                createTime = currentTime, updateTime = currentTime
                            )
                        )
                    mHandler.post { callback.onSuccess() }
                } catch (e: Exception) {
                    e.printStackTrace()
                    mHandler.post { callback.onError(e.localizedMessage ?: "") }
                }
            }
                .subscribeOn(Schedulers.io())
                .subscribe { }
        },
            { callback.onError(it) })
    }

    fun delete(entity: GoogleDriveItem, callback: DeleteCallback) {
        callback.onInitial()
        checkSignedInAccount({ signedIn ->
            Thread {
                val service = getDriveService(signedIn)
                try {
                    service.files().delete(entity.id).execute()
                    mHandler.post { callback.onSuccess() }
                } catch (e: Exception) {
                    e.printStackTrace()
                    mHandler.post { callback.onError(e.localizedMessage ?: "") }
                }
            }.start()
        }, { callback.onError(it) })
    }

    private fun checkSignedInAccount(onSuccess: (account: GoogleSignInAccount) -> Unit, onError: (message: String) -> Unit) {
        GoogleSignIn.getLastSignedInAccount(activity)?.let {
            onSuccess(it)
        } ?: run {
            onError(activity.getString(R.string.no_google_account_association))
        }
    }

    private fun getDriveService(signedIn: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(activity, setOf(DriveScopes.DRIVE_FILE)).apply {
            selectedAccount = signedIn.account
        }
        return Drive.Builder(NetHttpTransport(), GsonFactory(), credential).build()
    }

    private fun startZipFiles(signedIn: GoogleSignInAccount, account: Account, callback: UploadCallback) {
        val gameFolderName = if (account.lang == Account.Language.JP.ordinal) Configs.PREFIX_NAME_SB_JP else Configs.PREFIX_NAME_SB_TW
        activity.contentResolver.persistedUriPermissions.find { it.uri.toString().contains(gameFolderName) }?.let { uriPermission ->
            DocumentFile.fromTreeUri(activity, uriPermission.uri)?.let { gameDir ->
                val replacedName = account.folder.replace("%2F", "/", true)
                val folderName = replacedName.substring(replacedName.lastIndexOf("/") + 1)
                gameDir.findFile("files")?.listFiles()?.filter { it.isFile }?.let { list ->
                    val hashZipFile = hashMapOf(
                        "name" to "${folderName}.zip",
                        "path" to "${activity.externalCacheDir}/${folderName}.zip"
                    )
                    val fileList = list.map { it.uri.toString() }

                    mHandler.post { callback.onInitial(hashZipFile["name"]!!) }
                    try {
                        ZipManager.zip(activity.contentResolver, fileList, hashZipFile["path"]!!)
                        startUploadFile(signedIn, hashZipFile, callback)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } ?: run { callback.onError("找不到 files 資料夾。") }
            } ?: run {
                callback.onError("沒有資料夾存取權限。")
            }
        }
    }

    private fun startLegacyZipFiles(signedIn: GoogleSignInAccount, account: Account, callback: UploadCallback) {
        val folderPath = account.folder
        val filesPath = String.format("%s/%s", folderPath, "files")
        val filesFile = File(filesPath)
        val folderName = account.folder.substring(account.folder.lastIndexOf("/") + 1)
        val hashZipFile = hashMapOf(
            "name" to "${folderName}.zip",
            "path" to "${activity.externalCacheDir?.absolutePath}/${folderName}.zip"
        )
        val fileList = ArrayList<String>()

        mHandler.post { callback.onInitial(hashZipFile["name"]!!) }
        filesFile.listFiles()?.forEach { file -> fileList.add(file.absolutePath) }
        if (fileList.size > 0) {
            ZipManager.zip(fileList, hashZipFile["path"]!!)
            startUploadFile(signedIn, hashZipFile, callback)
        } else {
            callback.onError("資料夾內沒有檔案！")
        }
    }

    private fun startUploadFile(signedIn: GoogleSignInAccount, hashZipFile: HashMap<String, String>, callback: UploadCallback) {
        val service = getDriveService(signedIn)
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
                mediaHttpUploader.chunkSize = CHUNK_SIZE
                mediaHttpUploader.setProgressListener {
                    when (it.uploadState) {
                        MediaHttpUploader.UploadState.INITIATION_STARTED -> {
                            callback.onUploadStarted(0)
                        }

                        MediaHttpUploader.UploadState.INITIATION_COMPLETE -> {
                        }

                        MediaHttpUploader.UploadState.MEDIA_IN_PROGRESS -> {
                            val percent = it.progress * 100
                            callback.inProgress(percent.toInt())
                        }

                        MediaHttpUploader.UploadState.MEDIA_COMPLETE -> {
                            callback.onComplete(100)
                        }

                        else -> {
                        }
                    }
                }
            }.execute()

            qFile.files.forEach { service.files().delete(it.id).execute() }
            callback.onSuccess()
        } catch (e: IOException) {
            e.printStackTrace()
            callback.onError(e.localizedMessage ?: "")
        }
    }
}