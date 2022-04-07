package com.lcj.sb.account.switcher.fragment

import android.app.Activity
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
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.paging.toLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.lcj.sb.account.switcher.*
import com.lcj.sb.account.switcher.adapter.AccountAdapter
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.databinding.FragmentAccountBinding
import com.lcj.sb.account.switcher.repository.AccountRepository
import com.lcj.sb.account.switcher.utils.Configs
import com.lcj.sb.account.switcher.utils.FileManager

class AccountFragment : BaseFragment(), View.OnClickListener, BaseAdapter.AccountListListener {
    private lateinit var mBinding: FragmentAccountBinding
    private lateinit var mGameFolderPath: String
    private lateinit var mDisplayLang: Account.Language

    companion object {
        const val REQUEST_CODE_FOLDER_PERMISSION = 1001

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

        mBinding.createButton.setOnClickListener(this)
        mBinding.gameStartButton.setOnClickListener(this)
        mBinding.addFab.setOnClickListener(this)
        mBinding.gameFab.setOnClickListener(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val adapter = AccountAdapter(mActivity).apply { setOnClickListener(this@AccountFragment) }
        mBinding.accountList.layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        mBinding.accountList.adapter = adapter

        BaseDatabase.getInstance(mActivity).accountDAO()
            .liveAccounts(mDisplayLang.ordinal, false)
            .toLiveData(pageSize = 20)
            .observe(requireActivity()) { adapter.update(it) }
    }

    override fun onResume() {
        super.onResume()
        mActivity.invalidateOptionsMenu()
        BaseApplication.setCurrentScreen(mActivity, mDisplayLang.screenName, LOG_TAG)
        requireActivity().getPreferences(Context.MODE_PRIVATE).edit().apply {
            putBoolean(Configs.PREF_KEY_FIRST_RUN, false)
            putString(Configs.PREF_KEY_LANGUAGE, mDisplayLang.name)
            apply()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FOLDER_PERMISSION) {
            if (resultCode == Activity.RESULT_OK) {
                val contentResolver = mActivity.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(data?.data!!, takeFlags)
                createAccountFolder()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.create_button,
            R.id.add_fab -> mDisplayLang.packageName.let {
                if (FileManager.isPackageInstalled(it, mActivity)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (!hasFolderPermission()) {
                            val sm = mActivity.getSystemService(Context.STORAGE_SERVICE) as StorageManager
                            val intent = sm.primaryStorageVolume.createOpenDocumentTreeIntent()
                            var uri = intent.getParcelableExtra<Uri>(DocumentsContract.EXTRA_INITIAL_URI)
                            var scheme = uri.toString().replace("/root/", "/document/")

                            scheme += "%3A" + "Android%2Fdata"
                            uri = Uri.parse(scheme)
                            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
                            startActivityForResult(intent, REQUEST_CODE_FOLDER_PERMISSION)
                            Toast.makeText(mActivity, "請先給與 APP 存取 Android/data 資料夾權限。", Toast.LENGTH_LONG).show()
                        } else {
                            createAccountFolder()
                        }
                    } else {
                        createAccountFolder()
                    }
                } else {
                    showErrorNoInstalled(it)
                }
            }
            R.id.game_start_button,
            R.id.game_fab -> mDisplayLang.packageName.let {
                if (FileManager.isPackageInstalled(it, mActivity)) {
                    startApplication(it)
                } else {
                    showErrorNoInstalled(it)
                }
            }
        }
    }

    override fun onItemClick(account: Account) {
        AccountRepository.getInstance(mActivity).onItemClick(account)
    }

    override fun onDeleteClick(account: Account) {
        AccountRepository.getInstance(mActivity).onDeleteClick(account, object : BaseRepository.DeleteAccountCallback {
            override fun onSuccess() {
                mHandler.post {
                    mBinding.addFab.animate().translationY(0f).setInterpolator(LinearInterpolator()).start()
                    mBinding.gameFab.animate().translationY(0f).setInterpolator(LinearInterpolator()).start()
                    Snackbar.make(mContentView, getString(R.string.dialog_message_delete_success), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onError(message: String) {
                mHandler.post { Snackbar.make(mContentView, message, Snackbar.LENGTH_SHORT).show() }
            }

            override fun onNotExists() {
                mHandler.post {
                    Snackbar.make(mContentView, getString(R.string.dialog_message_delete_folder_not_exists), Snackbar.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onEditAliasClick(account: Account) {
        AccountRepository.getInstance(mActivity).onEditAliasClick(account)
    }

    override fun onSaveClick(account: Account) {
        AccountRepository.getInstance(mActivity).onSaveClick(mDisplayLang, account, object : BaseRepository.SaveAccountCallback {
            override fun onSuccess() {
                mHandler.post { Snackbar.make(mContentView, "備份成功", Snackbar.LENGTH_SHORT).show() }
            }

            override fun onError(message: String) {
                mHandler.post { Snackbar.make(mContentView, message, Snackbar.LENGTH_SHORT).show() }
            }
        })
    }

    override fun onLoadGameClick(account: Account) {
        AccountRepository.getInstance(mActivity).onLoadGameClick(account, object : BaseRepository.LoadAccountCallback {
            override fun onSuccess() {
                mHandler.post { mBinding.gameFab.performClick() }
            }

            override fun onError(message: String) {
                mHandler.post { Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show() }
            }
        })
    }

    override fun onMoreClick(account: Account) {
        AccountRepository.getInstance(mActivity).onMoreClick(account)
    }

    private fun showErrorNoInstalled(packageName: String) {
        AccountRepository.getInstance(mActivity).showErrorNoInstalled(packageName)
    }

    private fun hasFolderPermission(): Boolean {
        var result = false
        for (permission in mActivity.contentResolver.persistedUriPermissions) {
            if (permission.uri == Uri.parse(Configs.URI_ANDROID_DATA)) {
                result = true
                break
            }
        }
        return result
    }

    private fun createAccountFolder() {
        AccountRepository.getInstance(mActivity).showCreateAccountDialog(mDisplayLang, object : BaseRepository.CreateAccountCallback {
            override fun onSuccess() {
                mHandler.post { Toast.makeText(mActivity, "Success", Toast.LENGTH_SHORT).show() }
            }

            override fun onError(message: String) {
                mHandler.post { Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show() }
            }

            override fun onNotExists() {
                mHandler.post { Snackbar.make(mContentView, getString(R.string.game_folder_not_exists), Snackbar.LENGTH_SHORT).show() }
            }
        })
    }
}