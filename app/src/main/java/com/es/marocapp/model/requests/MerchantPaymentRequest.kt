package com.es.marocapp.model.requests

data class MerchantPaymentRequest(
    val amount: String,
    val context: String,
    val sender: String,
    val receiver: String
)