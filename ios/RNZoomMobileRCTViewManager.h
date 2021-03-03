//
//  RNZoomMobileRCTViewManager.h
//  RNZoomUs
//
//  Created by Tien Truong on 6/20/20.
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

@interface RNZoomMobileRCTViewManager : RCTViewManager
@property NSInteger uId;
@end
