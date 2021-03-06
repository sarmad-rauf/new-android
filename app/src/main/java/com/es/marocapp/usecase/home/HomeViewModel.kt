package com.es.marocapp.usecase.home

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.es.marocapp.R
import com.es.marocapp.locale.LocaleManager
import com.es.marocapp.model.requests.*
import com.es.marocapp.model.responses.*
import com.es.marocapp.network.ApiClient
import com.es.marocapp.network.ApiConstant
import com.es.marocapp.network.applyIOSchedulers
import com.es.marocapp.security.EncryptionUtils
import com.es.marocapp.usecase.BaseActivity
import com.es.marocapp.usecase.MainActivity
import com.es.marocapp.usecase.login.LoginActivity
import com.es.marocapp.usecase.sendmoney.SendMoneyActivity
import com.es.marocapp.utils.Constants
import com.es.marocapp.utils.SingleLiveEvent
import com.es.marocapp.utils.Tools
import io.reactivex.disposables.Disposable
import retrofit2.HttpException

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    var isLoading = ObservableField<Boolean>()
    var errorText = SingleLiveEvent<String>()
    var acountFri = MutableLiveData<String>()
    var getAccountHolderAdditionalInfoResponseListner = SingleLiveEvent<AccountHolderAdditionalInformationResponse>()
    var getAccountHolderPersonalInformationApiResponseListner = SingleLiveEvent<GetAccountHolderPersonalInformationResponse>()
    var setDefaultAccountResponseListener = SingleLiveEvent<SetDefaultAccountResponse>()
    var verifyOTPForDefaultAccountResponseListener = SingleLiveEvent<VerifyOTPForDefaultAccountResponse>()
    var getBalanceResponseListner = SingleLiveEvent<GetBalanceResponse>()
    var getTransactionsResponseListner = SingleLiveEvent<TransactionHistoryResponse>()
    var getAccountsResponseListner = SingleLiveEvent<GetAccountsResponse>()
    lateinit var disposable: Disposable

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text


    // API For GETACCOUNTS API
    fun requestForGetAccountsAPI(
        context: Context?
    ) {

        if (Tools.checkNetworkStatus(getApplication())) {

            isLoading.set(true)


            disposable = ApiClient.newApiClientInstance?.getServerAPI()?.getAccountsCall(
                GetAccountsRequest(ApiConstant.CONTEXT_AFTER_LOGIN,Constants.getNumberMsisdn(Constants.CURRENT_USER_MSISDN))
            )
                .compose(applyIOSchedulers())
                .subscribe(
                    { result ->
                        isLoading.set(false)

                        if (result?.responseCode != null )
                        {
                            getAccountsResponseListner.postValue(result)

                        } else {
                            errorText.postValue(context!!.getString(R.string.error_msg_generic))
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


    // API For CheckDefaultAccountStatus API
    fun requestForAccountHolderAddtionalInformationApi(
        context: Context?
    )
    {
        if (Tools.checkNetworkStatus(getApplication())) {

            isLoading.set(true)


            disposable = ApiClient.newApiClientInstance?.getServerAPI()?.getAccountHolderAddtionalInfoCall(
                GetAccountHolderInformationRequest(ApiConstant.CONTEXT_BEFORE_LOGIN, Constants.getNumberMsisdn(Constants.CURRENT_USER_MSISDN))
            )
                .compose(applyIOSchedulers())
                .subscribe(
                    { result ->
                        isLoading.set(false)

                        if (result?.responseCode != null) {
                            when(result?.responseCode) {
                                ApiConstant.API_SUCCESS -> {
                                    getAccountHolderAdditionalInfoResponseListner.postValue(result)
                                }
                                ApiConstant.API_SESSION_OUT -> (context as BaseActivity<*>).logoutAndRedirectUserToLoginScreen(context as MainActivity, LoginActivity::class.java,
                                    LoginActivity.KEY_REDIRECT_USER_SESSION_OUT)
                                ApiConstant.API_INVALID -> (context as BaseActivity<*>).logoutAndRedirectUserToLoginScreen(context as MainActivity, LoginActivity::class.java,
                                    LoginActivity.KEY_REDIRECT_USER_INVALID)
                                else ->  {
                                    getAccountHolderAdditionalInfoResponseListner.postValue(result)
                                }
                            }
                            

                        } else {
                            errorText.postValue(result?.description)
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

    // API For SetDefaultAccount
    fun requestForSetDefaultAccount(context: Context?)
    {
        if (Tools.checkNetworkStatus(getApplication())) {

            isLoading.set(true)

            var receiver=""
            if(Constants.IS_MERCHANT_USER) {
                receiver=Constants.getMerchantReceiverAlias(Constants.CURRENT_USER_MSISDN)
            }
            else if(Constants.IS_AGENT_USER){
                receiver=Constants.getAgentReceiverAlias(Constants.CURRENT_USER_MSISDN)
            }
            else if (Constants.IS_CONSUMER_USER){
                receiver=Constants.getTransferReceiverAlias(Constants.CURRENT_USER_MSISDN)
            }

            disposable = ApiClient.newApiClientInstance?.getServerAPI()?.setDefaultAccountStatus(
                SetDefaultAccountRequest(ApiConstant.CONTEXT_AFTER_LOGIN, receiver,"enrollment",
                    LocaleManager.selectedLanguage,Constants?.balanceInfoAndResponse?.profilename,Constants.CURRENT_USER_FIRST_NAME,Constants.CURRENT_USER_LAST_NAME)
            )
                .compose(applyIOSchedulers())
                .subscribe(
                    { result ->
                        isLoading.set(false)

                        if (result?.responseCode != null)
                        {
                            when(result?.responseCode) {
                                ApiConstant.API_SUCCESS -> {
                                    setDefaultAccountResponseListener.postValue(result)
                                }
                                ApiConstant.API_SESSION_OUT -> (context as BaseActivity<*>).logoutAndRedirectUserToLoginScreen(context as MainActivity, LoginActivity::class.java,
                                    LoginActivity.KEY_REDIRECT_USER_SESSION_OUT)
                                ApiConstant.API_INVALID -> (context as BaseActivity<*>).logoutAndRedirectUserToLoginScreen(context as MainActivity, LoginActivity::class.java,
                                    LoginActivity.KEY_REDIRECT_USER_INVALID)
                                else ->  {
                                    setDefaultAccountResponseListener.postValue(result)
                                }
                            }

                        } else {
                            errorText.postValue(result?.description)
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

    // API For VerifyOTPForSetDefaultAccount
    fun requestForVerifyOTPForSetDefaultAccount(
        context: Context?,
        referenceNumber: String,
        otp: String
    )
    {
        if (Tools.checkNetworkStatus(getApplication())) {

            isLoading.set(true)

            var receiver=""
            if(Constants.IS_MERCHANT_USER) {
                receiver=Constants.getMerchantReceiverAlias(Constants.CURRENT_USER_MSISDN)
            }
            else if(Constants.IS_AGENT_USER){
                receiver=Constants.getAgentReceiverAlias(Constants.CURRENT_USER_MSISDN)
            }
            else if (Constants.IS_CONSUMER_USER){
                receiver=Constants.getTransferReceiverAlias(Constants.CURRENT_USER_MSISDN)
            }

            disposable = ApiClient.newApiClientInstance?.getServerAPI()?.verifyOTPforSetDefaultAccountStatus(
                VerifyOTPForDefaultAccountRequest(ApiConstant.CONTEXT_AFTER_LOGIN, receiver,"confirm",LocaleManager.selectedLanguage,Constants?.balanceInfoAndResponse?.profilename,referenceNumber,
                    EncryptionUtils.encryptString(otp),Constants.CURRENT_USER_FIRST_NAME,Constants.CURRENT_USER_LAST_NAME)
            )
                .compose(applyIOSchedulers())
                .subscribe(
                    { result ->
                        isLoading.set(false)

                        if (result?.responseCode != null)
                        {
                            when(result?.responseCode) {
                                ApiConstant.API_SUCCESS -> {
                                    verifyOTPForDefaultAccountResponseListener.postValue(result)
                                }
                                ApiConstant.API_SESSION_OUT -> (context as BaseActivity<*>).logoutAndRedirectUserToLoginScreen(context as MainActivity, LoginActivity::class.java,
                                    LoginActivity.KEY_REDIRECT_USER_SESSION_OUT)
                                ApiConstant.API_INVALID -> (context as BaseActivity<*>).logoutAndRedirectUserToLoginScreen(context as MainActivity, LoginActivity::class.java,
                                    LoginActivity.KEY_REDIRECT_USER_INVALID)
                                else ->  {
                                    verifyOTPForDefaultAccountResponseListener.postValue(result)
                                }
                            }
                        } else {
                            errorText.postValue(result?.description)
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

    //Request For Get Transactions
    fun requestForGetTransactionHistoryApi(context: Context?,
                                           identity : String,isMerchant:Boolean
    )
    {
        if (Tools.checkNetworkStatus(getApplication())) {

            isLoading.set(true)
            var currentDate = ""
            var previousDateForTransactions = ""

            currentDate = Constants.getCurrentDate()
            previousDateForTransactions = Constants.getPreviousFromCurrentDate(currentDate,Constants.PREVIOUS_DAYS_TRANSACTION_COUNT.toInt())
            var number :String=""
            if(isMerchant)
            {
                number=identity
            }
            else{
                number= Constants.getNumberMsisdn(identity)
            }

            disposable = ApiClient.newApiClientInstance?.getServerAPI()?.getTrasactionHistoryCall(
                TransactionHistoryRequest(ApiConstant.CONTEXT_AFTER_LOGIN,currentDate,number,"0",previousDateForTransactions)
            )
                .compose(applyIOSchedulers())
                .subscribe(
                    { result ->
                        isLoading.set(false)

                        if (result?.responseCode != null)
                        {
                            when(result?.responseCode) {
                                ApiConstant.API_SUCCESS -> {
                                    getTransactionsResponseListner.postValue(result)
                                }
                                ApiConstant.API_SESSION_OUT -> (context as BaseActivity<*>).logoutAndRedirectUserToLoginScreen(context as MainActivity, LoginActivity::class.java,
                                    LoginActivity.KEY_REDIRECT_USER_SESSION_OUT)
                                ApiConstant.API_INVALID -> (context as BaseActivity<*>).logoutAndRedirectUserToLoginScreen(context as MainActivity, LoginActivity::class.java,
                                    LoginActivity.KEY_REDIRECT_USER_INVALID)
                                else ->  {
                                    getTransactionsResponseListner.postValue(result)
                                }
                            }

                        } else {
                            getTransactionsResponseListner.postValue(result)
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

    // API Called on to Store Personal INFO
    fun requestForGetAccountHolderPersonalInformationApi(
        context: Context?,
        userMsisdn: String
    ) {

        if (Tools.checkNetworkStatus(getApplication())) {
            disposable =
                ApiClient.newApiClientInstance?.getServerAPI()?.getAccountHolderPersonalInformation(
                    GetAccountHolderInformationRequest(
                        ApiConstant.CONTEXT_AFTER_LOGIN,
                        Constants.getNumberMsisdn(Constants.CURRENT_USER_MSISDN)
                    )
                )
                    .compose(applyIOSchedulers())
                    .subscribe(
                        { result ->


                            if (result?.responseCode != null && result?.responseCode!!.equals(
                                    ApiConstant.API_SUCCESS, true
                                )
                            ) {
                                getAccountHolderPersonalInformationApiResponseListner.postValue(result)

                            } else if (result?.responseCode != null && result.responseCode.equals(
                                    ApiConstant.API_FAILURE,
                                    true
                                )
                            ) {
                                getAccountHolderPersonalInformationApiResponseListner.postValue(result)
                            } else if (result.responseCode.equals(ApiConstant.API_SESSION_OUT)) {
                                (context as BaseActivity<*>).logoutAndRedirectUserToLoginScreen(
                                    context as SendMoneyActivity, LoginActivity::class.java,
                                    LoginActivity.KEY_REDIRECT_USER_SESSION_OUT
                                )
                            } else if (result.responseCode.equals(ApiConstant.API_INVALID)) {
                                (context as BaseActivity<*>).logoutAndRedirectUserToLoginScreen(
                                    context as SendMoneyActivity, LoginActivity::class.java,
                                    LoginActivity.KEY_REDIRECT_USER_INVALID
                                )
                            } else {
                                getAccountHolderPersonalInformationApiResponseListner.postValue(result)
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

    fun passNewFri(accountFri: String) {
     this.acountFri.value=accountFri
    }
}