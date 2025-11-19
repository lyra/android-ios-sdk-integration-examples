//
//  AppDelegate.m
//  objectiveC-sdk-integration-example
//
//  Created by Lyra Network on 16/09/2019.
//  Copyright © 2019 Lyra Network. All rights reserved.
//

#import "AppDelegate.h"
@import LyraPaymentSDK;

@interface AppDelegate ()

@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {

  // FIXME: Change by your public key
  NSString *publicKey = @"<REPLACE_ME>";

  // FIXME: Change by the right REST API Server Name (available in merchant BO: Settings->Shop->REST API Keys)
  NSString *apiServerName = @"<REPLACE_ME>"; // without / at the end, example https://myapiservername.com
  NSError *error = nil;

  // Configure SDK options
  NSMutableDictionary *configurationOptions = [[NSMutableDictionary alloc] init];
  [configurationOptions setValue:apiServerName forKey:Lyra.apiServerName];

  // uncomment for enable scan card functionality
  //[configurationOptions setValue:[NSNumber numberWithBool:true] forKey:Lyra.cardScanningEnabled];

  // Initialize Payment SDK
  [Lyra initialize:publicKey:configurationOptions error:&error];
  if (error != nil) {
    // TODO: Handle Payment SDK initilization error
  }
  return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application {
  // Sent when the application is about to move from active to inactive state. This can occur for certain types of
  // temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and
  // it begins the transition to the background state. Use this method to pause ongoing tasks, disable timers, and
  // invalidate graphics rendering callbacks. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
  // Use this method to release shared resources, save user data, invalidate timers, and store enough application state
  // information to restore your application to its current state in case it is terminated later. If your application
  // supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
  // Called as part of the transition from the background to the active state; here you can undo many of the changes
  // made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
  // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was
  // previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application {
  // Called when the application is about to terminate. Save data if appropriate. See also
  // applicationDidEnterBackground:.
}

@end
