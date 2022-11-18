package com.truelayer.demo

import com.truelayer.payments.core.domain.configuration.HttpConnectionConfiguration
import com.truelayer.payments.core.domain.configuration.HttpLoggingLevel

/**
 * Configuration object used to specify options for the SDK examples
 */
object Configuration {
    @JvmStatic
    val httpConfig = HttpConnectionConfiguration(
        timeoutMs = 45000,
        httpDebugLoggingLevel = HttpLoggingLevel.Body
    )

    @JvmStatic
    val paymentType = PaymentType.GBP
}

enum class PaymentType {
    EUR,
    GBP,
    GBP_PRESELECTED,
    MANDATE
}
