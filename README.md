# react-native-proximity

React Native wrapper for Estimote Proximity SDK.

You can read more about Estimote Proximity on [developer.estimote.com](https://developer.estimote.com).

- [Installation](#installation)
- [Location permission](#location-permission)
- [Background support](#background-support)
- [Usage & examples](#usage--examples)
- [Contact & feedback](#contact--feedback)

## Installation

```console
$ yarn add @estimote/react-native-proximity
$ react-native link @estimote/react-native-proximity
```

On **iOS**, you also need to:

1. Add Estimote Proximity SDK and its dependencies to your app's Xcode project. The easiest way to do that is via CocoaPods:

   `1.1.` Install CocoaPods: https://cocoapods.org

   `1.2.` In the `ios` directory of your app, add a `Podfile` with this content:

      ```ruby
      platform :ios, '10.0'

      target 'NAME_OF_YOUR_APP' do
        pod 'EstimoteProximitySDK', '~> 1.0'
      end
      ```

      The `NAME_OF_YOUR_APP` is usually the same thing you used with `react-native init`.

   `1.3.` Inside the `ios` directory, run:

      ```
      $ pod --repo-update install
      ```

2. In your Xcode project's Build Settings, find and enable "Always Embed Swift in Standard Libraries".

On **Android**, you need to:

- Bump the `minSdkVersion` of your app to 18, since that's the lowest Estimote Proximity SDK supports:

  - In the `android/build.gradle` file: find `minSdkVersion = 16` and change it to `18`.
  - In older versions of react-native, the `minSdkVersion` config is in `android/app/build.gradle`.

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

## Background support

Other than obtaining the `ALWAYS` location permission, there's just a little bit more to do if you want your app to keep getting the enter/exit/change callbacks in the background.

On **iOS**, you need to:

1. Open the Xcode project.
2. Click on the top-level item in the project navigator to access the project settings.
3. Make sure your app is selected in the "TARGETS" section.
4. Go to the "Capabilities" tab.
5. Enable "Background Modes" and check "Uses Bluetooth LE accessories".

On **Android**, you need to:

1. Make sure your target API level is at least 23.

   - New versions of react-native (0.56+) use 26+, so you don't need to do anything.
   - If you're still on an older version, then in the `android/app/build.gradle` file: find `targetSdkVersion 22` and change it to `23`.

2. When initializing the Proximity Observer in your JavaScript code, make sure to pass a "notification" config. See the `example/index.js` for more.

## Usage & examples

Check `example/index.js` for a quick run-down of how to use this library.

You can run the example app with the usual `react-native run-android` and `run-ios --device`. Note that iOS and Android simulators don't support Bluetooth, so **you need to run it on a physical device**.

To run the example on iOS, you also need to install pods (dependencies) first:

1. Install CocoaPods: https://cocoapods.org
2. Inside the `example/ios` directory, run:

   ```
   $ pod --repo-update install
   ```

## Contact & feedback

Let us know your thoughts, feedback, and questions on [forums.estimote.com][forums].

[forums]: https://forums.estimote.com
