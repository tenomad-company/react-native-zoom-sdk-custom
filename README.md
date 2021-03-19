
# react-native-zoom-sdk-custom
### Forked by TienTruongVan

This is a minimum bridge of https://github.com/zoom/zoom-sdk-android and https://github.com/zoom/zoom-sdk-ios

Tested on XCode 9.4.1 and node 10.14.1.

Pull requests are welcome.

## Getting started

`$ npm install react-native-zoom-sdk-custom`

### Mostly automatic installation

`$ react-native link react-native-zoom-sdk-custom`

#### Extra steps for Android

Since Zoom SDK `*.aar` libraries are not globally distributed
it is also required to manually go to your project's `android/build.gradle` and under `allprojects.repositories` add the following:
```gradle
allprojects {
    repositories {
        flatDir {
            dirs "$rootDir/../node_modules/react-native-zoom-sdk-custom/android/libs"
        }
        ...
    }
    ...
}
```

If you have problem with multiDex go to your project's `android/app/build.gradle` and under `android.defaultSettings` add the following:
```gradle
android {
    defaultConfig {
        multiDexEnabled true
        ...
    }
    ...
}
```

Note: In `android/app/build.gradle` I tried to set up `compile project(':react-native-zoom-sdk-custom')` with `transitive=false`
and it compiled well, but the app then crashes after running with initialize/meeting listener.
So the above solution seems to be the best for now.

#### Extra steps for iOS

1. In XCode, in your main project go to `Info` tab and add the following keys with appropriate description:
* `NSCameraUsageDescription`
* `NSMicrophoneUsageDescription`
* `NSPhotoLibraryUsageDescription`

2. In Podfile, add:
```
  pod 'ZoomSDK', :git => 'https://github.com/tenomad-company/zoom-sdk-ios.git'
```
3. Because this package includes Zoom SDK that works for both simulator and real device, when releasing to app store you may encounter problem with unsupported architecure. Please follow this answer to add script in `Build Phases` that filters out unsupported architectures: https://stackoverflow.com/questions/30547283/submit-to-app-store-issues-unsupported-architecture-x86. You may want to modify the script to be more specific, i.e. replace `'*.framework'` with `'MobileRTC.framework'`.

### Manual installation

#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-zoom-sdk-custom` and add `RNZoomUs.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNZoomUs.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<
5. Follow [Mostly automatic installation-> Extra steps for iOS](#extra-steps-for-ios)

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import ch.milosz.reactnative.RNZoomUsPackage;` to the imports at the top of the file
  - Add `new RNZoomUsPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-zoom-sdk-custom'
  	project(':react-native-zoom-sdk-custom').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-zoom-sdk-custom/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-zoom-sdk-custom')
  	```
4. Follow [Mostly automatic installation-> Extra steps for Android](#extra-steps-for-android)


## Usage
```ts
import ZoomUs from 'react-native-zoom-sdk-custom';

await ZoomUs.initialize(
  config.zoom.appKey,
  config.zoom.appSecret,
  config.zoom.domain
);

// Start Meeting
await ZoomUs.startMeeting(
  displayName,
  meetingNo,
  userId, // can be 'null'?
  userType, // for pro user use 2
  zoomAccessToken, // zak token
  zoomToken // can be 'null'?

  // NOTE: userId, userType, zoomToken should be taken from user hosting this meeting (not sure why it is required)
  // But it works with putting only zoomAccessToken
);

// OR Join Meeting
await ZoomUs.joinMeeting(
  displayName,
  meetingNo
);

// OR Join Meeting with password
await ZoomUs.joinMeetingWithPassword(
  displayName,
  meetingNo,
  'Enter password here'
);
```
See demo usage of this library: https://github.com/mieszko4/react-native-zoom-sdk-custom-test
