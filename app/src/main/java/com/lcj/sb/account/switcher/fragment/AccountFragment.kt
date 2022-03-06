package com.lcj.sb.account.switcher.fragment

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.paging.toLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.lcj.sb.account.switcher.BaseAdapter
import com.lcj.sb.account.switcher.BaseApplication
import com.lcj.sb.account.switcher.BaseFragment
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.adapter.AccountAdapter
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.databinding.FragmentAccountBinding
import com.lcj.sb.account.switcher.repository.AccountRepository
import com.lcj.sb.account.switcher.utils.Configs
import com.lcj.sb.account.switcher.utils.FileManager

class AccountFragment : BaseFragment(), View.OnClickListener, BaseAdapter.AccountListListener {
    private lateinit var mBinding: FragmentAccountBinding
    private lateinit var mGameFolderPath: String
    private lateinit var mDisplayLang: Account.Language

    companion object {
        fun newInstance(): AccountFragment {
            return AccountFragment()
        }

        fun newInstance(lang: Account.Language): AccountFragment {
            return AccountFragment().apply {
                arguments = Bundle().apply {
                    putInt(Configs.PREF_KEY_LANGUAGE, lang.ordinal)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentAccountBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDisplayLang = Account.Language.values()[arguments?.getInt(Configs.PREF_KEY_LANGUAGE)!!]

        mBinding.createButton.setOnClickListener(this)
        mBinding.gameStartButton.setOnClickListener(this)
        mBinding.addFab.setOnClickListener(this)
        mBinding.gameFab.setOnClickListener(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val adapter = AccountAdapter(mActivity).apply { setOnClickListener(this@AccountFragment) }
        mBinding.accountList.layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        mBinding.accountList.adapter = adapter

        BaseDatabase.getInstance(mActivity).accountDAO()
            .liveAccounts(mDisplayLang.ordinal, false)
            .toLiveData(pageSize = 20)
            .observe(requireActivity(), Observer { adapter.update(it) })
    }

    override fun onResume() {
        super.onResume()
        mActivity.invalidateOptionsMenu()
        BaseApplication.setCurrentScreen(mActivity, mDisplayLang.screenName, LOG_TAG)
        requireActivity().getPreferences(Context.MODE_PRIVATE).edit().apply {
            putBoolean(Configs.PREF_KEY_FIRST_RUN, false)
            putString(Configs.PREF_KEY_LANGUAGE, mDisplayLang.name)
            apply()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.create_button,
            R.id.add_fab -> mDisplayLang.packageName.let {
                if (FileManager.isPackageInstalled(it, mActivity)) {
                    AccountRepository.getInstance(mActivity).showCreateAccountDialog(mDisplayLang, {}, {
                        mHandler.post { Toast.makeText(mActivity, "error", Toast.LENGTH_SHORT).show() }
                    }, {
                        mHandler.post { Snackbar.make(mContentView, getString(R.string.game_folder_not_exists), Snackbar.LENGTH_SHORT).show() }
                    })
                } else {
                    showErrorNoInstalled(it)
                }
            }
            R.id.game_start_button,
            R.id.game_fab -> mDisplayLang.packageName.let {
                if (FileManager.isPackageInstalled(it, mActivity)) {
                    startApplication(it)
                } else {
                    showErrorNoInstalled(it)
                }
            }
        }
    }

    override fun onItemClick(account: Account) {
        AccountRepository.getInstance(mActivity).onItemClick(account)
    }

    override fun onDeleteClick(account: Account) {
        AccountRepository.getInstance(mActivity).onDeleteClick(account, {
            mHandler.post {
                mBinding.addFab.animate().translationY(0f).setInterpolator(LinearInterpolator()).start()
                mBinding.gameFab.animate().translationY(0f).setInterpolator(LinearInterpolator()).start()
                Snackbar.make(mContentView, getString(R.string.dialog_message_delete_success), Snackbar.LENGTH_SHORT).show()
            }
        }, {
            mHandler.post { Snackbar.make(mContentView, getString(R.string.dialog_message_delete_fail), Snackbar.LENGTH_SHORT).show() }
        }, {
            mHandler.post { Snackbar.make(mContentView, getString(R.string.dialog_message_delete_folder_not_exists), Snackbar.LENGTH_SHORT).show() }
        })
    }

    override fun onEditAliasClick(account: Account) {
        AccountRepository.getInstance(mActivity).onEditAliasClick(account)
    }

    override fun onSaveClick(account: Account) {
        AccountRepository.getInstance(mActivity).onSaveClick(mDisplayLang, account, {
            mHandler.post { Snackbar.make(mContentView, "備份成功", Snackbar.LENGTH_SHORT).show() }
        }, {
            mHandler.post { Snackbar.make(mContentView, getString(R.string.game_folder_not_exists), Snackbar.LENGTH_SHORT).show() }
        })
    }

    override fun onLoadGameClick(account: Account) {
        AccountRepository.getInstance(mActivity).onLoadGameClick(account, {
            mHandler.post { mBinding.gameFab.performClick() }
        }, {})
    }

    override fun onMoreClick(account: Account) {
        AccountRepository.getInstance(mActivity).onMoreClick(account)
    }

    private fun showErrorNoInstalled(packageName: String) {
        AccountRepository.getInstance(mActivity).showErrorNoInstalled(packageName)
    }
}