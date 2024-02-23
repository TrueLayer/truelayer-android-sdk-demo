package com.truelayer.demo.utils

import android.content.Context
import com.truelayer.demo.payments.PaymentType
import com.truelayer.payments.core.domain.configuration.Environment
import com.truelayer.payments.ui.screens.processor.ProcessorContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.lang.IllegalArgumentException

/**
 * Utility to store configuration options in shared preferences
 */
object PrefUtils {

    @JvmStatic
    fun setQuickstartUrl(address: String, context: Context) {
        val sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("quickstartUrl", address)
            apply()
        }
    }

    @JvmStatic
    fun getQuickstartUrl(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return sharedPreferences.getString("quickstartUrl", null) ?: ""
    }

    @JvmStatic
    fun setEnvironment(environment: Environment, context: Context) {
        val sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("environment", environment.name)
            apply()
        }
    }

    @JvmStatic
    fun getEnvironment(context: Context): Environment {
        val sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return sharedPreferences.getString("environment", "")?.let {
            try {
                Environment.valueOf(it)
            } catch (_: IllegalArgumentException) {
                sharedPreferences.edit().remove("environment").apply()
                Environment.SANDBOX
            }
        } ?: Environment.SANDBOX
    }

    @JvmStatic
    fun setPaymentType(paymentType: PaymentType, context: Context) {
        val sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("payment-type", paymentType.name)
            apply()
        }
    }

    @JvmStatic
    fun getPaymentType(context: Context): PaymentType {
        val sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return sharedPreferences.getString("payment-type", "")?.let {
            try {
                PaymentType.valueOf(it)
            } catch (_: IllegalArgumentException) {
                sharedPreferences.edit().remove("payment-type").apply()
                PaymentType.GBP
            }
        } ?: PaymentType.GBP
    }

    @JvmStatic
    fun setProcessorContext(processorContext: ProcessorContext, context: Context) {
        val sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("processorContext", Json.Default.encodeToString(processorContext))
            apply()
        }
    }

    @JvmStatic
    fun getProcessorContext(context: Context): ProcessorContext? {
        val sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return sharedPreferences.getString("processorContext", null)?.let {
            Json.Default.decodeFromString(it)
        }
    }
}
