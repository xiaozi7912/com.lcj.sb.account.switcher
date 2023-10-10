package com.lcj.sb.account.switcher.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.lcj.sb.account.switcher.BaseActivity
import com.lcj.sb.account.switcher.BuildConfig
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.databinding.ActivityMainBinding
import com.lcj.sb.account.switcher.fragment.AccountFragment
import com.lcj.sb.account.switcher.fragment.AccountsFragment
import com.lcj.sb.account.switcher.fragment.SettingsFragment
import com.lcj.sb.account.switcher.fragment.SyncManagementFragment
import com.lcj.sb.account.switcher.fragment.monster.MonsterFragment
import com.lcj.sb.account.switcher.utils.Configs
import com.lcj.sb.account.switcher.view.BottomMenuItemView
import com.lcj.sb.account.switcher.view.DrawerItemView

class MainActivity : BaseActivity() {
    private lateinit var mBinding: ActivityMainBinding

    private val mRequestManageExternalStorageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                getFCMInstanceId()
                initView()
                checkNewFeature()
            }
        }
    }

    private val mRequestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val totalCount = permissions.size
        var grantedCount = 0

        for (entry in permissions.entries) {
            if (entry.value) grantedCount++
        }
        if (totalCount == grantedCount) {
            getFCMInstanceId()
            initView()
            checkNewFeature()
        }
    }

    private var mSelectedFunctionId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setSupportActionBar(mBinding.mainToolBar)
        initRemoteConfig()
        checkPermissions()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!menu.hasVisibleItems()) menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        return when (mSelectedFunctionId) {
            R.id.main_drawer_item_accounts -> true
            R.id.main_drawer_item_settings -> false
            else -> super.onPrepareOptionsMenu(menu)
        }
    }

    override fun initView() {
        ActionBarDrawerToggle(this, mBinding.mainDrawerLayout, mBinding.mainToolBar, R.string.app_name, R.string.app_name).let {
            mBinding.mainDrawerLayout.addDrawerListener(it)
            it.syncState()
        }

        mBinding.mainDrawerItemAccounts.setDownloadAPKButtonVisibility(false)
        mBinding.mainDrawerItemSyncManagement.setDownloadAPKButtonVisibility(false)
        mBinding.mainDrawerItemSettings.setDownloadAPKButtonVisibility(false)
        mBinding.mainDrawerVersionTv.text = BuildConfig.VERSION_NAME

        mBinding.menuItemMonster.visibility = View.GONE

        mBinding.mainToolBar.setOnMenuItemClickListener { view ->
            getPreferences(Context.MODE_PRIVATE).let {
                val lang = Account.Language.valueOf(it.getString(Configs.PREF_KEY_LANGUAGE, "JP")!!)

                when (view.itemId) {
                    R.id.toolbar_menu_info -> when (lang) {
                        Account.Language.JP -> startWebSite(Configs.URL_WEB_SITE_JP)
                        Account.Language.TW -> startWebSite(Configs.URL_WEB_SITE_TW)
                    }

                    R.id.toolbar_menu_download -> when (lang) {
                        Account.Language.JP -> {
                            startActivity(Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse(Configs.URL_APK_JP)
                            })
                        }

                        Account.Language.TW -> {
                        }
                    }
                }
            }
            false
        }
        mBinding.mainDrawerItemSbJ.setDownloadAPKButtonClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(Configs.URL_APK_JP)
            })
        }

        mBinding.mainDrawerItemSbJ.setOnClickListener {
            (it as DrawerItemView)

            it.setImageRes(R.drawable.ic_launcher_jp_p)
            mBinding.mainDrawerItemSbT.setImageRes(R.drawable.ic_launcher_tw_n)
            mBinding.mainDrawerItemSettings.setImageAlpha(0.5f)
            onDrawerItemSBClick(it.getTitle(), Account.Language.JP)
        }
        mBinding.mainDrawerItemSbT.setOnClickListener {
            (it as DrawerItemView)

            it.setImageRes(R.drawable.ic_launcher_tw_p)
            mBinding.mainDrawerItemSbJ.setImageRes(R.drawable.ic_launcher_jp_n)
            mBinding.mainDrawerItemSettings.setImageAlpha(0.5f)
            onDrawerItemSBClick(it.getTitle(), Account.Language.TW)
        }
        mBinding.mainDrawerItemAccounts.setOnClickListener {
            (it as DrawerItemView)

            it.setImageAlpha(1.0f)
            mSelectedFunctionId = it.id
            mBinding.mainDrawerItemSbJ.setImageRes(R.drawable.ic_launcher_jp_n)
            mBinding.mainDrawerItemSbT.setImageRes(R.drawable.ic_launcher_tw_n)
            mBinding.mainDrawerItemSyncManagement.setImageAlpha(0.5f)
            mBinding.mainDrawerItemSettings.setImageAlpha(0.5f)
            showFragment(it.getTitle(), AccountsFragment.newInstance())
        }
        mBinding.mainDrawerItemSyncManagement.setOnClickListener {
            (it as DrawerItemView)

            it.setImageAlpha(1.0f)
            mSelectedFunctionId = it.id
            mBinding.mainDrawerItemSbJ.setImageRes(R.drawable.ic_launcher_jp_n)
            mBinding.mainDrawerItemSbT.setImageRes(R.drawable.ic_launcher_tw_n)
            mBinding.mainDrawerItemAccounts.setImageAlpha(0.5f)
            mBinding.mainDrawerItemSettings.setImageAlpha(0.5f)
            showFragment(it.getTitle(), SyncManagementFragment.newInstance())
        }
        mBinding.mainDrawerItemSettings.setOnClickListener {
            (it as DrawerItemView)

            it.setImageAlpha(1.0f)
            mSelectedFunctionId = it.id
            mBinding.mainDrawerItemSbJ.setImageRes(R.drawable.ic_launcher_jp_n)
            mBinding.mainDrawerItemSbT.setImageRes(R.drawable.ic_launcher_tw_n)
            mBinding.mainDrawerItemAccounts.setImageAlpha(0.5f)
            mBinding.mainDrawerItemSyncManagement.setImageAlpha(0.5f)
            showFragment(it.getTitle(), SettingsFragment.newInstance())
        }

        mBinding.menuItemMonster.setOnClickListener {
            (it as BottomMenuItemView)

            it.isActivated = true
            mBinding.menuItemAccount.isActivated = false
            mBinding.menuItemSync.isActivated = false
            mBinding.menuItemSettings.isActivated = false
            showFragment(it.getTitle(), MonsterFragment.newInstance())
        }
        mBinding.menuItemAccount.setOnClickListener {
            (it as BottomMenuItemView)

            it.isActivated = true
            mBinding.menuItemMonster.isActivated = false
            mBinding.menuItemSync.isActivated = false
            mBinding.menuItemSettings.isActivated = false
            showFragment(it.getTitle(), AccountsFragment.newInstance())
        }
        mBinding.menuItemSync.setOnClickListener {
            (it as BottomMenuItemView)

            it.isActivated = true
            mBinding.menuItemMonster.isActivated = false
            mBinding.menuItemAccount.isActivated = false
            mBinding.menuItemSettings.isActivated = false
            showFragment(it.getTitle(), SyncManagementFragment.newInstance())
        }
        mBinding.menuItemSettings.setOnClickListener {
            (it as BottomMenuItemView)

            it.isActivated = true
            mBinding.menuItemMonster.isActivated = false
            mBinding.menuItemAccount.isActivated = false
            mBinding.menuItemSync.isActivated = false
            showFragment(it.getTitle(), SettingsFragment.newInstance())
        }

        mBinding.menuItemAccount.performClick()
    }

    override fun initAdMob() {
        val adRequest = AdRequest.Builder().build()
        mBinding.adView.loadAd(adRequest)
    }

    private fun initRemoteConfig() {
        val configSetting = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(1800)
            .build()
        mRemoteConfig.setConfigSettingsAsync(configSetting)
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getFCMInstanceId()
            initView()
            checkNewFeature()
        } else {
            mRequestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }
    }

    private fun getFCMInstanceId() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.i(LOG_TAG, "FirebaseMessaging addOnSuccessListener")
                Log.i(LOG_TAG, "FirebaseMessaging addOnSuccessListener token : $token")
            }
            .addOnFailureListener { e ->
                Log.i(LOG_TAG, "FirebaseMessaging addOnFailureListener")
                e.printStackTrace()
            }
            .addOnCompleteListener { task ->
                Log.i(LOG_TAG, "FirebaseMessaging addOnCompleteListener")
                try {
                    Log.i(LOG_TAG, "FirebaseMessaging addOnCompleteListener task.result : ${task.result}")
                } catch (e: RuntimeExecutionException) {
                    e.printStackTrace()
                }
            }
        FirebaseInstallations.getInstance().id
            .addOnSuccessListener { id ->
                Log.i(LOG_TAG, "FirebaseInstallations addOnSuccessListener")
                Log.i(LOG_TAG, "FirebaseInstallations addOnSuccessListener id : $id")
            }.addOnFailureListener { e ->
                Log.i(LOG_TAG, "FirebaseInstallations addOnFailureListener")
                e.printStackTrace()
            }.addOnCompleteListener { task ->
                Log.i(LOG_TAG, "FirebaseInstallations addOnCompleteListener")
                Log.i(LOG_TAG, "FirebaseInstallations addOnCompleteListener task.result : ${task.result}")
            }
    }

    private fun checkNewFeature() {
        getPreferences(Context.MODE_PRIVATE).apply {
            val featureVersion = getInt(Configs.PREF_KEY_NEW_FEATURE, 0)
            if (featureVersion < Configs.VERSION_NEW_FEATURE) {
                showNewFeatureDialog()
            }
        }
    }

    private fun showNewFeatureDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("有新功能！")
            setMessage("新功能在「本機同步」中\n可刪除本機已同步的備份檔案\n是否要看操作教學影片")
            setPositiveButton("觀看影片") { _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://youtu.be/HybpZKYeKwE")
                })
            }
            setNegativeButton(getString(R.string.dialog_button_cancel)) { dialog, _ -> dialog.dismiss() }
            getPreferences(Context.MODE_PRIVATE).edit().apply {
                putInt(Configs.PREF_KEY_NEW_FEATURE, Configs.VERSION_NEW_FEATURE)
                apply()
            }
        }.create().show()
    }

    private fun onDrawerItemSBClick(title: String, lang: Account.Language) {
        supportActionBar!!.title = title

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.main_frame_layout, AccountFragment.newInstance())
        ft.commit()

        mBinding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        getPreferences(Context.MODE_PRIVATE).edit().apply {
            putBoolean(Configs.PREF_KEY_FIRST_RUN, false)
            putString(Configs.PREF_KEY_LANGUAGE, lang.name)
            apply()
        }
        mCurrentLang = lang
    }

    private fun showFragment(title: String, fragment: Fragment) {
        supportActionBar!!.title = title

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.main_frame_layout, fragment)
        ft.commit()

        mBinding.mainDrawerLayout.closeDrawer(GravityCompat.START)
    }
}
