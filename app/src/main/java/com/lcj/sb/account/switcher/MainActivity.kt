package com.lcj.sb.account.switcher

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.databinding.ActivityMainBinding
import com.lcj.sb.account.switcher.utils.AccountInfoManager
import com.lcj.sb.account.switcher.view.DrawerItemView
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : BaseActivity() {
    private lateinit var mBinding: ActivityMainBinding

    companion object {
        const val REQUEST_CODE_WRITE_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(mActivity, R.layout.activity_main)
        setSupportActionBar(mBinding.mainToolBar)
        requestPermissions()

        val account = Account(
                alias = "111",
                folder = "qqqq",
                lang = Account.Language.JP.ordinal,
                createTime = System.currentTimeMillis(),
                updateTime = System.currentTimeMillis()
        )

        Thread {
            BaseDatabase
                    .getInstance(mActivity)
                    .getAccountDao()
                    .insertAccount(account)
        }.start()
    }

    override fun onStop() {
        super.onStop()
        AccountInfoManager.getInstance().writeAccountInfoFile()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun initView() {
        super.initView()
        val toggle = ActionBarDrawerToggle(mActivity, mBinding.mainDrawerLayout, mBinding.mainToolBar, R.string.app_name, R.string.app_name)
        mBinding.mainDrawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        mBinding.mainDrawerItemSbJ.setOnClickListener {
            val drawerItem = it as DrawerItemView
            supportActionBar!!.title = drawerItem.getTitle()
            mBinding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        }
        mBinding.mainDrawerItemSbT.setOnClickListener {
            val drawerItem = it as DrawerItemView
            supportActionBar!!.title = drawerItem.getTitle()
            mBinding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    @AfterPermissionGranted(REQUEST_CODE_WRITE_PERMISSION)
    fun requestPermissions() {
        val perms = arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (EasyPermissions.hasPermissions(mActivity, *perms)) {
            AccountInfoManager.getInstance().readAccountInfoFile()
        } else {
            EasyPermissions.requestPermissions(mActivity, "Request Permission", REQUEST_CODE_WRITE_PERMISSION, *perms)
        }
    }
}
