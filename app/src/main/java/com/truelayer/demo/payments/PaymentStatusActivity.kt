package com.truelayer.demo.payments

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.truelayer.demo.MainActivity
import com.truelayer.demo.integrations.ActivityIntegrationActivity
import com.truelayer.demo.integrations.ActivityXIntegrationActivity
import com.truelayer.demo.integrations.ComposeIntegrationActivity
import com.truelayer.demo.integrations.JavaIntegrationActivity
import com.truelayer.demo.utils.PrefUtils

/**
 * Activity to receive redirects from the provider app and fetches the payment's status
 */
class PaymentStatusActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activityClass = when (PrefUtils.getIntegrationType(this)) {
            PrefUtils.IntegrationType.ACTIVITY -> ActivityIntegrationActivity::class.java
            PrefUtils.IntegrationType.ACTIVITY_X -> ActivityXIntegrationActivity::class.java
            PrefUtils.IntegrationType.COMPOSE -> ComposeIntegrationActivity::class.java
            PrefUtils.IntegrationType.JAVA -> JavaIntegrationActivity::class.java
        }

        val newIntent = Intent(this, activityClass)
        newIntent.data = intent.data
        startActivity(newIntent)
        finish()
    }
}
