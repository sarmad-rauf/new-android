package com.es.marocapp.model

import com.es.marocapp.model.responses.History

class CustomModelHistoryItem(var typeOfData : Int,
                             var date : String,
                             val historyList: History
                             )

data class History(
    val communicationchannel: String,
    val date: String,
    val fromaccount: String,
    val fromaccountholder: String,
    val fromamount: String,
    val fromavailablebalance: String,
    val fromcommittedbalance: String,
    val fromfee: String,
    val fromfri: String,
    val fromname: String,
    val fromnote: String,
    val fromposmsisdn: String,
    val fromtotalbalance: String,
    val initiatingaccountholder: String,
    val initiatinguser: String,
    val originalamount: String,
    val providercategory: String,
    val realuser: String,
    val toaccount: String,
    val toaccountholder: String,
    val toamount: String,
    val toavailablebalance: String,
    val tocommittedbalance: String,
    val tofee: String,
    val tofri: String,
    val tomessage: String,
    val toname: String,
    val toposmsisdn: String,
    val tototalbalance: String,
    val transactionid: String,
    val transactionstatus: String,
    val transfertype: String
)