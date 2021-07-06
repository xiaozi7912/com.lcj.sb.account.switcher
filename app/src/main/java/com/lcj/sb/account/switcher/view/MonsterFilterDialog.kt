package com.lcj.sb.account.switcher.view

import android.app.Activity
import android.view.LayoutInflater
import android.widget.ImageButton
import com.lcj.sb.account.switcher.BaseDialog
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.databinding.DialogMonsterFilterBinding
import com.lcj.sb.account.switcher.model.MonsterFilterModel

class MonsterFilterDialog(activity: Activity, callback: (model: MonsterFilterModel) -> Unit) : BaseDialog(activity) {
    private var mBinding = DialogMonsterFilterBinding.inflate(LayoutInflater.from(activity))
    private var mCallback = callback

    private val ELEMENT_BUTTON_IDS = intArrayOf(R.id.monster_element_0, R.id.monster_element_1, R.id.monster_element_2, R.id.monster_element_3, R.id.monster_element_4)
    private val TYPE_A_BUTTON_IDS = intArrayOf(R.id.monster_type_a_0, R.id.monster_type_a_1, R.id.monster_type_a_2)
    private val TYPE_B_BUTTON_IDS = intArrayOf(R.id.monster_type_b_1, R.id.monster_type_b_2, R.id.monster_type_b_3, R.id.monster_type_b_4, R.id.monster_type_b_5)

    init {
        mRootView = mBinding.root
        initView()
    }

    companion object {
        private var instance: MonsterFilterDialog? = null

        fun getInstance(activity: Activity, callback: (model: MonsterFilterModel) -> Unit): MonsterFilterDialog {
            if (instance == null) instance = MonsterFilterDialog(activity, callback)
            return instance!!
        }
    }

    override fun initView() {
        mBinding.monsterElement0.setOnClickListener { it.isSelected = !it.isSelected }
        mBinding.monsterElement1.setOnClickListener { it.isSelected = !it.isSelected }
        mBinding.monsterElement2.setOnClickListener { it.isSelected = !it.isSelected }
        mBinding.monsterElement3.setOnClickListener { it.isSelected = !it.isSelected }
        mBinding.monsterElement4.setOnClickListener { it.isSelected = !it.isSelected }
        mBinding.monsterTypeA0.setOnClickListener { it.isSelected = !it.isSelected }
        mBinding.monsterTypeA1.setOnClickListener { it.isSelected = !it.isSelected }
        mBinding.monsterTypeA2.setOnClickListener { it.isSelected = !it.isSelected }
        mBinding.monsterTypeB1.setOnClickListener { it.isSelected = !it.isSelected }
        mBinding.monsterTypeB2.setOnClickListener { it.isSelected = !it.isSelected }
        mBinding.monsterTypeB3.setOnClickListener { it.isSelected = !it.isSelected }
        mBinding.monsterTypeB4.setOnClickListener { it.isSelected = !it.isSelected }
        mBinding.monsterTypeB5.setOnClickListener { it.isSelected = !it.isSelected }
        mBinding.confirmButton.setOnClickListener {
            mCallback(MonsterFilterModel().apply {
                for ((index, viewId) in ELEMENT_BUTTON_IDS.withIndex()) {
                    val view: ImageButton = mRootView.findViewById(viewId)
                    if (view.isSelected) elements.add(index)
                }

                for ((index, viewId) in TYPE_A_BUTTON_IDS.withIndex()) {
                    val view: ImageButton = mRootView.findViewById(viewId)
                    if (view.isSelected) typeAs.add(index)
                }

                for ((index, viewId) in TYPE_B_BUTTON_IDS.withIndex()) {
                    val view: ImageButton = mRootView.findViewById(viewId)
                    if (view.isSelected) typeBs.add(index + 1)
                }

                elements.let {
                    if (it.size == 0) {
                        for (index in ELEMENT_BUTTON_IDS.indices) {
                            it.add(index)
                        }
                    }
                }
                typeAs.let {
                    if (it.size == 0) {
                        for (index in TYPE_A_BUTTON_IDS.indices) {
                            it.add(index)
                        }
                    }
                }
                typeBs.let {
                    if (it.size == 0) {
                        typeBs.add(0)
                    }
                }
            })
        }
    }

    override fun dismiss() {
        super.dismiss()
        instance = null
    }
}