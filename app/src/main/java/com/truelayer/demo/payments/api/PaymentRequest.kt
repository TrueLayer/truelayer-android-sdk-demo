package com.truelayer.demo.payments.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model of the request used to create a payment via the example-mobile-backend
 */
@Serializable
data class PaymentRequest(
    @SerialName("id")
    val id: String,
    @SerialName("amount_in_minor")
    val amountInMinor: String,
    @SerialName("payment_method")
    val paymentMethod: PaymentMethod,
    @SerialName("beneficiary")
    val beneficiary: Beneficiary
) {
    @Serializable
    data class PaymentMethod(
        @SerialName("statement_reference")
        val statementReference: String,
        @SerialName("type")
        val type: String
    )

    @Serializable
    data class Beneficiary(
        @SerialName("type")
        val type: String,
        @SerialName("name")
        val name: String,
        @SerialName("reference")
        val reference: String,
        @SerialName("scheme_identifier")
        val schemeIdentifier: SchemeIdentifier
    )

    @Serializable
    data class SchemeIdentifier(
        @SerialName("type")
        val type: String,
        @SerialName("account_number")
        val accountNumber: String,
        @SerialName("sort_code")
        val sortCode: String
    )
}
