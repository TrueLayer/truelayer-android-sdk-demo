package com.truelayer.demo.integrations

import android.os.Bundle
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
import com.truelayer.demo.Configuration
import com.truelayer.demo.payments.PaymentContextProvider
import com.truelayer.demo.ui.theme.Primary
import com.truelayer.demo.ui.theme.PrimaryDark
import com.truelayer.demo.ui.theme.Secondary
import com.truelayer.payments.core.domain.utils.onError
import com.truelayer.payments.core.domain.utils.onOk
import com.truelayer.payments.ui.TrueLayerUI
import com.truelayer.payments.ui.screens.processor.Processor
import com.truelayer.payments.ui.screens.processor.ProcessorContext
import com.truelayer.payments.ui.screens.processor.ProcessorResult
import com.truelayer.payments.ui.theme.*

/**
 * Example integration of the SDK with the Jetpack Compose
 */
class ComposeIntegrationActivity : AppCompatActivity() {

    private val paymentContextProvider = PaymentContextProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialise the payments configuration
        TrueLayerUI.init(context = applicationContext) {
            environment = Configuration.environment
            httpConnection = Configuration.httpConfig
        }

        // Customise the SDK's theme or use the provided defaults.
        val theme = TrueLayerTheme(
            lightPalette = LightColorDefaults.copy(
                primary = Primary,
                error = Secondary
            ),
            darkPalette = DarkColorDefaults.copy(
                primary = PrimaryDark,
                error = Secondary
            ),
            typography = TypographyDefaults
        )

        setContent {
            var flowResult by remember {
                mutableStateOf<ProcessorResult?>(null)
            }
            var paymentContext by remember { mutableStateOf<ProcessorContext?>(null) }
            var error by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(true) {
                paymentContextProvider.getPaymentContext()
                    .onOk { paymentContext = it }
                    .onError { error = it.localizedMessage }
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
                        Processor(
                            context = paymentContext!!,
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
