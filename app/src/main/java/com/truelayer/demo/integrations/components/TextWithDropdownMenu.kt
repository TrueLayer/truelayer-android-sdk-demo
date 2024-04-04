package com.truelayer.demo.integrations.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * UI component to display dropdown lists
 */
@Composable
fun <T> TextWithDropdownMenu(
    modifier: Modifier,
    label: String,
    dropdownItems: List<Pair<String, T>>,
    onClick: (T) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    TextButton(
        modifier = modifier,
        border = BorderStroke(1.dp, Color.Gray),
        onClick = { dropdownExpanded = true }
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurface)
        DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false },
            content = {
                dropdownItems.forEach {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .defaultMinSize(minHeight = 40.dp)
                                .clickable {
                                    dropdownExpanded = false
                                    onClick(it.second)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = it.first,
                                textAlign = TextAlign.Center
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        )
    }
}
