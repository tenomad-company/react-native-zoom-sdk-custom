#import "RNZoomUs.h"

@implementation RNZoomUs
{
    BOOL isInitialized;
    RCTPromiseResolveBlock initializePromiseResolve;
    RCTPromiseRejectBlock initializePromiseReject;
    RCTPromiseResolveBlock meetingPromiseResolve;
    RCTPromiseRejectBlock meetingPromiseReject;
}

- (instancetype)init {
    if (self = [super init]) {
        isInitialized = NO;
        initializePromiseResolve = nil;
        initializePromiseReject = nil;
        meetingPromiseResolve = nil;
        meetingPromiseReject = nil;
    }
    return self;
}

+ (BOOL)requiresMainQueueSetup
{
    return NO;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

- (NSArray<NSString *> *)supportedEvents
{
    return @[
        @"user_joined",
        @"user_leaved",
        @"user_waiting",
        @"user_joined_active",
        @"meeting_ready",
        @"meeting_join_confirmed",
        @"meeting_view_initialized",
        @"meeting_view_destroyed",
        @"meeting_chat_received"
    ];
}

RCT_EXPORT_MODULE()

- (void)enableCustomUI {
    [[[MobileRTC sharedRTC] getMeetingSettings] setEnableCustomMeeting:YES];
    [[[MobileRTC sharedRTC] getMeetingSettings] setAutoConnectInternetAudio:YES];
    [[[MobileRTC sharedRTC] getMeetingSettings] setMuteVideoWhenJoinMeeting:NO];
    [[[MobileRTC sharedRTC] getMeetingSettings] setMuteAudioWhenJoinMeeting:NO];
}

- (UIViewController *)topViewControllerWithRootViewController:(UIViewController*)rootViewController {
    if ([rootViewController isKindOfClass:[UITabBarController class]]) {
        UITabBarController* tabBarController = (UITabBarController*)rootViewController;
        return [self topViewControllerWithRootViewController:tabBarController.selectedViewController];
    } else if ([rootViewController isKindOfClass:[UINavigationController class]]) {
        UINavigationController* navigationController = (UINavigationController*)rootViewController;
        return [self topViewControllerWithRootViewController:navigationController.visibleViewController];
    } else if (rootViewController.presentedViewController) {
        UIViewController* presentedViewController = rootViewController.presentedViewController;
        return [self topViewControllerWithRootViewController:presentedViewController];
    } else {
        return rootViewController;
    }
}

/*
 * ----------------------------------
 * Basic
 * ----------------------------------
 */

RCT_EXPORT_METHOD(
  initialize: (NSDictionary *)data
  withSettings: (NSDictionary *)settings
  withResolve: (RCTPromiseResolveBlock)resolve
  withReject: (RCTPromiseRejectBlock)reject
)
{
  if (isInitialized) {
    resolve(@"Already initialize Zoom SDK successfully.");
    return;
  }

  isInitialized = true;

  @try {
    initializePromiseResolve = resolve;
    initializePromiseReject = reject;

    MobileRTCSDKInitContext *context = [[MobileRTCSDKInitContext alloc] init];
    context.domain = data[@"domain"];
    context.enableLog = YES;
    context.locale = MobileRTC_ZoomLocale_Default;

    //Note: This step is optional, Method is uesd for iOS Replaykit Screen share integration,if not,just ignore this step.
    // context.appGroupId = @"group.zoom.us.MobileRTCSampleExtensionReplayKit";
    BOOL initSuccess = [[MobileRTC sharedRTC] initialize:context];
    [[[MobileRTC sharedRTC] getMeetingSettings] disableShowVideoPreviewWhenJoinMeeting:settings[@"disableShowVideoPreviewWhenJoinMeeting"]];
    [self enableCustomUI];
      
    MobileRTCAuthService *authService = [[MobileRTC sharedRTC] getAuthService];
    if (authService) {
      authService.delegate = self;
      authService.clientKey = data[@"clientKey"];
      authService.clientSecret = data[@"clientSecret"];

      [authService sdkAuth];
    } else {
      NSLog(@"onZoomSDKInitializeResult, no authService");
    }
  } @catch (NSError *ex) {
      reject(@"ERR_UNEXPECTED_EXCEPTION", @"Executing initialize", ex);
  }
}

RCT_EXPORT_METHOD(
  startMeeting: (NSDictionary *)data
  withResolve: (RCTPromiseResolveBlock)resolve
  withReject: (RCTPromiseRejectBlock)reject
)
{
  @try {
    meetingPromiseResolve = resolve;
    meetingPromiseReject = reject;

    MobileRTCMeetingService *ms = [[MobileRTC sharedRTC] getMeetingService];
    if (ms) {
      ms.delegate = self;
      ms.customizedUImeetingDelegate = self;
        
      [self enableCustomUI];

      MobileRTCMeetingStartParam4WithoutLoginUser * params = [[MobileRTCMeetingStartParam4WithoutLoginUser alloc]init];
      params.userName = data[@"userName"];
      params.meetingNumber = data[@"meetingNumber"];
      params.userID = data[@"userId"];
      params.userType = MobileRTCUserType_APIUser;
      params.zak = data[@"zoomAccessToken"];
      MobileRTCMeetError startMeetingResult = [ms startMeetingWithStartParam:params];
      NSLog(@"startMeeting, startMeetingResult= %d", startMeetingResult);
    }
  } @catch (NSError *ex) {
      reject(@"ERR_UNEXPECTED_EXCEPTION", @"Executing startMeeting", ex);
  }
}

RCT_EXPORT_METHOD(
  joinMeeting: (NSDictionary *)data
  withResolve: (RCTPromiseResolveBlock)resolve
  withReject: (RCTPromiseRejectBlock)reject
)
{
  @try {
    meetingPromiseResolve = resolve;
    meetingPromiseReject = reject;

    MobileRTCMeetingService *ms = [[MobileRTC sharedRTC] getMeetingService];
    if (ms) {
      ms.delegate = self;
      ms.customizedUImeetingDelegate = self;
      [self enableCustomUI];

      MobileRTCMeetingJoinParam * joinParam = [[MobileRTCMeetingJoinParam alloc]init];
      joinParam.userName = data[@"userName"];
      joinParam.meetingNumber = data[@"meetingNumber"];
      joinParam.password =  data[@"password"];
      joinParam.participantID = data[@"participantID"];
      joinParam.zak = data[@"zoomAccessToken"];
      joinParam.webinarToken =  data[@"webinarToken"];
      joinParam.noAudio = data[@"noAudio"];
      joinParam.noVideo = data[@"noVideo"];
        
      MobileRTCMeetError joinMeetingResult = [ms joinMeetingWithJoinParam:joinParam];

      NSLog(@"joinMeeting, joinMeetingResult=%d", joinMeetingResult);
    }
  } @catch (NSError *ex) {
      reject(@"ERR_UNEXPECTED_EXCEPTION", @"Executing joinMeeting", ex);
  }
}

RCT_EXPORT_METHOD(
    leaveMeeting:(RCTPromiseResolveBlock)resolve
    withReject:(RCTPromiseRejectBlock)reject
    ) {
    MobileRTCMeetingService *ms = [[MobileRTC sharedRTC] getMeetingService];
    if (ms) {
        [ms leaveMeetingWithCmd:LeaveMeetingCmd_Leave];
        resolve(@"Room Left successfully");
    } else {
        reject(@"ERR_UNEXPECTED_EXCEPTION", @"AuthService not found", [NSError init]);
    }
}

RCT_EXPORT_METHOD(
    endMeeting:(RCTPromiseResolveBlock)resolve
    withReject:(RCTPromiseRejectBlock)reject
    ) {
    MobileRTCMeetingService *ms = [[MobileRTC sharedRTC] getMeetingService];
    if (ms) {
        [ms leaveMeetingWithCmd:LeaveMeetingCmd_End];
        resolve(@"Room End successfully");
    } else {
        reject(@"ERR_UNEXPECTED_EXCEPTION", @"AuthService not found", [NSError init]);
    }
}

/*
 * ----------------------------------
 * User actions
 * ----------------------------------
 */

RCT_EXPORT_METHOD(
    getCurrentID:(RCTPromiseResolveBlock)resolve
    withReject:(RCTPromiseRejectBlock)reject
    ) {
    MobileRTCMeetingService *ms = [[MobileRTC sharedRTC] getMeetingService];
    if (ms) {
        NSUInteger uID = [ms myselfUserID];
        NSNumber *payload = [[NSNumber alloc] initWithUnsignedLong:uID];
        resolve(payload);
    } else {
        reject(@"ERR_UNEXPECTED_EXCEPTION", @"Executing getCurrentID", [[NSError init] initWithText:@"MobileRTCMeetingService not found!"]);
    }
}

RCT_EXPORT_METHOD(
    getUsers:(RCTPromiseResolveBlock)resolve
    withReject:(RCTPromiseRejectBlock)reject
    ) {
    MobileRTCMeetingService *ms = [[MobileRTC sharedRTC] getMeetingService];
    if (ms) {
        NSArray *users = [ms getInMeetingUserList];
        resolve(users);
    } else {
        reject(@"ERR_UNEXPECTED_EXCEPTION", @"Executing getCurrentID", [[NSError init] initWithText:@"MobileRTCMeetingService not found!"]);
    }
}

/*
 * ----------------------------------
 * Video actions
 * ----------------------------------
 */

RCT_EXPORT_METHOD(pinVideo:(NSUInteger)userID
                  withResolve:(RCTPromiseResolveBlock)resolve
                  withReject:(RCTPromiseRejectBlock)reject
                  ) {
    [[[MobileRTC sharedRTC] getMeetingService] pinVideo:YES withUser:userID];
    resolve(@(YES));
}

RCT_EXPORT_METHOD(switchVideoMute:(RCTPromiseResolveBlock)resolve
                  withReject:(RCTPromiseRejectBlock)reject) {
    MobileRTCMeetingService *ms = [[MobileRTC sharedRTC] getMeetingService];
    if (ms && [ms canUnmuteMyVideo]) {
        BOOL mute = [ms isSendingMyVideo];
        MobileRTCVideoError error = [ms muteMyVideo:mute];
        NSLog(@"switchVideoMute: %d",error);
    }
    resolve(@(YES));
}

RCT_EXPORT_METHOD(switchCamera:(RCTPromiseResolveBlock)resolve
                  withReject:(RCTPromiseRejectBlock)reject) {
    MobileRTCMeetingService *ms = [[MobileRTC sharedRTC] getMeetingService];
    if (ms) [ms switchMyCamera];
    resolve(@(YES));
}

/*
 * ----------------------------------
 * Audio actions
 * ----------------------------------
 */

RCT_EXPORT_METHOD(connectMyAudio:(BOOL)on
                  withResolve:(RCTPromiseResolveBlock)resolve
                  withReject:(RCTPromiseRejectBlock)reject) {
    BOOL success = [[[MobileRTC sharedRTC] getMeetingService] connectMyAudio:on];
    
    [[[MobileRTC sharedRTC] getMeetingService] myAudioType];
    resolve(@(success));
}

RCT_EXPORT_METHOD(switchAudioSource:(RCTPromiseResolveBlock)resolve
                  withReject:(RCTPromiseRejectBlock)reject) {
    MobileRTCAudioError error = [[[MobileRTC sharedRTC] getMeetingService] switchMyAudioSource];
    if (error == MobileRTCAudioError_Success) {
        resolve(@"Switch Audio Success!");
    } else {
        reject(@"ERR_UNEXPECTED_EXCEPTION",
               [NSString stringWithFormat:@"SwitchMyAudioSource: %d", error],
               [NSError errorWithDomain:@"us.zoom.sdk" code:error userInfo:nil]);
    }
}

RCT_EXPORT_METHOD(switchAudioMute:(RCTPromiseResolveBlock)resolve
                  withReject:(RCTPromiseRejectBlock)reject) {
    MobileRTCMeetingService *ms = [[MobileRTC sharedRTC] getMeetingService];
    if (ms)
    {
        MobileRTCAudioType audioType = [ms myAudioType];
        NSLog(@"switchAudioMute, %lu", audioType);
        
        switch (audioType)
        {
            case MobileRTCAudioType_VoIP: //voip
            case MobileRTCAudioType_Telephony: //phone
            {
                if ([ms canUnmuteMyAudio]) {
                    BOOL isMuted = [ms isMyAudioMuted];
                    [ms muteMyAudio:!isMuted];
                }
                break;
            }
            case MobileRTCAudioType_None:
            {
                //Supported VOIP
                if ([ms isSupportedVOIP]) {
                    NSLog(@"isSupportedVOIP");
                    
                    if (@available(iOS 8, *)) {
                        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:NSLocalizedString(@"To hear others\n please join audio", @"")
                                                                                                 message:nil
                                                                                          preferredStyle:UIAlertControllerStyleAlert];
                        
                        [alertController addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"Call via Internet", @"")
                                                                            style:UIAlertActionStyleDefault
                                                                          handler:^(UIAlertAction *action) {
                            //Join VOIP
                            MobileRTCMeetingService *ms = [[MobileRTC sharedRTC] getMeetingService];
                            if (ms)[ms connectMyAudio:YES];
                        }]];
                        
                        [alertController addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"Cancel", nil)
                                                                            style:UIAlertActionStyleCancel
                                                                          handler:^(UIAlertAction *action) {}]
                        ];
                        
                        UIViewController *rootViewController = [self topViewControllerWithRootViewController:[[[UIApplication sharedApplication] delegate] window].rootViewController];
                        [rootViewController presentViewController:alertController animated:YES completion:nil];
                    }
                }
                break;
            }
        }
    }
}

/*
 * ----------------------------------
 * Chat actions
 * ----------------------------------
 */

RCT_EXPORT_METHOD(sendMessage:(NSString *)message
                  withResolve:(RCTPromiseResolveBlock)resolve
                  withReject:(RCTPromiseRejectBlock)reject) {
    MobileRTCMeetingService *ms = [[MobileRTC sharedRTC] getMeetingService];
    if (ms) {
        [ms sendChatToGroup:MobileRTCChatGroup_All WithContent:message];
        resolve(@"Success");
    } else {
        reject(
            @"ERR_ZOOM_SEND_MESSAGE",
            [NSString stringWithFormat:@"Error: %@", @"Meeting Service Not Found"],
            [NSError errorWithDomain:@"us.zoom.sdk" code:0 userInfo:nil]
        );
    }
}

/*
 * ----------------------------------
 * Handlers
 * ----------------------------------
 */

// MARK: MobileRTCAuthDelegate

- (void)onMobileRTCAuthReturn:(MobileRTCAuthError)returnValue {
    NSLog(@"nZoomSDKInitializeResult, errorCode=%d", returnValue);
    if (returnValue != MobileRTCAuthError_Success) {
        initializePromiseReject(
            @"ERR_ZOOM_INITIALIZATION",
            [NSString stringWithFormat:@"Error: %d", returnValue],
            [NSError errorWithDomain:@"us.zoom.sdk" code:returnValue userInfo:nil]
            );
    } else {
        initializePromiseResolve(@"Initialize Zoom SDK successfully.");
    }
}

// MARK: MobileRTCMeetingServiceDelegate
- (void)onMeetingReturn:(MobileRTCMeetError)errorCode internalError:(NSInteger)internalErrorCode {
    NSLog(@"onMeetingReturn, error=%d, internalErrorCode=%zd", errorCode, internalErrorCode);

    if (!meetingPromiseResolve) {
        return;
    }

    if (errorCode != MobileRTCMeetError_Success) {
        meetingPromiseReject(
            @"ERR_ZOOM_MEETING",
            [NSString stringWithFormat:@"Error: %d, internalErrorCode=%zd", errorCode, internalErrorCode],
            [NSError errorWithDomain:@"us.zoom.sdk" code:errorCode userInfo:nil]
            );
    } else {
        meetingPromiseResolve(@"Connected to zoom meeting");
    }

    meetingPromiseResolve = nil;
    meetingPromiseReject = nil;
}

- (void)onMeetingStateChange:(MobileRTCMeetingState)state {
    NSLog(@"onMeetingStatusChanged, meetingState=%d", state);

    if (state == MobileRTCMeetingState_InMeeting || state == MobileRTCMeetingState_Idle) {
        if (!meetingPromiseResolve) {
            return;
        }

        NSLog(@"onMeetingStatusChanged resolved!");
        meetingPromiseResolve(@"Connected to zoom meeting");

        meetingPromiseResolve = nil;
        meetingPromiseReject = nil;
    }
}

- (void)onMicrophoneStatusError:(MobileRTCMicrophoneError)error {
    NSLog(@"onMicrophoneStatusError, %lu", error);
}

- (void)onMeetingError:(MobileRTCMeetError)errorCode message:(NSString *)message {
    NSLog(@"onMeetingError, errorCode=%d, message=%@", errorCode, message);

    if (!meetingPromiseResolve) {
        return;
    }

    if ([message isEqualToString:@"success"]) {
        meetingPromiseResolve(@"Connected to zoom meeting");
    } else {
        meetingPromiseReject(
            @"ERR_ZOOM_MEETING",
            [NSString stringWithFormat:@"Error: %d, internalErrorCode=%@", errorCode, message],
            [NSError errorWithDomain:@"us.zoom.sdk" code:errorCode userInfo:nil]
            );
    }

    meetingPromiseResolve = nil;
    meetingPromiseReject = nil;
}

- (void)onWaitingRoomStatusChange:(BOOL)needWaiting
{
    if (needWaiting) {
        NSLog(@"needWaiting true");
    } else {
        NSLog(@"needWaiting false");
    }
}

- (void)onJBHWaitingWithCmd:(JBHCmd)cmd
{
    NSLog(@"onJBHWaitingWithCmd %d", cmd);
    BOOL isWaiting = (cmd == JBHCmd_Show);
    [self sendEventWithName:@"user_waiting" body:@(isWaiting)];
}

- (void)onMeetingReady {
    NSLog(@"===== onMeetingReady");
    [self sendEventWithName:@"meeting_ready" body:@(YES)];
}

- (void)onJoinMeetingConfirmed {
    NSLog(@"===== onJoinMeetingConfirmed");
    [self sendEventWithName:@"meeting_join_confirmed" body:@(YES)];
}

/*!
 @brief The function will be invoked once the user joins the meeting.
 @result The ID of user who joins the meeting.
 */
- (void)onSinkMeetingUserJoin:(NSUInteger)userID {
    NSLog(@"===== onSinkMeetingUserJoin %lu", userID);
    [self sendEventWithName:@"user_joined" body:@(userID)];
    NSLog(@"===== myAudioType %lu", [[[MobileRTC sharedRTC] getMeetingService] myAudioType]);
}

/*!
 @brief The function will be invoked once the user leaves the meeting.
 @result The ID of user who leaves the meeting.
 */
- (void)onSinkMeetingUserLeft:(NSUInteger)userID {
    NSLog(@"===== onSinkMeetingUserLeft %lu", userID);
    [self sendEventWithName:@"user_leaved" body:@(userID)];
}

- (void)onSinkMeetingActiveVideo:(NSUInteger)userID
{
    NSLog(@"===== onSinkMeetingActiveVideo %lu", userID);
    [self sendEventWithName:@"user_joined_active" body:@(userID)];
}

- (void)onInMeetingChat:(NSString *)messageID {
    NSLog(@"===== onInMeetingChat %@", messageID);
    MobileRTCMeetingChat *chat = [[[MobileRTC sharedRTC] getMeetingService] meetingChatByID:messageID];
    [self sendEventWithName:@"meeting_chat_received" body:chat];
    NSLog(@"===== %@", chat);
}

// MARK: MobileRTCWaitingRoomServiceDelegate
- (void)onWaitingRoomUserJoin:(NSUInteger)userId {
    NSLog(@"===== onWaitingRoomUserJoin %lu", userId);
}

- (void)onWaitingRoomUserLeft:(NSUInteger)userId {
    NSLog(@"===== onWaitingRoomUserLeft %lu", userId);
}

// MARK: MobileRTCCustomizedUIMeetingDelegate
- (void)onInitMeetingView {
    NSLog(@"onInitMeetingView");
    [self sendEventWithName:@"meeting_view_initialized" body:@(YES)];
}

- (void)onDestroyMeetingView {
    NSLog(@"onDestroyMeetingView");
    [self sendEventWithName:@"meeting_view_destroyed" body:@(YES)];
}

@end
