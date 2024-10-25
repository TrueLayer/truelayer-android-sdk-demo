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
import com.truelayer.payments.core.utils.extractTrueLayerRedirectParams
import com.truelayer.payments.ui.TrueLayerUI
import com.truelayer.payments.ui.screens.processor.ProcessorActivityContract
import com.truelayer.payments.ui.screens.processor.ProcessorContext
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityIntegrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialise the payments configuration
        TrueLayerUI.init(context = applicationContext) {
            environment = PrefUtils.getEnvironment(this@ActivityIntegrationActivity)
            httpConnection = HttpConnectionConfiguration(
                httpDebugLoggingLevel = HttpLoggingLevel.None
            )
        }

        tryHandleIntentWithRedirectFromBankData(intent)

        binding.launchButton.setOnClickListener {
            scope.launch {
                launchPaymentFlow()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            tryHandleIntentWithRedirectFromBankData(it)
        }
    }

    private fun tryHandleIntentWithRedirectFromBankData(intent: Intent) {
        val params = intent.data.extractTrueLayerRedirectParams()
        val storedProcessorContext = PrefUtils.getProcessorContext(this)
        if (params.isNotEmpty() && storedProcessorContext != null &&
            (storedProcessorContext.id == params["payment_id"] || storedProcessorContext.id == params["mandate_id"])) {
            // The user is returning from the provider app
            // and the payment/mandate ID matches the one we have stored
            // so we can fetch the payment status
            startPaymentActivity(storedProcessorContext)
        }
    }

    private fun startPaymentActivity(processorContext: ProcessorContext) {
        // Create an intent with the payment context to start the payment flow
        val intent = ProcessorActivityContract().createIntent(this, processorContext)
        // Start activity for result to receive the results of the payment flow
        startActivityForResult(intent, 0)
    }

    private suspend fun launchPaymentFlow() {
        val paymentType = PrefUtils.getPaymentType(this)
        // Create a payment context
        val processorContextProvider = ProcessorContextProvider(PrefUtils.getQuickstartUrl(this))
        when (val processorContext = processorContextProvider.getProcessorContext(paymentType, this)) {
            is Ok -> {
                PrefUtils.setIntegrationType(PrefUtils.IntegrationType.ACTIVITY, this@ActivityIntegrationActivity)
                startPaymentActivity(processorContext.value)
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
