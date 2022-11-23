package com.truelayer.demo.integrations

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.truelayer.demo.Configuration
import com.truelayer.demo.R
import com.truelayer.demo.databinding.ActivityIntegrationBinding
import com.truelayer.demo.payments.PaymentContextProvider
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
    private val paymentContextProvider = PaymentContextProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityIntegrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialise the payments configuration
        TrueLayerUI.init(context = applicationContext) {
            environment = Configuration.environment
            httpConnection = Configuration.httpConfig
        }

        binding.titleTextView.text = getString(R.string.integration_activity)
        binding.launchPaymentButton.setOnClickListener {
            scope.launch {
                launchPaymentFlow()
            }
        }
    }

    private suspend fun launchPaymentFlow() {
        // Create a payment context
        when (val paymentContext = paymentContextProvider.getPaymentContext()) {
            is Ok -> {
                // Create an intent with the payment context to start the payment flow
                val intent = ProcessorActivityContract().createIntent(
                    this@ActivityIntegrationActivity,
                    paymentContext.value
                )
                // Start activity for result to receive the results of the payment flow
                startActivityForResult(intent, 0)
            }
            is Fail -> withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@ActivityIntegrationActivity,
                    "Unable to get payment context: ${paymentContext.error}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Retrieve the result of the payment flow
        val result = ProcessorResult.unwrapResult(data)
        Toast.makeText(this, result.toString(), Toast.LENGTH_LONG).show()
    }
}
