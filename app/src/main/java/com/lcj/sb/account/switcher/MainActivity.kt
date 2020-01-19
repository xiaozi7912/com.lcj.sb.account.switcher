package com.lcj.sb.account.switcher

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.AdRequest
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.databinding.ActivityMainBinding
import com.lcj.sb.account.switcher.fragment.AccountFragment
import com.lcj.sb.account.switcher.model.RemoteConfigModel
import com.lcj.sb.account.switcher.utils.Configs
import com.lcj.sb.account.switcher.utils.PackageUtils
import com.lcj.sb.account.switcher.view.DrawerItemView
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.FileFilter
import java.util.regex.Pattern


class MainActivity : BaseActivity() {
    private lateinit var mBinding: ActivityMainBinding

    companion object {
        const val REQUEST_CODE_WRITE_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(mActivity, R.layout.activity_main)

        initRemoteConfig()
        setSupportActionBar(mBinding.mainToolBar)
        requestPermissions()
        reloadAd()
    }

    override fun onResume() {
        super.onResume()
        fetchRemoteConfig()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun initView() {
        val toggle = ActionBarDrawerToggle(mActivity, mBinding.mainDrawerLayout, mBinding.mainToolBar, R.string.app_name, R.string.app_name)
        mBinding.mainDrawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        mBinding.mainDrawerVersionTv.text = BuildConfig.VERSION_NAME

        mBinding.mainToolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.toolbar_menu_info -> when (mCurrentLang) {
                    Account.Language.JP -> startWebSite(Configs.URL_WEB_SITE_JP)
                    Account.Language.TW -> startWebSite(Configs.URL_WEB_SITE_TW)
                }
            }
            false
        }

        mBinding.mainDrawerItemSbJ.setDownloadAPKButtonClickListener(View.OnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(Configs.URL_APK_JP)
            })
        })
        mBinding.mainDrawerItemSbJ.setOnClickListener {
            val currentItem = it as DrawerItemView
            val anotherItem = mBinding.mainDrawerItemSbT

            currentItem.setImageRes(R.drawable.ic_launcher_jp_p)
            anotherItem.setImageRes(R.drawable.ic_launcher_tw_n)
            onDrawerItemSBClick(currentItem.getTitle(), Account.Language.JP)
        }
        mBinding.mainDrawerItemSbT.setOnClickListener {
            val currentItem = it as DrawerItemView
            val anotherItem = mBinding.mainDrawerItemSbJ

            currentItem.setImageRes(R.drawable.ic_launcher_tw_p)
            anotherItem.setImageRes(R.drawable.ic_launcher_jp_n)
            onDrawerItemSBClick(currentItem.getTitle(), Account.Language.TW)
        }

        if (!mFirstRun) selectLanguage()
    }

    override fun reloadAd() {
        val adRequest = AdRequest.Builder().build()
        mBinding.mainAdView.loadAd(adRequest)
    }

    @AfterPermissionGranted(REQUEST_CODE_WRITE_PERMISSION)
    fun requestPermissions() {
        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (EasyPermissions.hasPermissions(mActivity, *perms)) {
//            AccountInfoManager.getInstance().readAccountInfoFile()
            getFCMInstanceId()
            if (mFirstRun) loadExistsBackup()
            initView()
        } else {
            EasyPermissions.requestPermissions(mActivity, "Request Permission", REQUEST_CODE_WRITE_PERMISSION, *perms)
        }
    }

    private fun initRemoteConfig() {
        Log.i(LOG_TAG, "initRemoteConfig")
        val configSetting = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(1800)
                .build()
        mRemoteConfig.setConfigSettingsAsync(configSetting)
    }

    private fun fetchRemoteConfig() {
        Log.i(LOG_TAG, "fetchRemoteConfig")
        updateDownloadAPKButton()
        mRemoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.let {
                            if (it) updateDownloadAPKButton()
                        }
                    } else {
                        task.exception?.printStackTrace()
                    }
                }
    }

    private fun updateDownloadAPKButton() {
        Log.i(LOG_TAG, "updateDownloadAPKButton")
        val strConfigs = mRemoteConfig.getString("sb_configs")
        val remoteConfig = Gson().fromJson(strConfigs, RemoteConfigModel::class.java)
        var currentJPCode = PackageUtils.getInstance(mActivity).getVersionCode(Configs.PREFIX_NAME_SB_JP)
        var currentTWCode = PackageUtils.getInstance(mActivity).getVersionCode(Configs.PREFIX_NAME_SB_TW)

        if (BuildConfig.DEBUG) {
            currentJPCode--
            currentTWCode--
        }

        if (remoteConfig != null) {
            if (remoteConfig.versionCodeJP > currentJPCode) {
                mBinding.mainDrawerItemSbJ.setDownloadAPKButtonVisibility(true)
            } else {
                mBinding.mainDrawerItemSbJ.setDownloadAPKButtonVisibility(false)
            }

            if (remoteConfig.versionCodeTW > currentTWCode) {
                mBinding.mainDrawerItemSbT.setDownloadAPKButtonVisibility(false)
            } else {
                mBinding.mainDrawerItemSbT.setDownloadAPKButtonVisibility(false)
            }
        }
    }

    private fun getFCMInstanceId() {
        FirebaseInstanceId.getInstance().instanceId
                .addOnSuccessListener {
                    Log.i(LOG_TAG, "addOnSuccessListener")
                    Log.v(LOG_TAG, "addOnSuccessListener it.id : ${it.id}")
                    Log.v(LOG_TAG, "addOnSuccessListener it.token : ${it.token}")
                }
                .addOnFailureListener {
                    Log.i(LOG_TAG, "addOnFailureListener")
                }
                .addOnCompleteListener {
                    Log.i(LOG_TAG, "addOnCompleteListener")
                }
    }

    private fun loadExistsBackup() {
        Thread {
            File(Configs.PATH_APP_DATA).apply {
                listFiles(FileFilter {
                    val pattern = Pattern.compile("jp\\.gungho\\.bm\\.\\w+")
                    pattern.matcher(it.name).matches()
                }).sorted().forEach {
                    Log.v(LOG_TAG, "it.name : ${it.name}")
                    val currentTime = System.currentTimeMillis()
                    BaseDatabase.getInstance(mActivity).accountDAO()
                            .insertAccount(Account(
                                    alias = it.name,
                                    folder = it.absolutePath,
                                    lang = Account.Language.JP.ordinal,
                                    createTime = currentTime,
                                    updateTime = currentTime))
                }

                listFiles(FileFilter {
                    val pattern = Pattern.compile("com\\.ghg\\.sb\\.\\w+")
                    pattern.matcher(it.name).matches()
                }).sorted().forEach {
                    Log.v(LOG_TAG, "it.name : ${it.name}")
                    val currentTime = System.currentTimeMillis()
                    BaseDatabase.getInstance(mActivity).accountDAO()
                            .insertAccount(Account(
                                    alias = it.name,
                                    folder = it.absolutePath,
                                    lang = Account.Language.TW.ordinal,
                                    createTime = currentTime,
                                    updateTime = currentTime))
                }

                PreferenceManager.getDefaultSharedPreferences(mActivity).edit().apply {
                    putBoolean(Configs.PREF_KEY_FIRST_RUN, false)
                    apply()
                }
                mHandler.post { selectLanguage() }
            }
        }.start()
    }

    private fun selectLanguage() {
        when (mCurrentLang) {
            Account.Language.JP -> {
                mBinding.mainDrawerItemSbJ.performClick()
            }
            Account.Language.TW -> {
                mBinding.mainDrawerItemSbT.performClick()
            }
        }
    }

    private fun onDrawerItemSBClick(title: String, lang: Account.Language) {
        supportActionBar!!.title = title

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.main_frame_layout, AccountFragment.newInstance())
        ft.commit()

        mBinding.mainDrawerLayout.closeDrawer(GravityCompat.START)
        PreferenceManager.getDefaultSharedPreferences(mActivity).edit().apply {
            putString(Configs.PREF_KEY_LANGUAGE, lang.name)
            apply()
        }
        mCurrentLang = lang
    }
}
