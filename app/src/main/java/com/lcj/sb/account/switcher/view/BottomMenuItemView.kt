package com.lcj.sb.account.switcher.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.databinding.BottomMenuItemViewBinding

class BottomMenuItemView : LinearLayout {
    private lateinit var mBinding: BottomMenuItemViewBinding

    constructor(context: Context?) : super(context) {
        init(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    private fun init(context: Context?, attrs: AttributeSet?) {
        mBinding = BottomMenuItemViewBinding.inflate(LayoutInflater.from(context), this, true)
        if (attrs != null) {
            context!!.theme.obtainStyledAttributes(attrs, R.styleable.BottomMenuItemView, 0, 0)
                    .apply {
                        try {
                            mBinding.iconView.setImageResource(getResourceId(R.styleable.BottomMenuItemView_src, R.drawable.round_account_box_black_24))
                            mBinding.titleView.text = getString(R.styleable.BottomMenuItemView_title)!!
                        } finally {
                            recycle()
                        }
                    }
        }
    }

    fun getTitle(): String = mBinding.titleView.text.toString()
}