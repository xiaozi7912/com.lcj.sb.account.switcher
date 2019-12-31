package com.lcj.sb.account.switcher.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton


class FabScrollBehavior : FloatingActionButton.Behavior {
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
        if (dyConsumed > 0) { // 向下滑动
            animateOut(child)
        } else if (dyConsumed < 0) { // 向上滑动
            animateIn(child)
        }
    }

    // FAB移出屏幕动画（隐藏动画）
    private fun animateOut(fab: FloatingActionButton) {
        val layoutParams = fab.layoutParams as CoordinatorLayout.LayoutParams
        val bottomMargin = layoutParams.bottomMargin
        fab.animate().translationY(fab.height + bottomMargin.toFloat()).setInterpolator(LinearInterpolator()).start()
    }

    // FAB移入屏幕动画（显示动画）
    private fun animateIn(fab: FloatingActionButton) {
        fab.animate().translationY(0f).setInterpolator(LinearInterpolator()).start()
    }
}