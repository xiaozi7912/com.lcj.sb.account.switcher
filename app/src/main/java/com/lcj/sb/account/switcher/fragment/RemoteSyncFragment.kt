package com.lcj.sb.account.switcher.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.material.snackbar.Snackbar
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.lcj.sb.account.switcher.BaseApplication
import com.lcj.sb.account.switcher.BuildConfig
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.database.entity.FolderSync
import com.lcj.sb.account.switcher.databinding.FragmentRemoteBackupBinding
import com.lcj.sb.account.switcher.utils.Configs
import com.lcj.sb.account.switcher.utils.ZipManager
import com.lcj.sb.account.switcher.view.AccountUploadDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RemoteSyncFragment : BaseFragment() {
    private lateinit var mBinding: FragmentRemoteBackupBinding

    private lateinit var mSignInClient: GoogleSignInClient

    companion object {
        fun newInstance(): RemoteSyncFragment {
            return RemoteSyncFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentRemoteBackupBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.signInButton.setOnClickListener {
            startActivityForResult(mSignInClient.signInIntent, Configs.REQUEST_CODE_GOOGLE_SIGN_IN)
        }
        mBinding.signOutButton.setOnClickListener {
            mSignInClient.signOut().addOnCompleteListener {
                checkLastSignedInAccount()
            }
        }
        mBinding.settingsSbJSyncBtn.setOnClickListener { onSyncJPButtonClick() }
        mBinding.settingsSbTSyncBtn.setOnClickListener { onSyncTWButtonClick() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initGoogleSignIn()
        checkLastSignedInAccount()
        updateSyncView(Account.Language.JP)
        updateSyncView(Account.Language.TW)
    }

    override fun onResume() {
        super.onResume()
        BaseApplication.setCurrentScreen(mActivity, Configs.SCREEN_REMOTE_BACKUP, LOG_TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Configs.REQUEST_CODE_GOOGLE_SIGN_IN -> handleSignInResult(data)
            else -> {
            }
        }
    }

    private fun initGoogleSignIn() {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE_APPDATA))
                .build().let { mSignInClient = GoogleSignIn.getClient(activity!!, it) }
    }

    private fun checkLastSignedInAccount(): GoogleSignInAccount? {
        val account = GoogleSignIn.getLastSignedInAccount(mActivity)
        updateAccountUI(account)
        return account
    }

    private fun handleSignInResult(result: Intent?) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener { account ->
                    updateAccountUI(account)
                }
                .addOnFailureListener { err ->
                    err.printStackTrace()
                }
    }

    private fun updateAccountUI(account: GoogleSignInAccount?) {
        if (account != null) {
            mBinding.signInButton.visibility = View.INVISIBLE
            mBinding.signOutButton.visibility = View.VISIBLE
            if (checkGrantedScopes(account, arrayOf(DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_FILE))) {
                mBinding.settingsSignInAccountTv.text = account.displayName
            } else {
                mBinding.settingsSignInAccountTv.text = "權限不足，請重新登入"
            }
        } else {
            mBinding.signInButton.visibility = View.VISIBLE
            mBinding.signOutButton.visibility = View.INVISIBLE
            mBinding.settingsSignInAccountTv.text = "尚未綁定Google帳號"
        }
    }

    private fun checkGrantedScopes(account: GoogleSignInAccount, scopes: Array<String>): Boolean {
        val scopeSize = scopes.size
        var count = 0

        account.grantedScopes.forEach {
            if (scopes.contains(it.scopeUri)) count++
        }
        return (count == scopeSize)
    }

    private fun updateSyncView(lang: Account.Language) {
        val type = FolderSync.Type.REMOTE
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        val d = BaseDatabase.getInstance(mActivity).folderSyncDAO().folderSync(type.ordinal, lang.ordinal)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ entity ->
                    entity?.let {
                        when (lang) {
                            Account.Language.JP -> mBinding.settingsSbJSyncTimeTv.text = sdf.format(it.updateTime)
                            Account.Language.TW -> mBinding.settingsSbTSyncTimeTv.text = sdf.format(it.updateTime)
                        }
                    }
                }, { err -> err.printStackTrace() })
    }

    private fun startUploadAccounts(lang: Account.Language) {
        val signedInAccount = checkLastSignedInAccount()
        if (signedInAccount != null) {
            AccountUploadDialog.getInstance(mActivity).show()
            Thread {
                val db = BaseDatabase.getInstance(mActivity)
                val currentTime = System.currentTimeMillis()
                val type = FolderSync.Type.REMOTE
                var folderSize: Int

                db.accountDAO().accounts(lang.ordinal, false).apply {
                    folderSize = size
                    mHandler.post {
                        AccountUploadDialog.getInstance(mActivity).setFileName("")
                        AccountUploadDialog.getInstance(mActivity).setFileCount(0, folderSize)
                        AccountUploadDialog.getInstance(mActivity).setProgress(0)
                    }
                }.forEachIndexed { index, account ->
                    val folderPath = account.folder
                    val filesPath = String.format("%s/%s", folderPath, "files")
                    val filesFile: File? = File(filesPath)
                    val folderName = account.folder.substring(account.folder.lastIndexOf("/") + 1)
                    val hashZipFile = hashMapOf(
                            "name" to "${folderName}.zip",
                            "path" to "${mActivity.externalCacheDir?.absolutePath}/${folderName}.zip")
                    val fileList = ArrayList<String>()

                    filesFile?.listFiles()?.forEach { file -> fileList.add(file.absolutePath) }
                    if (fileList.size > 0) {
                        ZipManager.zip(fileList, hashZipFile["path"]!!)

                        val credential = GoogleAccountCredential.usingOAuth2(activity, setOf(DriveScopes.DRIVE_FILE)).apply {
                            selectedAccount = signedInAccount.account
                        }
                        val driveService = Drive.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                GsonFactory(), credential)
                                .build()
                        try {
                            val queryFolder = driveService.files().list()
                                    .setQ("name='${BuildConfig.APPLICATION_ID}'")
                                    .execute()
                            val queryFile = driveService.files().list()
                                    .setQ("name='${hashZipFile["name"]}'")
                                    .execute()
                            val folderFile = if (queryFolder.files.size == 0) {
                                driveService.files().create(com.google.api.services.drive.model.File().apply {
                                    name = BuildConfig.APPLICATION_ID
                                    mimeType = "application/vnd.google-apps.folder"
                                }).execute()
                            } else {
                                queryFolder.files.first()
                            }

                            driveService.files().create(com.google.api.services.drive.model.File().apply {
                                parents = Collections.singletonList(folderFile.id)
                                name = hashZipFile["name"]
                            }, FileContent("application/zip", File(hashZipFile["path"]!!))).apply {
                                mediaHttpUploader.chunkSize = (1 * 1024 * 1024)
                                mediaHttpUploader.setProgressListener {
                                    when (it.uploadState) {
                                        MediaHttpUploader.UploadState.INITIATION_STARTED -> {
                                            mHandler.post {
                                                AccountUploadDialog.getInstance(mActivity).setFileName(hashZipFile["name"]!!)
                                                AccountUploadDialog.getInstance(mActivity).setFileCount((index + 1), folderSize)
                                                AccountUploadDialog.getInstance(mActivity).setProgress(0)
                                            }
                                        }
                                        MediaHttpUploader.UploadState.INITIATION_COMPLETE -> {
                                        }
                                        MediaHttpUploader.UploadState.MEDIA_IN_PROGRESS -> {
                                            val percent = it.progress * 100
                                            mHandler.post { AccountUploadDialog.getInstance(mActivity).setProgress(percent.toInt()) }
                                        }
                                        MediaHttpUploader.UploadState.MEDIA_COMPLETE -> {
                                            mHandler.post { AccountUploadDialog.getInstance(mActivity).setProgress(100) }
                                        }
                                        else -> {
                                        }
                                    }
                                }
                            }.execute()

                            queryFile.files.forEach {
                                driveService.files().delete(it.id).execute()
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }

                db.folderSyncDAO().folderSync(type.ordinal, lang.ordinal)
                        .subscribe({ entity ->
                            entity!!.updateTime = currentTime
                            db.folderSyncDAO().update(entity)
                            updateSyncView(lang)
                        }, { err ->
                            err.printStackTrace()
                            db.folderSyncDAO().insert(FolderSync(type.ordinal, lang.ordinal, currentTime))
                            updateSyncView(lang)
                        })
                mHandler.post {
                    AccountUploadDialog.getInstance(mActivity).dismiss()
                    Snackbar.make(mContentView, getString(R.string.file_upload_completed), Snackbar.LENGTH_SHORT).show()
                }
            }.start()
        } else {
            Snackbar.make(mContentView, getString(R.string.no_google_account_association), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun onSyncJPButtonClick() {
        startUploadAccounts(Account.Language.JP)
    }

    private fun onSyncTWButtonClick() {
        startUploadAccounts(Account.Language.TW)
    }
}