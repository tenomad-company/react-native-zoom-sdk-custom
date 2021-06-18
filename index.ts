import { NativeModule, NativeModules } from 'react-native'
import invariant from 'invariant'

const { RNZoomUs } = NativeModules

if (!RNZoomUs) console.error('RNZoomUs native module is not linked.')

const DEFAULT_USER_TYPE = 2

interface RNZoomUsInitializeCommonParams {
  domain?: string;
  iosAppGroupId?: string;
  iosScreenShareExtensionId?: string;
}
export interface RNZoomUsInitializeParams extends RNZoomUsInitializeCommonParams {
  clientKey: string;
  clientSecret: string;
}

export interface RNZoomUsSDKInitParams extends RNZoomUsInitializeCommonParams {
  jwtToken: string;
  // we don't care for the rest, for now
}

async function initialize(
  params: RNZoomUsInitializeParams | RNZoomUsSDKInitParams,
  settings: {
    // ios only
    disableShowVideoPreviewWhenJoinMeeting?: boolean
  } = {
      // more details inside: https://github.com/mieszko4/react-native-zoom-us/issues/28
      disableShowVideoPreviewWhenJoinMeeting: true,
    },
) {
  invariant(typeof params === 'object',
    'ZoomUs.initialize expects object param. Consider to check migration docs. ' +
    'Check Link: https://github.com/mieszko4/react-native-zoom-us/blob/master/docs/UPGRADING.md',
  )

  if ('jwtToken' in params) {
    invariant(params.jwtToken, 'ZoomUs.initialize requires jwtToken')
  } else {
    invariant(params.clientKey, 'ZoomUs.initialize requires clientKey')
    invariant(params.clientSecret, 'ZoomUs.initialize requires clientSecret')
  }

  if (!params.domain) params.domain = 'zoom.us'

  return RNZoomUs.initialize(params, settings)
}

export interface RNZoomUsJoinMeetingParams {
  userName: string
  meetingNumber: string | number
  password?: string
  participantID?: string
  autoConnectAudio?: boolean
  noAudio?: boolean
  noVideo?: boolean

  // android only fields:
  noInvite?: boolean
  noBottomToolbar?: boolean
  noPhoneDialIn?: boolean
  noPhoneDialOut?: boolean
  noMeetingEndMessage?: boolean
  noMeetingErrorMessage?: boolean
  noShare?: boolean
  noTitlebar?: boolean

  noButtonLeave?: boolean
  noButtonMore?: boolean
  noButtonParticipants?: boolean
  noButtonShare?: boolean
  noTextMeetingId?: boolean
  noTextPassword?: boolean

  // ios only fields:
  zoomAccessToken?: string
  webinarToken?: string
}
async function joinMeeting(params: RNZoomUsJoinMeetingParams) {
  let { meetingNumber, noAudio = false, noVideo = false, autoConnectAudio = false } = params
  invariant(meetingNumber, 'ZoomUs.joinMeeting requires meetingNumber')
  if (typeof meetingNumber !== 'string') meetingNumber = meetingNumber.toString()

  // without noAudio, noVideo fields SDK can stack on joining meeting room for release build
  return RNZoomUs.joinMeeting({
    ...params,
    meetingNumber,
    noAudio: !!noAudio, // required
    noVideo: !!noVideo, // required
    autoConnectAudio,   // required
  })
}

async function joinMeetingWithPassword(...params) {
  console.warn("ZoomUs.joinMeetingWithPassword is deprecated. Use joinMeeting({ password: 'xxx', ... })")
  return RNZoomUs.joinMeetingWithPassword(...params)
}

export interface RNZoomUsStartMeetingParams {
  userName: string
  meetingNumber: string | number
  userId: string
  userType?: number // looks like can be different for IOS and Android
  zoomAccessToken: string

  // android only fields:
  noInvite?: boolean

  noButtonLeave?: boolean
  noButtonMore?: boolean
  noButtonParticipants?: boolean
  noButtonShare?: boolean
  noTextMeetingId?: boolean
  noTextPassword?: boolean
}
async function startMeeting(params: RNZoomUsStartMeetingParams) {
  let { userType = DEFAULT_USER_TYPE, meetingNumber } = params

  invariant(meetingNumber, 'ZoomUs.startMeeting requires meetingNumber')
  if (typeof meetingNumber !== 'string') meetingNumber = meetingNumber.toString()

  return RNZoomUs.startMeeting({ userType, ...params, meetingNumber })
}

async function leaveMeeting() {
  return RNZoomUs.leaveMeeting()
}

async function connectAudio() {
  return RNZoomUs.connectAudio()
}

export const ZoomEmitter = RNZoomUs as NativeModule;

export default {
  initialize,
  joinMeeting,
  joinMeetingWithPassword,
  startMeeting,
  leaveMeeting,
  connectAudio,
}
