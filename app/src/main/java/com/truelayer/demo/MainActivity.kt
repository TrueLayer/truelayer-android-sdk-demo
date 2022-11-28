package com.truelayer.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.truelayer.demo.integrations.ActivityIntegrationActivity
import com.truelayer.demo.integrations.ActivityXIntegrationActivity
import com.truelayer.demo.integrations.ComposeIntegrationActivity
import com.truelayer.demo.integrations.Implementation
import com.truelayer.demo.integrations.JavaIntegrationActivity
import com.truelayer.demo.integrations.components.ImplementationItem
import com.truelayer.demo.integrations.components.TextWithDropdownMenu
import com.truelayer.demo.payments.PaymentType
import com.truelayer.demo.ui.theme.SDKDemoTheme
import com.truelayer.demo.utils.PrefUtils
import com.truelayer.payments.core.domain.configuration.Environment

/**
 * Activity listing the various integration types for the SDK
 */
class MainActivity : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val implementations = listOf(
            Implementation(
                name = R.string.integration_compose,
                icon = R.drawable.compose,
                activity = ComposeIntegrationActivity::class.java
            ),
            Implementation(
                name = R.string.integration_activityx,
                icon = R.drawable.androidx,
                activity = ActivityXIntegrationActivity::class.java
            ),
            Implementation(
                name = R.string.integration_activity,
                icon = R.drawable.android,
                activity = ActivityIntegrationActivity::class.java
            ),
            Implementation(
                name = R.string.integration_java,
                icon = R.drawable.java,
                activity = JavaIntegrationActivity::class.java
            )
        )

        setContent {
            val context = LocalContext.current
            var apiUrl by remember { mutableStateOf(PrefUtils.getQuickstartUrl(this)) }
            var env by remember { mutableStateOf(PrefUtils.getEnvironment(this)) }
            var paymentType by remember { mutableStateOf(PrefUtils.getPaymentType(this)) }

            SDKDemoTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize().padding(top = 16.dp)
                ) { paddingValues ->
                    Column(
                        Modifier.padding(paddingValues)
                    ) {
                        TextField(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            value = apiUrl,
                            label = {
                                Text(stringResource(id = R.string.config_api_label))
                            },
                            onValueChange = {
                                apiUrl = it
                                PrefUtils.setQuickstartUrl(it, this@MainActivity)
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp)
                        ) {
                            TextWithDropdownMenu(
                                modifier = Modifier.weight(1f),
                                label = paymentType.name,
                                dropdownItems = PaymentType.values().map { it.name to it },
                                onClick = {
                                    paymentType = it
                                    PrefUtils.setPaymentType(it, this@MainActivity)
                                }
                            )

                            TextWithDropdownMenu(
                                modifier = Modifier.weight(1f),
                                label = env.name,
                                dropdownItems = Environment.values().map { it.name to it },
                                onClick = {
                                    env = it
                                    PrefUtils.setEnvironment(it, this@MainActivity)
                                }
                            )
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) {
                            items(implementations) { implementation ->
                                ImplementationItem(
                                    name = stringResource(implementation.name),
                                    image = painterResource(implementation.icon),
                                    onClick = {
                                        if (apiUrl.isBlank()) {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Missing Payments Quickstart URL",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            val intent = Intent(context, implementation.activity)
                                            startActivity(intent)
                                        }
                                    }
                                )
                                Divider(modifier = Modifier.padding(all = 4.dp), color = Color.Transparent)
                            }
                        }
                    }
                }
            }
        }
    }
}
