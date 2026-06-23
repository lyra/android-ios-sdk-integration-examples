//
//  ViewController.swift
//  sdk_ios_swift_sample
//
//  Created by Lyra Network on 19/09/2019.
//  Copyright © 2019 Lyra Network. All rights reserved.
//

import LyraPaymentSDK
import UIKit

class ViewController: UIViewController {

  // 1. Init server comunication class for get createPayment context
  let serverCommunication = ServerCommunication()

  override func viewDidLoad() {
    super.viewDidLoad()

  }

  @IBAction func executeSdkPayment(_ sender: Any) {

    Task {

      do {
        // 2. Get formToken (required param in SDK process method)
        let formToken = try await self.serverCommunication.getFormToken()
        // 3. Call the PaymentSDK process method
        try Lyra.process(
          self, formToken,
          onSuccess: { (_ lyraResponse: LyraResponse) -> Void in

            //4. Verify the payment using your server: Check the response integrity by verifying the hash on your server
            Task {
              do {
                let verified = try await self.serverCommunication
                  .verifyPayment(lyraResponse)

                self.showMessage(
                  verified ? "Payment success" : "Payment fail"
                )
              } catch {
                self.showMessage("Verification failed")
              }
            }
          },
          onError: { (_ error: LyraError, _ lyraResponse: LyraResponse?) -> Void in

            //TODO: Handle Payment SDK error in process payment request
            self.showMessage("Payment fail: \(error.errorMessage)")

          })
      } catch let error {
        self.showMessage(error.localizedDescription)
      }
    }

  }

  func showMessage(_ message: String?) {
    DispatchQueue.main.async {
      let alert = UIAlertController(title: "", message: message, preferredStyle: UIAlertController.Style.alert)
      alert.addAction(UIAlertAction(title: "Ok", style: .default, handler: nil))
      self.present(alert, animated: true, completion: nil)
    }
  }

}
