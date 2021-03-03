//
//  EnumConverter.h
//  RNZoomUs
//
//  Created by Tien Truong on 22/02/2021.
//
#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

#import "RCTViewManager.h"
#import <MobileRTC/MobileRTC.h>
#import <MobileRTC/MobileRTCVideoView.h>
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN
 
# define DLog(tag, fmt, ...) NSLog((@"%@: " fmt), tag, ##__VA_ARGS__);

@interface EnumConverter : NSObject
+ (MobileRTCVideoAspect)convertAspect:(NSString*)aspect;
@end

NS_ASSUME_NONNULL_END
