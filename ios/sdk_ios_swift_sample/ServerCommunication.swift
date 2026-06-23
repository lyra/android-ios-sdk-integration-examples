//
//  ServerCommunication.swift
//  sdk_ios_swift_sample
//
//  Created by Lyra Network on 19/09/2019.
//  Copyright © 2019 Lyra Network. All rights reserved.
//

import Foundation
import LyraPaymentSDK

/// This class represents the communication with your merchant server to create the necessary payment context for the SDK and to verify the payment on your server.
/// It is an example of how you can implement this communication. Check API REST integration documentation.
class ServerCommunication {

  // FIXME: change by the right merchant payment server url
  let kMerchantServerUrl = "<REPLACE_ME>"  // without / at the end, example https://myserverurl.com

  //Create Payment Context Parameters
  // Change by the desired parameters if necessary

  //Customer Informations
  var email = "customeremail@domain.com"
  var customerReference = "customerRef"

  var amount = 100
  var currency = "EUR"
  var orderId = ""

  // Environment TEST or PRODUCTION, refer to documentation for more information
  // FIXME: change by your targeted environment
  var paymentMode = "TEST"

  // TRUE to display a "register the card" switch in the payment form
  var askRegisterPay = false

  /// This method send to  merchant server the information necessary for create the formtoken request to execute the payment using the Lyra  Payment SDK.
  /// - Returns: <#description#>
  func getFormToken() async throws -> String {
    guard let serverUrl = NSURL(string: "\(kMerchantServerUrl)/createPayment") else {
      throw AppError.invalidURL
    }
    //Create dictionary with params. Check API REST integration documentation
    var paramsDict: [String: Any] = [
      "amount": amount, "mode": paymentMode, "customer": ["email": email, "reference": customerReference],
      "currency": currency, "orderId": orderId, "formTokenVersion": Lyra.getFormTokenVersion(),
    ]
    if askRegisterPay {
      paramsDict["formAction"] = "ASK_REGISTER_PAY"
    }
    var request = URLRequest(url: serverUrl as URL)
    request.httpMethod = "POST"
    let jsonParams = try JSONSerialization.data(withJSONObject: paramsDict, options: [])
    request.httpBody = jsonParams
    request.setValue("application/json; charset=utf-8", forHTTPHeaderField: "Content-Type")


    let session = URLSession(configuration: .default)
    let (data, _) = try await session.data(for: request)

    // Parse answer
    if let json = try? JSONSerialization.jsonObject(with: data, options: .mutableContainers),
      let objectResponse = json as? [String: Any],
      let serverResponse = objectResponse["answer"] as? [String: Any],
      let formToken = serverResponse["formToken"] as? String
    {
      return formToken
    } else {
      //Handle error in request for obtain a formToken
      throw AppError.invalidServerResponse
    }
  }

  /// This method send to merchant server the  necessary information for validate the payment.
  /// - Parameter lyraResponse: Corresponds to the object that returns the SDK with the payment information once the payment processing is finished.
  /// - Returns: True is verify is OK, false otherwise
  func verifyPayment(_ lyraResponse: LyraResponse) async throws -> Bool {

    guard let serverUrl = NSURL(string: "\(kMerchantServerUrl)/verifyResult") else {
      throw AppError.invalidURL
    }
    var request = URLRequest(url: serverUrl as URL)
    request.httpMethod = "POST"
    request.httpBody = lyraResponse.getResponseData()
    request.setValue("application/json; charset=utf-8", forHTTPHeaderField: "Content-Type")

    let session = URLSession(configuration: .default)
    let (_, response) = try await session.data(for: request)
    if let httpResponse = response as? HTTPURLResponse {
      if httpResponse.statusCode == 200 {
        return true
      } else {
        return false
      }
    }
    throw AppError.invalidServerResponse

  }

}
