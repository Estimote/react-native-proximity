// @flow
'use strict'

import { AppRegistry } from 'react-native'
import App from './App'

import * as RNEP from 'react-native-estimote-proximity'

AppRegistry.registerComponent('example', () => App)



const zone1 = new RNEP.ProximityZone(5, 'beacon', 'beetroot')
zone1.onEnterAction = (attachment) => {
  console.log('zone1 onEnter', attachment)
}
zone1.onExitAction = (attachment) => {
  console.log('zone1 onExit', attachment)
}
zone1.onChangeAction = (attachments) => {
  console.log('zone1 onChange', attachments)
}

const zone2 = new RNEP.ProximityZone(5, 'beacon', 'candy')
zone2.onEnterAction = (attachment) => {
  console.log('zone2 onEnter', attachment)
}
zone2.onExitAction = (attachment) => {
  console.log('zone2 onExit', attachment)
}
zone2.onChangeAction = (attachments) => {
  console.log('zone2 onChange', attachments)
}

const credentials = new RNEP.CloudCredentials('<#APP_ID#>', '<#APP_TOKEN#>')

RNEP.proximityObserver.initialize(credentials)
RNEP.proximityObserver.startObservingZones([zone1, zone2])
