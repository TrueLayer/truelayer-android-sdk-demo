package com.truelayer.demo.payments

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.truelayer.demo.BuildConfig
import com.truelayer.demo.Configuration
import com.truelayer.demo.PaymentType
import com.truelayer.demo.payments.api.PaymentRequest
import com.truelayer.demo.payments.api.PaymentService
import com.truelayer.demo.payments.api.PaymentStatus
import com.truelayer.payments.core.domain.utils.Fail
import com.truelayer.payments.core.domain.utils.Ok
import com.truelayer.payments.core.domain.utils.Outcome
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.truelayer.payments.ui.screens.processor.ProcessorContext.PaymentContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.UUID

/**
 * Utility class used to create payments via the example-mobile-backedn for testing the integrations
 */
@Suppress("BlockingMethodInNonBlockingContext")
class PaymentContextProvider {

    // The redirect URI of this demo app that is specified in the AndroidManifest.xml
    private val redirectUri: String = "truelayer://demo"

    // The URI to the example-mobile-backend that is specified in build.gradle
    private val mobileBackendUrl: String = BuildConfig.MOBILE_BACKEND_URI

    private val jsonDefault = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    // Generates a payment context to be used for testing integrations
    suspend fun getPaymentContext(): Outcome<PaymentContext, Throwable> {
        val service = createPaymentService()
        val paymentRequest = createPaymentRequest()

        return try {
            val payment = when (Configuration.paymentType) {
                PaymentType.GBP -> service.createGBPPayment(paymentRequest)
                PaymentType.EUR -> service.createEuroPayment(paymentRequest)
                PaymentType.GBP_PRESELECTED -> service.createPreSelectedProviderPayment(
                    paymentRequest
                )
            }

            Ok(PaymentContext(payment.id, payment.resourceToken, redirectUri))
        } catch (e: Exception) {
            Fail(e)
        }
    }

    // Generates a payment context to be used for testing integrations and returns results with lambda
    @OptIn(DelicateCoroutinesApi::class)
    fun getPaymentContext(callback: (Outcome<PaymentContext, Throwable>) -> Unit) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                callback(getPaymentContext())
            }
        }
    }

    // Gets the status of the payment specified by the ID
    suspend fun getPaymentStatus(paymentId: String): Outcome<PaymentStatus, Throwable> {
        val service = createPaymentService()

        return try {
            val paymentStatus = service.getPaymentStatus(paymentId)
            Ok(paymentStatus)
        } catch (e: Exception) {
            Fail(e)
        }
    }

    // Creates a Retrofit service for the example-mobile-backend
    @OptIn(ExperimentalSerializationApi::class)
    private fun createPaymentService(): PaymentService {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        return Retrofit.Builder()
            .baseUrl(mobileBackendUrl)
            .client(client)
            .addConverterFactory(
                jsonDefault.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(PaymentService::class.java)
    }

    // Creates a new PaymentRequest object
    private fun createPaymentRequest(): PaymentRequest {
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
