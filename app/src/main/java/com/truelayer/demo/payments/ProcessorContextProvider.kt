package com.truelayer.demo.payments

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.truelayer.demo.payments.api.PaymentRequest
import com.truelayer.demo.payments.api.PaymentService
import com.truelayer.demo.utils.PrefUtils
import com.truelayer.payments.core.domain.utils.Fail
import com.truelayer.payments.core.domain.utils.Ok
import com.truelayer.payments.core.domain.utils.Outcome
import com.truelayer.payments.ui.screens.processor.ProcessorContext
import com.truelayer.payments.ui.screens.processor.ProcessorContext.PaymentContext
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.UUID

/**
 * Utility class used to create payments via the Payments Quickstart API for testing the integrations
 */
class ProcessorContextProvider(
    // The URI to the Payments Quickstart API
    private val apiURL: String,
) {

    // The redirect URI of this demo app that is specified in the AndroidManifest.xml
    // The same URI needs to be also registered in the TrueLayer console
    // in `App Settings` as `Allowed redirect URIs`.
    private val redirectUri: String = "truelayer://demo"

    private val jsonDefault = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    // Generates a payment context to be used for testing integrations
    suspend fun getProcessorContext(paymentType: PaymentType, context: Context): Outcome<ProcessorContext, Throwable> {
        val service = createPaymentService()
        val paymentRequest = createRequest()

        return try {
            val payment = when (paymentType) {
                PaymentType.GBP -> service.createGBPPayment(paymentRequest)
                PaymentType.EUR -> service.createEuroPayment(paymentRequest)
                PaymentType.GBP_PRESELECTED -> service.createPreSelectedProviderPayment(
                    paymentRequest
                )
                PaymentType.MANDATE -> service.createMandate(paymentRequest)
            }

            val processorContext = if (paymentType == PaymentType.MANDATE) {
                ProcessorContext.MandateContext(payment.id, payment.resourceToken, redirectUri)
            } else {
                PaymentContext(payment.id, payment.resourceToken, redirectUri)
            }

            PrefUtils.setProcessorContext(processorContext, context)

            Ok(processorContext)
        } catch (e: Exception) {
            Fail(e)
        }
    }

    // Generates a payment context to be used for testing integrations and returns results with lambda
    @OptIn(DelicateCoroutinesApi::class)
    fun getProcessorContext(paymentType: PaymentType, context: Context, callback: (Outcome<ProcessorContext, Throwable>) -> Unit) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                callback(getProcessorContext(paymentType, context))
            }
        }
    }

    // Creates a Retrofit service for the Payments Quickstart API
    private fun createPaymentService(): PaymentService {
        val interceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        return Retrofit.Builder()
            .baseUrl(apiURL)
            .client(client)
            .addConverterFactory(
                jsonDefault.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(PaymentService::class.java)
    }

    // Creates a new PaymentRequest object
    private fun createRequest(): PaymentRequest {
        return PaymentRequest(
            id = UUID.randomUUID().toString(),
            amountInMinor = "1",
            paymentMethod = PaymentRequest.PaymentMethod(
                statementReference = "some ref",
                type = "bank_transfer"
            ),
            beneficiary = PaymentRequest.Beneficiary(
                type = "external_account",
                name = "John Doe",
                reference = "Test Ref",
                schemeIdentifier = PaymentRequest.SchemeIdentifier(
                    type = "sort_code_account_number",
                    accountNumber = "12345677",
                    sortCode = "123456"
                )
            )
        )
    }
}
