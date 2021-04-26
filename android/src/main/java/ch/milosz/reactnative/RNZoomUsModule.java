package ch.milosz.reactnative;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.ArrayList;
import java.util.List;

import us.zoom.sdk.CameraDevice;
import us.zoom.sdk.FreeMeetingNeedUpgradeType;
import us.zoom.sdk.InMeetingAudioController;
import us.zoom.sdk.InMeetingChatController;
import us.zoom.sdk.InMeetingChatMessage;
import us.zoom.sdk.InMeetingEventHandler;
import us.zoom.sdk.InMeetingService;
import us.zoom.sdk.InMeetingServiceListener;
import us.zoom.sdk.InMeetingVideoController;
import us.zoom.sdk.JoinMeetingOptions;
import us.zoom.sdk.JoinMeetingParams;
import us.zoom.sdk.MeetingError;
import us.zoom.sdk.MeetingService;
import us.zoom.sdk.MeetingServiceListener;
import us.zoom.sdk.MeetingSettingsHelper;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.StartMeetingOptions;
import us.zoom.sdk.StartMeetingParamsWithoutLogin;
import us.zoom.sdk.ZoomError;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdk.ZoomSDKInitParams;
import us.zoom.sdk.ZoomSDKInitializeListener;

public class RNZoomUsModule extends ReactContextBaseJavaModule implements ZoomSDKInitializeListener, MeetingServiceListener, InMeetingServiceListener, LifecycleEventListener {
    private final static String TAG = "RNZoomUs";
    private final static int MY_CAMERA_REQUEST_CODE = 100;
    private final static int MY_MICROPHONE_REQUEST_CODE = 101;
    private final ReactApplicationContext reactContext;

    private Boolean isInitialized = false;
    private Boolean isInMeeting = false;
    private Promise initializePromise;
    private Promise meetingPromise;

    public RNZoomUsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return "RNZoomUs";
    }

    public void sendEvent(String eventName, @Nullable Object params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }


    public void enableCustomUI() {
        MeetingSettingsHelper helper = ZoomSDK.getInstance().getMeetingSettingsHelper();
        helper.setCustomizedMeetingUIEnabled(true);
        helper.enableForceAutoStartMyVideoWhenJoinMeeting(true);
    }

    /*
     * ----------------------------------
     * Basic
     * ----------------------------------
     */

    @ReactMethod
    public void initialize(final ReadableMap params, final ReadableMap settings, final Promise promise) {
        if (isInitialized) {
            promise.resolve("Already initialize Zoom SDK successfully.");
            return;
        }

        try {
            isInitialized = true;
            initializePromise = promise;

            reactContext.getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ZoomSDK zoomSDK = ZoomSDK.getInstance();
                    ZoomSDKInitParams initParams = new ZoomSDKInitParams();
                    initParams.appKey = params.getString("clientKey");
                    initParams.appSecret = params.getString("clientSecret");
                    initParams.domain = params.getString("domain");
                    initParams.enableLog = true;
                    zoomSDK.initialize(reactContext.getCurrentActivity(), RNZoomUsModule.this, initParams);
                }
            });
        } catch (Exception ex) {
            promise.reject("ERR_UNEXPECTED_EXCEPTION", ex);
        }
    }

    @ReactMethod
    public void startMeeting(final ReadableMap paramMap, final Promise promise) {
        try {
            ZoomSDK zoomSDK = ZoomSDK.getInstance();
            if (!zoomSDK.isInitialized()) {
                promise.reject("ERR_ZOOM_START", "ZoomSDK has not been initialized successfully");
                return;
            }

            meetingPromise = promise;
            final MeetingService meetingService = zoomSDK.getMeetingService();

            final String displayName = paramMap.getString("userName");
            final String meetingNo = paramMap.getString("meetingNumber");
            final String userId = paramMap.getString("userId");
            final String zoomAccessToken = paramMap.getString("zoomAccessToken");

            checkCameraPermission();
            enableCustomUI();

            if (meetingService.getMeetingStatus() != MeetingStatus.MEETING_STATUS_IDLE) {
                long lMeetingNo = 0;
                try {
                    lMeetingNo = Long.parseLong(meetingNo);
                } catch (NumberFormatException e) {
                    promise.reject("ERR_ZOOM_START", "Invalid meeting number: " + meetingNo);
                    return;
                }

                if (meetingService.getCurrentRtcMeetingNumber() == lMeetingNo) {
                    meetingService.returnToMeeting(reactContext.getCurrentActivity());
                    promise.resolve("Already joined zoom meeting");
                    return;
                }
            }

            reactContext.getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StartMeetingOptions opts = new StartMeetingOptions();
                    StartMeetingParamsWithoutLogin params = new StartMeetingParamsWithoutLogin();
                    params.displayName = displayName;
                    params.meetingNo = meetingNo;
                    params.userId = userId;
                    params.userType = MeetingService.USER_TYPE_API_USER;
                    params.zoomAccessToken = zoomAccessToken;

                    int startMeetingResult = meetingService.startMeetingWithParams(reactContext.getCurrentActivity(), params, opts);
                    Log.i(TAG, "startMeeting, Result= " + startMeetingResult);

                    if (startMeetingResult != MeetingError.MEETING_ERROR_SUCCESS) {
                        promise.reject("ERR_ZOOM_START", "startMeeting, errorCode=" + startMeetingResult);
                    }
                }
            });
        } catch (Exception ex) {
            promise.reject("ERR_UNEXPECTED_EXCEPTION", ex);
        }
    }

    @ReactMethod
    public void joinMeeting(final ReadableMap paramMap, Promise promise) {
        try {
            ZoomSDK zoomSDK = ZoomSDK.getInstance();
            if (!zoomSDK.isInitialized()) {
                promise.reject("ERR_ZOOM_JOIN", "ZoomSDK has not been initialized successfully");
                return;
            }

            meetingPromise = promise;
            final MeetingService meetingService = zoomSDK.getMeetingService();

            JoinMeetingOptions opts = new JoinMeetingOptions();
            if (paramMap.hasKey("participantID"))
                opts.participant_id = paramMap.getString("participantID");
            if (paramMap.hasKey("noAudio")) opts.no_audio = paramMap.getBoolean("noAudio");
            if (paramMap.hasKey("noVideo")) opts.no_video = paramMap.getBoolean("noVideo");

            JoinMeetingParams params = new JoinMeetingParams();
            params.displayName = paramMap.getString("displayName");
            params.meetingNo = paramMap.getString("meetingNo");
            if (paramMap.hasKey("password")) params.password = paramMap.getString("password");

            int joinMeetingResult = meetingService.joinMeetingWithParams(reactContext.getCurrentActivity(), params, opts);
            Log.i(TAG, "joinMeeting, joinMeetingResult=" + joinMeetingResult);

            if (joinMeetingResult != MeetingError.MEETING_ERROR_SUCCESS) {
                promise.reject("ERR_ZOOM_JOIN", "joinMeeting, errorCode=" + joinMeetingResult);
            }
        } catch (Exception ex) {
            promise.reject("ERR_UNEXPECTED_EXCEPTION", ex);
        }
    }

    @ReactMethod
    public void leaveMeeting(final Promise promise) {
        try {
            reactContext.getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ZoomSDK zoomSDK = ZoomSDK.getInstance();
                    zoomSDK.getMeetingService().leaveCurrentMeeting(false);
                    promise.resolve("Room Left successfully");
                    isInMeeting = false;
                }
            });
        } catch (Exception ex) {
            promise.reject("ERR_UNEXPECTED_EXCEPTION", ex);
        }
    }

    @ReactMethod
    public void endMeeting(final Promise promise) {
        try {
            reactContext.getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ZoomSDK zoomSDK = ZoomSDK.getInstance();
                    zoomSDK.getMeetingService().leaveCurrentMeeting(true);
                    promise.resolve("Room End successfully");
                    isInMeeting = false;
                }
            });
        } catch (Exception ex) {
            promise.reject("ERR_UNEXPECTED_EXCEPTION", ex);
        }
    }

    /*
     * ----------------------------------
     * User actions
     * ----------------------------------
     */

    @ReactMethod
    public void getCurrentID(Promise promise) {
        try {
            ZoomSDK zoomSDK = ZoomSDK.getInstance();
            if (!zoomSDK.isInitialized()) {
                promise.reject("ERR_ZOOM_JOIN", "ZoomSDK has not been initialized successfully");
                return;
            }

            int uID = (int) zoomSDK.getInMeetingService().getMyUserID();
            Log.i(TAG, "getCurrentID: " + uID);

            promise.resolve(uID);
        } catch (Exception ex) {
            promise.reject("ERR_UNEXPECTED_EXCEPTION", ex);
        }
    }

    @ReactMethod
    public void getUsers(Promise promise) {
        try {
            ZoomSDK zoomSDK = ZoomSDK.getInstance();
            if (!zoomSDK.isInitialized()) {
                promise.reject("ERR_ZOOM_JOIN", "ZoomSDK has not been initialized successfully");
                return;
            }

            List<Long> userList = zoomSDK.getInMeetingService().getInMeetingUserList();

            WritableArray uIDs = Arguments.createArray();
            for (int i = 0; i < userList.size(); i++) {
                uIDs.pushInt(userList.get(i).intValue());
            }

            promise.resolve(uIDs);
        } catch (Exception ex) {
            promise.reject("ERR_UNEXPECTED_EXCEPTION", ex);
        }
    }

    /*
     * ----------------------------------
     * Video actions
     * ----------------------------------
     */

    @ReactMethod
    public void pinVideo(int userID, Promise promise) {
        InMeetingVideoController ctrl = ZoomSDK.getInstance().getInMeetingService().getInMeetingVideoController();
        ctrl.pinVideo(true, userID);
    }

    @ReactMethod
    public void checkVideoRotation(Promise promise) {
        Display display = ((WindowManager) reactContext.getCurrentActivity().getSystemService(Service.WINDOW_SERVICE)).getDefaultDisplay();
        int displayRotation = display.getRotation();
        InMeetingVideoController controller = ZoomSDK.getInstance().getInMeetingService().getInMeetingVideoController();
        controller.rotateMyVideo(displayRotation);
        promise.resolve(true);
    }

    @ReactMethod
    public void switchVideoMute(final Promise promise) {
        final InMeetingVideoController ctrl = ZoomSDK.getInstance().getInMeetingService().getInMeetingVideoController();
        reactContext.getCurrentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (ctrl.isMyVideoMuted()) {
                    if (ctrl.canUnmuteMyVideo()) ctrl.muteMyVideo(false);
                } else {
                    ctrl.muteMyVideo(true);
                }
                promise.resolve(true);
            }
        });
    }

    @ReactMethod
    public void switchCamera(final Promise promise) {
        final InMeetingVideoController ctrl = ZoomSDK.getInstance().getInMeetingService().getInMeetingVideoController();
        if (ctrl.canSwitchCamera()) {
            final List<CameraDevice> devices = ctrl.getCameraDeviceList();
            if (devices != null && devices.size() > 2) {
                List<String> itemLabelList = new ArrayList<>();
                for (CameraDevice device : devices) {
                    itemLabelList.add(device.getDeviceName());
                }
                String[] itemLabels = itemLabelList.toArray(new String[0]);

                AlertDialog.Builder builder = new AlertDialog.Builder(getCurrentActivity());
                builder.setTitle("Select camera");
                builder.setItems(itemLabels, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        ctrl.switchCamera(devices.get(i).getDeviceId());
                        promise.resolve(true);
                    }
                });
                builder.show();
            } else {
                reactContext.getCurrentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ctrl.switchToNextCamera();
                        promise.resolve(true);
                    }
                });
            }
        } else {
            promise.reject("ERR_CANNOT_SWITCH_CAMERA", "Cannot switch camera");
        }
    }

    @ReactMethod
    public void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getCurrentActivity(), new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
    }

    /*
     * ----------------------------------
     * Audio actions
     * ----------------------------------
     */

    // You can check whether the audio is connected by calling `isAudioConnected` method:
    @ReactMethod
    public void isAudioConnected(Promise promise) {
        InMeetingAudioController ctrl = ZoomSDK.getInstance().getInMeetingService().getInMeetingAudioController();
        promise.resolve(ctrl.isAudioConnected());
    }

    // You can check whether your audio is muted by calling `isMyAudioMuted` method:
    @ReactMethod
    public void isMyAudioMuted(Promise promise) {
        InMeetingAudioController ctrl = ZoomSDK.getInstance().getInMeetingService().getInMeetingAudioController();
        promise.resolve(ctrl.isMyAudioMuted());
    }

    // You can check whether you can unmute your audio by calling `canUnmuteMyAudio` method:
    @ReactMethod
    public void canUnmuteMyAudio(Promise promise) {
        InMeetingAudioController ctrl = ZoomSDK.getInstance().getInMeetingService().getInMeetingAudioController();
        promise.resolve(ctrl.canUnmuteMyAudio());
    }

    // If you would like to connect audio with "Voice over IP", you can implement the `connectAudioWithVoIP` method:
    @ReactMethod
    public void connectAudioWithVoIP(Promise promise) {
        InMeetingAudioController ctrl = ZoomSDK.getInstance().getInMeetingService().getInMeetingAudioController();
        ctrl.connectAudioWithVoIP();
        promise.resolve(true);
    }

    // Check whether you can switch audio output.
    @ReactMethod
    public void canSwitchAudioOutput(Promise promise) {
        InMeetingAudioController ctrl = ZoomSDK.getInstance().getInMeetingService().getInMeetingAudioController();
        promise.resolve(ctrl.canSwitchAudioOutput());
    }

    @ReactMethod
    public void getLoudSpeakerStatus(Promise promise) {
        InMeetingAudioController ctrl = ZoomSDK.getInstance().getInMeetingService().getInMeetingAudioController();
        promise.resolve(ctrl.getLoudSpeakerStatus());
    }

    @ReactMethod
    public void disconnectAudio(Promise promise) {
        InMeetingAudioController ctrl = ZoomSDK.getInstance().getInMeetingService().getInMeetingAudioController();
        ctrl.disconnectAudio();
        promise.resolve(true);
    }

    @ReactMethod
    public void switchAudioMute(final Promise promise) {
        final InMeetingAudioController ctrl = ZoomSDK.getInstance().getInMeetingService().getInMeetingAudioController();

        reactContext.getCurrentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (ctrl.isMyAudioMuted()) {
                    if (ctrl.canUnmuteMyAudio()) ctrl.muteMyAudio(false);
                } else {
                    ctrl.muteMyAudio(true);
                }
                promise.resolve(true);
            }
        });
    }

    @ReactMethod
    public void switchLoudSpeaker(Promise promise) {
        InMeetingAudioController ctrl = ZoomSDK.getInstance().getInMeetingService().getInMeetingAudioController();
        if (ctrl.canSwitchAudioOutput()) {
            ctrl.setLoudSpeakerStatus(!ctrl.getLoudSpeakerStatus());
        }
        promise.resolve(true);
    }

    @ReactMethod
    public void initConfigAudio(final Promise promise) {
        final InMeetingAudioController inMeetingAudioController = ZoomSDK.getInstance().getInMeetingService().getInMeetingAudioController();
        reactContext.getCurrentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean isAudioConnected = inMeetingAudioController.isAudioConnected();
                Log.d(TAG, "isAudioConnected: " + isAudioConnected);

                if (!isAudioConnected) {
                    inMeetingAudioController.muteMyAudio(false);
                    inMeetingAudioController.connectAudioWithVoIP();
                    Log.d(TAG, "initConfigAudio complet ed");
                }
                promise.resolve(true);
            }
        });
    }

    @ReactMethod
    public void checkAudioPermission(String title, String message, final Promise promise) {
        if (ContextCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getCurrentActivity(), Manifest.permission.RECORD_AUDIO)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                if (title == null || title.isEmpty()) title = "Microphone Required";
                if (message == null || message.isEmpty())
                    message = "This application need to access microphone to make a call meeting";

                new AlertDialog.Builder(getReactApplicationContext().getCurrentActivity())
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(getCurrentActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, MY_MICROPHONE_REQUEST_CODE);
                                if (promise != null) promise.resolve(true);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (promise != null) promise.resolve(false);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getCurrentActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, MY_MICROPHONE_REQUEST_CODE);
                if (promise != null) promise.resolve(true);
            }
        } else {
            if (promise != null) promise.resolve(true);
        }
    }

    /*
     * ----------------------------------
     * Chat
     * ----------------------------------
     */

    @ReactMethod
    public void sendMessage(String message, Promise promise) {
        InMeetingChatController ctrl = ZoomSDK.getInstance().getInMeetingService().getInMeetingChatController();
        ctrl.sendChatToGroup(InMeetingChatController.MobileRTCChatGroup.MobileRTCChatGroup_All, message);
        promise.resolve(true);
    }

    /*
     * ----------------------------------
     * Zoom Init Handlers
     * ----------------------------------
     */

    @Override
    public void onZoomSDKInitializeResult(int errorCode, int internalErrorCode) {
        Log.i(TAG, "onZoomSDKInitializeResult, errorCode=" + errorCode + ", internalErrorCode=" + internalErrorCode);
        if (errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
            initializePromise.reject(
                    "ERR_ZOOM_INITIALIZATION",
                    "Error: " + errorCode + ", internalErrorCode=" + internalErrorCode
            );
        } else {
            registerListener();
            initializePromise.resolve("Initialize Zoom SDK successfully.");
        }
    }

    @Override
    public void onMeetingStatusChanged(MeetingStatus meetingStatus, int errorCode, int internalErrorCode) {
        Log.i(TAG, "onMeetingStatusChanged, meetingStatus=" + meetingStatus + ", errorCode=" + errorCode + ", internalErrorCode=" + internalErrorCode);

        if (meetingPromise == null) return;

        if (meetingStatus == MeetingStatus.MEETING_STATUS_FAILED) {
            meetingPromise.reject(
                    "ERR_ZOOM_MEETING",
                    "Error: " + errorCode + ", internalErrorCode=" + internalErrorCode
            );
            meetingPromise = null;
        } else if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING) {
            meetingPromise.resolve("Connected to zoom meeting");
            meetingPromise = null;
        }
    }

    @Override
    public void onZoomAuthIdentityExpired() {
        Log.i(TAG, "onZoomAuthIdentityExpired");
        initializePromise.reject(
                "ERR_ZOOM_IDENTITY_",
                "Error: Auth Identity Expiredentication"
        );
    }

    private void registerListener() {
        Log.i(TAG, "registerListener");
        ZoomSDK zoomSDK = ZoomSDK.getInstance();
        MeetingService meetingService = zoomSDK.getMeetingService();
        InMeetingService mInMeetingService = zoomSDK.getInMeetingService();
        if (meetingService != null) {
            meetingService.addListener(this);
            mInMeetingService.addListener(this);
        }
    }

    private void unregisterListener() {
        Log.i(TAG, "unregisterListener");
        ZoomSDK zoomSDK = ZoomSDK.getInstance();
        if (zoomSDK.isInitialized()) {
            MeetingService meetingService = zoomSDK.getMeetingService();
            InMeetingService mInMeetingService = zoomSDK.getInMeetingService();
            meetingService.removeListener(this);
            mInMeetingService.removeListener(this);
        }
    }

    @Override
    public void onCatalystInstanceDestroy() {
        unregisterListener();
    }

    /*
     * ----------------------------------
     * React LifeCycle
     * ----------------------------------
     */

    @Override
    public void onHostDestroy() {
        unregisterListener();
    }

    @Override
    public void onHostPause() {
    }

    @Override
    public void onHostResume() {
    }

    /*
     * ----------------------------------
     * In meetings
     * ----------------------------------
     */

    @Override
    public void onMeetingNeedPasswordOrDisplayName(boolean b, boolean b1, InMeetingEventHandler inMeetingEventHandler) {

    }

    @Override
    public void onWebinarNeedRegister() {

    }

    @Override
    public void onJoinWebinarNeedUserNameAndEmail(InMeetingEventHandler inMeetingEventHandler) {

    }

    @Override
    public void onMeetingNeedColseOtherMeeting(InMeetingEventHandler inMeetingEventHandler) {

    }

    @Override
    public void onMeetingFail(int i, int i1) {

    }

    @Override
    public void onMeetingLeaveComplete(long reason) {
        Log.i(TAG, "onMeetingLeaveComplete: " + reason);
        isInMeeting = false;
        sendEvent("meeting_view_destroyed", ((int) reason));  // us.zoom.sdk.MeetingEndReason
    }

    @Override
    public void onMeetingUserJoin(List<Long> list) {
        Log.i(TAG, "onMeetingUserJoin: " + list.toString());
        int n = list.size() - 1;
        int userID = list.get(n).intValue();
        Log.i(TAG, "onMeetingUserJoin userID: " + userID);
        sendEvent("user_joined", userID);
    }

    @Override
    public void onMeetingUserLeave(List<Long> list) {
        Log.i(TAG, "onMeetingUserLeave: " + list.toString());
        int n = list.size() - 1;
        int userID = list.get(n).intValue();
        Log.i(TAG, "onMeetingUserLeave userID: " + userID);
        sendEvent("user_left", userID);
    }

    @Override
    public void onMeetingUserUpdated(long l) {
    }

    @Override
    public void onMeetingHostChanged(long l) {

    }

    @Override
    public void onMeetingCoHostChanged(long l) {

    }

    @Override
    public void onActiveVideoUserChanged(long l) {
    }

    @Override
    public void onActiveSpeakerVideoUserChanged(long id) {
        Log.d(TAG, "onActiveSpeakerVideoUserChanged: " + id);
    }

    @Override
    public void onSpotlightVideoChanged(boolean b) {

    }

    @Override
    public void onUserVideoStatusChanged(long l) {

    }

    @Override
    public void onUserVideoStatusChanged(long id, VideoStatus videoStatus) {
        Log.d(TAG, "onUserVideoStatusChanged: " + id + " " + videoStatus);
    }

    @Override
    public void onUserNetworkQualityChanged(long l) {

    }

    @Override
    public void onMicrophoneStatusError(InMeetingAudioController.MobileRTCMicrophoneError mobileRTCMicrophoneError) {
        Log.d(TAG, "onMicrophoneStatusError: " + mobileRTCMicrophoneError);
    }

    @Override
    public void onUserAudioStatusChanged(long id) {
    }

    @Override
    public void onUserAudioStatusChanged(long id, AudioStatus audioStatus) {
        Log.d(TAG, "onUserAudioStatusChanged: " + id + " " + audioStatus);
    }

    @Override
    public void onHostAskUnMute(long l) {

    }

    @Override
    public void onHostAskStartVideo(long l) {

    }

    @Override
    public void onUserAudioTypeChanged(long id) {
        Log.d(TAG, "onUserAudioTypeChanged: " + id);
    }

    @Override
    public void onMyAudioSourceTypeChanged(int id) {
        Log.d(TAG, "onMyAudioSourceTypeChanged: " + id);
    }

    @Override
    public void onLowOrRaiseHandStatusChanged(long l, boolean b) {

    }

    @Override
    public void onMeetingSecureKeyNotification(byte[] bytes) {

    }

    @Override
    public void onChatMessageReceived(InMeetingChatMessage inMeetingChatMessage) {

    }

    @Override
    public void onSilentModeChanged(boolean b) {

    }

    @Override
    public void onFreeMeetingReminder(boolean b, boolean b1, boolean b2) {

    }

    @Override
    public void onMeetingActiveVideo(long userId) {
        Log.i(TAG, "onMeetingActiveVideo: " + userId);

        if (!isInMeeting) {
            isInMeeting = true;
            sendEvent("meeting_ready", ((int) userId));
        }
    }

    @Override
    public void onSinkAttendeeChatPriviledgeChanged(int i) {

    }

    @Override
    public void onSinkAllowAttendeeChatNotification(int i) {

    }

    @Override
    public void onUserNameChanged(long l, String s) {

    }

    @Override
    public void onFreeMeetingNeedToUpgrade(FreeMeetingNeedUpgradeType freeMeetingNeedUpgradeType, String s) {

    }

    @Override
    public void onFreeMeetingUpgradeToGiftFreeTrialStart() {

    }

    @Override
    public void onFreeMeetingUpgradeToGiftFreeTrialStop() {

    }

    @Override
    public void onFreeMeetingUpgradeToProMeeting() {

    }

    @Override
    public void onClosedCaptionReceived(String s) {

    }

    @Override
    public void onRecordingStatus(RecordingStatus recordingStatus) {

    }
}
