// @flow
'use strict'

import { AppRegistry } from 'react-native'
import App from './App'

import * as RNEP from 'react-native-estimote-proximity'

AppRegistry.registerComponent('example', () => App)



// will trigger when the user is within ~ 5 m of any beacon with attachment "type: lobby"
// you can add attachments to your beacons on https://cloud.estimote.com, in Beacon Settings
const zone1 = new RNEP.ProximityZone(5, 'type', 'lobby')
zone1.onEnterAction = (attachment) => {
  console.log('zone1 onEnter', attachment)
}
zone1.onExitAction = (attachment) => {
  console.log('zone1 onExit', attachment)
}
zone1.onChangeAction = (attachments) => {
  console.log('zone1 onChange', attachments)
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
      const credentials = new RNEP.CloudCredentials('<#APP_ID#>', '<#APP_TOKEN#>')
      RNEP.proximityObserver.initialize(credentials)
      RNEP.proximityObserver.startObservingZones([zone1, zone2])
    }
  }, error => {
    console.error('Error when trying to obtain location permission', error)
  })
