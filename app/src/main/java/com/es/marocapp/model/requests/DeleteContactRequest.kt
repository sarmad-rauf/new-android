package com.es.marocapp.model.requests

data class DeleteContactRequest(
    val contactIdentity : String,
    val context: String
)