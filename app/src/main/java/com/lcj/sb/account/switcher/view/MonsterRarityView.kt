package com.lcj.sb.account.switcher.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import com.lcj.sb.account.switcher.R

class MonsterRarityView : LinearLayout {
    constructor(context: Context?) : super(context) {
        init(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    private fun init(context: Context?, attrs: AttributeSet?) {
        orientation = HORIZONTAL

        if (attrs != null) {
            context!!.theme.obtainStyledAttributes(attrs, R.styleable.BottomMenuItemView, 0, 0)
                    .apply {
                        val rarity = getInteger(R.styleable.MonsterRarityView_rarity, 0)
                        updateView(rarity)
                    }
        }
    }

    fun updateView(rarity: Int) {
        for (i in 0 until rarity) {
            val starView = ImageView(context)
            starView.setImageResource(R.drawable.round_star_rate_white_24)
            starView.imageTintList = ColorStateList.valueOf(Color.parseColor("#ffff00"))
            addView(starView)
        }
    }
}