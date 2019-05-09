# react-native-proximity

React Native wrapper for Estimote Proximity SDK.

You can read more about Estimote Proximity on [developer.estimote.com](https://developer.estimote.com).

- [Installation](#installation)
- [Location permission](#location-permission)
- [Background support](#background-support)
- [Usage & examples](#usage--examples)
  - ["Already observing"](#already-observing)
- [Contact & feedback](#contact--feedback)

## Installation

```console
$ yarn add @estimote/react-native-proximity
$ # or if you use npm:
$ npm install @estimote/react-native-proximity --save

$ react-native link @estimote/react-native-proximity
```

On **iOS**, you also need to:

1. Add Estimote Proximity SDK and its dependencies to your app's Xcode project. The easiest way to do that is via CocoaPods:

   1.1. Install CocoaPods: https://cocoapods.org

   1.2. In the `ios` directory of your app, add a `Podfile` with this content:

      ```ruby
      platform :ios, '10.0'

      target 'NAME_OF_YOUR_APP' do
        pod 'EstimoteProximitySDK', '~> 1.0'
      end
      ```

      The `NAME_OF_YOUR_APP` is usually the same thing you used with `react-native init`.

      > The latest version of the Estimote Proximity SDK for iOS requires Xcode 10.2 or later. If you're on an earlier version of Xcode 10, use Proximity SDK 1.2.1. If you're using Xcode < 10, use Proximity SDK 1.1.0. If you're using Xcode < 9.3 ... you should consider updating (-; or drop by [forums.estimote.com][forums] for help.
      >
      > You set the version of the iOS Proximity SDK to use in the Podfile: change `~> 1.0` to `1.2.1` (or another version you want to use).

   1.3. Inside the `ios` directory, run:

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

**Note**: Background Modes undergo extra scrutiny during App Store review. If you're submitting your app to the App Store, we recommend proactively explaining in the notes why/how your app uses Bluetooth in the background.

On **Android**, you need to:

1. Make sure your target API level is at least 23.

   - New versions of react-native (0.56+) use 26+, so you don't need to do anything.
   - If you're still on an older version, then in the `android/app/build.gradle` file: find `targetSdkVersion 22` and change it to `23`.

2. When initializing the Proximity Observer in your JavaScript code, make sure to pass a "notification" config. See the `example/proximityObserver.js` for more.

## Usage & examples

Check [`example/proximityObserver.js`](https://github.com/Estimote/react-native-proximity/blob/master/example/proximityObserver.js) for a quick run-down of how to use this library.

You can run the example app with the usual `react-native run-android` and `run-ios --device`. Note that iOS and Android simulators don't support Bluetooth, so **you need to run it on a physical device**.

Oh, and remember to run `yarn install` inside the `example` first.

> `npm` won't work out-of-the-box for the `example`, since `npm` will create a local symlink to the proximity plugin, and React Native's bundler doesn't work with symlinks. Use `yarn`, or change the `package.json` by replacing `"file:../"` with the latest version of the proximity plugin.

To run the example on iOS, you also need to install Estimote Proximity SDK and its dependencies:

1. Install CocoaPods: https://cocoapods.org
2. Inside the `example/ios` directory, run:

   ```
   $ pod --repo-update install
   ```

> The latest version of the Estimote Proximity SDK for iOS requires Xcode 10.2 or later. If you're on an earlier version of Xcode 10, use Proximity SDK 1.2.1. If you're using Xcode < 10, use Proximity SDK 1.1.0. If you're using Xcode < 9.3 ... you should consider updating (-; or drop by [forums.estimote.com][forums] for help.
>
> You set the version of the iOS Proximity SDK to use in the Podfile: change `~> 1.0` to `1.2.1` (or another version you want to use).

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
