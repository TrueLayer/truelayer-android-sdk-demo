package com.truelayer.demo

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.truelayer.demo.ui.theme.SDKDemoTheme
import com.truelayer.demo.utils.PrefUtils
import com.truelayer.payments.core.domain.configuration.Environment

/**
 * Activity listing the various integration types for the SDK
 */
class MainActivity : AppCompatActivity() {

    @OptIn(ExperimentalMaterialApi::class)
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

            SDKDemoTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize().padding(top = 16.dp)
                ) { paddingValues ->
                    Column(
                        Modifier.padding(paddingValues)
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = stringResource(id = R.string.config_api_label)
                        )

                        Row {
                            TextField(
                                modifier = Modifier.fillMaxWidth(0.7f).padding(horizontal = 16.dp),
                                value = apiUrl,
                                onValueChange = {
                                    apiUrl = it
                                    PrefUtils.setQuickstartUrl(it, this@MainActivity)
                                }
                            )

                            TextWithDropdownMenu(
                                label = env.name,
                                dropdownItems = Environment.values().map { it.name to it },
                                onClick = {
                                    env = it
                                    PrefUtils.setEnvironment(it, this@MainActivity)
                                }
                            )
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()

                        ) {
                            items(implementations) { implementation ->
                                ImplementationItem(
                                    name = stringResource(implementation.name),
                                    image = painterResource(implementation.icon),
                                    onClick = {
                                        val intent = Intent(context, implementation.activity)
                                        startActivity(intent)
                                    }
                                )
                                Divider(modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
