package com.truelayer.demo.integrations

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Example integration of the SDK with the AndroidX AppCompat Activity component
 */
class ActivityXIntegrationActivity : AppCompatActivity() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var newIntentConsumer: Consumer<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("ActivityXIntegrationActivity", "onCreate: $intent")

        val binding = ActivityIntegrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialise the payments configuration
        TrueLayerUI.init(context = applicationContext) {
            environment = PrefUtils.getEnvironment(this@ActivityXIntegrationActivity)
            httpConnection = HttpConnectionConfiguration(
                httpDebugLoggingLevel = HttpLoggingLevel.None
            )
        }

        newIntentConsumer = Consumer<Intent> { intent ->
            // extract payment id
            Log.e("ActivityXIntegrationActivity", "newIntentConsumer: $intent")
            val flow = registerFlow()
            tryHandleIntentWithRedirectFromBankData(intent, flow)
        }
        newIntentConsumer?.let {
            addOnNewIntentListener(it)
        }

        val flow = registerFlow()

        tryHandleIntentWithRedirectFromBankData(intent, flow)

        binding.launchButton.setOnClickListener {
            scope.launch {
                startNewPayment(flow)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("ActivityXIntegrationActivity", "onDestroy")
        newIntentConsumer?.let { removeOnNewIntentListener(it) }
    }

    private fun tryHandleIntentWithRedirectFromBankData(intent: Intent, flow: ActivityResultLauncher<ProcessorContext>) {
        val params = intent.data.extractTrueLayerRedirectParams()
        val storedProcessorContext = PrefUtils.getProcessorContext(this)
        if (params.isNotEmpty() && storedProcessorContext != null &&
            (storedProcessorContext.id == params["payment_id"] || storedProcessorContext.id == params["mandate_id"])) {
            // The user is returning from the provider app
            // and the payment/mandate ID matches the one we have stored
            // so we can fetch the payment status
            flow.launch(storedProcessorContext)
        }
    }

    private fun registerFlow(): ActivityResultLauncher<ProcessorContext> {
        // Create a contract to receive the results
        val contract = ProcessorActivityContract()
        // Handle the result returned from the SDK at the end of the payment flow
        return registerForActivityResult(contract) {
            Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
            Log.e("ActivityXIntegrationActivity", it.toString())
        }
    }

    private suspend fun startNewPayment(flow: ActivityResultLauncher<ProcessorContext>) {
        val paymentType = PrefUtils.getPaymentType(this)

        val processorContextProvider = ProcessorContextProvider(PrefUtils.getQuickstartUrl(this))
        val processorContext = processorContextProvider.getProcessorContext(paymentType, this)

        // Create a payment context
        when (processorContext) {
            is Ok -> {
                PrefUtils.setIntegrationType(PrefUtils.IntegrationType.ACTIVITY_X, this@ActivityXIntegrationActivity)
                // Start the payment flow
                flow.launch(processorContext.value)
            }
            is Fail -> {
                // Display error if payment context creation failed
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ActivityXIntegrationActivity,
                        getString(R.string.processor_context_error, processorContext.error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
