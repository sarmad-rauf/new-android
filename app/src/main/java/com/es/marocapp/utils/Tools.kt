package com.es.marocapp.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.util.Log
import com.es.marocapp.security.EncryptionUtils
import com.es.marocapp.utils.PrefUtils.PreKeywords.PREF_KEY_IS_FIRSTTIME
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import java.text.DecimalFormat

object Tools {

    fun hasValue(value: String?): Boolean {
        try {
            if (value != null && !value.equals("null", ignoreCase = true) && !value.equals(
                    "",
                    ignoreCase = true
                )
            ) {
                return true
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }

    fun checkNetworkStatus(context: Context): Boolean {
        var isNetwork = false
        try {
            if (context != null) {
                val connMgr =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork = connMgr?.getActiveNetworkInfo()
                if (activeNetwork != null) {
                    // connected to the internet
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        // connected to wifi
                        isNetwork = true
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        // connected to mobile data
                        isNetwork = true
                    }
                } else {
                    // not connected to the internet
                    isNetwork = false
                }
            }
        } catch (e: Exception) {
        }
        return isNetwork
    }

    fun generateQR(texto: String): Bitmap? {
        val writer = QRCodeWriter()
        try {
            val bitMatrix: BitMatrix = writer.encode(texto, BarcodeFormat.QR_CODE, 150, 150)
            val width = 150
            val height = 150
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (bitMatrix.get(x, y)) bmp.setPixel(x, y, Color.BLACK) else bmp.setPixel(
                        x,
                        y,
                        Color.WHITE
                    )
                }
            }
            return bmp;
        } catch (e: WriterException) {
            Log.e("QR ERROR", e.toString())
            return null
        }
    }

    fun generateEMVcoString(number: String, enteredAmount: String): String {
        var amount = enteredAmount
        var amountTag = ""
        var purposeOfTransaction = ""
        var pointOfInitiationMethod = ""
        if (!amount.isNullOrBlank()) {
            var df = DecimalFormat("00000")
            amount = df.format(Integer.parseInt(amount)).toString()
            amountTag =
                Constants.EMVco.Amount_Transaction_ID + Constants.EMVco.Amount_Transaction_SIZE + amount
            purposeOfTransaction = Constants.EMVco.dynamic
            pointOfInitiationMethod = Constants.EMVco.Point_Of_Initiation_Method_VALUE
        } else {
            purposeOfTransaction = Constants.EMVco.static
            pointOfInitiationMethod = Constants.EMVco.Point_Of_Initiation_Method_VALUE_STATIC
        }

        var Paid_Entity_Reference_VALUE = EncryptionUtils.encryptStringAESCBC(number)
        var Masked_Paid_Entity_Reference_VALUE = ""

        Masked_Paid_Entity_Reference_VALUE =
            number.substring(0, 4) + "######" + number.substring(10)
/*
        val myName = StringBuilder(number)
        myName.setCharAt(4, '#')
        myName.setCharAt(5, '#')
        myName.setCharAt(6, '#')
        myName.setCharAt(7, '#')
        myName.setCharAt(8, '#')
        myName.setCharAt(9, '#')
        Masked_Paid_Entity_Reference_VALUE=newName.toString()*/

        var qrString =
            Constants.EMVco.Payload_Format_Indicator_ID + Constants.EMVco.Payload_Format_Indicator_SIZE + Constants.EMVco.Payload_Format_Indicator_VALUE +
                    Constants.EMVco.Point_Of_Initiation_Method_ID + Constants.EMVco.Point_Of_Initiation_Method_SIZE + pointOfInitiationMethod +
                    Constants.EMVco.Merchant_Account_Information_ID + Constants.EMVco.Merchant_Account_Information_SIZE + Constants.EMVco.Merchant_Account_Information_Value +
                    Constants.EMVco.Globally_Unique_Identifier_ID + Constants.EMVco.Globally_Unique_Identifier_SIZE + Constants.EMVco.Globally_Unique_Identifier_VALUE +
                    Constants.EMVco.Encryption_Format_ID + Constants.EMVco.Encryption_Format_SIZE + Constants.EMVco.Encryption_Format_VALUE +
                    Constants.EMVco.Paid_Entity_Reference_Format_ID + Constants.EMVco.Paid_Entity_Reference_Format_SIZE + Constants.EMVco.Paid_Entity_Reference_Format_VALUE +
                    Constants.EMVco.Paid_Entity_Reference_ID + Constants.EMVco.Paid_Entity_Reference_SIZE + Paid_Entity_Reference_VALUE +
                    Constants.EMVco.Masked_Paid_Entity_Reference_ID + Constants.EMVco.Masked_Paid_Entity_Reference_SIZE_13 + "+" + Masked_Paid_Entity_Reference_VALUE +
                    Constants.EMVco.Currency_Transaction_ID + Constants.EMVco.Currency_Transaction_SIZE + Constants.EMVco.Currency_Transaction_VALUE +
                    amountTag + purposeOfTransaction
        /*Constants.EMVco.Unreserved_Template_ID + Constants.EMVco.Unreserved_Template_SIZE + Constants.EMVco.Unreserved_Template_VALUE +
        Constants.EMVco.Unreserved_Globally_Unique_Identifier_ID + Constants.EMVco.Unreserved_Globally_Unique_Identifier_SIZE + Constants.EMVco.Unreserved_Globally_Unique_Identifier_VALUE +
        Constants.EMVco.Operation_Type_ID + Constants.EMVco.Operation_Type_SIZE + Constants.EMVco.Operation_Type_VALUE +
        Constants.EMVco.Signature_Format_ID + Constants.EMVco.Signature_Format_SIZE + Constants.EMVco.Signature_Format_VALUE +
        Constants.EMVco.QR_Version_ID + Constants.EMVco.QR_Version_SIZE + Constants.EMVco.QR_Version_VALUE +
        Constants.EMVco.QR_Instance_ID + Constants.EMVco.QR_Instance_SIZE + Constants.EMVco.QR_Instance_VALUE*/

        val generatedCRC = generateChecksumCRC16(qrString.toByteArray())

        qrString += Integer.toHexString(generatedCRC)

        return qrString
    }

    fun extractNumberFromEMVcoQR(text: String): String {
        var num = ""
        try {
            if (text.contains(Constants.EMVco.Payload_Format_Indicator_ID + Constants.EMVco.Payload_Format_Indicator_SIZE + Constants.EMVco.Payload_Format_Indicator_VALUE)) {
                num =
                    text.split(Constants.EMVco.Paid_Entity_Reference_ID + Constants.EMVco.Paid_Entity_Reference_SIZE)[1]
                if (text.contains(Constants.EMVco.Masked_Paid_Entity_Reference_ID + Constants.EMVco.Masked_Paid_Entity_Reference_SIZE_12)) {
                    num =
                        num.split(Constants.EMVco.Masked_Paid_Entity_Reference_ID + Constants.EMVco.Masked_Paid_Entity_Reference_SIZE_12)[0]
                    num = EncryptionUtils.decryptStringAESCBC(num)
                } else if (text.contains(Constants.EMVco.Masked_Paid_Entity_Reference_ID + Constants.EMVco.Masked_Paid_Entity_Reference_SIZE_13)) {
                    num =
                        num.split(Constants.EMVco.Masked_Paid_Entity_Reference_ID + Constants.EMVco.Masked_Paid_Entity_Reference_SIZE_13)[0]
                    num = EncryptionUtils.decryptStringAESCBC(num)
                } else {
                    num = ""
                }
            }
        } catch (e: Exception) {
            num = ""
        }
        Log.d("DecryptedNumber", num)
        return num
    }

    fun extractAmountFromEMVcoQR(text: String): String {
        var amount = ""
        try {
            if (text.contains(Constants.EMVco.Payload_Format_Indicator_ID + Constants.EMVco.Payload_Format_Indicator_SIZE + Constants.EMVco.Payload_Format_Indicator_VALUE)) {
                amount =
                    text.split(Constants.EMVco.Amount_Transaction_ID + Constants.EMVco.Amount_Transaction_SIZE)[1]

                amount = amount.substring(0, 5)
            }
        } catch (e: Exception) {
            amount = ""
        }
        Log.d("Amount", amount)
        return amount
    }

    fun extractPointOfInitiationFromEMVcoQR(text: String) {
        var pointOfInitiation = ""
        try {
            if (text.contains(Constants.EMVco.Payload_Format_Indicator_ID + Constants.EMVco.Payload_Format_Indicator_SIZE + Constants.EMVco.Payload_Format_Indicator_VALUE)) {
                pointOfInitiation =
                    text.split(Constants.EMVco.Point_Of_Initiation_Method_ID + Constants.EMVco.Point_Of_Initiation_Method_SIZE)[1].substring(
                        0,
                        2
                    )

                if (pointOfInitiation == "11") {
                    Log.d("PointOfInitiation", "Static")
                } else {
                    Log.d("PointOfInitiation", "Dynamic")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun generateMerchantEMVcoString(number: String, enteredAmount: String): String {
        var amount = enteredAmount
        var amountTag = ""
        var CRC = ""
        var pointOfInitiationMethod = ""
        if (!amount.isNullOrBlank()) {
            var df = DecimalFormat("00000")
            amount = df.format(Integer.parseInt(amount)).toString()
            amountTag =
                Constants.MerchantEMVco.Amount_Transaction_ID + Constants.MerchantEMVco.Amount_Transaction_SIZE + amount
            CRC = Constants.MerchantEMVco.dynamic
            pointOfInitiationMethod = Constants.MerchantEMVco.Point_Of_Initiation_Method_VALUE
        } else {
            CRC = Constants.MerchantEMVco.static
            pointOfInitiationMethod =
                Constants.MerchantEMVco.Point_Of_Initiation_Method_VALUE_STATIC
        }

        var Paid_Entity_Reference_VALUE = EncryptionUtils.encryptStringAESCBC(number)
        var Masked_Paid_Entity_Reference_VALUE = ""

        Masked_Paid_Entity_Reference_VALUE =
            number.substring(0, 4) + "######" + number.substring(10)

        var qrString =
            Constants.MerchantEMVco.Payload_Format_Indicator_ID + Constants.MerchantEMVco.Payload_Format_Indicator_SIZE + Constants.MerchantEMVco.Payload_Format_Indicator_VALUE +
                    Constants.MerchantEMVco.Point_Of_Initiation_Method_ID + Constants.MerchantEMVco.Point_Of_Initiation_Method_SIZE + pointOfInitiationMethod +
                    Constants.MerchantEMVco.Merchant_Account_Information_ID + Constants.MerchantEMVco.Merchant_Account_Information_SIZE + Constants.MerchantEMVco.Merchant_Account_Information_Value +
                    Constants.MerchantEMVco.Globally_Unique_Identifier_ID + Constants.MerchantEMVco.Globally_Unique_Identifier_SIZE + Constants.MerchantEMVco.Globally_Unique_Identifier_VALUE +
                    Constants.MerchantEMVco.Encryption_Format_ID + Constants.MerchantEMVco.Encryption_Format_SIZE + Constants.MerchantEMVco.Encryption_Format_VALUE +
                    Constants.MerchantEMVco.Paid_Entity_Reference_Format_ID + Constants.MerchantEMVco.Paid_Entity_Reference_Format_SIZE + Constants.MerchantEMVco.Paid_Entity_Reference_Format_VALUE +
                    Constants.MerchantEMVco.Paid_Entity_Reference_ID + Constants.MerchantEMVco.Paid_Entity_Reference_SIZE + Paid_Entity_Reference_VALUE +
                    Constants.MerchantEMVco.Masked_Paid_Entity_Reference_ID + Constants.MerchantEMVco.Masked_Paid_Entity_Reference_SIZE_13 + "+" + Masked_Paid_Entity_Reference_VALUE +
                    Constants.MerchantEMVco.Merchant_Category_Code_ID + Constants.MerchantEMVco.Merchant_Category_Code_SIZE + Constants.MerchantEMVco.Merchant_Category_Code_VALUE +
                    Constants.MerchantEMVco.Currency_Transaction_ID + Constants.MerchantEMVco.Currency_Transaction_SIZE + Constants.MerchantEMVco.Currency_Transaction_VALUE +
                    amountTag +
                    Constants.MerchantEMVco.Country_Code_ID + Constants.MerchantEMVco.Country_Code_SIZE + Constants.MerchantEMVco.Country_Code_VALUE +
                    Constants.MerchantEMVco.Merchant_Name_ID + Constants.MerchantEMVco.Merchant_Name_SIZE + Constants.MerchantEMVco.Merchant_Name_VALUE +
                    Constants.MerchantEMVco.Merchant_City_ID + Constants.MerchantEMVco.Merchant_City_SIZE + Constants.MerchantEMVco.Merchant_City_VALUE + CRC
        /*Constants.MerchantEMVco.Unreserved_Template_ID + Constants.MerchantEMVco.Unreserved_Template_SIZE + Constants.MerchantEMVco.Unreserved_Template_VALUE +
        Constants.MerchantEMVco.Unreserved_Globally_Unique_Identifier_ID + Constants.MerchantEMVco.Unreserved_Globally_Unique_Identifier_SIZE + Constants.MerchantEMVco.Unreserved_Globally_Unique_Identifier_VALUE +
        Constants.MerchantEMVco.Operation_Type_ID + Constants.MerchantEMVco.Operation_Type_SIZE + Constants.MerchantEMVco.Operation_Type_VALUE +
        Constants.MerchantEMVco.Signature_Format_ID + Constants.MerchantEMVco.Signature_Format_SIZE + Constants.MerchantEMVco.Signature_Format_VALUE +
        Constants.MerchantEMVco.QR_Version_ID + Constants.MerchantEMVco.QR_Version_SIZE + Constants.MerchantEMVco.QR_Version_VALUE +
        Constants.MerchantEMVco.QR_Instance_ID + Constants.MerchantEMVco.QR_Instance_SIZE + Constants.MerchantEMVco.QR_Instance_VALUE*/

        val generatedCRC = generateChecksumCRC16(qrString.toByteArray())

        qrString += Integer.toHexString(generatedCRC)

        return qrString
    }

    private fun generateChecksumCRC16(bytes: ByteArray): Int {
        var crc = 0xFFFF
        var temp: Int
        var crcByte: Int
        for (byteIndex in bytes.indices) {
            crcByte = bytes[byteIndex].toInt()
            for (bit_index in 0..7) {
                temp = crc shr 15 xor (crcByte shr 7)
                crc = crc shl 1
                crc = crc and 0xFFFF
                if (temp > 0) {
                    crc = crc xor 0x1021
                    crc = crc and 0xFFFF
                }
                crcByte = crcByte shl 1
                crcByte = crcByte and 0xFF
            }
        }
        return crc
    }

    fun openDialerWithNumber(context: Context) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:" + Constants.HELPLINE_NUMBER)
        context.startActivity(intent)
    }

    fun isFirstTime(context: Context): Boolean {

        if (PrefUtils.getBoolean(context, PREF_KEY_IS_FIRSTTIME, true)) {
            //the app is being launched for first time, do something
            Log.d("Comments", "First time")

            // record the fact that the app has been started at least once
            PrefUtils.addBoolean(context, PREF_KEY_IS_FIRSTTIME, false)

            return true
        } else {
            return false
        }
    }
}