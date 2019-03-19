//
//  RNIOSExportJsToReact.h
//  RNAlibcSdk
//
//  Created by robin on 2019/3/14.
//  Copyright © 2019 Facebook. All rights reserved.
//

#ifndef RNEventEmitter_h
#define RNEventEmitter_h


#endif /* RNEventEmitter_h */
#import <React/RCTEventEmitter.h>
#import <React/RCTBridgeModule.h>

#define RNEventEmitter(noti) [[NSNotificationCenter defaultCenter] postNotificationName:@"event-emitted" object:noti];

@interface RNEventEmitter : RCTEventEmitter<RCTBridgeModule>

- (void) toGetCartData: (NSString*) url;
@end
