# Payment Mobile SDK integration example

## Summary

The aim of this repository is to explain how to integrate our Payment Mobile SDK into an iOS application using Swift.


## Table of contents

- [Payment Mobile SDK integration example](#payment-mobile-sdk-integration-example)
  - [Summary](#summary)
  - [Table of contents](#table-of-contents)
  - [Prerequisites](#prerequisites)
  - [Getting started](#getting-started)
    - [Execute this sample](#execute-this-sample)
    - [How does it work](#how-does-it-work)
        - [Initialize the SDK](#initialize-the-sdk)
        - [Make a payment](#make-a-payment)
  - [Technology](#technology)
  - [Troubleshooting](#troubleshooting)
  - [Copyright and license](#copyright-and-license)

## Prerequisites

In order to be able to perform a successful payment with our Mobile SDK you must have: 
* A contract with your Payment service provider.
* A deployed server capable to communicate with the payment platform, in order to verify data and create the payment session (please check out java server sample or the integration documentation for more information).
* Your public key to initialize the SDK. This key can be found in the merchant back-office in Settings -> Shop -> API -> REST API Keys.
* Your REST API Server Name to initialize the SDK. This key can be found in the merchant back-office in Settings -> Shop -> API -> REST API Keys.

## Getting started

### Execute this sample

1. Clone the repo, `git clone REPO_URL`. 

2. Install the dependencies using CocoaPods by the following command `pod update`.

3. Open the project under Xcode with the `.xcworkspace` file.

4. Edit the following fields in `AppDelegate.swift`:
    - **publicKey**: replace with your public key that you can find in your back-office.
    - **apiServerName**: replace with your REST API server name that you can find in your back-office.

4. Edit the following field in `ServerCommunication.swift`:
    - **kMerchantServerUrl**: replace by your merchant server url.
    

5. Run it and that's all! :)


### How does it work

#### Initialize the SDK

It is necessary and important to call the `initialize` method of the SDK on the start of your application. 

```swift
 try Lyra.initialize(publicKey, apiServerName, InitOptions(cardScanningEnabled: true))
```
The "InitOptions" optional parameter corresponds to an object that allows you to configure the behavior of the SDK. The possibles keys in this dictionary are:

| Key             | Value format    | Description                                                        
| :-------------------- | :-------- | :----------------------------------------------------------------- | 
|cardScanningEnabled   | Bool    | Enable/Disable the scan card fuctionality. If not set, the functionality will be disable.
|applePayMerchantName  | String  | Nom de la boutique à afficher sur la fenêtre modale Apple Pay au dessus du montant. Nécessaire pour la prise en charge de Apple Pay.
|applePayMerchantId    | String  | Numéro de contrat Apple Pay. Nécessaire pour la prise en charge de Apple Pay. 
|theme                 | String  | Nom du fichier de thème à utiliser pour personnaliser les vues SDK. 

#### Make a payment

To proceed with processing the payment using the `process` method of the SDK, you first have to retrieve the `formToken` object from your server. After the `process` method of the SDK is called with the `formToken`:

```swift
let response = try await Lyra.process(self, formToken)
```

The SDK will guide the user through the payment process. When the payment succeed, you will have to check the response integrity on your server. 

An optional `ProcessOptions` parameter can be transmitted to the `process` method for customizing the payment.


*Please check official integration documentation for further information and to check other SDK modes and functionality.* 


## Technology

Developed in Xcode 11.0 and written in Swift 5, this sample app requires iOS 11.0 or superior.

## Troubleshooting

Check official integration documentation in order to check all possible error codes.

## Copyright and license
	The MIT License

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in
	all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
	THE SOFTWARE.
