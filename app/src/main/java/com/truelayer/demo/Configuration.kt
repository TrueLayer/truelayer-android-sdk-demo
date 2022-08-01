package com.truelayer.demo

import com.truelayer.payments.core.domain.configuration.Environment
import com.truelayer.payments.core.domain.configuration.HttpConnectionConfiguration
import com.truelayer.payments.core.domain.configuration.HttpLoggingLevel

/**
 * Configuration object used to specify options for the SDK examples
 */
object Configuration {
    @JvmStatic
    val httpConfig = HttpConnectionConfiguration(
        timeoutMs = 5000,
        httpDebugLoggingLevel = HttpLoggingLevel.Body
    )

    @JvmStatic
    val environment = Environment.DEVELOPMENT

    @JvmStatic
    val paymentType = PaymentType.GBP
}

enum class PaymentType {
    EUR,
    GBP,
    GBP_PRESELECTED
}
