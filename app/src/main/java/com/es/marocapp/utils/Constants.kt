package com.es.marocapp.utils

import android.app.Application
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.os.Build
import android.text.format.Formatter.formatIpAddress
import android.util.Log
import com.es.marocapp.locale.LanguageData
import com.es.marocapp.locale.LocaleManager
import com.es.marocapp.model.CustomModelHistoryItem
import com.es.marocapp.model.responses.Account
import com.es.marocapp.model.responses.BalanceInfoAndLimitResponse
import com.es.marocapp.model.responses.Contact
import com.es.marocapp.model.responses.LoginWithCertResponse
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


object Constants {

    //Error Msgs
    val SHOW_DEFAULT_ERROR = "SHOW_DEFAULT_ERROR"
    val SHOW_INTERNET_ERROR = "Please check your internet!"
    val SHOW_SERVER_ERROR = "Something went wrong!"

    const val IDENTIFICATION_TYPE = "CNIC"
    const val SECRET_TYPE = "password"
    const val TRANSFER_TYPE_PAYMENT = "INTEROP_TRANSFER"
    const val MERCHANT_TYPE_PAYMENT = "INTEROP_TRANSFER_SEND"
    const val TYPE_PAYMENT = "PAYMENT"
    const val TYPE_BILL_PAYMENT = "BILL_PAYMENT"
    const val TYPE_COMMISSIONING = "COMMISSIONING"
    const val OPERATION_TYPE_CREANCIER = "creancier"
    const val OPERATION_TYPE_CREANCE = "creance"
    const val OPERATION_TYPE_IMPAYES = "impayes"
    const val TYPE_CASH_IN = "CASH_IN"

    //preLoginData
    var APP_DATE_FORMAT = "yyyy-mm-dd"
    var APP_CN_REGEX = "[a-zA-Z]{2}[0-9]{6}"
    var APP_CN_LENGTH = "8"
    var APP_MSISDN_PREFIX = "+000"
    var APP_MSISDN_LENGTH = "12"
    var APP_MSISDN_REGEX = ""
    var APP_CIL_LENGTH = "6"
    var APP_CIL_REGEX = ""
    var quickAmountsList : ArrayList<String> = arrayListOf()
    var quickRechargeAmountsList : ArrayList<String> = arrayListOf()
    var URL_FOR_FAQ =""
    var URL_FOR_TERMSANDCONDITIONS =""
    var APP_VERSION =""
    var URL_FOR_UPDATE_APP =""


    var APPLICATION_IP_ADDRESS = ""
    var CURRENT_DEVICE_ID = ""
    var CURRENT_NUMBER_DEVICE_ID = ""
    var CURRENT_CURRENCY_TYPE = ""
    var CURRENT_CURRENCY_TYPE_TO_SHOW = ""
    var AMOUNT_CONVERSION_VALUE= ""
    var HELPLINE_NUMBER= ""

    var HEADERS_AFTER_LOGINS = false
    var HEADERS_FOR_PAYEMNTS = false
    var CURRENT_USER_MSISDN = ""
    var CURRENT_USER_CREDENTIAL = ""
    var LOGGED_IN_USER = ""
    var LOGGED_IN_USER_COOKIE = ""

    //USER_PROFILE
    var IS_AGENT_USER = false
    var IS_CONSUMER_USER = false
    var IS_MERCHANT_USER = false

    var IS_DEFAULT_ACCOUNT_SET = false
    var IS_FIRST_TIME = true
    
    //Responses
    lateinit var balanceInfoAndResponse : BalanceInfoAndLimitResponse
      var getAccountsResponse : Account? =null
    lateinit var getAccountsResponseArray : ArrayList<Account>
    lateinit var loginWithCertResponse : LoginWithCertResponse
    lateinit var currentTransactionItem : CustomModelHistoryItem
    var mContactListArray : ArrayList<Contact> = arrayListOf()

    fun getCurrentDate() : String{
        val calendar = Calendar.getInstance(TimeZone.getDefault())

        val currentYear = calendar[Calendar.YEAR].toString()
        val currentMonth = (calendar[Calendar.MONTH] + 1).toString()
        val currentDay = calendar[Calendar.DAY_OF_MONTH].toString()
        var formattedDate = "$currentYear-$currentMonth-$currentDay"
        return formattedDate
    }

    fun createUserToken() : String{
        var token = SimpleDateFormat("yyyyMMddHHmmssSS")
            .format(Date()) + Random()
            .nextInt(999998) + "($LOGGED_IN_USER)"
        return token
    }

    fun getSelectedLanguage() : String{
        return LocaleManager.selectedLanguage
    }

    fun createUserLoggedInToken() : String{
        var token = SimpleDateFormat("yyyyMMddHHmmssSS")
            .format(Date()) + Random()
            .nextInt(999998) + "($CURRENT_USER_MSISDN)"
        return token
    }

    fun getIPAddress(application: Application){
        val wm =
            application.getSystemService(WIFI_SERVICE) as WifiManager?
        APPLICATION_IP_ADDRESS = formatIpAddress(wm!!.connectionInfo.ipAddress)
    }

    fun setBase64EncodedString(str : String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LOGGED_IN_USER_COOKIE = Base64.getEncoder().encodeToString(str.toByteArray())
            Log.d("Base64",Base64.getEncoder().encodeToString(str.toByteArray()));
        } else {
            LOGGED_IN_USER_COOKIE = android.util.Base64.encodeToString(str.toByteArray(), android.util.Base64.NO_WRAP)
        }
        Log.d("Base64",android.util.Base64.encodeToString(str.toByteArray(), android.util.Base64.NO_WRAP));


    }

    fun getBase64EncryptedToString(encrptedString : String) : String{
        val decodedBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getDecoder().decode(encrptedString)
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val decodedString = String(decodedBytes)
        return decodedString
    }

    fun getNumberMsisdn(number : String) : String{
        return "$number/MSISDN"
    }

    fun getTransferReceiverAlias(number: String) : String{
        return "$number@hpss.sub.sp/SP"
    }

    fun getMerchantReceiverAlias(number: String) : String{
        return "$number@hpss.mer.sp/SP"
    }

    fun getAirTimeReceiverAlias(number : String) : String{
        return "$number@ocs.prepaid.sp/SP"
    }

    fun getAgentReceiverAlias(number: String) : String{
        return "$number@hpss.mer.sp/SP"
    }

    fun getPostPaidMobileDomainAlias(number: String) : String{
        return "$number@bscs.mobile.sp/SP"
    }

    fun getPostPaidFixedDomainAlias(number: String) : String{
        return "$number@bscs.fixed.sp/SP"
    }

    fun getPostPaidInternetDomainAlias(number: String) : String{
        return "$number@bscs.internet.sp/SP"
    }

    fun getFatoratiAlias(number: String) :String{
        return "$number@fatourati.sp/SP"
    }

    fun addAmountAndFee(amount : Double, fee : Double): String{
        return (amount+fee).toString()
    }

    fun parseDateFromString(dateString : String) : String{
        var myDate = ""
        var df: DateFormat = SimpleDateFormat("yyyyMMdd")
        val d: Date
        try {
            d = df.parse(dateString)
            df = SimpleDateFormat("dd/MM/yyyy")
            myDate= df.format(d)
        } catch (e: ParseException) {
        }

        return myDate
    }

    fun getMonthFromParsedDate(date : String) : String{
        val d = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(date)
        val cal = Calendar.getInstance()
        cal.time = d
        return SimpleDateFormat("MMMM").format(cal.time)
    }

    fun converValueToTwoDecimalPlace(value : Double) : String{
        val result = java.lang.String.format("%.2f", value)
        return result
    }

    fun getZoneFormattedDateAndTime(dateToFormat : String): String {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val output = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")

        var d: Date? = null
        try {
            d = input.parse("2018-02-02T06:54:57.744Z")
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val formatted: String = output.format(d)
        Log.i("DATE", "" + formatted)
        return formatted
    }
}