package com.truelayer.demo

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
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
import com.truelayer.demo.ui.theme.SDKDemoTheme

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

            SDKDemoTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(implementations) { implementation ->
                            ImplementationItem(
                                name = stringResource(implementation.name),
                                image = painterResource(implementation.icon),
                                onClick = {
                                    val intent = Intent(context, implementation.activity)
                                    intent.data = implementation.data
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
