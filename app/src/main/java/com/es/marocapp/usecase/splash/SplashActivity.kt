package com.es.marocapp.usecase.splash


import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.es.marocapp.BuildConfig
import com.es.marocapp.R
import com.es.marocapp.databinding.AcitivtySplashBinding
import com.es.marocapp.locale.LanguageData
import com.es.marocapp.locale.LocaleManager
import com.es.marocapp.model.responses.GetPreLoginDataResponse
import com.es.marocapp.model.responses.translations.TranslationInnerObject
import com.es.marocapp.network.ApiConstant
import com.es.marocapp.security.EncryptionUtils
import com.es.marocapp.usecase.BaseActivity
import com.es.marocapp.usecase.login.LoginActivity
import com.es.marocapp.utils.*
import java.lang.reflect.Method
import java.util.*
import com.scottyab.rootbeer.RootBeer





class SplashActivity : BaseActivity<AcitivtySplashBinding>() {

    private val READ_PHONE_STATE_REQUEST_CODE = 112

    private val PERMISSION_TAG = "permissions"

    override fun setLayout(): Int {
        return R.layout.acitivty_splash
    }

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }

    lateinit var mActivityViewModel: SplashActivityViewModel


    override fun init(savedInstanceState: Bundle?) {

        mActivityViewModel = ViewModelProvider(this)[SplashActivityViewModel::class.java]

        mDataBinding.apply {
            viewmodel = mActivityViewModel
        }

        Constants.isTutorialShowing = PrefUtils.getBoolean(
            this@SplashActivity,
            PrefUtils.PreKeywords.PREF_KEY_IS_SHOW_TUTORIALS,
            true
        )
        Logger.debugLog("trxH", "isTutorialShowing  ${Constants.isTutorialShowing}");

        if(checkRootDetection(this@SplashActivity)) {
            loadNDKValues()
            setupPermissions()
            checkInternetOrMobileConnection()
            subscribe()

            subscribeForTranslationsApiResponse()
        }
        /*val decrptedNumber = EncryptionUtils.decryptStringAESCBC("iIk0fEvEpOB5fuJ2n3mpMQ==")
        Logger.debugLog("AESCBCNumber",decrptedNumber)*/

    }

    private fun checkRootDetection(splashActivity: SplashActivity): Boolean {

        //if root detection is enabled
        if(BuildConfig.ROOT_DETECTION_ENABLED)
        {
            val rootBeer = RootBeer(splashActivity)
            return !rootBeer.isRooted
        }
        //else return true
        return true
    }

    private fun checkInternetOrMobileConnection() {
        /*val conMgr: ConnectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo: NetworkInfo = conMgr.getActiveNetworkInfo()
        if (netInfo == null) {
            Constants.getIPAddress(application)
        } else {
            Constants.getDeviceIPAddress(true)
        }*/

        val mgr =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = mgr.activeNetworkInfo

        if (netInfo != null) {
            if (netInfo.isConnected) {
                // Internet Available
                Constants.getIPAddress(application)
            } else {
                //No internet
                Constants.getDeviceIPAddress(true)
            }
        } else {
            //No internet
            Constants.getDeviceIPAddress(true)
        }
    }

    // NDK methods Start
    external fun getSecureKeyValues(): String
    external fun getServerPublicKeys(): String
    external fun getAesGcmHexKey(): String
    external fun getAesGcmHexIV(): String
    external fun getAesCBCHexKey(): String
    external fun getAesCBCHexIV(): String

    fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_PHONE_STATE
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(PERMISSION_TAG, "Permission to read phone state denied")
            makeRequestPermission()
        } else {
//            val telephonyManager =
//                application.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
//            Constants.CURRENT_DEVICE_ID = telephonyManager!!.deviceId

            //   checkInternetOrMobileConnection()
            setDeviceIMEI()

            mActivityViewModel.requestForGetPreLoginDataApi(
                this@SplashActivity,
                BuildConfig.VERSION_NAME
            )
        }
    }

    private fun makeRequestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_PHONE_STATE),
            READ_PHONE_STATE_REQUEST_CODE
        )
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_PHONE_STATE_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i(PERMISSION_TAG, "Permission has been denied by user")
                } else {
//                    val telephonyManager =
//                        application.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
                    setDeviceIMEI()
                }
            }
        }
        //checkInternetOrMobileConnection()
        mActivityViewModel.requestForGetPreLoginDataApi(
            this@SplashActivity,
            BuildConfig.VERSION_NAME
        )
    }

    private fun subscribe() {
        val resultObserver = Observer<Boolean> {
            startNewActivityAndClear(this@SplashActivity, LoginActivity::class.java)
        }

        val preLoginDataObserver = Observer<GetPreLoginDataResponse> {
            if (it.responseCode.equals(ApiConstant.API_SUCCESS, true)) {
                Constants.APP_MSISDN_PREFIX = EncryptionUtils.decryptString(it.msisdnPrefix)
                Constants.APP_MSISDN_LENGTH = EncryptionUtils.decryptString(it.msisdnLength)
                Constants.APP_MSISDN_REGEX = it.msisdnRegex
                Constants.APP_MSISDN_POSTPAIDBILL_MOBILE_REGEX = it.postpaidBillMobileRegex
                Constants.APP_MSISDN_POSTPAIDBILL_FIXE_REGEX = it.postpaidBillFixedRegex
                Constants.APP_MSISDN_POSTPAIDBILL_INTERNET_REGEX = it.postpaidBillInternetRegex
                Constants.APP_BILL_PAYMENT_CODE_REGEX = it.postpaidBillCodeRegex
                Constants.APP_CIL_LENGTH = it.cilLength
                Constants.APP_CIL_REGEX = it.cilRegex
                Constants.APP_CN_LENGTH = it.cnLength
                Constants.APP_CN_REGEX = it.cnRegex
                Constants.APP_OTP_LENGTH = it.androidOtpLength
                Constants.MERCHANT_AGENT_PROFILE_NAME = it.agentMerchantAccountProfile
                Constants.upgradeSupportedProfiles = it.upgradeSupportedProfiles
                Constants.reasonUpgradeToLevelTwo = it.reasonUpgradeToLevelTwo
                Constants.reasonUpgradeToLevelThree = it.reasonUpgradeToLevelThree
                Constants.fatouratiSeperateMenuBillNames = it.fatouratiSeperateMenuBillNames
                Constants.fatouratiTsavMatriculeDdVals = it.fatouratiTsavMatriculeDdVals
                Constants.registrationProfiles = it.registrationProfiles
                Constants.airtimeMaxNumOfRetries = it.airtimeMaxNumOfRetries
                Constants.maxFileSizeUploadLimitInMBs = it.maxFileSizeUploadLimitInMBs.toInt()
                Constants.fatouratiTsavMatriculeDdValsMap = it.fatouratiTsavMatriculeDdValsMap
                if (it.marocFatouratiLogoPath != null) {
                    Constants.marocFatouratiLogoPath = it.marocFatouratiLogoPath
                }
                if (it.iamBillsTriggerFatouratiFlow != null) {
                    Constants.iamBillsTriggerFatouratiFlow = it.iamBillsTriggerFatouratiFlow
                }

                Constants.CityNameRegex = it.cityNameRegex
                if (it.defaultAccountOtpLength != null) {
                    Constants.APP_DEFAULT_ACCOUNT_OTP_LENGTH =
                        it?.defaultAccountOtpLength?.toInt()!!
                }
                Constants.APP_DEFAULT_ACCOUNT_OTP_REGEX = it?.defaultAccountOtpRegex
                Constants.APP_AIR_TIME_FIXE_REGEX = it?.msisdnFixedLineRegex
                Constants.APP_ADDFAVORITE_NICK_LENGTH = it?.billFavoriteLength?.toInt()
                Constants.APP_DATE_FORMAT = it.dateFormat
                Constants.CURRENT_CURRENCY_TYPE = it.currencyOnEwp
                Constants.CURRENT_CURRENCY_TYPE_TO_SHOW = it.currencyToShow
                Constants.AMOUNT_CONVERSION_VALUE = it.amountConversionValue
                Constants.HELPLINE_NUMBER = it.helpLineNumber
                Constants.HELPLINENUMBERAGENT = it.helpLineNumberAgent
                Constants.BILLTYPEINWI = it.billTypeInwi
                Constants.REASON_FOR_UPDATE_PROFILE = it.reasonForUpdateEmail
                Constants.URL_FOR_FAQ = it.faqs
                Constants.URL_FOR_TERMSANDCONDITIONS = it.termsAndConditions
                Constants.APP_VERSION = it.version
                Constants.URL_FOR_UPDATE_APP = it.url
                Constants.KEY_FOR_WALLET_BALANCE_MAX = it.walletBalanceLimitKey
                Constants.KEY_FOR_POST_PAID_TELECOM_BILL = it.billTypePostPaid
                Constants.PREVIOUS_DAYS_TRANSACTION_COUNT = it.numberOfTransactions
                Constants.MERCHENTAGENTPROFILEARRAY = it.agentMerchantProfile

                //Adding Aliases Value from API to Contants
                Constants.NUMBER_MSISDN_ALIAS = it.numberAlias
                Constants.TRANSFER_RECEIVER_ALIAS = it.getTransferReceiverAlias
                Constants.MERCHANT_RECEIVER_ALIAS = it.getMerchantReceiverAlias
                Constants.AIR_TIME_RECEIVER_ALIAS = it.getAirtimeReceiverAlias
                Constants.AIR_TIME_Pass_Store_RECEIVER_ALIAS = it.getAirtimeReceiverAliasPassStore
                Constants.AGNET_RECEIVER_ALIAS = it.getAgentReceiverAlias
                Constants.POST_PAID_MOBILE_ALIAS = it.getPostPaidMobileDomainAlias
                Constants.POST_PAID_FIXED_ALIAS = it.getPostPaidFixedDomainAlias
                Constants.POST_PAID_INTERNET_ALIAS = it.getPostPaidInternetDomainAlias
                Constants.FATOURATI_ALIAS = it.getFatouratiAlias
                //--------------------------------------------------------------------------------------

                //Adding Transfer Type From API to Contants
                Constants.TRANSFER_TYPE_PAYMENT = it.transferTypePayment
                Constants.MERCHANT_TYPE_PAYMENT = it.merchantTypePayment
                Constants.TYPE_PAYMENT = it.typePayment
                Constants.TYPE_BILL_PAYMENT = it.typeBillPayment
                Constants.TYPE_COMMISSIONING = it.typeCommisioning
                Constants.OPERATION_TYPE_CREANCIER = it.operationTypeCreancier
                Constants.OPERATION_TYPE_CREANCE = it.operationTypeCreance
                Constants.OPERATION_TYPE_IMPAYES = it.operationTypeImpayes
                Constants.TYPE_CASH_IN = it.typeCashIn
                Constants.PAYMENT_TYPE_SEND_MONEY = it.paymentTypeSendMoney
                Constants.PAYMENT_TYPE_INITIATE_MERCHANT = it.paymentTypeInitiateMerchant
                //--------------------------------------------------------------------------------------

                if (it.cmiWebpageUrl != null) {
                    Constants.CASH_IN_VIA_CARD_URL = it.cmiWebpageUrl
                }
                if (Tools.isFirstTime(this)) {
                    if (!it.defaultLanguage.isNullOrEmpty()) {
                        LocaleManager.selectedLanguage = it.defaultLanguage
                        LocaleManager.setAppLanguage(this, it.defaultLanguage)
                    }
                }
                if (it.quickAmounts.isNotEmpty()) {
                    Constants.quickAmountsList.addAll(it.quickAmounts)
                } else {
                    Constants.quickAmountsList.apply {
                        add("50")
                        add("100")
                        add("250")
                        add("500")
                    }
                }
                if (it.quickRechargeAmounts.isNotEmpty()) {
                    Constants.quickRechargeAmountsList.addAll(it.quickRechargeAmounts)
                } else {
                    Constants.quickRechargeAmountsList.apply {
                        addAll(it.quickRechargeAmounts)
                    }
                }
                mActivityViewModel.requestForTranslationsApi(this)
            } else if (it.responseCode.equals(ApiConstant.API_INVALID_VERSION)) {
                DialogUtils.showUpdateAPPDailog(
                    this@SplashActivity,
                    it.description,
                    object : DialogUtils.OnCustomDialogListner {
                        override fun onCustomDialogOkClickListner() {
                            val appPackageName =
                                packageName // getPackageName() from Context or Activity object

                            try {
                                startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("market://${it.url}$appPackageName")
                                    )
                                )
//                                Uri.parse("market://${it.url}=$appPackageName")
                            } catch (anfe: ActivityNotFoundException) {
                                startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("${it.url}$appPackageName")
                                    )
                                )
//                                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                            }
                        }
                    }, R.drawable.ic_force_update, getString(R.string.ok)
                )
            } else {
                DialogUtils.showErrorDialoge(
                    this@SplashActivity,
                    it.description,
                    getString(R.string.ok)
                )
            }
        }

        val errorText = Observer<String> {
            DialogUtils.showErrorDialoge(this@SplashActivity, it, getString(R.string.ok))
        }

        mActivityViewModel.mHandler.observe(this, resultObserver)
        mActivityViewModel.preLoginDataResponseListener.observe(this, preLoginDataObserver)
        mActivityViewModel.errorText.observe(this, errorText)
    }

    private fun subscribeForTranslationsApiResponse() {
        mActivityViewModel.translationApiResponseListener.observe(this, Observer {
            if (it.responseCode.equals(ApiConstant.API_SUCCESS)) {
                setTranslations(it.labelList)
                startNewActivityAndClear(this@SplashActivity, LoginActivity::class.java)
            } else {
                DialogUtils.showErrorDialoge(
                    this@SplashActivity,
                    it.description,
                    getString(R.string.ok)
                )
            }
        })
    }

    private fun setTranslations(labelList: Map<String?, TranslationInnerObject?>?) {

        LanguageData.stringsHashMap = labelList
        var temp = labelList
        Logger.debugLog("LableListObj", labelList.toString())

    }

    fun setDeviceIMEI() {
        var myuniqueID: String?
        val myversion = Integer.valueOf(Build.VERSION.SDK_INT)
        if (myversion < 23) {
            /*val manager =
                applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info = manager.connectionInfo
            myuniqueID = info.macAddress*/
            val mngr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            myuniqueID = mngr.deviceId
        } else if (myversion > 23 && myversion < 29) {
            val mngr =
                getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            myuniqueID = mngr.deviceId
        } else {
            val androidId: String =
                Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
            myuniqueID = androidId
        }

        Constants.CURRENT_DEVICE_ID = myuniqueID.toString()
    }

    fun getPhoneIMEI(context: Context): String? {
        var deviceID = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var permissionResult =
                context.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE)
            if (permissionResult == PackageManager.PERMISSION_DENIED) {
                permissionResult =
                    context.checkCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE")
            }
            val isPermissionGranted =
                permissionResult == PackageManager.PERMISSION_GRANTED
            if (!isPermissionGranted) {
                deviceID = getDeviceIDFromReflection(context).toString()
            } else {
                deviceID = getDeviceIDFromSystem(context).toString()
            }
        } else {
            deviceID = getDeviceIDFromSystem(context).toString()
        }

        return deviceID
    }

    fun getDeviceIDFromReflection(context: Context?): String? {
        var deviceID = ""
        try {
            val multiSimUtilsClazz =
                Class.forName("android.provider.MultiSIMUtils")
            val getDefaultMethod: Method =
                multiSimUtilsClazz.getMethod("getDefault", Context::class.java)
            val `object`: Any = getDefaultMethod.invoke(null, context)
            val method: Method =
                multiSimUtilsClazz.getMethod("getDeviceId", Int::class.javaPrimitiveType)
            deviceID = method.invoke(`object`, 0) as String
        } catch (e: Exception) {
        }
        return deviceID
    }

    @SuppressLint("MissingPermission")
    fun getDeviceIDFromSystem(context: Context): String? {
        val tm =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var deviceID: String? = null
        if (tm != null) {
            try {
                deviceID = tm.deviceId
            } catch (e: java.lang.Exception) {

            }
        }
        return deviceID
    }

    fun loadNDKValues() {

        try {// The Key used for encription decription
            RootValues.getInstance().keyValueFromNdk = getSecureKeyValues()
            RootValues.getInstance().hexKeyAesGcm = getAesGcmHexKey()
            RootValues.getInstance().hexIVAesGcm = getAesGcmHexIV()
            RootValues.getInstance().hexKeyAesCBC = getAesCBCHexKey()
            RootValues.getInstance().hexIVAesCBC = getAesCBCHexIV()

            // get Server Public Keys
            val keysPublicServer = getServerPublicKeys()
            val separator = ":::::::" //7 chars
            if (keysPublicServer != null) {
                val keysArray = keysPublicServer.split(separator)
                if (keysArray != null && keysArray.size > 0) {
                    if (RootValues.getInstance().keysPublicServerFromNdk == null) {
                        RootValues.getInstance().keysPublicServerFromNdk = ArrayList()
                    }
                    RootValues.getInstance().keysPublicServerFromNdk.addAll(keysArray)
                }
            }
        } catch (e: Exception) {
        }

    }
// NDK methods End
}