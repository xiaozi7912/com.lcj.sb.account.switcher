package com.lcj.sb.account.switcher.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.lcj.sb.account.switcher.adapter.AccountAdapter
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.databinding.DialogBackupAccountBinding
import com.lcj.sb.account.switcher.databinding.FragmentAccountBinding
import com.lcj.sb.account.switcher.utils.Configs
import com.lcj.sb.account.switcher.utils.FileManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AccountFragment : BaseFragment() {
    private lateinit var mBinding: FragmentAccountBinding
    private lateinit var mAdapter: AccountAdapter

    companion object {
        fun newInstance(): AccountFragment {
            return AccountFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentAccountBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.addFab.setOnClickListener {
            showBackupAccountDialog()
//            Snackbar.make(view, "Hello Snackbar", Snackbar.LENGTH_LONG).show();
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(LOG_TAG, "onActivityCreated ${mCurrentLang.name}")
        val layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)

        mAdapter = AccountAdapter(mActivity, ArrayList())
        mBinding.accountList.layoutManager = layoutManager
        mBinding.accountList.adapter = mAdapter
    }

    override fun onResume() {
        super.onResume()
        BaseDatabase.getInstance(mActivity).getAccountDao()
                .loadAccounts(mCurrentLang.ordinal)
                .observe(this, Observer<List<Account>> { t -> mAdapter.update(t!!) })
    }

    private fun showBackupAccountDialog() {
        val builder = AlertDialog.Builder(mActivity)
        val binding = DialogBackupAccountBinding.inflate(layoutInflater)
        builder.setView(binding.root)
        val alertDialog = builder.create()
        alertDialog.show()
        binding.backupCancelBtn.setOnClickListener { Log.i(LOG_TAG, "backupCancelBtn") }
        binding.backupSubmitBtn.setOnClickListener {
            Log.i(LOG_TAG, "backupSubmitBtn")
            val currentTime = System.currentTimeMillis()

            val resPath = when (mCurrentLang) {
                Account.Language.JP -> String.format("%s/%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB_JP)
                Account.Language.TW -> String.format("%s/%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB_TW)
            }
            val destPath = when (mCurrentLang) {
                Account.Language.JP -> String.format("%s/%s.%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB_JP, currentTime)
                Account.Language.TW -> String.format("%s/%s.%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB_TW, currentTime)
            }

            Log.v(LOG_TAG, "folderPath $resPath")
            Log.v(LOG_TAG, "isFolderExists ${FileManager.isFolderExists(resPath)}")
            Observable.just(FileManager.isFolderExists(resPath))
                    .subscribeOn(Schedulers.io())
                    .doOnNext { exists ->
                        val account = Account(
                                alias = binding.backupInputEt.text.toString(),
                                folder = destPath,
                                lang = mCurrentLang.ordinal,
                                createTime = currentTime,
                                updateTime = currentTime
                        )
                        BaseDatabase.getInstance(mActivity)
                                .getAccountDao()
                                .insertAccount(account)

                        if (exists) FileManager.backupFolder(resPath, destPath)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { alertDialog.dismiss() }
        }
    }
}