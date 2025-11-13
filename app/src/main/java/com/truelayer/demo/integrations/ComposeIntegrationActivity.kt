package com.truelayer.demo.integrations

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

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
            typography = TypographyDefaults
        )

        val paymentType = PrefUtils.getPaymentType(this)

        setContent {
            var flowResult by remember {
                mutableStateOf<ProcessorResult?>(null)
            }
            val coroutineScope = rememberCoroutineScope()
            var isLoading by remember { mutableStateOf(false) }
            var processorContext by rememberSaveable { mutableStateOf<ProcessorContext?>(null) }
            var error by remember { mutableStateOf<String?>(null) }

            ScreenContent(
                isLoading = isLoading,
                error = error,
                flowResult = flowResult,
                onStartFlow = {
                    // reset the state
                    error = null
                    flowResult = null
                    processorContext = null
                    // start the flow
                    isLoading = true
                    coroutineScope.launch {
                        processorContextProvider.getProcessorContext(paymentType, this@ComposeIntegrationActivity)
                            .onOk {
                                isLoading = false
                                processorContext = it
                                PrefUtils.setIntegrationType(PrefUtils.IntegrationType.COMPOSE, this@ComposeIntegrationActivity)
                            }
                            .onError {
                                isLoading = false
                                error = it.localizedMessage
                            }
                    }
                },
                onBack = { finish() }

            )

            if (processorContext != null && flowResult == null) {
                // Launch the SDK with the ProcessorContext for the payment/mandate created
                Processor(
                    context = processorContext!!,
                    theme = theme,
                    onSuccess = { flowResult = it },
                    onFailure = { flowResult = it }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContent(
    isLoading: Boolean = false,
    error: String? = null,
    flowResult: ProcessorResult? = null,
    onStartFlow: () -> Unit = { },
    onBack: () -> Unit = { }
) {
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Compose Example")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(Icons.Sharp.ArrowBackIosNew, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("This is the example for the TrueLayer Payments SDK Compose integration." +
                    "Press the button below to start the flow.")
            Spacer(Modifier.height(16.dp))
            if (flowResult == null) {
                Button(
                    onClick = onStartFlow,
                    enabled = !isLoading
                ) {
                    Text("Start flow")
                }
            } else {
                Spacer(Modifier.height(16.dp))
                Text("The flow has finished.")
                Spacer(Modifier.height(16.dp))
                Text("Result: $flowResult")
                Button(
                    onClick = onBack
                ) {
                    Text("Exit")
                }
            }
        }
        if (isLoading) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Creating payment")
            }
        } else if (error != null) {
            Toast.makeText(
                LocalContext.current,
                stringResource(id = R.string.processor_context_error, error!!),
                Toast.LENGTH_LONG
            ).show()
            onBack()
        }
    }

}

@Preview
@Composable
fun PreviewScreenContent() {
    MaterialTheme {
        ScreenContent()
    }
}