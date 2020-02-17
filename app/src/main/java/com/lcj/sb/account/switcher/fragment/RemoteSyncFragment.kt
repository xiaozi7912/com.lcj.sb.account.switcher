package com.lcj.sb.account.switcher.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.material.snackbar.Snackbar
import com.google.api.services.drive.DriveScopes
import com.lcj.sb.account.switcher.*
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
import java.util.*

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
            Configs.REQUEST_CODE_GOOGLE_SIGN_IN -> handleSignInResult(data)
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
                if (dataList.isNotEmpty()) {
                    mAdapter.update(dataList)
                } else {
                    mBinding.settingsNoFileText.visibility = View.VISIBLE
                }
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
                    Log.i(LOG_TAG, "onItemClick")
                }

                override fun onDeleteClick(entity: GoogleDriveItem) {
                    Log.i(LOG_TAG, "onDeleteClick")
                    AlertDialog.Builder(mActivity).apply {
                        setTitle("刪除雲端備份")
                        setMessage("確定要刪除Google Drive上的備份嗎？")
                        setPositiveButton(getString(R.string.dialog_button_confirmed)) { dialog, which ->
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
                        setNegativeButton(getString(R.string.dialog_button_cancel)) { dialog, which ->
                            dialog.dismiss()
                        }
                    }.create().show()
                }

                override fun onDownloadClick(entity: GoogleDriveItem) {
                    Log.i(LOG_TAG, "onDownloadClick")
                    RemoteProgressDialog.getInstance(mActivity).show()
                    SyncRepository.getInstance(mActivity).download(entity, object : BaseRepository.DownloadCallback {
                        override fun onInitial() {
                            RemoteProgressDialog.getInstance(activity).setTitle("下載中：${entity.name}")
                            RemoteProgressDialog.getInstance(activity).setProgress(0)
                        }

                        override fun inProgress(progress: Int) {
                            RemoteProgressDialog.getInstance(activity).setProgress(progress)
                        }

                        override fun onComplete(progress: Int) {
                            RemoteProgressDialog.getInstance(activity).setProgress(progress)
                        }

                        override fun onUnzip() {
                            RemoteProgressDialog.getInstance(activity).setTitle("解壓縮中：${entity.name}")
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
}