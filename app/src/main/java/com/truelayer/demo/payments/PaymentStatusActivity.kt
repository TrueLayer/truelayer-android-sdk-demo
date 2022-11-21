package com.truelayer.demo.payments

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.truelayer.demo.R
import com.truelayer.demo.payments.api.PaymentStatus
import com.truelayer.demo.utils.PrefUtils
import com.truelayer.payments.core.utils.extractTrueLayerRedirectParams
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

        setContent {
            val paymentId = params["payment_id"]
            val mandateId = params["mandate_id"]
            val viewModel = viewModel<PaymentStatusViewModel>(
                factory = paymentStatusViewModel(paymentId, mandateId, PrefUtils.getQuickstartUrl(this))
            )
            val status by viewModel.status.collectAsState()
            val error by viewModel.error.collectAsState()

            // Start polling for status updates
            if (paymentId != null) {
                viewModel.pollPaymentStatus()
            } else {
                viewModel.pollMandateStatus()
            }

            Theme(
                navigationTransition = { current, transition, direction ->
                    stackNavigation(current, transition, direction)
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(text = stringResource(id = R.string.status_title), style = MaterialTheme.typography.titleMedium)
                        when (status) {
                            PaymentStatus.Status.AUTHORIZING -> {
                                // If the SDK has done it's work already, it will be ok
                                // to wait for status change
                                CircularProgressIndicator()
                            }
                            PaymentStatus.Status.AUTHORIZATION_REQUIRED -> {
                                // If you encounter this state, it's most likely that the SDK didn't
                                // get chance to do its work yet. Start the CoordinatorFlow.
                                // If the SDK has done its work, then this state would be considered
                                // an error.
                                // Because we are using this view to query the state of the payment
                                // after redirect from the bank this should never happen.
                                Image(imageVector = Icons.Filled.Error, contentDescription = null)
                            }
                            PaymentStatus.Status.AUTHORIZED,
                            PaymentStatus.Status.SETTLED,
                            PaymentStatus.Status.EXECUTED -> {
                                Image(
                                    imageVector = Icons.Filled.CheckCircle,
                                    colorFilter = ColorFilter.tint(Color.Green),
                                    contentDescription = null
                                )
                            }
                            PaymentStatus.Status.FAILED -> {
                                Image(imageVector = Icons.Filled.Error, contentDescription = null)
                            }
                        }

                        Text(text = status.toString(), style = MaterialTheme.typography.bodyLarge)
                        Text(text = error, color = Color.Red)
                    }
                }
            }
        }
    }
}
