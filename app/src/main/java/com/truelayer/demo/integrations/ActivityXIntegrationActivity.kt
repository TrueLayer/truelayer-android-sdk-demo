package com.truelayer.demo.integrations

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.truelayer.demo.Configuration
import com.truelayer.demo.R
import com.truelayer.demo.databinding.ActivityIntegrationBinding
import com.truelayer.demo.payments.PaymentContextProvider
import com.truelayer.payments.core.domain.utils.Fail
import com.truelayer.payments.core.domain.utils.Ok
import com.truelayer.payments.ui.TrueLayerUI
import com.truelayer.payments.ui.models.PaymentContext
import com.truelayer.payments.ui.screens.coordinator.FlowCoordinatorActivityContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Example integration of the SDK with the AndroidX AppCompat Activity component
 */
class ActivityXIntegrationActivity : AppCompatActivity() {
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

        // Create a contract to receive the results
        val contract = FlowCoordinatorActivityContract()

        // Handle the result when returned at the end of the payment flow
        val flow = registerForActivityResult(contract) {
            Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
        }

        binding.titleTextView.text = getString(R.string.integration_activityx)
        binding.launchPaymentButton.setOnClickListener {
            scope.launch {
                launchPaymentFlow(flow)
            }
        }
    }

    private suspend fun launchPaymentFlow(flow: ActivityResultLauncher<PaymentContext>) {
        // Create a payment context
        when (val paymentContext = paymentContextProvider.getPaymentContext()) {
            is Ok -> {
                // Start the payment flow
                flow.launch(paymentContext.value)
            }
            is Fail -> {
                // Display error if payment context creation failed
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ActivityXIntegrationActivity,
                        "Unable to get payment context: ${paymentContext.error}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
