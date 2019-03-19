//
//  RNCallIosAction.h
//  RNAlibcSdk
//
//  Created by robin on 2019/3/14.
//  Copyright © 2019 Facebook. All rights reserved.
//

#ifndef MyConnectionURLProtocol_h
#define MyConnectionURLProtocol_h


#endif /* RNCallIosAction_h */
#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
@interface MyConnectionURLProtocol : NSURLProtocol

@end

@interface MyConnectionURLProtocol () <NSURLConnectionDataDelegate>

@property (nonatomic, strong) NSURLConnection * connection;

@end

