package com.truelayer.demo.payments.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit service to use APIs from the example-mobile-backend
 */
interface PaymentService {

    // Gets the status of a payment by ID
    @GET("v3/payment/{id}")
    suspend fun getPaymentStatus(@Path("id") paymentId: String): PaymentStatus

    // Creates a new GBP payment
    @POST("v3/payment")
    suspend fun createGBPPayment(@Body paymentRequest: PaymentRequest): Payment

    // Creates a new Euro payment
    @POST("v3/payment/euro")
    suspend fun createEuroPayment(@Body paymentRequest: PaymentRequest): Payment

    // Creates a new GBP payment with a provider already selected
    @POST("v3/payment/provider")
    suspend fun createPreSelectedProviderPayment(@Body paymentRequest: PaymentRequest): Payment
}
