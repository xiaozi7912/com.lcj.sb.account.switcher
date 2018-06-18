package com.lcj.sb.account.switcher

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.lcj.sb.account.switcher.adapter.AccountListAdapter
import com.lcj.sb.account.switcher.model.AccountModel
import java.io.File
import java.io.FileFilter
import java.util.*

class MainActivity : BaseActivity() {
    var mAccountListView: RecyclerView? = null

    var mDataList: ArrayList<AccountModel>? = null
    var mAccountListAdapter: AccountListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        checkSBFolderExists()
        initAccountList()
    }

    override fun onResume() {
        super.onResume()
        Log.i(LOG_TAG, "onResume")
    }

    override fun initView() {
        super.initView()
        mAccountListView = findViewById(R.id.main_account_list)
    }

    fun checkSBFolderExists() {
        Log.i(LOG_TAG, "checkSBFolderExists")
        Log.v(LOG_TAG, "checkSBFolderExists PATH_EXTERNAL_STORAGE : " + PATH_EXTERNAL_STORAGE)
        Log.v(LOG_TAG, "checkSBFolderExists PATH_APP_DATA : " + PATH_APP_DATA)
        var dirAppData: File = File(PATH_APP_DATA)
        var dirSB: File = File(String.format("%s/%s", PATH_APP_DATA, PREFIX_NAME_SB))
        var fileFilter: FileFilter = FileFilter { file ->
            var regex: Regex = Regex(String.format("%s\\.\\w", PREFIX_NAME_SB))
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

        var layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        mAccountListView?.layoutManager = layoutManager
        mAccountListView?.adapter = mAccountListAdapter
    }
}
