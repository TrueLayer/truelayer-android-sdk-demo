package com.truelayer.demo.payments.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model of the response when a payment is created via the example-mobile-backend
 */
@Serializable
data class Payment(
    @SerialName("id")
    val id: String,
    @SerialName("resource_token")
    val resourceToken: String
)
