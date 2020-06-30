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
    let kMerchantServerUrl = "<REPLACE_ME>" // without / at the end, example https://myserverurl.com
    
    // FIXME: change by the rigth merchant server credentials
    let username = "<REPLACE_ME>"
    let password = "<REPLACE_ME>"
    
    
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
    
    
    /// This method send to your merchant server the information necessary for create the request to execute the payment using the Payment SDK.
    ///
    /// - Parameter onGetContextCompletion: The completion block to be executed after the getProcessPaymentContext has finished.
    func getPaymentContext(onGetContextCompletion: @escaping (_ getContextSuccess: Bool, _ formToken: String?, _ error: NSError?) -> Void) {
        
        //create request
        guard let serverUrl = NSURL(string: "\(kMerchantServerUrl)/createPayment") else {
            onGetContextCompletion(false, nil, nil)
            return
        }
        //Create dictionary with params. Check API REST integration documentation
        var paramsDict: [String: Any] = ["amount": amount, "mode": paymentMode, "customer": ["email": email, "reference": customerReference], "currency": currency, "orderId": orderId, "formTokenVersion": Lyra.getFormTokenVersion()]
        if askRegisterPay {
            paramsDict["formAction"] = "ASK_REGISTER_PAY"
        }
        var request = URLRequest(url: serverUrl as URL)
        request.httpMethod = "POST"
        do {
            let jsonParams = try JSONSerialization.data(withJSONObject: paramsDict, options: [])
            request.httpBody = jsonParams
            
            let loginString = String(format: "%@:%@", username, password)
            let loginData = loginString.data(using: String.Encoding.utf8)!
            let base64LoginString = loginData.base64EncodedString()
            
            request.setValue("application/json; charset=utf-8", forHTTPHeaderField: "Content-Type")
            request.setValue("Basic \(base64LoginString)", forHTTPHeaderField: "Authorization")
            
        } catch {
            onGetContextCompletion(false, nil, nil)
            return
        }
        // Call server to obtain a formToken
        let task = URLSession.shared.dataTask(with: request) { (data: Data?, _: URLResponse?, error: Error?) in
            if error != nil || data == nil {
                onGetContextCompletion(false, nil, error! as NSError)
            }
            if let json = try? JSONSerialization.jsonObject(with: data!, options: .mutableContainers),
                let objectResponse = json as? [String: Any],
                let serverResponse = objectResponse["answer"] as? [String: Any] {
                ServerCommunication.extractFormToken(serverResponse, completion: onGetContextCompletion)
            } else {
                //Handle error in request for obtain a formToken
                onGetContextCompletion(false, nil, nil)
            }
        }
        task.resume()
    }
    
    ///  This method send to your merchant server the information necessary for validate the payment.
    ///
    /// - Parameters:
    ///   - lyraResponse: Corresponds to the object that returns the SDK with the payment information once the payment processing is finished.
    ///   - onVerifyPaymentCompletion: The completion block to be executed after the payment validation has finished.
    func verifyPayment(_ lyraResponse: LyraResponse, onVerifyPaymentCompletion: @escaping (_ paymentVerified: Bool, _ isErrorConnection: Bool) -> Void) {
        // Build request
        guard let serverUrl = NSURL(string: "\(kMerchantServerUrl)/verifyResult") else {
            onVerifyPaymentCompletion(false, true)
            return
        }
        var request = URLRequest(url: serverUrl as URL)
        request.httpMethod = "POST"
        request.httpBody = lyraResponse.getResponseData()
        
        let loginString = String(format: "%@:%@", username, password)
        let loginData = loginString.data(using: String.Encoding.utf8)!
        let base64LoginString = loginData.base64EncodedString()
        
        request.setValue("application/json; charset=utf-8", forHTTPHeaderField: "Content-Type")
        request.setValue("Basic \(base64LoginString)", forHTTPHeaderField: "Authorization")
        
        // Call server to verify the operation
        let task = URLSession.shared.dataTask(with: request) { (data: Data?, _ response: URLResponse?, error: Error?) in
            if error != nil {
                onVerifyPaymentCompletion(false, true)
            } else if let httpResponse = response as? HTTPURLResponse {
                if httpResponse.statusCode == 200 {
                    onVerifyPaymentCompletion(true, false)
                } else {
                    onVerifyPaymentCompletion(false, false)
                }
            }
        }
        task.resume()
    }

    /// This method extract formToken from the given serverResponse.
    /// - Parameters:
    ///   - serverResponse: String that correspond with the ServerResponse.
    ///   - completion: The completion block to be execute after the formToken is getted.
    private static func extractFormToken( _ serverResponse: [String: Any], completion: @escaping (Bool, String, NSError?) -> Void) {
        if let formToken = serverResponse["formToken"] as? String {
            completion(true, formToken, nil)
        } else if let errorCode = serverResponse["errorCode"] as? String {
            let errorMsg = serverResponse["errorMessage"] as? String
            let detailErrorCode = serverResponse["detailedErrorCode"] as? String
            let detailErrorMsg = serverResponse["detailedErrorMessage"] as? String
            let message = "Error Code: \(errorCode)" + "\n" + "Error Message: \(errorMsg ?? "")" + "\n" + "Error Detail Code: \(detailErrorCode ?? "")" + "\n" + "Error Detail Message: \(detailErrorMsg ?? "")"
            completion(false, "", NSError(domain: "com.lyra.server.communication", code: 1, userInfo: [NSLocalizedFailureReasonErrorKey: message]))
        } else {
            completion(false, "", NSError(domain: "com.lyra.server.communication", code: 2, userInfo: [NSLocalizedFailureReasonErrorKey: "Invalid formToken"]))
        }
    }
}
