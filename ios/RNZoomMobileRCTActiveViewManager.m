//
//  RNZoomMobileRCTActiveViewManager.m
//  RNZoomUs
//
//  Created by Tien Truong on 6/26/20.
//

#import "RNZoomMobileRCTActiveViewManager.h"

#define TAG @"RNZoomUs-Video-Active"

@implementation RNZoomMobileRCTActiveViewManager
RCT_EXPORT_MODULE(RNZoomMobileRCTActiveView);

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

- (UIView *)view {
    return [[MobileRTCActiveVideoView alloc] init];
}

RCT_CUSTOM_VIEW_PROPERTY(renderInfo, NSDictionary, MobileRTCActiveVideoView) {
    @try {
        NSInteger userId = [json[@"userId"] integerValue];
        NSString *videoAspect = json[@"videoAspect"];
        MobileRTCVideoAspect aspect = [EnumConverter convertAspect:videoAspect];
        
        DLog(TAG, @"RenderInfo: %ld", userId);
        
        if (self.uId != 0 || self.uId != userId) {
            [view setVideoAspect: aspect];
            [view showAttendeeVideoWithUserID:userId];
            self.uId = userId;
        }
    } @catch (NSException *exception) {
        DLog(TAG, @"RenderInfo Error: %@", [exception reason]);
        [view stopAttendeeVideo];
    }
}
@end
