package com.truelayer.demo.integrations.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

/**
 * UI component to represent an integration of the SDK and launch it's respective Activity
 */
@Composable
fun ImplementationItem(
    name: String,
    image: Painter,
    onClick: () -> Unit
) = Card(
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    shape = MaterialTheme.shapes.medium,
    modifier = Modifier
        .fillMaxWidth()
        .height(88.dp),
    onClick = onClick
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Center
    ) {
        Row(verticalAlignment = CenterVertically) {
            Image(
                modifier = Modifier.size(58.dp),
                painter = image,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
