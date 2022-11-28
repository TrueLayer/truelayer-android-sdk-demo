package com.truelayer.demo.integrations

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Model used to represent each type of implementation of the SDK
 */
data class Implementation(
    @StringRes val name: Int,
    @DrawableRes val icon: Int,
    val activity: Class<*>
)
