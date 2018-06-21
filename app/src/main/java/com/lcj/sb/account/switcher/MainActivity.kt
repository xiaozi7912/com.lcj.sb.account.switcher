package com.lcj.sb.account.switcher

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.lcj.sb.account.switcher.adapter.AccountListAdapter
import com.lcj.sb.account.switcher.model.AccountModel
import com.lcj.sb.account.switcher.utils.AccountInfoManager
import com.lcj.sb.account.switcher.utils.Configs
import java.io.File
import java.io.FileFilter
import java.util.*

class MainActivity : BaseActivity() {
    var mCurrentAccountTextView: TextView? = null
    var mAccountListView: RecyclerView? = null
    var mActionStatusTextView: TextView? = null

    var mDataList: ArrayList<AccountModel>? = null
    var mAccountListAdapter: AccountListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        checkSBFolderExists()
        loadCurrentAccount()
        initAccountList()
    }

    override fun onResume() {
        super.onResume()
        Log.i(LOG_TAG, "onResume")
    }

    override fun initView() {
        super.initView()
        mCurrentAccountTextView = findViewById(R.id.main_current_account_text)
        mAccountListView = findViewById(R.id.main_account_list)
        mActionStatusTextView = findViewById(R.id.main_action_status_text)

        mCurrentAccountTextView?.text = ""
        mActionStatusTextView?.text = ""
    }

    fun loadCurrentAccount() {
        Log.i(LOG_TAG, "loadCurrentAccount")
        AccountInfoManager.getInstance().readAccountInfoFile()
        mCurrentAccountTextView?.text = AccountInfoManager.getInstance().mCurrentAccount
    }

    fun checkSBFolderExists() {
        Log.i(LOG_TAG, "checkSBFolderExists")
        Log.v(LOG_TAG, "checkSBFolderExists Configs.PATH_EXTERNAL_STORAGE : " + Configs.PATH_EXTERNAL_STORAGE)
        Log.v(LOG_TAG, "checkSBFolderExists Configs.PATH_APP_DATA : " + Configs.PATH_APP_DATA)
        var dirAppData: File = File(Configs.PATH_APP_DATA)
        var dirSB: File = File(String.format("%s/%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB))
        var fileFilter: FileFilter = FileFilter { file ->
            var regex: Regex = Regex(String.format("%s\\.\\w", Configs.PREFIX_NAME_SB))
            file.name.contains(regex)
        }
        var folders: Array<File> = dirAppData.listFiles(fileFilter)

        if (!dirSB.exists()) dirSB.mkdir()

        Arrays.sort(folders)
        mDataList = ArrayList<AccountModel>()
        for (folder in folders) {
            Log.v(LOG_TAG, "checkSBFolderExists folder.name : " + folder.name)
            Log.v(LOG_TAG, "checkSBFolderExists folder.absolutePath : " + folder.absolutePath)
            mDataList?.add(AccountModel(folder.name, folder.absolutePath))
        }
    }

    fun initAccountList() {
        Log.i(LOG_TAG, "initAccountList")
        mAccountListAdapter = AccountListAdapter(mActivity, mDataList!!)
        mAccountListAdapter?.setCallback(object : AccountListAdapter.Callback {
            override fun onLoadSuccess(account: String?) {
                Log.i(LOG_TAG, "onLoadSuccess")
                AccountInfoManager.getInstance().mCurrentAccount = account
                AccountInfoManager.getInstance().writeAccountInfoFile()
                Log.v(LOG_TAG, "onLoadSuccess account : " + account)
                mHandler.post {
                    mCurrentAccountTextView?.text = account
                    Toast.makeText(mActivity, "Load Success", Toast.LENGTH_SHORT).show()
                }

                var intent = packageManager.getLaunchIntentForPackage(Configs.PREFIX_NAME_SB)
                startActivity(intent)
            }

            override fun onSaveStart() {
                Log.i(LOG_TAG, "onSaveStart")
                mDataList?.forEach { item -> item.disable = true }
                mHandler.post {
                    mActionStatusTextView?.text = "File Saving..."
                    mAccountListAdapter?.notifyDataSetChanged()
                }
            }

            override fun onSaveSuccess() {
                mDataList?.forEach { item -> item.disable = false }
                mHandler.post {
                    mActionStatusTextView?.text = ""
                    mAccountListAdapter?.notifyDataSetChanged()
                    Toast.makeText(mActivity, "Save Success", Toast.LENGTH_SHORT).show()
                }
            }
        })

        var layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        mAccountListView?.layoutManager = layoutManager
        mAccountListView?.adapter = mAccountListAdapter
    }
}
