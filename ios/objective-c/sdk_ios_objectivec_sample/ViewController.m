//
//  ViewController.m
//  objectiveC-sdk-integration-example
//
//  Created by Lyra Network on 16/09/2019.
//  Copyright © 2019 Lyra Network. All rights reserved.
//

#import "ViewController.h"
@import LyraPaymentSDK;
#import "ServerCommunication.h"

@interface ViewController ()

@property(nonatomic, strong) ServerCommunication *serverComunication;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

- (IBAction)executeSdkPayment:(id)sender {

    // 1. Init server comunication class for get createPayment context
    _serverComunication = [[ServerCommunication alloc] init];

    // 2. Execute getProcessPaymentContext for get the formToken (required param in SDK process method)
    [_serverComunication getProcessPaymentContext:^(BOOL getContextSuccess, NSString *formToken, NSError* error) {
        if (!getContextSuccess || formToken == nil) {
            //TODO: Handle error in getProcessPaymentContext
            NSString *errorMessage = error != nil ? [[error userInfo] objectForKey:NSLocalizedFailureReasonErrorKey] :  @"Error getting payment context"
            [self showMessage: errorMessage];
            return;
        }
        //After the payment context has beeen obtained
        // 3. Call the PaymentSDK process method
        NSError *error = nil;
        [Lyra processWithContextViewController:self formToken:formToken error:&error onSuccess:^(LyraResponse *lyraResponse) {

            //4. Verify the payment using your server: Check the response integrity by verifying the hash on your server
            [self verifyPayment:lyraResponse];

        } onError:^(LyraError *lyraError, LyraResponse *lyraResponse) {
            //TODO: Handle Payment SDK error in process payment request
            [self showMessage: [NSString stringWithFormat:@"Payment fail: %@", lyraError.errorMessage]];
            
        }];
    }];
}

- (void) showMessage:(NSString*) message
{
    dispatch_async(dispatch_get_main_queue(), ^{
        UIAlertController * alertController = [UIAlertController
                                               alertControllerWithTitle:@""
                                               message:message
                                               preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction* okButton = [UIAlertAction
                                   actionWithTitle:@"Ok"
                                   style:UIAlertActionStyleDefault
                                   handler:^(UIAlertAction * action) {
        }];
        [alertController addAction:okButton];
        [self presentViewController:alertController animated:YES completion:nil];
    });
}

/// Check the response integrity by verifying the hash on your server
/// @param lyraResponse  Response of process payment
- (void) verifyPayment:(LyraResponse*) lyraResponse  {
    [_serverComunication verifyPayment:lyraResponse withCompletion:^(BOOL paymentVerified, BOOL isErrorConnection) {
        if(paymentVerified)
            [self showMessage: @"Payment success"];
        else
            [self showMessage: @"Payment verification fail"];
    }];
}

@end
