#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#import "RCTEventEmitter.h"
#import "RCTConvert.h"
#else
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTConvert.h>
#endif

#import <MobileRTC/MobileRTC.h>

@interface RNZoomUs : RCTEventEmitter <RCTBridgeModule, MobileRTCAuthDelegate, MobileRTCMeetingServiceDelegate, MobileRTCWaitingRoomServiceDelegate, MobileRTCCustomizedUIMeetingDelegate>

@end
