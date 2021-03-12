
import { NativeEventEmitter, NativeModules, requireNativeComponent } from 'react-native';
const { RNZoomUs } = NativeModules;
const RNZoomEmitter = new NativeEventEmitter(RNZoomUs);

export const ZoomMobileRCTView = requireNativeComponent('RNZoomMobileRCTView');
export const ZoomMobileRCTActiveView = requireNativeComponent('RNZoomMobileRCTActiveView');

export const ZoomEvent = {
    USER_JOIN: "user_joined", 
    USER_LEAVE: "user_leaved", 
    USER_WAITING: "user_waiting",
    MEETING_READY:"meeting_ready",
    MEETING_JOIN_CONFIRMED:"meeting_join_confirmed",
    MEETING_VIEW_INITIALIZED:"meeting_view_initialized",
    MEETING_VIEW_DESTROYED:"meeting_view_destroyed",
    MEETING_CHAT_RECEIVED:"meeting_chat_received",
}

export const ZoomEndReason = {
    END_BY_SELF: 0,
    KICK_BY_HOST: 1,
    END_BY_HOST: 2,
    END_FOR_JBHTIMEOUT: 3,
    END_FOR_FREEMEET_TIMEOUT: 4,
    END_FOR_NOATEENDEE: 5,
    END_BY_HOST_START_ANOTHERMEETING: 6,
    END_BY_SDK_CONNECTION_BROKEN: 7,
}

class ZoomSDK {
    // ----- CALL ACTIONS ----
    initialize =  (params, settings) => RNZoomUs.initialize(params, settings);
    startMeeting =  (params) => RNZoomUs.startMeeting(params);
    joinMeeting = (params) => RNZoomUs.joinMeeting(params);
    leaveMeeting =  () => RNZoomUs.leaveMeeting();
    endMeeting =  () => RNZoomUs.endMeeting();

    // ----- STREAMS INFORMATION ----
    getCurrentID = () => RNZoomUs.getCurrentID();
    getUsers = () => RNZoomUs.getUsers();

    // ----- VIDEO ACTIONS ----
    pinVideo = id => RNZoomUs.pinVideo(id);
    switchVideoMute = () => RNZoomUs.switchVideoMute();
    switchCamera = () => RNZoomUs.switchCamera();
    // Android Only
    checkVideoRotation = () => RNZoomUs.checkVideoRotation();

    // ----- AUDIO ACTIONS ----
    disconnectAudio = () => RNZoomUs.disconnectAudio();
    switchAudioMute = () => RNZoomUs.switchAudioMute();
    // Android Only
    checkAudioPermission = (title, message) => RNZoomUs.checkAudioPermission(title, message);
    initConfigAudio = () => RNZoomUs.initConfigAudio();
    switchLoudSpeaker = () => RNZoomUs.switchLoudSpeaker();
    getLoudSpeakerStatus = () => RNZoomUs.getLoudSpeakerStatus();
    isAudioConnected = () => RNZoomUs.isAudioConnected();
    isMyAudioMuted = () => RNZoomUs.isMyAudioMuted();
    canUnmuteMyAudio = () => RNZoomUs.canUnmuteMyAudio();
    canSwitchAudioOutput = () => RNZoomUs.canSwitchAudioOutput();
    connectAudioWithVoIP = () => RNZoomUs.connectAudioWithVoIP();
    disconnectAudio = () => RNZoomUs.disconnectAudio();
    // iOS Only
    connectMyAudio = (on) => RNZoomUs.connectMyAudio(on);
    switchAudioSource = () => RNZoomUs.switchAudioSource();

    // ----- EVENTS ----
    addEvent = (event, callback) => RNZoomEmitter.addListener(event, callback);
    removeEvent = (event, callback) => RNZoomEmitter.removeListener(event, callback)
    removeAllEvent = () => RNZoomEmitter.removeAllListeners();

    onUserJoined = callback => RNZoomEmitter.addListener(ZoomEvent.USER_JOIN, callback);
    onUserLeft = callback => RNZoomEmitter.addListener(ZoomEvent.USER_LEAVE, callback);
    onUserWaiting = callback => RNZoomEmitter.addListener(ZoomEvent.USER_WAITING, callback);
    onMeetingReady = callback => RNZoomEmitter.addListener(ZoomEvent.MEETING_READY, callback);
    onJoinConfirmed = callback => RNZoomEmitter.addListener(ZoomEvent.MEETING_JOIN_CONFIRMED, callback);
    onMeetingViewInitialized = callback => RNZoomEmitter.addListener(ZoomEvent.MEETING_VIEW_INITIALIZED, callback);
    onMeetingViewDestroyed = callback => RNZoomEmitter.addListener(ZoomEvent.MEETING_VIEW_DESTROYED, callback);
    onMeetingChatReceived = callback => RNZoomEmitter.addListener(ZoomEvent.MEETING_CHAT_RECEIVED, callback);

    offUserJoined = callback => RNZoomEmitter.removeListener(ZoomEvent.USER_JOIN, callback);
    offUserLeft = callback => RNZoomEmitter.removeListener(ZoomEvent.USER_LEAVE, callback);
    offUserWaiting = callback => RNZoomEmitter.removeListener(ZoomEvent.USER_WAITING, callback);
    offMeetingReady = callback => RNZoomEmitter.removeListener(ZoomEvent.MEETING_READY, callback);
    offJoinConfirmed = callback => RNZoomEmitter.removeListener(ZoomEvent.MEETING_JOIN_CONFIRMED, callback);
    offMeetingViewInitialized = callback => RNZoomEmitter.removeListener(ZoomEvent.MEETING_VIEW_INITIALIZED, callback);
    offMeetingViewDestroyed = callback => RNZoomEmitter.removeListener(ZoomEvent.MEETING_VIEW_DESTROYED, callback);
    offMeetingChatReceived = callback => RNZoomEmitter.removeListener(ZoomEvent.MEETING_CHAT_RECEIVED, callback);
}

export default new ZoomSDK();