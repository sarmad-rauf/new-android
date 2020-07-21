package com.es.marocapp.usecase.transaction

import android.os.Bundle
import com.es.marocapp.R
import com.es.marocapp.databinding.FragmentTransactionDetailsBinding
import com.es.marocapp.locale.LanguageData
import com.es.marocapp.model.CustomModelHistoryItem
import com.es.marocapp.usecase.BaseActivity
import com.es.marocapp.utils.Constants

class TransactionDetailsActivity : BaseActivity<FragmentTransactionDetailsBinding>(){

    private lateinit var mItemDetailsToShow : CustomModelHistoryItem
    private var amount = ""
    private var fee = ""

    override fun init(savedInstanceState: Bundle?) {
        mItemDetailsToShow = Constants.currentTransactionItem
        mDataBinding.imgBackButton.setOnClickListener {
            this@TransactionDetailsActivity.finish()
        }
        setStrings()
        updateUI()
    }

    private fun setStrings() {
        mDataBinding.statusTitle.text = LanguageData.getStringValue("TransactionStatus")
        mDataBinding.tvTransactionHistoryTitle.text = LanguageData.getStringValue("TransactionDetails")
        mDataBinding.dateTitle.text = LanguageData.getStringValue("Time")
        mDataBinding.transactionIDTitle.text = LanguageData.getStringValue("TransactionID")
        mDataBinding.ReceiverNameTitle.text = LanguageData.getStringValue("ReceiverName")
        mDataBinding.ReceiverIdentityTitle.text = LanguageData.getStringValue("ReceiverNumber")
        mDataBinding.SenderNameTitle.text = LanguageData.getStringValue("SenderName")
        mDataBinding.SenderIdentityTitle.text = LanguageData.getStringValue("SenderNumber")
        mDataBinding.amountTitle.text = LanguageData.getStringValue("Amount")
        mDataBinding.feeTitle.text = LanguageData.getStringValue("Fee")
        mDataBinding.totalAmountTitle.text = LanguageData.getStringValue("Total")
    }

    private fun updateUI(){
        //status
        if(mItemDetailsToShow.historyList.transactionstatus.isNullOrEmpty()){
            mDataBinding.statusVal.text = "-"
        }else{
            mDataBinding.statusVal.text = mItemDetailsToShow.historyList.transactionstatus
        }

        //Date
        if(mItemDetailsToShow.date.isNullOrEmpty()){
            mDataBinding.dateVal.text = "-"
        }else{
            mDataBinding.dateVal.text = Constants.getZoneFormattedDateAndTime(mItemDetailsToShow.date)
        }

        //TransactionID
        if(mItemDetailsToShow.historyList.transactionid.isNullOrEmpty()){
            mDataBinding.transactionIDVal.text = "-"
        }else{

            mDataBinding.transactionIDVal.text = mItemDetailsToShow.historyList.transactionid
        }

        //ReceiverName
        if(mItemDetailsToShow.historyList.toname.isNullOrEmpty()){
            mDataBinding.ReceiverNameVal.text = "-"
        }else{

            mDataBinding.ReceiverNameVal.text = mItemDetailsToShow.historyList.toname
        }

        //ReceiverNumber
        if(mItemDetailsToShow.historyList.tofri.isNullOrEmpty()){
            mDataBinding.ReceiverIdentityVal.text = "-"
        }else{

            mDataBinding.ReceiverIdentityVal.text = getUpdateFri(mItemDetailsToShow.historyList.tofri)
        }

        //SenderName
        if(mItemDetailsToShow.historyList.fromname.isNullOrEmpty()){
            mDataBinding.SenderNameVal.text = "-"
        }else{

            mDataBinding.SenderNameVal.text = mItemDetailsToShow.historyList.fromname
        }

        //SenderNumber
        if(mItemDetailsToShow.historyList.fromfri.isNullOrEmpty()){
            mDataBinding.SenderIdentityVal.text = "-"
        }else{

            mDataBinding.SenderIdentityVal.text = getUpdateFri(mItemDetailsToShow.historyList.fromfri)
        }

        //Amount
        if(mItemDetailsToShow.historyList.toamount.isNullOrEmpty()){
            amount = "0.00"
            mDataBinding.amountVal.text = "DH 0.00"
        }else{
            amount = mItemDetailsToShow.historyList.toamount
            mDataBinding.amountVal.text = Constants.CURRENT_CURRENCY_TYPE_TO_SHOW+" "+mItemDetailsToShow.historyList.toamount
        }

        //Fee
        if(mItemDetailsToShow.historyList.fromfee.isNullOrEmpty()){
            fee = "0.00"
            mDataBinding.feeVal.text = "DH 0.00"
        }else{
            fee = mItemDetailsToShow.historyList.fromfee
            mDataBinding.feeVal.text = Constants.CURRENT_CURRENCY_TYPE_TO_SHOW+" "+mItemDetailsToShow.historyList.fromfee
        }

        //TotalAmount
        var totalAmount = Constants.addAmountAndFee(amount.toDouble(),fee.toDouble())
        totalAmount = Constants.converValueToTwoDecimalPlace(totalAmount.toDouble())
        mDataBinding.totalAmountVal.text = Constants.CURRENT_CURRENCY_TYPE_TO_SHOW+" "+totalAmount


    }

    private fun getFri(fri: String): String {
        var userFri = fri.substringAfter("212")
        userFri = userFri.subSequence(0,9).toString()
        userFri = "${Constants.APP_MSISDN_PREFIX}$userFri"
        userFri = userFri.removePrefix("+")
        return userFri
    }

    private fun getUpdateFri(fri: String): String {
        var userFri = fri.substringAfter(":")
        userFri = userFri.substringBefore("@")
        userFri = userFri.substringBefore("/")
        return userFri
    }

    override fun setLayout(): Int {
        return R.layout.fragment_transaction_details
    }

}
