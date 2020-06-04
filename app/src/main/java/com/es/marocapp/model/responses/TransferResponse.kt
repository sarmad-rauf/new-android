package com.es.marocapp.model.responses

data class TransferResponse(
    val description: String,
    val extension: Any,
    val financialTransactionId: Any,
    val responseCode: String,
    val scheduledTransactionId: Any,
    val senderBalanceAfter: String,
    val senderFee: Any
)