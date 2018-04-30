# react-native-estimote-proximity

React Native wrapper for Estimote Proximity SDK.

You can read more about Estimote Proximity on [developer.estimote.com](https://developer.estimote.com).

## Getting started

```
$ npm install react-native-estimote-proximity --save
$ react-native link react-native-estimote-proximity
```

On **Android**, that's it. On **iOS**, you also need to add Estimote Proximity SDK to your app's Xcode project. The easiest way to do that is via CocoaPods:

1. Install CocoaPods: https://cocoapods.org

2. In the `ios` directory of your app, add a `Podfile` with this content:

   ```ruby
   platform :ios, '10.0'

   target 'NAME_OF_YOUR_APP' do
     pod 'EstimoteProximitySDK'
   end
   ```

   The `NAME_OF_YOUR_APP` is usually the same thing you used with `react-native init`.

3. Inside the `ios` directory, run:

   ```
   $ pod --repo-update install
   ```

If you want to run the bundled `example`, you also need to do steps 1 and 3 inside the `example/ios` directory.

## Usage

```javascript
import * as RNEP from 'react-native-estimote-proximity'

// generate Estimote Cloud credentials for your app at:
// https://cloud.estimote.com/#/apps/add/your-own-app
const credentials = new RNEP.CloudCredentials('APP_ID', 'APP_TOKEN')
RNEP.proximityObserver.initialize(credentials)

const zone1 = new RNEP.ProximityZone(5, 'type', 'lobby')
// will trigger when the user is within ~ 5 m of any beacon with attachment "type: lobby"
// you can add attachments to your beacons on https://cloud.estimote.com, in Beacon Settings
zone1.onEnterAction = (attachment) => {
  const venue = attachment.payload.get('venue')
  notifyFrontDesk(venue)
  console.log(`Welcome to ${venue}, somebody will be with you shortly.`)
}

RNEP.proximityObserver.startObservingZones([zone1])
```

## Contact & feedback

Let us know your thoughts and feedback on [forums.estimote.com][forums].

[forums]: https://forums.estimote.com
