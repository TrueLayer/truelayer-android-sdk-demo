package com.truelayer.demo.integrations

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.truelayer.demo.Configuration
import com.truelayer.demo.databinding.ActivityIntegrationBinding
import com.truelayer.demo.payments.ProcessorContextProvider
import com.truelayer.demo.utils.PrefUtils
import com.truelayer.payments.core.domain.utils.Fail
import com.truelayer.payments.core.domain.utils.Ok
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
    private lateinit var processorContextProvider: ProcessorContextProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        processorContextProvider = ProcessorContextProvider(PrefUtils.getQuickstartUrl(this))

        val binding = ActivityIntegrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialise the payments configuration
        TrueLayerUI.init(context = applicationContext) {
            environment = PrefUtils.getEnvironment(this@ActivityXIntegrationActivity)
            httpConnection = Configuration.httpConfig
        }

        // Create a contract to receive the results
        val contract = ProcessorActivityContract()

        // Handle the result when returned at the end of the payment flow
        val flow = registerForActivityResult(contract) {
            Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
        }

        binding.launchButton.setOnClickListener {
            scope.launch {
                launchFlow(flow)
            }
        }
    }

    private suspend fun launchFlow(flow: ActivityResultLauncher<ProcessorContext>) {
        // Create a payment context
        when (val processorContext = processorContextProvider.getProcessorContext()) {
            is Ok -> {
                // Start the payment flow
                flow.launch(processorContext.value)
            }
            is Fail -> {
                // Display error if payment context creation failed
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ActivityXIntegrationActivity,
                        "Unable to get processor context: ${processorContext.error}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
