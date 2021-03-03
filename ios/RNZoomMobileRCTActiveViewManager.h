//
//  RNZoomMobileRCTActiveViewManager.h
//  RNZoomUs
//
//  Created by Tien Truong on 6/26/20.
//

#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

#import "RCTViewManager.h"
#import <MobileRTC/MobileRTC.h>
#import <MobileRTC/MobileRTCVideoView.h>

#import "EnumConverter.h"

@interface RNZoomMobileRCTActiveViewManager : RCTViewManager
@property NSInteger uId;
@end
