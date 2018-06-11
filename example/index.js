// @flow
'use strict'

import { AppRegistry } from 'react-native'
import App from './App'

import * as RNEP from '@estimote/react-native-proximity'

AppRegistry.registerComponent('example', () => App)



// will trigger when the user is within ~ 5 m of any beacon with attachment "type: lobby"
// you can add attachments to your beacons on https://cloud.estimote.com, in Beacon Settings
const zone1 = new RNEP.ProximityZone(5, 'type', 'lobby')
zone1.onEnterAction = (attachment) => {
  console.log('zone1 onEnter')

  // each `attachment` has a:
  // - `deviceIdentifier`
  console.log('zone1 onEnter deviceIdentifier', attachment.deviceIdentifier)

  // - `payload`, which is a JavaScript [Map][1] of all the key-value pairs
  //              assigned to the beacon in Estimote Cloud
  // [1]: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map
  console.log('zone1 "beacon" =>', attachment.payload.get('beacon')) // get individual values
  console.log('zone1 all key-values', Array.from(attachment.payload)) // show all key-value pairs
}
zone1.onExitAction = (attachment) => {
  console.log('zone1 onExit')
}
zone1.onChangeAction = (attachments) => {
  console.log('zone1 onChange')
}

const zone2 = new RNEP.ProximityZone(5, 'type', 'conf_room')
zone2.onEnterAction = (attachment) => {
  console.log('zone2 onEnter', attachment)
}
zone2.onExitAction = (attachment) => {
  console.log('zone2 onExit', attachment)
}
zone2.onChangeAction = (attachments) => {
  console.log('zone2 onChange', attachments)
}

RNEP.locationPermission.request()
  .then(permission => {
    console.log(`location permission: ${permission}`)
    if (permission !== RNEP.locationPermission.DENIED) {
      // generate Estimote Cloud credentials for your app at:
      // https://cloud.estimote.com/#/apps/add/your-own-app
      const credentials = new RNEP.CloudCredentials('<#APP ID#>', '<#APP TOKEN#>')

      const config = {
        // modern versions of Android require a notification informing the user that the app is active in the background
        // if you don't need proximity observation to work in the background, you can omit the entire `notification` config
        notification: {
          title: 'Exploration mode is on',
          text: "We'll notify you when you're next to something interesting.",
          //icon: 'my_drawable' // if omitted, will default to the app icon (i.e., mipmap/ic_launcher)
        }
      }

      RNEP.proximityObserver.initialize(credentials, config)
      RNEP.proximityObserver.startObservingZones([zone1, zone2])
    }
  }, error => {
    console.error('Error when trying to obtain location permission', error)
  })
