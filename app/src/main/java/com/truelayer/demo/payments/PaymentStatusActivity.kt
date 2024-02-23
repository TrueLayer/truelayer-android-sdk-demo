package com.truelayer.demo.payments

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.truelayer.demo.utils.PrefUtils
import com.truelayer.payments.core.utils.extractTrueLayerRedirectParams
import com.truelayer.payments.ui.screens.processor.Processor
import com.truelayer.payments.ui.theme.Theme
import com.truelayer.payments.ui.theme.stackNavigation

/**
 * Activity to receive redirects from the provider app and fetches the payment's status
 */
class PaymentStatusActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Extract the payment/mandate parameters from the URL
        val params = intent.data.extractTrueLayerRedirectParams()
        val processorContext = PrefUtils.getProcessorContext(this)

        setContent {
            val resourceId = params["payment_id"] ?: params["mandate_id"]

            val activity = this

            Theme(
                navigationTransition = { current, transition, direction ->
                    stackNavigation(current, transition, direction)
                }
            ) {
                if(resourceId != null && processorContext != null && processorContext.id == resourceId) {
                    // Display the payment result screen or handle any subsequent actions
                    Processor(
                        context = processorContext,
                        onSuccess = {
                            activity.finish()
                        },
                        onFailure = {
                            activity.finish()
                        }
                    )
                }
                else {
                    AlertDialog(
                        title = { Text(text = "Whoops", style = MaterialTheme.typography.titleMedium) },
                        text = { Text(text = "Error getting payment result") },
                        confirmButton = {
                            TextButton(onClick = { activity.finish() }) {
                                Text("Close")
                            }
                        },
                        onDismissRequest = { activity.finish() }
                    )
                }
            }
        }
    }
}
