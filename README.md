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
