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
    - **username**: replace with your user value for basic authentication in merchant server.
    - **password**: replace with your password value for basic authentication in merchant server.
    

5. Run it and that's all! :)


### How does it work

#### Initialize the SDK

It is necessary and important to call the `initialize` method of the SDK on the start of your application. 

```swift
//Configure SDK options
var configurationOptions = [String : Any]()
configurationOptions[Lyra.apiServerName] = apiServerName
  
//Initialize Payment SDK
try Lyra.initialize(publicKey, configurationOptions)
```
The "configurationOptions" parameter corresponds to a Dictionary that allows you to configure the behavior of the SDK. The possibles keys in this dictionary are:

| Key             | Value format    | Description                                                        | Required   |
| :-------------------- | :-------- | :----------------------------------------------------------------- | :--------|
| apiServerName         | String    | Your REST API server name that you can find in your back-office. | Required |
| cardScanningEnabled   | Bool    | Enable/Disable the scan card fuctionality. If not set, the functionality will be disable. | Optional |

#### Make a payment

Before calling the `process` method of the SDK to process the payment,  it is necessary to, first, create a session using your server.
In this sample, this is done by the `getPaymentContext` method in ServerCommunication class:

```swift
// 1. Init server comunication class for get createPayment context
let serverCommunication = ServerCommunication()

// 2. Execute getPaymentContext for get the formToken (required param in SDK process method)
serverCommunication.getPaymentContext { (getContextSuccess, formToken, error) in
...
let objectResponse = json as? [String: Any]
let serverResponse = objectResponse["answer"] as? [String: Any]
let formToken = serverResponse["formToken"] as? String 

}
```

In this sample, in case of error calling the server, a message will be displayed with the error text.
  
Otherwise, the `process` method is called with the formToken. The formToken is checked and the `process` SDK method is called.

```swift
Lyra.process(self, formToken!,
	onSuccess: { ( _ lyraResponse: LyraResponse) -> Void in

		//Verify the payment using your server: Check the response integrity by verifying the hash on your server
		self.verifyPayment(lyraResponse)
	},
	onError: { (_ error: LyraError, _ lyraResponse: LyraResponse?) -> Void in

		//TODO: Handle Payment SDK error in process payment request
		self.showMessage("Payment fail: \(error.errorMessage)")
	})
```

The SDK will guide the user through the payment process. When the payment succeed, you will have to check the response integrity on your server. 


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
