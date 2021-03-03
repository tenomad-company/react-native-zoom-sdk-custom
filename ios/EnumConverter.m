//
//  EnumConverter.m
//  RNZoomUs
//
//  Created by Tien Truong on 22/02/2021.
//

#import "EnumConverter.h"

@implementation EnumConverter
+ (MobileRTCVideoAspect)convertAspect:(NSString*)aspect {
    if (!aspect) return MobileRTCVideoAspect_Original;
    
    NSArray *items = @[@"letterBox", @"panAndScan", @"fullFilled", @"original"];
    NSUInteger index = [items indexOfObject:aspect];
    switch (index) {
        case 0:
            return MobileRTCVideoAspect_LetterBox;
        case 1:
            return MobileRTCVideoAspect_PanAndScan;
        case 2:
            return MobileRTCVideoAspect_Full_Filled;
        default:
            return MobileRTCVideoAspect_Original;
    }
}
@end

@implementation RCTConvert(VideoAspect)
RCT_ENUM_CONVERTER(MobileRTCVideoAspect, (@{
    @"original": @(MobileRTCVideoAspect_Original),
    @"letterBox": @(MobileRTCVideoAspect_LetterBox),
    @"panAndScan": @(MobileRTCVideoAspect_PanAndScan),
    @"fullFilled": @(MobileRTCVideoAspect_Full_Filled),
    }), MobileRTCVideoAspect_Original, intValue)
@end

@implementation RCTConvert (JBHStatus)
RCT_ENUM_CONVERTER(JBHCmd, (@{
    @"show": @(JBHCmd_Show),
    @"hide": @(JBHCmd_Hide)
    }), JBHCmd_Hide, intValue)
@end
