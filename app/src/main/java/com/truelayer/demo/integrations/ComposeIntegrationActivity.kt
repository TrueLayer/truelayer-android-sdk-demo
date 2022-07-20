package com.truelayer.demo.integrations

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.truelayer.demo.Configuration
import com.truelayer.demo.payments.PaymentContextProvider
import com.truelayer.demo.ui.theme.Primary
import com.truelayer.demo.ui.theme.PrimaryDark
import com.truelayer.demo.ui.theme.PrimaryVariant
import com.truelayer.demo.ui.theme.Secondary
import com.truelayer.payments.core.domain.utils.Ok
import com.truelayer.payments.core.domain.utils.errorOrNull
import com.truelayer.payments.ui.TrueLayerUI
import com.truelayer.payments.ui.models.PaymentContext
import com.truelayer.payments.ui.screens.coordinator.FlowCoordinator
import com.truelayer.payments.ui.screens.coordinator.FlowCoordinatorResult
import com.truelayer.payments.ui.theme.DarkColorDefaults
import com.truelayer.payments.ui.theme.LightColorDefaults
import com.truelayer.payments.ui.theme.Theme
import com.truelayer.payments.ui.theme.TrueLayerTheme
import com.truelayer.payments.ui.theme.TypographyDefaults
import com.truelayer.payments.ui.theme.stackNavigation

/**
 * Example integration of the SDK with the Jetpack Compose
 */
class ComposeIntegrationActivity : AppCompatActivity() {

    private val paymentContextProvider = PaymentContextProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The following lines are required for the accompanist-ui-insets to work properly
        WindowCompat.setDecorFitsSystemWindows(window, false)
        @Suppress("DEPRECATION")
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        // Initialise the payments configuration
        TrueLayerUI.init(context = applicationContext) {
            environment = Configuration.environment
            httpConnection = Configuration.httpConfig
        }

        // Customise the SDK's theme or use the provided defaults.
        val theme = TrueLayerTheme(
            lightPalette = LightColorDefaults.copy(
                primary = Primary,
                primaryVariant = PrimaryVariant,
                error = Secondary
            ),
            darkPalette = DarkColorDefaults.copy(
                primary = PrimaryDark,
                primaryVariant = PrimaryVariant,
                error = Secondary
            ),
            typography = TypographyDefaults
        )

        setContent {
            var flowResult by remember {
                mutableStateOf<FlowCoordinatorResult?>(null)
            }
            var paymentContext by remember { mutableStateOf<PaymentContext?>(null) }
            var error by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(true) {
                val outcome = paymentContextProvider.getPaymentContext()
                if (outcome is Ok) {
                    paymentContext = outcome.value
                } else {
                    error = outcome.errorOrNull()?.localizedMessage
                }
            }
            Theme(
                theme = theme,
                navigationTransition = { current, transition, direction ->
                    stackNavigation(current, transition, direction)
                }
            ) {
                when {
                    error != null -> {
                        Toast.makeText(
                            this@ComposeIntegrationActivity,
                            "Unable to get payment context: $error",
                            Toast.LENGTH_LONG
                        ).show()
                        this@ComposeIntegrationActivity.finish()
                    }
                    paymentContext == null -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Text("Authenticating")
                        }
                    }
                    paymentContext != null && flowResult == null -> {
                        FlowCoordinator(
                            paymentContext = paymentContext!!,
                            onSuccess = { flowResult = it },
                            onFailure = { flowResult = it }
                        )
                    }
                    flowResult != null -> {
                        Toast.makeText(this, flowResult.toString(), Toast.LENGTH_LONG).show()
                        this@ComposeIntegrationActivity.finish()
                    }
                }
            }
        }
    }
}
