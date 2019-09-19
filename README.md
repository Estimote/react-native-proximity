# react-native-proximity

React Native wrapper for Estimote Proximity SDK.

You can read more about Estimote Proximity on [developer.estimote.com](https://developer.estimote.com).

- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Location permission](#location-permission)
- [Bluetooth permission](#bluetooth-permission)
- [Background support](#background-support)
- [Usage & examples](#usage--examples)
  - ["Already observing"](#already-observing)
- [Contact & feedback](#contact--feedback)

## Prerequisites

- React Native 0.60

  - if you're on React Native < 0.60, use version 0.5.0 of this plugin:

    ```console
    $ yarn add @estimote/react-native-proximity@0.5.0
    $ # or, if you use npm:
    $ npm install @estimote/react-native-proximity@0.5.0 --save --save-exact

    $ react-native link @estimote/react-native-proximity
    ```

    then, follow the README from the 0.5.0 version:

    https://github.com/Estimote/react-native-proximity/blob/v0.5.0/README.md

- "ejected" React Native project
  - see [Getting Started](https://facebook.github.io/react-native/docs/getting-started) and the "React Native CLI Quickstart" tab for everything you'll need to build native code

- while normally you only need Xcode 9.4 or later, this plugin instead requires Xcode 10.2 or later

## Installation

```console
$ yarn add @estimote/react-native-proximity
$ # or, if you use npm:
$ npm install @estimote/react-native-proximity --save
```

On **iOS**, you'll also need to:

- edit `ios/Podfile` and change `platform :ios, '9.0'` to `platform :ios, '10.0'`
- run `pod --repo-update install` inside the `ios` directory
- in your Xcode project's Build Settings, find and set "Always Embed Swift in Standard Libraries" to "YES"
  - exception: if you only target iOS 12.2 or later, you can leave this at "NO" (that's because starting with iOS 12.2, Swift standard libraries are included in the system itself, and no longer need to be bundled with the app)

On **Android**, you'll also need to:

- edit `android/build.gradle` and change `minSdkVersion = 16` to `minSdkVersion = 18`

## Location permission

Since detecting proximity to Bluetooth beacons gives you some idea of the user's location, both iOS and Android require that the user agrees to that, even though we're not actually using GPS or the likes. Location services must also be globally enabled in the system.

```javascript
import * as RNEP from '@estimote/react-native-proximity'

// this will trigger a popup with "allow this app to access your location?"
RNEP.locationPermission.request()
  .then(permission => { // this is the user's decision
    // permission can be equal to:
    // * RNEP.locationPermission.DENIED - proximity detection won't work
    // * RNEP.locationPermission.WHEN_IN_USE - only when the app is active
    // * RNEP.locationPermission.ALWAYS - even when the app is not active
  })
```

On **iOS**, you also need to make sure you have the following entries in your app's Info.plist file: (you'll usually find it at `ios/APP_NAME/Info.plist`)

  ```xml
  <key>NSLocationWhenInUseUsageDescription</key>
  <string>We'll show you things near you in the app.</string>
  <key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
  <string>We'll show you things near you in the app. With "always" access, we'll
          also send you notifications when you're near something interesting.</string>
  ```

These are the messages that iOS will show as part of the location permission popup.

On **Android**, the `targetSdkVersion` needs to be at least 23. New versions of react-native (0.56+) use 26+, so you don't need to do anything. If you're still on an older version, then in the `android/app/build.gradle` file: find `targetSdkVersion 22` and change it to `23`.

## Bluetooth permission

On **iOS**, you also need to add the following entry in your app's Info.plist file: (you'll usually find it at `ios/APP_NAME/Info.plist`)

```xml
<key>NSBluetoothAlwaysUsageDescription</key>
<string>We use Bluetooth beacons for better location accuracy indoors.</string>
```

This message will show as part of the Bluetooth permission popup introduced in iOS 13.

## Background support

Other than obtaining the `ALWAYS` location permission, there's just a little bit more to do if you want your app to keep getting the enter/exit/change callbacks in the background.

On **iOS**, you need to:

1. Open the Xcode project.
2. Click on the top-level item in the project navigator to access the project settings.
3. Make sure your app is selected in the "TARGETS" section.
4. Go to the "Capabilities" tab.
5. Enable "Background Modes" and check "Uses Bluetooth LE accessories".

**Note**: Background Modes undergo extra scrutiny during App Store review. If you're submitting your app to the App Store, we recommend proactively explaining in the notes why/how your app uses Bluetooth in the background.

On **Android**, you need to:

1. Make sure your target API level is at least 23.

   - New versions of react-native (0.56+) use 26+, so you don't need to do anything.
   - If you're still on an older version, then in the `android/app/build.gradle` file: find `targetSdkVersion 22` and change it to `23`.

2. When initializing the Proximity Observer in your JavaScript code, make sure to pass a "notification" config. See the `example/proximityObserver.js` for more.

3. Add the following line to android/main/src/AndroidManifest.xml:

   ```
   <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
   ```

## Usage & examples

Check [`example/proximityObserver.js`](https://github.com/Estimote/react-native-proximity/blob/master/example/proximityObserver.js) for a quick run-down of how to use this library.

To set up the included example app for running:

1. Run `npm install` or `yarn install` inside `example`.
2. Run `pod --repo-update install` inside `example/ios`.

Then, you can do the usual `react-native run-android` and `run-ios --device`. Note that iOS and Android simulators don't support Bluetooth, so **you need to run it on a physical device**.

### "Already observing"

Some consideration is needed as to where to put the code that starts the Proximity Observer, and to properly stopping the Observer when necessary, or you might see this warning:

> 'startObservingZones' was called while already observing.

Generally speaking, there are two options, depending on your use case:

1. **Foreground**, with proximity events tied to a particular component. For example, maybe you have a map component, where you want to mark the user's approximate location, and re-render the map based on the proximity events you're getting. In this case, it's usually best to:

   - set up your zones in the `constructor`
   - start the Proximity Observer in the `constructor` or in `componentDidMount`
   - stop the Proximity Observer in the `componentWillUnmount`

2. **Background**, with proximity events tied to the app itself, and not to any particular component—not even the `App` component! This is important, because given proper configuration (see "Location permission" and "Background support" above), proximity events usually work even if the user swipes the app away at the app switcher. When this happens, the UI and all the components get removed, but the app itself can still process proximity events—for example, to show local notifications.

   In this case, you **must**\* start the Proximity Observer somewhere down the chain which starts in `index.js`, just like in the example app. There's usually no need to stop the Observer, since you want it running at all times.

   > \* well, technically, there are other options that'll work equally well; but when in doubt, use `index.js`

These are just two general recommendations, but every app is different, so if you're not sure how to handle your particular use case, drop a post at [forums.estimote.com][forums], and we'll be happy to help.

> If you're using redux for your state management, a good idea might be to start the Proximity Observer where you create your redux store, and have the proximity events dispatch appropriate actions. The rest of your app can then subscribe to state/store changes as needed, and benefit from the proximity events this way. What a beautiful separation of concerns!

## Contact & feedback

Let us know your thoughts, feedback, and questions on [forums.estimote.com][forums].

[forums]: https://forums.estimote.com
