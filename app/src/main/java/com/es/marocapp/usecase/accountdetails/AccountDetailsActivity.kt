package com.es.marocapp.usecase.accountdetails

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.es.marocapp.R
import com.es.marocapp.adapter.AccountDetailLimitListAdapter
import com.es.marocapp.databinding.LayoutAccountDetailsBinding
import com.es.marocapp.locale.LanguageData
import com.es.marocapp.model.responses.Limits
import com.es.marocapp.usecase.BaseActivity
import com.es.marocapp.usecase.login.LoginActivityViewModel
import com.es.marocapp.utils.Constants


class AccountDetailsActivity : BaseActivity<LayoutAccountDetailsBinding>(){

    lateinit var mActivityViewModel: AccoutDetailsViewModel
    lateinit var mLimitListAdapter : AccountDetailLimitListAdapter

    override fun init(savedInstanceState: Bundle?) {
        mActivityViewModel = ViewModelProvider(this@AccountDetailsActivity)[AccoutDetailsViewModel::class.java]
        mDataBinding.apply {
            viewmodel = mActivityViewModel
        }

        var respone = Constants.balanceInfoAndResponse
        if(Constants.IS_AGENT_USER){
        for (i in Constants.getAccountsResponseArray.indices) {

            if (Constants.getAccountsResponseArray[i].accountType.equals(
                    Constants.TYPE_COMMISSIONING,
                    true
                )
            ){
               mDataBinding.commisioningBalanceCardContainer.visibility=android.view.View.VISIBLE
                mDataBinding.accountDetailCommisinningBalance.text = Constants.CURRENT_CURRENCY_TYPE_TO_SHOW+" "+Constants.getAccountsResponseArray[i].balance
            }
        }

        }
        else{
            mDataBinding.commisioningBalanceCardContainer.visibility=android.view.View.GONE
        }
        mDataBinding.accountDetailName.text = respone?.firstname+" "+respone?.surname
        mDataBinding.accountDetailAccountNum.text = Constants.CURRENT_USER_MSISDN.replace("212","0").trim()
        mDataBinding.accountDetailCurrentBalance.text = respone?.balance+" "+respone?.currnecy
        mDataBinding.appNameTitleSenRevContainer.text = respone?.profilename

        if(respone?.limitsList!=null) {
            mLimitListAdapter =
                AccountDetailLimitListAdapter(respone?.limitsList as ArrayList<Limits>)

            mDataBinding.mAccountDetailLimitInfo.apply {
                adapter = mLimitListAdapter
                layoutManager = LinearLayoutManager(this@AccountDetailsActivity)
            }
        }

        mDataBinding.imgBackButton.setOnClickListener {
            this@AccountDetailsActivity.finish()
        }

        //TODO add Reycler IN View and POpulate according to list
//        if(!respone?.limitsList.isNullOrEmpty()){
//            mDataBinding.tvYearlySendingLimitTitle.text = respone?.limitsList?.get(0)?.name
//            mDataBinding.tvYearlySendingLimitVal.text = respone?.currnecy +" "+respone?.limitsList?.get(0)?.threshhold
//        }
        var limitListInfo = respone?.limitsList

        setStings()

    }

    private fun setStings() {
        mDataBinding.tvAccountDetailTitle.text = LanguageData.getStringValue("AccountDetails")
        mDataBinding.accountDetailAccountNumTitle.text = LanguageData.getStringValue("AcountNumber")
        mDataBinding.accountDetailCurrentBalanceTitle.text = LanguageData.getStringValue("CurrentBalance")
        mDataBinding.profileTitleSenRevContainer.text = LanguageData.getStringValue("Profile")
        mDataBinding.accountDetailHi.text = LanguageData.getStringValue("Hi")
        mDataBinding.accountDetailCommisinningBalanceTitle.text = LanguageData.getStringValue("Commission")
    }

    override fun setLayout(): Int {
        return R.layout.layout_account_details
    }

}
