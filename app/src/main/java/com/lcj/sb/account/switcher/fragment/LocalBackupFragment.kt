package com.lcj.sb.account.switcher.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.lcj.sb.account.switcher.BaseApplication
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.database.entity.FolderSync
import com.lcj.sb.account.switcher.databinding.FragmentLocalBackupBinding
import com.lcj.sb.account.switcher.utils.Configs
import com.lcj.sb.account.switcher.utils.FileManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat

class LocalBackupFragment : BaseFragment() {
    private lateinit var mBinding: FragmentLocalBackupBinding

    companion object {
        fun newInstance(): LocalBackupFragment {
            return LocalBackupFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentLocalBackupBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.settingsSbJSyncBtn.setOnClickListener { onSyncJPButtonClick() }
        mBinding.settingsSbTSyncBtn.setOnClickListener { onSyncTWButtonClick() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        BaseApplication.analytics.setCurrentScreen(mActivity, Configs.SCREEN_LOCAL_BACKUP, LOG_TAG)

        updateSyncJPView()
        updateSyncTWView()
    }

    private fun updateSyncJPView() {
        Log.i(LOG_TAG, "updateSyncJPView")
        val type = FolderSync.Type.LOCAL.ordinal
        val lang = Account.Language.JP.ordinal

        BaseDatabase.getInstance(mActivity).folderSyncDAO().folderSync(type, lang)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { entity ->
                    entity?.let {
                        mBinding.settingsSbJSyncTimeTv.text = SimpleDateFormat("yyyy-mm-dd HH:mm:ss").format(it.updateTime)
                    }
                }.subscribe()
    }

    private fun updateSyncTWView() {
        Log.i(LOG_TAG, "updateSyncTWView")
        val type = FolderSync.Type.LOCAL.ordinal
        val lang = Account.Language.TW.ordinal

        BaseDatabase.getInstance(mActivity).folderSyncDAO().folderSync(type, lang)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { entity ->
                    entity?.let {
                        mBinding.settingsSbTSyncTimeTv.text = SimpleDateFormat("yyyy-mm-dd HH:mm:ss").format(it.updateTime)
                    }
                }.subscribe()
    }

    private fun onSyncJPButtonClick() {
        Log.i(LOG_TAG, "onSyncJPButtonClick")
        FileManager.syncBackupFolder(mActivity, Account.Language.JP) {
            updateSyncJPView()
            Toast.makeText(mActivity, "同步成功！", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onSyncTWButtonClick() {
        Log.i(LOG_TAG, "onSyncTWButtonClick")
        FileManager.syncBackupFolder(mActivity, Account.Language.TW) {
            updateSyncTWView()
            Toast.makeText(mActivity, "同步成功！", Toast.LENGTH_SHORT).show()
        }
    }
}