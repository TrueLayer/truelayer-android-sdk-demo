package com.truelayer.demo.integrations

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.truelayer.demo.R
import com.truelayer.demo.payments.ProcessorContextProvider
import com.truelayer.demo.ui.theme.Primary
import com.truelayer.demo.ui.theme.PrimaryDark
import com.truelayer.demo.ui.theme.Secondary
import com.truelayer.demo.utils.PrefUtils
import com.truelayer.payments.core.domain.configuration.HttpConnectionConfiguration
import com.truelayer.payments.core.domain.configuration.HttpLoggingLevel
import com.truelayer.payments.core.domain.utils.onError
import com.truelayer.payments.core.domain.utils.onOk
import com.truelayer.payments.ui.TrueLayerUI
import com.truelayer.payments.ui.screens.processor.Processor
import com.truelayer.payments.ui.screens.processor.ProcessorContext
import com.truelayer.payments.ui.screens.processor.ProcessorResult
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

    private lateinit var processorContextProvider: ProcessorContextProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        processorContextProvider = ProcessorContextProvider(PrefUtils.getQuickstartUrl(this))

        // Initialise the payments configuration
        TrueLayerUI.init(context = applicationContext) {
            environment = PrefUtils.getEnvironment(this@ComposeIntegrationActivity)
            httpConnection = HttpConnectionConfiguration(
                httpDebugLoggingLevel = HttpLoggingLevel.None
            )
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

        val paymentType = PrefUtils.getPaymentType(this)

        setContent {
            var flowResult by remember {
                mutableStateOf<ProcessorResult?>(null)
            }
            var processorContext by remember { mutableStateOf<ProcessorContext?>(null) }
            var error by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(true) {
                processorContextProvider.getProcessorContext(paymentType, this@ComposeIntegrationActivity)
                    .onOk { processorContext = it }
                    .onError { error = it.localizedMessage }
            }
            Theme(
                theme = theme,
                navigationTransition = { current, transition, direction ->
                    stackNavigation(current, transition, direction)
                }
            ) {
                when {
                    // Display any errors that occur when creating a payment/mandate
                    error != null -> {
                        Toast.makeText(
                            this@ComposeIntegrationActivity,
                            stringResource(id = R.string.processor_context_error, error!!),
                            Toast.LENGTH_LONG
                        ).show()
                        this@ComposeIntegrationActivity.finish()
                    }
                    // Display loading UI while payment/mandate is being created
                    processorContext == null -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Text("Authenticating")
                        }
                    }
                    // Launch the SDK with the ProcessorContext for the payment/mandate created
                    processorContext != null && flowResult == null -> {
                        Processor(
                            context = processorContext!!,
                            onSuccess = { flowResult = it },
                            onFailure = { flowResult = it }
                        )
                    }
                    // Display the result of the payment/mandate flow
                    flowResult != null -> {
                        Toast.makeText(this, flowResult.toString(), Toast.LENGTH_LONG).show()
                        this@ComposeIntegrationActivity.finish()
                    }
                }
            }
        }
    }
}
