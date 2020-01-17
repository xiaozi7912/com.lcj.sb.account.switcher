package com.lcj.sb.account.switcher.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.lcj.sb.account.switcher.R

class DrawerItemView : LinearLayout {
    private lateinit var mIconView: ImageView
    private lateinit var mTitleView: TextView
    private lateinit var mApkDownloadButton: ImageButton

    private var mIconId: Int = 0
    private lateinit var mTitleStr: String

    constructor(context: Context?) : super(context) {
        init(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    private fun init(context: Context?, attrs: AttributeSet?) {
        View.inflate(context, R.layout.drawer_item_view, this)
        mIconView = findViewById(R.id.drawer_item_icon)
        mTitleView = findViewById(R.id.drawer_item_title)
        mApkDownloadButton = findViewById(R.id.drawer_item_apk_download_btn)

        if (attrs != null) {
            context!!.theme.obtainStyledAttributes(attrs, R.styleable.DrawerItemView, 0, 0)
                    .apply {
                        try {
                            mIconId = getResourceId(R.styleable.DrawerItemView_src, R.drawable.ic_launcher_jp_p)
                            mTitleStr = getString(R.styleable.DrawerItemView_title)!!
                        } finally {
                            recycle()
                        }
                    }
            mIconView.setImageResource(mIconId)
            mTitleView.text = mTitleStr
        }
    }

    fun setImageRes(resId: Int) {
        mIconView.setImageResource(resId)
    }

    fun getTitle(): String {
        return mTitleStr
    }

    fun setDownloadAPKButtonVisibility(isVisible: Boolean) {
        if (isVisible) {
            mApkDownloadButton.visibility = View.VISIBLE
        } else {
            mApkDownloadButton.visibility = View.INVISIBLE
        }
    }

    fun setDownloadAPKButtonClickListener(listener: OnClickListener) {
        mApkDownloadButton.setOnClickListener(listener)
    }
}