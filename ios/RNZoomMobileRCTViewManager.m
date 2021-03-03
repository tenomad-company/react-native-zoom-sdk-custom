//
//  RNZoomMobileRCTViewManager.m
//  RNZoomUs
//
//  Created by Tien Truong on 6/20/20.
//

#import "RNZoomMobileRCTViewManager.h"

#define TAG @"RNZoomUs-Video"

@implementation RNZoomMobileRCTViewManager
RCT_EXPORT_MODULE(RNZoomMobileRCTView);

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

- (UIView *)view {
    return [[MobileRTCVideoView alloc] init];
}

RCT_CUSTOM_VIEW_PROPERTY(renderInfo, NSDictionary, MobileRTCVideoView) {
    @try {
        NSInteger userId = [json[@"userId"] integerValue];
        NSString *videoAspect = json[@"videoAspect"];
        MobileRTCVideoAspect aspect = [EnumConverter convertAspect:videoAspect];
        DLog(TAG, @"RenderInfo: %ld", userId);
        
        if (userId == 0) {
            self.uId = userId;
            [view stopAttendeeVideo];
            return;
        }
        
        if (self.uId != 0 || self.uId != userId) {
            self.uId = userId;
            [view setVideoAspect: aspect];
            [view showAttendeeVideoWithUserID:userId];
        }
    } @catch (NSException *exception) {
        if (view) [view stopAttendeeVideo];
        DLog(TAG, @"RenderInfo Error: %@", [exception reason]);
    }
}
@end
