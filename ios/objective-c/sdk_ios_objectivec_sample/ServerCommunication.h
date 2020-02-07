//
//  ServerCommunication.h
//  objectiveC-sdk-integration-example
//
//  Created by Lyra Network on 17/09/2019.
//  Copyright © 2019 Lyra Network. All rights reserved.
//

#import <Foundation/Foundation.h>
@import LyraPaymentSDK_INTE;

NS_ASSUME_NONNULL_BEGIN

typedef void(^onGetPaymentContextCompletion)(BOOL getContextSuccess, NSString* _Nullable serverAnswer);  
typedef void(^onVerifyPaymentCompletion)(BOOL paymentVerified, BOOL isErrorConnection);

/**
 This class represents the communication with your merchant server to create the necessary payment context for the SDK and to verify the payment on your server. It is an example of how you can implement this communication. Check API REST integration documentation.
 */
@interface ServerCommunication : NSObject

/**
 This method send to your merchant server the information necessary for create the request to execute the payment using the Payment SDK.

 @param onGetPaymentContextCompletion The completion block to be executed after the getProcessPaymentContext has finished.
 */
- (void) getProcessPaymentContext:(onGetPaymentContextCompletion) onGetPaymentContextCompletion;

/**
 This method send to your merchant server the information necessary for validate the payment.

 @param lyraResponse Corresponds to the object that returns the SDK with the payment information once the payment processing is finished.
 @param onVerifyPaymentCompletion The completion block to be executed after the payment validation has finished.
 */
- (void) verifyPayment:(LyraResponse*)lyraResponse withCompletion:(onVerifyPaymentCompletion) onVerifyPaymentCompletion;

@end

NS_ASSUME_NONNULL_END
