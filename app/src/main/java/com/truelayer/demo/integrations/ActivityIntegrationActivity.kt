package com.truelayer.demo.integrations

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.truelayer.demo.R
import com.truelayer.demo.databinding.ActivityIntegrationBinding
import com.truelayer.demo.payments.ProcessorContextProvider
import com.truelayer.demo.utils.PrefUtils
import com.truelayer.payments.core.domain.configuration.HttpConnectionConfiguration
import com.truelayer.payments.core.domain.configuration.HttpLoggingLevel
import com.truelayer.payments.core.domain.utils.Fail
import com.truelayer.payments.core.domain.utils.Ok
import com.truelayer.payments.ui.TrueLayerUI
import com.truelayer.payments.ui.screens.processor.ProcessorActivityContract
import com.truelayer.payments.ui.screens.processor.ProcessorResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Example integration of the SDK with the Activity component
 */
class ActivityIntegrationActivity : Activity() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var processorContextProvider: ProcessorContextProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        processorContextProvider = ProcessorContextProvider(PrefUtils.getQuickstartUrl(this))

        val binding = ActivityIntegrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialise the payments configuration
        TrueLayerUI.init(context = applicationContext) {
            environment = PrefUtils.getEnvironment(this@ActivityIntegrationActivity)
            httpConnection = HttpConnectionConfiguration(
                httpDebugLoggingLevel = HttpLoggingLevel.None
            )
        }

        binding.launchButton.setOnClickListener {
            scope.launch {
                launchPaymentFlow()
            }
        }
    }

    private suspend fun launchPaymentFlow() {
        val paymentType = PrefUtils.getPaymentType(this)
        // Create a payment context
        when (val processorContext = processorContextProvider.getProcessorContext(paymentType)) {
            is Ok -> {
                // Create an intent with the payment context to start the payment flow
                val intent = ProcessorActivityContract().createIntent(
                    this@ActivityIntegrationActivity,
                    processorContext.value
                )
                // Start activity for result to receive the results of the payment flow
                startActivityForResult(intent, 0)
            }
            is Fail -> withContext(Dispatchers.Main) {
                // Display error if payment context creation failed
                Toast.makeText(
                    this@ActivityIntegrationActivity,
                    getString(R.string.processor_context_error, processorContext.error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Handle the result returned from the SDK at the end of the payment flow
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Extract the result of the payment flow from the intent
        val result = ProcessorResult.unwrapResult(data)
        Toast.makeText(this, result.toString(), Toast.LENGTH_LONG).show()
    }
}
