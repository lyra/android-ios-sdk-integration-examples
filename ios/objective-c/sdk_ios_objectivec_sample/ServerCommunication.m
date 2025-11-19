//
//  ServerCommunication.m
//  objectiveC-sdk-integration-example
//
//  Created by Lyra Network on 17/09/2019.
//  Copyright © 2019 Lyra Network. All rights reserved.
//

#import "ServerCommunication.h"

@implementation ServerCommunication

// FIXME: change by the right merchant payment server url
NSString *const kMerchantServerUrl = @"<REPLACE_ME>"; // without / at the end, example https://myserverurl.com

// FIXME: change by the rigth merchant server credentials
NSString *const username = @"<REPLACE_ME>";
NSString *const password = @"<REPLACE_ME>";

// Create Payment Context Parameters

// Customer Informations
//  Change by the desired parameters if necessary
NSString *const email = @"customeremail@domain.com";
NSString *const customerReference = @"customerRef";

// Payment parameters
// Change by the desired parameters if necessary
NSString *const amount = @"100";
NSString *const currency = @"EUR";
NSString *const orderId = @"";

// Environment TEST or PRODUCTION, refer to documentation for more information
// FIXME: change by your targeted environment
NSString *const paymentMode = @"TEST";

// TRUE to display a "register the card" switch in the payment form
bool const askRegisterPay = false;

- (void)getProcessPaymentContext:(onGetPaymentContextCompletion)onGetPaymentContextCompletion {
  // create dictionary with params. Check API REST integration documentation
  NSDictionary *customerDict = @{@"email" : email, @"reference" : customerReference};
  NSMutableDictionary *paramsDict = [[NSMutableDictionary alloc] init];
  [paramsDict setValue:amount forKey:@"amount"];
  [paramsDict setValue:paymentMode forKey:@"mode"];
  [paramsDict setValue:customerDict forKey:@"customer"];
  [paramsDict setValue:currency forKey:@"currency"];
  [paramsDict setValue:orderId forKey:@"orderId"];
  [paramsDict setValue:[NSString stringWithFormat:@"%li", [Lyra getFormTokenVersion]] forKey:@"formTokenVersion"];
  if (askRegisterPay)
    paramsDict[@"formAction"] = @"ASK_REGISTER_PAY";

  // create request
  NSURL *url = [[NSURL alloc] initWithString:[NSString stringWithFormat:@"%@/createPayment", kMerchantServerUrl]];
  NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:url];
  request.HTTPMethod = @"POST";

  NSError *error = nil;
  NSData *data = [NSJSONSerialization dataWithJSONObject:paramsDict options:kNilOptions error:&error];
  if (error == nil) {
    request.HTTPBody = data;
    [request addValue:@"application/json; charset=utf-8" forHTTPHeaderField:@"Content-Type"];
    NSString *authStr = [NSString stringWithFormat:@"%@:%@", username, password];
    NSData *authData = [authStr dataUsingEncoding:NSUTF8StringEncoding];
    NSString *authValue = [NSString stringWithFormat:@"Basic %@", [authData base64EncodedStringWithOptions:0]];
    [request setValue:authValue forHTTPHeaderField:@"Authorization"];

    NSURLSessionDataTask *task = [[NSURLSession sharedSession]
        dataTaskWithRequest:request
          completionHandler:^(NSData *_Nullable data, NSURLResponse *_Nullable response, NSError *_Nullable error) {
            if (error != nil || data == nil) {
              onGetPaymentContextCompletion(false, nil, error);
            } else {
              NSError *parseError = nil;
              NSDictionary *objectResponse = [NSJSONSerialization JSONObjectWithData:data options:0 error:&parseError];
              NSDictionary *serverResponse = [objectResponse objectForKey:@"answer"];
              if (serverResponse != nil)
                [self extractFormToken:serverResponse onExtractFormCompletion:onGetPaymentContextCompletion];
              else
                onGetPaymentContextCompletion(false, nil, parseError);
            }
          }];
    [task resume];
  } else
    onGetPaymentContextCompletion(false, nil, error);
}

- (void)verifyPayment:(LyraResponse *)lyraResponse withCompletion:(onVerifyPaymentCompletion)onVerifyPaymentCompletion {
  // Build request
  NSURL *url = [[NSURL alloc] initWithString:[NSString stringWithFormat:@"%@/verifyResult", kMerchantServerUrl]];
  NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:url];
  request.HTTPMethod = @"POST";
  request.HTTPBody = [lyraResponse getResponseData];
  [request addValue:@"application/json; charset=utf-8" forHTTPHeaderField:@"Content-Type"];
  NSString *authStr = [NSString stringWithFormat:@"%@:%@", username, password];
  NSData *authData = [authStr dataUsingEncoding:NSUTF8StringEncoding];
  NSString *authValue = [NSString stringWithFormat:@"Basic %@", [authData base64EncodedStringWithOptions:0]];
  [request setValue:authValue forHTTPHeaderField:@"Authorization"];

  // Call server to verify the operation
  NSURLSessionDataTask *task = [[NSURLSession sharedSession]
      dataTaskWithRequest:request
        completionHandler:^(NSData *_Nullable data, NSURLResponse *_Nullable response, NSError *_Nullable error) {
          if (error != nil)
            onVerifyPaymentCompletion(false, true);
          else {
            NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *)response;
            if ([httpResponse statusCode] == 200)
              onVerifyPaymentCompletion(true, false);
            else
              onVerifyPaymentCompletion(false, false);
          }
        }];
  [task resume];
}

/// This method extract formToken from the given serverResponse.
/// @param serverResponse  String that correspond with the ServerResponse.
/// @param onGetPaymentContextCompletion The completion block to be execute after the formToken is getted.
- (void)extractFormToken:(NSDictionary *)serverResponse
    onExtractFormCompletion:(onGetPaymentContextCompletion)onGetPaymentContextCompletion {
  NSString *formToken = (NSString *)[serverResponse objectForKey:@"formToken"];
  if (formToken != nil) {
    onGetPaymentContextCompletion(true, formToken, nil);
  } else if ((NSString *)[serverResponse objectForKey:@"errorCode"] != nil) {
    NSString *errorCode = (NSString *)[serverResponse objectForKey:@"errorCode"];
    NSString *errorMessage = (NSString *)[serverResponse objectForKey:@"errorMessage"];
    NSString *detailErrorCode = (NSString *)[serverResponse objectForKey:@"detailedErrorCode"];
    NSString *detailErrorMsg = (NSString *)[serverResponse objectForKey:@"detailedErrorMessage"];
    NSString *message = [[NSString alloc]
        initWithFormat:@"Error Code: %@ \n Error Message: %@ \n Error Detail Code: %@ \n Error Detail Message: %@",
                       errorCode, errorMessage, detailErrorCode, detailErrorMsg];
    NSError *error = [[NSError alloc] initWithDomain:@"com.lyra.server.communication"
                                                code:1
                                            userInfo:@{NSLocalizedFailureReasonErrorKey : message}];
    onGetPaymentContextCompletion(false, nil, error);
  } else {
    NSError *error = [[NSError alloc] initWithDomain:@"com.lyra.server.communication"
                                                code:2
                                            userInfo:@{NSLocalizedFailureReasonErrorKey : @"Invalid formToken"}];
    onGetPaymentContextCompletion(false, nil, error);
  }
}

@end
