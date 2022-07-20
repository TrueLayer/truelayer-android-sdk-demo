package com.truelayer.demo.payments.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model of the response when a payment's status is retrieved via the example-mobile-backend
 */
@Serializable
data class PaymentStatus(
    @SerialName("id")
    val id: String,
    @SerialName("status")
    val status: Status
) {

    @Serializable
    enum class Status {
        @SerialName("failed")
        FAILED,

        @SerialName("settled")
        SETTLED,

        @SerialName("executed")
        EXECUTED,

        @SerialName("authorized")
        AUTHORIZED,

        @SerialName("authorizing")
        AUTHORIZING,

        @SerialName("authorization_required")
        AUTHORIZATION_REQUIRED
    }
}
