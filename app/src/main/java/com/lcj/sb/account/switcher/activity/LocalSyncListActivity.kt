package com.lcj.sb.account.switcher.activity

import android.app.AlertDialog
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.paging.toLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.material.snackbar.Snackbar
import com.lcj.sb.account.switcher.BaseActivity
import com.lcj.sb.account.switcher.BaseAdapter
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.adapter.LocalSyncListAdapter
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.databinding.ActivityLocalSyncListBinding
import com.lcj.sb.account.switcher.utils.Configs
import java.io.File

class LocalSyncListActivity : BaseActivity(), RecyclerView.OnItemTouchListener {
    private lateinit var mBinding: ActivityLocalSyncListBinding
    private lateinit var mDetector: GestureDetectorCompat

    companion object {
        private const val FLING_MIN_DELTA_VALUE = 120
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(mActivity, R.layout.activity_local_sync_list)

        setSupportActionBar(mBinding.toolBar)
        initView()
        initGestureDetector()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        mDetector.onTouchEvent(e)
        return false
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
    }

    override fun initView() {
        mCurrentLang = Account.Language.valueOf(intent.getStringExtra(Configs.INTENT_KEY_LANGUAGE))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val adapter = LocalSyncListAdapter(mActivity)
        adapter.setOnClickListener(object : BaseAdapter.LocalSyncListListener {
            override fun onItemClick(account: Account) {
                onListItemClick(account)
            }

            override fun onIconClick(account: Account) {
                onListIconClick(account)
            }

            override fun onDeleteClick(account: Account) {
                onListDeleteClick(account)
            }

            override fun onUploadClick(account: Account) {
            }
        })

        mBinding.recyclerView.layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        mBinding.recyclerView.adapter = adapter
        mBinding.recyclerView.addOnItemTouchListener(this)
        BaseDatabase.getInstance(mActivity).accountDAO()
                .liveAccounts(mCurrentLang.ordinal)
                .toLiveData(pageSize = 20)
                .observe(this, Observer { adapter.update(it) })
    }

    override fun initAdMob() {
        val adRequest = AdRequest.Builder().build()
        mBinding.adView.loadAd(adRequest)
    }

    private fun initGestureDetector() {
        mDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                e?.let { event ->
                    val childView = mBinding.recyclerView.findChildViewUnder(event.x, event.y)
                    val position = childView?.let { view -> mBinding.recyclerView.getChildAdapterPosition(view) }
                }
                return super.onSingleTapConfirmed(e)
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                val deltaX = (e2?.x!! - e1?.x!!) / mDisplayMetrics.density
                if (deltaX >= FLING_MIN_DELTA_VALUE) onBackPressed()
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })
    }

    private fun onListItemClick(account: Account) {
    }

    private fun onListIconClick(account: Account) {
    }

    private fun onListDeleteClick(account: Account) {
        AlertDialog.Builder(mActivity).apply {
            setTitle(getString(R.string.dialog_title_delete_backup))
            setMessage(getString(R.string.dialog_message_delete_backup))
            setPositiveButton(getString(R.string.dialog_button_confirmed)) { dialog, which ->
                val folder = File(account.folder)
                Thread {
                    if (folder.exists()) {
                        if (folder.isDirectory) {
                            folder.listFiles().forEach { childFile ->
                                if (childFile.isDirectory) {
                                    childFile.listFiles().forEach { it.delete() }
                                }
                                childFile.delete()
                            }

                            if (folder.delete()) {
                                BaseDatabase.getInstance(mActivity).accountDAO().delete(account)
                                mHandler.post { Snackbar.make(mContentView, getString(R.string.dialog_message_delete_success), Snackbar.LENGTH_SHORT).show() }
                            } else {
                                mHandler.post { Snackbar.make(mContentView, getString(R.string.dialog_message_delete_fail), Snackbar.LENGTH_SHORT).show() }
                            }
                        }
                    } else {
                        BaseDatabase.getInstance(mActivity).accountDAO().delete(account)
                        mHandler.post { Snackbar.make(mContentView, getString(R.string.dialog_message_delete_folder_not_exists), Snackbar.LENGTH_SHORT).show() }
                    }
                }.start()
            }
            setNegativeButton(getString(R.string.dialog_button_cancel)) { dialog, which ->
                dialog.dismiss()
            }
        }.create().show()
    }
}