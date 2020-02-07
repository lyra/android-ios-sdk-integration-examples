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
NSString *const kMerchantServerUrl = @"<REPLACE_ME>";

//Create Payment Context Parameters

//Customer Informations
// Change by the desired parameters if necessary
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

- (void) getProcessPaymentContext:(onGetPaymentContextCompletion) onGetPaymentContextCompletion {
    //create dictionary with params. Check API REST integration documentation
    NSDictionary *customerDict = @{@"email": email, @"reference": customerReference };
    NSMutableDictionary *paramsDict = [[NSMutableDictionary alloc] init];
    [paramsDict setValue:amount forKey:@"amount"];
    [paramsDict setValue:paymentMode forKey:@"mode"];
    [paramsDict setValue:customerDict forKey:@"customer"];
    [paramsDict setValue:currency forKey:@"currency"];
    [paramsDict setValue:orderId forKey:@"orderId"];
    [paramsDict setValue:[NSString stringWithFormat:@"%li", [Lyra getFormTokenVersion]] forKey:@"formTokenVersion"];
    if (askRegisterPay)
        paramsDict[@"formAction"] = @"ASK_REGISTER_PAY";

    //create request
    NSURL *url = [[NSURL alloc] initWithString:[NSString stringWithFormat:@"%@/createPayment", kMerchantServerUrl]];
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:url];
    request.HTTPMethod = @"POST";

    NSError *error = nil;
    NSData *data = [NSJSONSerialization dataWithJSONObject:paramsDict
                                                   options:kNilOptions error:&error];
    if (error == nil) {
        request.HTTPBody = data;
        [request addValue:@"application/json; charset=utf-8" forHTTPHeaderField:@"Content-Type"];

        NSURLSessionDataTask *task = [[NSURLSession sharedSession] dataTaskWithRequest: request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
            if (error != nil || data == nil) {
                onGetPaymentContextCompletion(false, nil);
            } else {
                NSString* serverAnswer = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
                if(serverAnswer != nil)
                    onGetPaymentContextCompletion(true, serverAnswer);
                else
                    onGetPaymentContextCompletion(false, nil);
            }
        }];
        [task resume];
    } else
        onGetPaymentContextCompletion(false, nil);
}

- (void) verifyPayment:(LyraResponse*)lyraResponse withCompletion:(onVerifyPaymentCompletion) onVerifyPaymentCompletion {
    //Build request
    NSURL *url = [[NSURL alloc] initWithString:[NSString stringWithFormat:@"%@/verifyResult", kMerchantServerUrl]];
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:url];
    request.HTTPMethod = @"POST";
    request.HTTPBody = [lyraResponse getResponseData];
    [request addValue:@"application/json; charset=utf-8" forHTTPHeaderField:@"Content-Type"];

    // Call server to verify the operation
    NSURLSessionDataTask *task = [[NSURLSession sharedSession] dataTaskWithRequest: request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        if (error != nil)
            onVerifyPaymentCompletion(false, true);
        else {
            NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse*) response;
            if ([httpResponse statusCode] == 200)
                onVerifyPaymentCompletion(true, false);
            else
                onVerifyPaymentCompletion(false, false);
        }

    }];
    [task resume];
}

@end
