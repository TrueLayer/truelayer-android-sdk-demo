<p align="center">
    <img height="100px" src="./truelayer_logo.svg" />
</p>

<br>  

# TrueLayer Android SDK Demo
This project provides examples for integrating the TrueLayer Android SDK. Check out the [integration guide](https://docs.truelayer.com/docs/android-sdk-for-payments-v3) for more details.

## Before you begin
Register `truelayer://demo` as a `redirect_uri` in your [developer console](https://console.truelayer.com). This is used at the end of the payment journey to redirect back to this app.
> You may register a different scheme but then you need to modify the `Manifest` file and `ProcessorContextProvider.redirectUri` property.

## API Setup
This app uses our [Payments Quickstart API](https://github.com/TrueLayer/payments-quickstart) to simplify the process of creating payments 
and retrieving their status. You will need to setup your own installation of this project to use this app.
Payments Quickstart is a project that will allow you to instantly get up to speed with SDK integration without a need for your own backend to be ready.

>Beware this project is meant to be used for testing only, and the functionality behind (or at least part of it) will need to be implemented on your own backend service.

## Configuration
<p align="center">
    <img height="300px" src="./screenshot.png" />
</p>
The app allows you to configure your use of the Payments Quickstart API within the app. Simply add the URI to the API in the field on the app's main screen and select
your environment from the dropdown.

You can also select between launching flows for payments in different currencies or mandates.

 
## :warning: **For SDK version 3.8.0+ go [here](./MigrateTo3.8.0.md)** :warning:

## Version 3.9.0

Introduction of AIS+PIS flow for EUR payments. This allows users to select their account instead of typing in the IBAN.

This version is set to use:
- `kotlin` : `1.9.25`
- `compose-bom` : `2024.11.00`
- `desugaring` : `2.1.3`
- `com.android.tools.build:gradle` : `8.7.0`
- `gradle-8.9`
- `targetSDK`: `35`
- `jvmTarget`: `19`
- `javaVersion`: `JavaVersion.VERSION_19`

## Version 3.9.1

The Java version has been downgraded from 19 to 17 to allow more flexibility for integrators.
```
jvmTarget = 17 
sourceCompatibility = JavaVersion.VERSION_17 
targetCompatibility = JavaVersion.VERSION_17
```

## Version 4.0.1

- A new, gorgeous, conversion-driven UI in parity with the 2025 TrueLayer Web Hosted Payments Page, with the following new features available automatically
  for all GBP and EUR+Ireland single payments:
    - A single-screen, modern, declarative UI system under the hood, allowing for smooth transitions and overall snappier look-and-feel.
    - Retries: change bank / payment providers on the fly, or retry failed payments without restarting the SDK.
    - Cancellation screen streamlining: more insight on why users are abandoning their payments.
    - Provider pre-selection: automatically select the last used provider, allowing for faster one-click payments and more engaged, better converting users.

Heads-up: the following requests do not support the New UI and will fallback onto the legacy UI, as seen in versions 3.9.1 and below:
- Mandates / recurring payments
- EUR payments for countries other than Ireland-only

Contains minor bug fixes, improvements and underlying library updates.

The update from version 3.8.0, 3.9.x to version 4.0.1 should be seamless.

This version is set to use:
- `kotlin` : `2.1.20`
- `compose-bom` : `2025.06.01`
- `desugaring` : `2.1.5`
- `androidx.datastore.datastore-preferences` : `1.1.7`

Other libraries have been updated:

- `androidx.activity.activity-compose` : `1.10.1`
- `androidx.appcompat.appcompat` : `1.7.1`
- `androidx.compose.runtime.runtime-tracing` : `1.8.3`
- `androidx.core.core-ktx` : `1.16.0`
- `androidx.fragment.fragment-ktx` : `1.8.8`
- `androidx.lifecycle.lifecycle-livedata-ktx` : `2.9.1`
- `androidx.lifecycle.lifecycle-runtime-compose-android` : `2.9.1`
- `androidx.lifecycle.lifecycle-runtime-ktx` : `2.9.1`
- `androidx.lifecycle.lifecycle-viewmodel-compose` : `2.9.1`
- `androidx.lifecycle.lifecycle-viewmodel-ktx` : `2.9.1`
- `androidx.navigation.navigation-compose` : `2.9.1`
- `androidx.room.room-compiler` : `2.7.1`
- `androidx.room.room-ktx` : `2.7.1`
- `androidx.room.room-runtime` : `2.7.1`
- `androidx.work.work-multiprocess` : `2.10.2`
- `androidx.work.work-runtime-ktx` : `2.10.2`
- `com.android.tools.desugar_jdk_libs` : `2.1.5`
- `io.coil-kt.coil-compose` : `2.7.0`
- `io.coil-kt.coil-svg` : `2.7.0`
- `io.coil-kt.coil` : `2.7.0`
- `org.jetbrains.kotlinx.kotlinx-coroutines-android` : `1.10.2`
- `org.jetbrains.kotlinx.kotlinx-coroutines-core` : `1.10.2`
- `org.jetbrains.kotlinx.kotlinx-coroutines-test` : `1.10.2`

## How does the payment flow with the SDK works?

```mermaid
sequenceDiagram
	participant app as Demo App
	participant backend as Payments Quickstart
	participant SDK as TrueLayer SDK
	participant Bank as Bank App

	app ->> backend: Create Payment
	backend ->> app: Processor Context
	app ->>+ SDK: Start Processor Flow (ProcessorContext)
	SDK ->> SDK: Execute authorization flow

	note left of app: Depending on the payment provider (the bank) <br/>there are two possible scenarios:<br/>redirect flow(1) or embedded flow(2).

	alt 1. Redirect flow
	note left of app: Redirect flow involves launching<br/> the Bank app or Bank website.
	SDK -->>+ Bank: Redirect to bank app
	SDK --x app: Notify result
	Bank -->>- app: Redirect after authorization process (with redirect uri eg. truelayer://demo )
	else 2. Embedded flow
		note left of app: Embedded flow covers entire process<br/> inside the SDK.
	SDK --x- app: Notify result
	end

	loop query payment status
	note left of app: Query the payment status<br/>until payment is no longer<br/>in AUTHORISING state.
	app ->> backend: Query payment status
	backend ->> app: Payment status
	end
```
