package com.lcj.sb.account.switcher.fragment

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.lcj.sb.account.switcher.AccountInfoActivity
import com.lcj.sb.account.switcher.BaseApplication
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.adapter.AccountAdapter
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.databinding.DialogBackupAccountBinding
import com.lcj.sb.account.switcher.databinding.DialogEditAccountBinding
import com.lcj.sb.account.switcher.databinding.FragmentAccountBinding
import com.lcj.sb.account.switcher.model.AccountEditModel
import com.lcj.sb.account.switcher.utils.Configs
import com.lcj.sb.account.switcher.utils.FileManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.BufferedReader
import java.io.InputStreamReader

class AccountFragment : BaseFragment() {
    private lateinit var mBinding: FragmentAccountBinding
    private lateinit var mAdapter: AccountAdapter
    private lateinit var mGameFolderPath: String
    private lateinit var mDisplayLang: Account.Language

    companion object {
        fun newInstance(): AccountFragment {
            return AccountFragment()
        }

        fun newInstance(lang: Account.Language): AccountFragment {
            return AccountFragment().apply {
                arguments = Bundle().apply {
                    putInt(Configs.PREF_KEY_LANGUAGE, lang.ordinal)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentAccountBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDisplayLang = Account.Language.values()[arguments?.getInt(Configs.PREF_KEY_LANGUAGE)!!]

        mBinding.addFab.setOnClickListener {
            getPackageName().let {
                if (FileManager.isPackageInstalled(it, mActivity)) {
                    showBackupAccountDialog()
                } else {
                    showErrorNoInstalled(it)
                }
            }
        }

        mBinding.gameFab.setOnClickListener {
            getPackageName().let {
                if (FileManager.isPackageInstalled(it, mActivity)) {
                    startApplication(it)
                } else {
                    showErrorNoInstalled(it)
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)

        mAdapter = AccountAdapter(mActivity)
        mAdapter.setOnClickListener(object : AccountAdapter.OnClickListener {
            override fun onItemClick(holder: AccountAdapter.ViewHolder, account: Account) {
                Intent(mActivity, AccountInfoActivity::class.java).let {
                    it.putExtras(Bundle().apply {
                        putSerializable(Configs.INTENT_KEY_ACCOUNT, account)
                    })
                    startActivity(it)
                }
            }

            override fun onMoreClick(holder: AccountAdapter.ViewHolder, account: Account) {
                AlertDialog.Builder(mActivity)
                        .setTitle(account.alias)
                        .setMessage(String.format(getString(R.string.dialog_folder_path), account.folder))
                        .setNeutralButton(getString(R.string.dialog_button_backup)) { dialog, which ->
                            onBackupClick(holder, account)
                            dialog.dismiss()
                        }
                        .setNegativeButton(getString(R.string.dialog_button_edit)) { dialog, which ->
                            onEditClick(holder, account)
                            dialog.dismiss()
                        }
                        .setPositiveButton(getString(R.string.dialog_button_load)) { dialog, which ->
                            onLoadClick(holder, account)
                            dialog.dismiss()
                        }.create().show()
            }
        })

        mBinding.accountList.layoutManager = layoutManager
        mBinding.accountList.adapter = mAdapter

        BaseDatabase.getInstance(mActivity).accountDAO()
                .accounts(mDisplayLang.ordinal)
                .observe(this, Observer { mAdapter.update(it) })
    }

    override fun onResume() {
        super.onResume()
        mActivity.invalidateOptionsMenu()

        when (mDisplayLang) {
            Account.Language.JP -> {
                mGameFolderPath = String.format("%s/%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB_JP)
                BaseApplication.analytics.setCurrentScreen(mActivity, Configs.SCREEN_SB_JP, LOG_TAG)
            }
            Account.Language.TW -> {
                mGameFolderPath = String.format("%s/%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB_TW)
                BaseApplication.analytics.setCurrentScreen(mActivity, Configs.SCREEN_SB_TW, LOG_TAG)
            }
        }

        PreferenceManager.getDefaultSharedPreferences(mActivity).edit().apply {
            putBoolean(Configs.PREF_KEY_FIRST_RUN, false)
            putString(Configs.PREF_KEY_LANGUAGE, mDisplayLang.name)
            apply()
        }
    }

    private fun onEditClick(holder: AccountAdapter.ViewHolder, account: Account) {
        AccountEditModel(account.alias).let { model ->
            val binding = DialogEditAccountBinding.inflate(layoutInflater)
            binding.model = model

            AlertDialog.Builder(mActivity, R.style.CustomDialog).create().let { dialog ->
                binding.editAccountAliasEdit.setText(account.alias)

                binding.editAccountCancelBtn.setOnClickListener {
                    model.onCancelClick()
                    dialog.dismiss()
                }
                binding.editAccountEditBtn.setOnClickListener {
                    model.onEditClick(mActivity, account)
                    dialog.dismiss()
                }

                dialog.show()
                dialog.window?.apply {
                    clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                    setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                }
                dialog.setContentView(binding.root)
            }
        }
    }

    private fun onLoadClick(holder: AccountAdapter.ViewHolder, account: Account) {
        val langStr = if (account.lang == Account.Language.JP.ordinal) Configs.PREFIX_NAME_SB_JP else Configs.PREFIX_NAME_SB_TW
        val srcFolder: String = String.format("%s/%s", account.folder, "files")
        val dstFolder: String = String.format("%s/%s", Configs.PATH_APP_DATA, langStr)
        val command: String = String.format("cp -a %s %s", srcFolder, dstFolder)

        Thread {
            val process: Process = Runtime.getRuntime().exec(command)
            val buffReader = BufferedReader(InputStreamReader(process.inputStream))
            var readLine: String?

            do {
                readLine = buffReader.readLine()
            } while (readLine != null)

            buffReader.close()
            process.waitFor()

            mHandler.post {
                mBinding.gameFab.performClick()
            }
        }.start()

        setAccountSelected(account)
    }

    private fun onBackupClick(holder: AccountAdapter.ViewHolder, account: Account) {
        Thread {
            FileManager.backupFolder(mGameFolderPath, account.folder) { current, total, finished ->
                if (finished) {
                    account.let {
                        it.updateTime = System.currentTimeMillis()
                        BaseDatabase.getInstance(mActivity).accountDAO().update(it)
                    }
                }
            }
        }.start()
    }

    private fun setAccountSelected(account: Account) {
        Thread {
            account.selected = true
            BaseDatabase.getInstance(mActivity).accountDAO().deselectAll(account.lang)
            BaseDatabase.getInstance(mActivity).accountDAO().update(account)
        }.start()
    }

    private fun showBackupAccountDialog() {
        AlertDialog.Builder(mActivity, R.style.CustomDialog).create().let { dialog ->
            val binding = DialogBackupAccountBinding.inflate(layoutInflater)

            binding.backupCancelBtn.setOnClickListener {
                dialog.dismiss()
            }
            binding.backupSubmitBtn.setOnClickListener {
                val currentTime = System.currentTimeMillis()
                val destPath = when (mDisplayLang) {
                    Account.Language.JP -> String.format("%s/%s.%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB_JP, currentTime)
                    Account.Language.TW -> String.format("%s/%s.%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB_TW, currentTime)
                }

                it.isEnabled = false
                Observable.just(FileManager.isFolderExists(mGameFolderPath))
                        .subscribeOn(Schedulers.io())
                        .doOnNext { exists ->
                            val account = Account(
                                    alias = binding.backupInputEt.text.toString(),
                                    folder = destPath,
                                    lang = mDisplayLang.ordinal,
                                    createTime = currentTime,
                                    updateTime = currentTime
                            )
                            BaseDatabase.getInstance(mActivity).accountDAO().insert(account)

                            if (exists) FileManager.backupFolder(mGameFolderPath, destPath) { current, total, finished ->
                                binding.backupProgressBar.max = total
                                binding.backupProgressBar.progress = current
                            }
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { dialog.dismiss() }
            }

            dialog.show()
            dialog.window?.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
            dialog.setContentView(binding.root)
        }
    }

    private fun showErrorNoInstalled(packageName: String) {
        AlertDialog.Builder(mActivity)
                .setMessage(R.string.no_install_game)
                .setPositiveButton(R.string.google_play_text) { dialog, which ->
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                        setPackage("com.android.vending")
                    }
                    startActivity(intent)
                }
                .setNegativeButton(R.string.dialog_button_cancel) { dialog, which ->
                    dialog.dismiss()
                }.create().show()
    }
}