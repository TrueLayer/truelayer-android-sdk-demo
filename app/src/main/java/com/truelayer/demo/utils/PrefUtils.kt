package com.truelayer.demo.utils

import android.content.Context
import com.truelayer.payments.core.domain.configuration.Environment
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
}
