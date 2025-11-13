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
import com.truelayer.payments.ui.screens.processor.ProcessorResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Example integration of the SDK with the AndroidX AppCompat Activity component
 */
class ActivityXIntegrationActivity : AppCompatActivity() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var newIntentConsumer: Consumer<Intent>? = null

    private lateinit var flow: ActivityResultLauncher<ProcessorContext>

    private var currentProcessorContext: ProcessorContext? = null

    companion object {
        const val TAG = "ActivityX"
    }

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
            tryHandleIntentWithRedirectFromBankData(intent, flow)
            this.intent = intent
        }
        newIntentConsumer?.let {
            addOnNewIntentListener(it)
        }

        flow = registerFlow()

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentProcessorContext?.let {
            try {
                val ctx = Json.encodeToString(it)
                outState.putString("processorContext", ctx)
            } catch (e: Throwable) {
                Log.e(TAG, e.toString())
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.getString("processorContext")?.let {
            try {
                currentProcessorContext = Json.Default.decodeFromString(it)
            } catch (e: Throwable) {
                // failed to decode
                Log.e(TAG, e.toString())
            }
        }
    }

    private fun tryHandleIntentWithRedirectFromBankData(intent: Intent, flow: ActivityResultLauncher<ProcessorContext>) {
        val params = intent.data.extractTrueLayerRedirectParams()
        val storedProcessorContext = PrefUtils.getProcessorContext(this)
        if (params.isNotEmpty() && storedProcessorContext != null &&
            (storedProcessorContext.id == params["payment_id"] || storedProcessorContext.id == params["mandate_id"])) {
            // The user is returning from the provider app
            // and the payment/mandate ID matches the one we have stored
            // so we can fetch the payment status
            currentProcessorContext = storedProcessorContext
            flow.launch(storedProcessorContext)
        } else {
            currentProcessorContext?.let {
                flow.launch(it)
            }
        }
    }

    private fun registerFlow(): ActivityResultLauncher<ProcessorContext> {
        // Create a contract to receive the results
        val contract = ProcessorActivityContract()
        // Handle the result returned from the SDK at the end of the payment flow
        return registerForActivityResult(contract) {
            if (it is ProcessorResult.Failure &&
                it.reason == ProcessorResult.FailureReason.Unknown && it.resultShown == ProcessorResult.ResultShown.None) {
                // in this case the Processor was terminated without setting a result
                // this is a common case when a redirect from bank is coming
                // and the same activity that is holding the SDK is not brought forward
                // but a new one is created. In such case it is safe to ignore it.
                return@registerForActivityResult
            }
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
