package com.lcj.sb.account.switcher.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.toLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.lcj.sb.account.switcher.*
import com.lcj.sb.account.switcher.adapter.LocalSyncListAdapter
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.database.entity.FolderSync
import com.lcj.sb.account.switcher.databinding.FragmentLocalBackupBinding
import com.lcj.sb.account.switcher.repository.SyncRepository
import com.lcj.sb.account.switcher.utils.Configs
import com.lcj.sb.account.switcher.utils.FileManager
import com.lcj.sb.account.switcher.view.RemoteProgressDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

class LocalSyncFragment : BaseFragment() {
    private lateinit var mBinding: FragmentLocalBackupBinding
    private lateinit var mAdapter: LocalSyncListAdapter

    companion object {
        fun newInstance(): LocalSyncFragment {
            return LocalSyncFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentLocalBackupBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.settingsSbJRoot.visibility = View.GONE
        mBinding.settingsSbTRoot.visibility = View.GONE
        mBinding.settingsSbJRoot.setOnClickListener { showLocalSyncList(Account.Language.JP) }
        mBinding.settingsSbJSyncBtn.setOnClickListener { onSyncJPButtonClick() }
        mBinding.settingsSbTRoot.setOnClickListener { showLocalSyncList(Account.Language.TW) }
        mBinding.settingsSbTSyncBtn.setOnClickListener { onSyncTWButtonClick() }
        mBinding.typeSwitchJpButton.setOnClickListener { onTypeSwitchJPClick() }
        mBinding.typeSwitchTwButton.setOnClickListener { onTypeSwitchTWClick() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initRecyclerView()
        updateSyncView(Account.Language.JP)
        updateSyncView(Account.Language.TW)
        mBinding.typeSwitchJpButton.performClick()
    }

    override fun onResume() {
        super.onResume()
        BaseApplication.setCurrentScreen(mActivity, Configs.SCREEN_LOCAL_BACKUP, LOG_TAG)
    }

    private fun initRecyclerView() {
        mAdapter = LocalSyncListAdapter(mActivity).apply {
            setOnClickListener(object : BaseAdapter.LocalSyncListListener {
                override fun onItemClick(account: Account) {
                    Log.i(LOG_TAG, "onItemClick")
                }

                override fun onIconClick(account: Account) {
                    Log.i(LOG_TAG, "onIconClick")
                }

                override fun onDeleteClick(account: Account) {
                    Log.i(LOG_TAG, "onDeleteClick")
                }

                override fun onUploadClick(account: Account) {
                    Log.i(LOG_TAG, "onUploadClick")
                    RemoteProgressDialog.getInstance(mActivity).show()
                    SyncRepository.getInstance(mActivity).upload(account, object : BaseRepository.UploadCallback {
                        override fun onInitial(fileName: String) {
                            RemoteProgressDialog.getInstance(mActivity).setTitle("檔案上傳中\n$fileName")
                            RemoteProgressDialog.getInstance(mActivity).setFileCount(0, 0)
                            RemoteProgressDialog.getInstance(mActivity).setProgress(0)
                        }

                        override fun onUploadStarted(progress: Int) {
                            mHandler.post { RemoteProgressDialog.getInstance(mActivity).setProgress(progress) }
                        }

                        override fun inProgress(progress: Int) {
                            mHandler.post { RemoteProgressDialog.getInstance(mActivity).setProgress(progress) }
                        }

                        override fun onComplete(progress: Int) {
                            mHandler.post { RemoteProgressDialog.getInstance(mActivity).setProgress(progress) }
                        }

                        override fun onSuccess() {
                            mHandler.post {
                                RemoteProgressDialog.getInstance(mActivity).dismiss()
                                Snackbar.make(mContentView, getString(R.string.file_upload_completed), Snackbar.LENGTH_SHORT).show()
                            }
                        }

                        override fun onError(message: String) {
                            mHandler.post {
                                RemoteProgressDialog.getInstance(mActivity).dismiss()
                                Snackbar.make(mContentView, message, Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    })
                }
            })
        }
        mBinding.recyclerView.layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        mBinding.recyclerView.adapter = mAdapter
    }

    private fun updateSyncView(lang: Account.Language) {
        val type = FolderSync.Type.LOCAL
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

    private fun refresh() {

    }

    private fun showLocalSyncList(lang: Account.Language) {
//        startActivity(Intent(mActivity, LocalSyncListActivity::class.java).apply {
//            putExtra(Configs.INTENT_KEY_LANGUAGE, lang.name)
//        })
    }

    private fun defaultTypeSwitchButton() {
        mBinding.typeSwitchJpButton.isActivated = false
        mBinding.typeSwitchTwButton.isActivated = false
    }

    private fun onSyncJPButtonClick() {
        Log.i(LOG_TAG, "onSyncJPButtonClick")
        FileManager.syncBackupFolder(mActivity, Account.Language.JP) {
            updateSyncView(Account.Language.JP)
            Snackbar.make(mContentView, getString(R.string.sync_account_completed), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun onSyncTWButtonClick() {
        Log.i(LOG_TAG, "onSyncTWButtonClick")
        FileManager.syncBackupFolder(mActivity, Account.Language.TW) {
            updateSyncView(Account.Language.TW)
            Snackbar.make(mContentView, getString(R.string.sync_account_completed), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun onTypeSwitchJPClick() {
        defaultTypeSwitchButton()
        mBinding.typeSwitchJpButton.isActivated = true
        BaseDatabase.getInstance(mActivity).accountDAO()
            .liveAccounts(Account.Language.JP.ordinal)
            .toLiveData(pageSize = 20)
            .observe(this, androidx.lifecycle.Observer { mAdapter.update(it) })
    }

    private fun onTypeSwitchTWClick() {
        defaultTypeSwitchButton()
        mBinding.typeSwitchTwButton.isActivated = true
        BaseDatabase.getInstance(mActivity).accountDAO()
            .liveAccounts(Account.Language.TW.ordinal)
            .toLiveData(pageSize = 20)
            .observe(this, androidx.lifecycle.Observer { mAdapter.update(it) })
    }
}