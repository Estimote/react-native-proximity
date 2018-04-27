// @flow
'use strict'

import { NativeModules, NativeEventEmitter } from 'react-native'

const { RNEstimoteProximity } = NativeModules
const RNEstimoteProximityEmitter = new NativeEventEmitter(RNEstimoteProximity);

export class DeviceAttachment {

  deviceIdentifier: string
  payload: Map<string, string>

  constructor(deviceIdentifier: string, payload: Map<string, string>) {
    this.deviceIdentifier = deviceIdentifier
    this.payload = payload
  }

  static fromJSON(json: any): DeviceAttachment {
    return new DeviceAttachment(
      json.deviceIdentifier,
      // $FlowFixMe
      new Map(Object.entries(json.payload)));
  }
}

export class ProximityZone {

  range: number
  attachmentKey: string
  attachmentValue: string

  onEnterAction: (attachment: DeviceAttachment) => void
  onExitAction: (attachment: DeviceAttachment) => void
  onChangeAction: (attachments: Array<DeviceAttachment>) => void

  _id: string

  constructor(range: number, attachmentKey: string, attachmentValue: string) {
    this.range = range
    this.attachmentKey = attachmentKey
    this.attachmentValue = attachmentValue

    this._id = Math.random().toString(36).substring(2)
  }
}

export class CloudCredentials {

  appId: string
  appToken: string

  constructor(appId: string, appToken: string) {
    this.appId = appId
    this.appToken = appToken
  }
}

export const proximityObserver = { // singleton object

  initialize(credentials: CloudCredentials) {
    RNEstimoteProximity.initialize({
      appId: credentials.appId,
      appToken: credentials.appToken
    })
  },

  startObservingZones(zones: Array<ProximityZone>) {
    const zonesById: Map<string, ProximityZone> = zones.reduce((map, z) => {
      return map.set(z._id, z)
    }, new Map())

    this.onEnterSubscription = RNEstimoteProximityEmitter.addListener(
      `Enter`, event => {
        // $FlowFixMe
        const onEnterAction = zonesById.get(event.zoneId).onEnterAction
        if (typeof onEnterAction === 'function') {
          const attachment = DeviceAttachment.fromJSON(event.attachment)
          onEnterAction(attachment)
        }
    })

    this.onExitSubscription = RNEstimoteProximityEmitter.addListener(
      `Exit`, event => {
        // $FlowFixMe
        const onExitAction = zonesById.get(event.zoneId).onExitAction
        if (typeof onExitAction === 'function') {
          const attachment = DeviceAttachment.fromJSON(event.attachment)
          onExitAction(attachment)
        }
    })

    this.onChangeSubscription = RNEstimoteProximityEmitter.addListener(
      `Change`, event => {
        // $FlowFixMe
        const onChangeAction = zonesById.get(event.zoneId).onChangeAction
        if (typeof onChangeAction === 'function') {
          const attachments = event.attachments.map(
            attachment => DeviceAttachment.fromJSON(attachment))
          onChangeAction(attachments)
        }
    })

    const zonesJSON = zones.map(z => ({
      _id: z._id,
      range: z.range,
      attachmentKey: z.attachmentKey,
      attachmentValue: z.attachmentValue
    }))

    RNEstimoteProximity.startObservingZones(zonesJSON)
  },

  stopObservingZones() {
    RNEstimoteProximity.stopObservingZones()

    this.onEnterSubscription.remove()
    this.onExitSubscription.remove()
    this.onChangeSubscription.remove()
  }
}
