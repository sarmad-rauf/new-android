package com.es.marocapp.model.requests

data class InitiateTransferRequest(
    val amount: String,
    val context: String,
    val identity: String,
    val message: String
)