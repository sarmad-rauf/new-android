package com.es.marocapp.usecase.approvals

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import com.es.marocapp.R
import com.es.marocapp.model.requests.BalanceInfoAndLimtRequest
import com.es.marocapp.model.requests.GetApprovalRequest
import com.es.marocapp.model.requests.InitiateTransferQuoteRequest
import com.es.marocapp.model.requests.UserApprovalRequest
import com.es.marocapp.model.responses.*
import com.es.marocapp.network.ApiClient
import com.es.marocapp.network.ApiConstant
import com.es.marocapp.network.applyIOSchedulers
import com.es.marocapp.usecase.BaseActivity
import com.es.marocapp.usecase.MainActivity
import com.es.marocapp.usecase.cashservices.CashServicesActivity
import com.es.marocapp.usecase.login.LoginActivity
import com.es.marocapp.utils.Constants
import com.es.marocapp.utils.SingleLiveEvent
import com.es.marocapp.utils.Tools
import io.reactivex.disposables.Disposable
import retrofit2.HttpException

class ApprovalViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var selectedapproval: Approvaldetail
    var totalTax: Double = 0.0
    var isCurrentSelectedLanguageEng: Boolean=false
    var start  = Gravity.START
    var end = Gravity.END
    lateinit var bundle: Bundle

    //used on ApprovalDetailFragment to add tax with fee
    var selectedTaxDetail: String? = "0"

    var isApproved: Boolean = false
    lateinit var disposable: Disposable
    var isLoading = ObservableField<Boolean>()
    var errorText = SingleLiveEvent<String>()
    var getApprovalResponseListner = SingleLiveEvent<GetApprovalsResponse>()
    var getUsersApprovalResponseListner = SingleLiveEvent<UserApprovalResponse>()
    var getBalanceResponseListner = SingleLiveEvent<GetBalanceResponse>()
    var getCashOutQuoteResponseListner = SingleLiveEvent<InitiateTransferQuoteResponse>()
    var amountToTransfer = ""
    var transferdAmountTo = ""
    var reciever = ""


    //Request For Get Approvals
    fun requestForGetApprovalsApi(context: Context?)
    {
        if (Tools.checkNetworkStatus(getApplication())) {

            isLoading.set(true)


            disposable = ApiClient.newApiClientInstance?.getServerAPI()?.getApprovalsCall(
                GetApprovalRequest(ApiConstant.CONTEXT_AFTER_LOGIN)
            )
                .compose(applyIOSchedulers())
                .subscribe(
                    { result ->
                        isLoading.set(false)


                        if(result?.responseCode != null){
                            when(result?.responseCode) {
                                ApiConstant.API_SUCCESS ->  {
//                                    val list :List<Approvaldetail>=
//                                    var testData= GetApprovalsResponse()
                                    getApprovalResponseListner.postValue(result)}
                                ApiConstant.API_SESSION_OUT -> (context as BaseActivity<*>).logoutAndRedirectUserToLoginScreen(context as MainActivity, LoginActivity::class.java,LoginActivity.KEY_REDIRECT_USER_SESSION_OUT)
                                ApiConstant.API_INVALID -> (context as BaseActivity<*>).logoutAndRedirectUserToLoginScreen(context as MainActivity, LoginActivity::class.java,LoginActivity.KEY_REDIRECT_USER_INVALID)
                                else ->  getApprovalResponseListner.postValue(result)
                            }
                        }
                        else{
                            getApprovalResponseListner.postValue(result)
                        }


                    },
                    { error ->
                        isLoading.set(false)

                        //Display Error Result Code with with Configure Message
                        try {
                            if (context != null && error != null) {
                                errorText.postValue(context.getString(R.string.error_msg_generic) + (error as HttpException).code())
                            }
                        } catch (e: Exception) {
                            errorText.postValue(context!!.getString(R.string.error_msg_generic))
                        }

                    })


        } else {

            errorText.postValue(Constants.SHOW_INTERNET_ERROR)
        }

    }

    //Request For CashOUT_Qoute
    fun requestForCashOUTQouteApi(context: Context?,
                                           amount : String,
                                           userMsisdn : String,
                                           amountReciever : String

    )
    {
        if (Tools.checkNetworkStatus(getApplication())) {

            isLoading.set(true)
            amountToTransfer = amount
            transferdAmountTo = userMsisdn
            reciever = amountReciever


            disposable = ApiClient.newApiClientInstance?.getServerAPI()?.getCashoutQuoteCall(
                InitiateTransferQuoteRequest(amount, ApiConstant.CONTEXT_BEFORE_LOGIN,reciever,Constants.getNumberMsisdn(transferdAmountTo))
            )
                .compose(applyIOSchedulers())
                .subscribe(
                    { result ->
                        isLoading.set(false)

                        if (result?.responseCode != null)
                        {
                            when(result?.responseCode) {
                                ApiConstant.API_SUCCESS -> {
                                    getCashOutQuoteResponseListner.postValue(result)
                                }
                                ApiConstant.API_SESSION_OUT -> (context as BaseActivity<*>).logoutAndRedirectUserToLoginScreen(context as CashServicesActivity, LoginActivity::class.java,
                                    LoginActivity.KEY_REDIRECT_USER_SESSION_OUT)
                                ApiConstant.API_INVALID -> (context as BaseActivity<*>).logoutAndRedirectUserToLoginScreen(context as CashServicesActivity, LoginActivity::class.java,
                                    LoginActivity.KEY_REDIRECT_USER_INVALID)
                                else ->  {
                                    getCashOutQuoteResponseListner.postValue(result)
                                }
                            }

                        } else {
                            getCashOutQuoteResponseListner.postValue(result)
                        }


                    },
                    { error ->
                        isLoading.set(false)

                        //Display Error Result Code with with Configure Message
                        try {
                            if (context != null && error != null) {
                                errorText.postValue(context.getString(R.string.error_msg_generic) + (error as HttpException).code())
                            }
                        } catch (e: Exception) {
                            errorText.postValue(context!!.getString(R.string.error_msg_generic))
                        }

                    })


        } else {

            errorText.postValue(Constants.SHOW_INTERNET_ERROR)
        }

    }

    //Request For User Approvals
    fun requestForUserApprovalsApi(
        context: Context?,
        approveID : String,
        approved : String
    )
    {
        if (Tools.checkNetworkStatus(getApplication())) {

            isLoading.set(true)


            disposable = ApiClient.newApiClientInstance?.getServerAPI()?.getUserApprovalsCall(
                UserApprovalRequest(approveID,approved,ApiConstant.CONTEXT_AFTER_LOGIN)
            )
                .compose(applyIOSchedulers())
                .subscribe(
                    { result ->
                        isLoading.set(false)

                        if(result?.responseCode != null){
                            when(result?.responseCode) {
                                ApiConstant.API_SUCCESS ->  getUsersApprovalResponseListner.postValue(result)
                                ApiConstant.API_SESSION_OUT -> (context as BaseActivity<*>).logoutAndRedirectUserToLoginScreen(context as MainActivity, LoginActivity::class.java,LoginActivity.KEY_REDIRECT_USER_SESSION_OUT)
                                ApiConstant.API_INVALID -> (context as BaseActivity<*>).logoutAndRedirectUserToLoginScreen(context as MainActivity, LoginActivity::class.java,LoginActivity.KEY_REDIRECT_USER_INVALID)
                                else ->  getUsersApprovalResponseListner.postValue(result)
                            }
                        }
                        else{
                            getUsersApprovalResponseListner.postValue(result)
                        }


                    },
                    { error ->
                        isLoading.set(false)

                        //Display Error Result Code with with Configure Message
                        try {
                            if (context != null && error != null) {
                                errorText.postValue(context.getString(R.string.error_msg_generic) + (error as HttpException).code())
                            }
                        } catch (e: Exception) {
                            errorText.postValue(context!!.getString(R.string.error_msg_generic))
                        }

                    })


        } else {

            errorText.postValue(Constants.SHOW_INTERNET_ERROR)
        }

    }

    //Request For Get Updated Balance
    fun requestForGetBalanceApi(
        context: Context?
    )
    {
        if (Tools.checkNetworkStatus(getApplication())) {

            isLoading.set(true)


            disposable = ApiClient.newApiClientInstance?.getServerAPI()?.getBalance(
                BalanceInfoAndLimtRequest(ApiConstant.CONTEXT_AFTER_LOGIN,Constants.getNumberMsisdn(Constants.CURRENT_USER_MSISDN))
            )
                .compose(applyIOSchedulers())
                .subscribe(
                    { result ->
                        isLoading.set(false)

                        if(result?.responseCode != null){
                            when(result?.responseCode) {
                                ApiConstant.API_SUCCESS ->  getBalanceResponseListner.postValue(result)
                                ApiConstant.API_SESSION_OUT -> (context as BaseActivity<*>).logoutAndRedirectUserToLoginScreen(context as MainActivity, LoginActivity::class.java,LoginActivity.KEY_REDIRECT_USER_SESSION_OUT)
                                ApiConstant.API_INVALID -> (context as BaseActivity<*>).logoutAndRedirectUserToLoginScreen(context as MainActivity, LoginActivity::class.java,LoginActivity.KEY_REDIRECT_USER_INVALID)
                                else ->  getBalanceResponseListner.postValue(result)
                            }
                        }
                        else{
                            getBalanceResponseListner.postValue(result)
                        }


                    },
                    { error ->
                        isLoading.set(false)

                        //Display Error Result Code with with Configure Message
                        try {
                            if (context != null && error != null) {
                                errorText.postValue(context.getString(R.string.error_msg_generic) + (error as HttpException).code())
                            }
                        } catch (e: Exception) {
                            errorText.postValue(context!!.getString(R.string.error_msg_generic))
                        }

                    })


        } else {

            errorText.postValue(Constants.SHOW_INTERNET_ERROR)
        }

    }

}