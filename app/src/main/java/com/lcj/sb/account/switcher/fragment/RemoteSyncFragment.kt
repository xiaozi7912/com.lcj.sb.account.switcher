package com.lcj.sb.account.switcher.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.material.snackbar.Snackbar
import com.google.api.services.drive.DriveScopes
import com.lcj.sb.account.switcher.BaseAdapter
import com.lcj.sb.account.switcher.BaseApplication
import com.lcj.sb.account.switcher.BaseFragment
import com.lcj.sb.account.switcher.BaseRepository
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.adapter.GoogleDriveAdapter
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.database.entity.FolderSync
import com.lcj.sb.account.switcher.database.entity.GoogleDriveItem
import com.lcj.sb.account.switcher.databinding.FragmentRemoteBackupBinding
import com.lcj.sb.account.switcher.repository.SyncRepository
import com.lcj.sb.account.switcher.utils.Configs
import com.lcj.sb.account.switcher.view.ProgressDialog
import com.lcj.sb.account.switcher.view.RemoteProgressDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.Locale

class RemoteSyncFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var mBinding: FragmentRemoteBackupBinding
    private lateinit var mSignInClient: GoogleSignInClient
    private lateinit var mAdapter: GoogleDriveAdapter

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
        mBinding.settingsGoogleAccountRoot.visibility = View.GONE
        mBinding.settingsSbJRoot.visibility = View.GONE
        mBinding.settingsSbTRoot.visibility = View.GONE

        mBinding.signInButton.setOnClickListener {
            startActivityForResult(mSignInClient.signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN)
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
        initRecyclerView()
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
            REQUEST_CODE_GOOGLE_SIGN_IN -> handleSignInResult(data)
            REQUEST_CODE_FOLDER_PERMISSION -> {
                if (resultCode == Activity.RESULT_OK) {
                    val contentResolver = mActivity.contentResolver
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(data?.data!!, takeFlags)
                    Toast.makeText(mActivity, "請重新點擊下載。", Toast.LENGTH_LONG).show()
                }
            }

            else -> {
            }
        }
    }

    override fun onRefresh() {
        SyncRepository.getInstance(mActivity).fetch(object : BaseRepository.FetchCallback {
            override fun onInitial() {
                mBinding.settingsNoFileText.visibility = View.GONE
                mBinding.progressBar.visibility = View.VISIBLE
            }

            override fun onSuccess(dataList: ArrayList<GoogleDriveItem>) {
                if (dataList.isEmpty()) mBinding.settingsNoFileText.visibility = View.VISIBLE
                mAdapter.update(dataList)
                mBinding.progressBar.visibility = View.GONE
            }

            override fun onError(message: String) {
                mBinding.settingsNoFileText.visibility = View.VISIBLE
                mBinding.progressBar.visibility = View.GONE
                Snackbar.make(mContentView, message, Snackbar.LENGTH_SHORT).show()
            }
        })
        mBinding.swipeRefreshLayout.isRefreshing = false
    }

    private fun initRecyclerView() {
        mAdapter = GoogleDriveAdapter(mActivity).apply {
            setListener(object : BaseAdapter.RemoteSyncListListener {
                override fun onItemClick(entity: GoogleDriveItem) {
                }

                override fun onDeleteClick(entity: GoogleDriveItem) {
                    AlertDialog.Builder(mActivity).apply {
                        setTitle("刪除雲端備份")
                        setMessage("確定要刪除Google Drive上的備份嗎？")
                        setPositiveButton(getString(R.string.dialog_button_confirmed)) { dialog, _ ->
                            SyncRepository.getInstance(mActivity).delete(entity, object : BaseRepository.DeleteCallback {
                                override fun onInitial() {
                                    dialog.dismiss()
                                    ProgressDialog.getInstance(mActivity).show()
                                }

                                override fun onSuccess() {
                                    ProgressDialog.getInstance(mActivity).dismiss()
                                    Snackbar.make(mContentView, "刪除成功！", Snackbar.LENGTH_SHORT).show()
                                    onRefresh()
                                }

                                override fun onError(message: String) {
                                    ProgressDialog.getInstance(mActivity).dismiss()
                                    Snackbar.make(mContentView, message, Snackbar.LENGTH_SHORT).show()
                                }
                            })
                        }
                        setNegativeButton(getString(R.string.dialog_button_cancel)) { dialog, _ ->
                            dialog.dismiss()
                        }
                    }.create().show()
                }

                override fun onDownloadClick(entity: GoogleDriveItem) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val packageName = entity.lang.packageName
                        if (!hasFolderPermission(packageName)) {
                            val sm = mActivity.getSystemService(Context.STORAGE_SERVICE) as StorageManager
                            val intent = sm.primaryStorageVolume.createOpenDocumentTreeIntent()
                            intent.getParcelableExtra<Uri>(DocumentsContract.EXTRA_INITIAL_URI).let { uri ->
                                val scheme = String.format("%s%%3AAndroid%%2Fdata%%2F%s", uri.toString().replace("/root/", "/document/"), packageName)
                                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(scheme))
                                startActivityForResult(intent, REQUEST_CODE_FOLDER_PERMISSION)
                                Toast.makeText(mActivity, "請先給與 APP 存取 Android/data 資料夾權限。", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            downloadGameData(entity)
                        }
                    } else {
                        downloadGameData(entity)
                    }
                }
            })
        }

        mBinding.recyclerView.layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        mBinding.recyclerView.adapter = mAdapter
        mBinding.swipeRefreshLayout.setOnRefreshListener(this)

        onRefresh()
    }

    private fun initGoogleSignIn() {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE_APPDATA))
            .build().let { mSignInClient = GoogleSignIn.getClient(mActivity, it) }
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
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN)

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

    private fun onSyncJPButtonClick() {
    }

    private fun onSyncTWButtonClick() {
    }

    private fun downloadGameData(entity: GoogleDriveItem) {
        RemoteProgressDialog.getInstance(mActivity).show()
        SyncRepository.getInstance(mActivity).download(entity, object : BaseRepository.DownloadCallback {
            override fun onInitial() {
                RemoteProgressDialog.getInstance(mActivity).setTitle("檔案下載中\n${entity.name}")
                RemoteProgressDialog.getInstance(mActivity).setProgress(0)
            }

            override fun inProgress(progress: Int) {
                RemoteProgressDialog.getInstance(mActivity).setProgress(progress)
            }

            override fun onComplete(progress: Int) {
                RemoteProgressDialog.getInstance(mActivity).setProgress(progress)
            }

            override fun onUnzip() {
                RemoteProgressDialog.getInstance(mActivity).setTitle("解壓縮中\n${entity.name}")
            }

            override fun onSuccess() {
                RemoteProgressDialog.getInstance(mActivity).dismiss()
                Snackbar.make(mContentView, getString(R.string.file_download_completed), Snackbar.LENGTH_SHORT).show()
            }

            override fun onError(message: String) {
                RemoteProgressDialog.getInstance(mActivity).dismiss()
                Snackbar.make(mContentView, message, Snackbar.LENGTH_SHORT).show()
            }
        })
    }
}