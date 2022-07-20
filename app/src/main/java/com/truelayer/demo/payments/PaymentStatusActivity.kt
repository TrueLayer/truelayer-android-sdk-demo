package com.truelayer.demo.payments

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.truelayer.demo.Configuration
import com.truelayer.demo.R
import com.truelayer.demo.payments.api.PaymentStatus
import com.truelayer.payments.core.utils.extractTrueLayerRedirectParams
import com.truelayer.payments.ui.TrueLayerUI
import com.truelayer.payments.ui.theme.DarkColorDefaults
import com.truelayer.payments.ui.theme.LightColorDefaults
import com.truelayer.payments.ui.theme.Theme
import com.truelayer.payments.ui.theme.TrueLayerTheme
import com.truelayer.payments.ui.theme.TypographyDefaults
import com.truelayer.payments.ui.theme.stackNavigation

/**
 * Activity to receive redirects from the provider app and fetches the payment's status
 */
class PaymentStatusActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The following lines are required for the accompanist-ui-insets to work properly
        WindowCompat.setDecorFitsSystemWindows(window, false)
        @Suppress("DEPRECATION")
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        // Extract the payment id
        val params = intent.data.extractTrueLayerRedirectParams()

        // Initialise the payments configuration
        TrueLayerUI.init(context = applicationContext) {
            environment = Configuration.environment
            httpConnection = Configuration.httpConfig
        }

        // Your payments custom theme or use the provided defaults.
        val theme = TrueLayerTheme(
            lightPalette = LightColorDefaults,
            darkPalette = DarkColorDefaults,
            typography = TypographyDefaults
        )

        setContent {
            val paymentId = params["payment_id"]
            val viewModel = viewModel<PaymentStatusViewModel>(
                factory = paymentStatusViewModel(paymentId)
            )
            val paymentStatus by viewModel.status.collectAsState()
            val error by viewModel.error.collectAsState()

            // Start polling for payment status updates
            viewModel.pollPaymentStatus()

            Theme(
                theme = theme,
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
                        Text(text = stringResource(id = R.string.payment_status_title), style = MaterialTheme.typography.h5)
                        when (paymentStatus) {
                            PaymentStatus.Status.AUTHORIZING,
                            PaymentStatus.Status.AUTHORIZATION_REQUIRED -> {
                                CircularProgressIndicator()
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

                        Text(text = paymentStatus.toString(), style = MaterialTheme.typography.body1)
                        Text(text = error, color = Color.Red)
                    }
                }
            }
        }
    }
}
