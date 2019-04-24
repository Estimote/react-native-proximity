// @flow
"use strict";

import {
  NativeModules,
  NativeEventEmitter,
  Platform,
  PermissionsAndroid
} from "react-native";

const { RNEstimoteProximity } = NativeModules;
const RNEstimoteProximityEmitter = new NativeEventEmitter(RNEstimoteProximity);

export class ProximityContext {
  tag: string;
  attachments: { [string]: string };
  deviceIdentifier: string;

  static fromJSON(json: any): ProximityContext {
    const context = new ProximityContext();
    context.tag = json["tag"];
    context.attachments = json["attachments"];
    context.deviceIdentifier = json["deviceIdentifier"];
    return context;
  }
}

export class ProximityZone {
  range: number;
  tag: string;

  onEnterAction: (context: ProximityContext) => void;
  onExitAction: (context: ProximityContext) => void;
  onChangeAction: (contexts: Array<ProximityContext>) => void;

  _id: string;

  constructor(range: number, tag: string) {
    this.range = range;
    this.tag = tag;

    this._id = Math.random()
      .toString(36)
      .substring(2);
  }
}

export class CloudCredentials {
  appId: string;
  appToken: string;

  constructor(appId: string, appToken: string) {
    this.appId = appId;
    this.appToken = appToken;
  }
}

type ProximityObserverConfig = {
  notification?: {
    title?: string,
    text?: string,
    icon?: string
  }
};

// singleton object
export const proximityObserver = {

  isObserving: false,

  initialize(credentials: CloudCredentials, config: ProximityObserverConfig) {
    RNEstimoteProximity.initialize(
      Object.assign({}, config, {
        appId: credentials.appId,
        appToken: credentials.appToken
      })
    );
  },

  startObservingZones(zones: Array<ProximityZone>) {
    if (this.isObserving) {
      console.warn("'startObservingZones' was called while already observing. I'll stop the previous observation for you, but make sure that you're properly managing the lifecycle of the observer. See also: https://github.com/Estimote/react-native-proximity#already-observing");
      this.stopObservingZones();
    }
    this.isObserving = true;

    const zonesById: Map<string, ProximityZone> = zones.reduce((map, z) => {
      return map.set(z._id, z);
    }, new Map());

    this.onEnterSubscription = RNEstimoteProximityEmitter.addListener(
      `Enter`,
      event => {
        const zone = zonesById.get(event.zoneId);
        if (zone === undefined) {
          return;
        }
        const onEnterAction = zone.onEnterAction;
        if (typeof onEnterAction === "function") {
          const context = ProximityContext.fromJSON(event.context);
          onEnterAction(context);
        }
      }
    );

    this.onExitSubscription = RNEstimoteProximityEmitter.addListener(
      `Exit`,
      event => {
        const zone = zonesById.get(event.zoneId);
        if (zone === undefined) {
          return;
        }
        const onExitAction = zone.onExitAction;
        if (typeof onExitAction === "function") {
          const context = ProximityContext.fromJSON(event.context);
          onExitAction(context);
        }
      }
    );

    this.onChangeSubscription = RNEstimoteProximityEmitter.addListener(
      `Change`,
      event => {
        const zone = zonesById.get(event.zoneId);
        if (zone === undefined) {
          console.error('Got a proximity event for an unknown zone. This is most likely a bug, please report to https://github.com/Estimote/react-native-proximity/issues');
          return;
        }
        const onChangeAction = zone.onChangeAction;
        if (typeof onChangeAction === "function") {
          const contexts = event.contexts.map(context =>
            ProximityContext.fromJSON(context)
          );
          onChangeAction(contexts);
        }
      }
    );

    const zonesJSON = zones.map(z => ({
      _id: z._id,
      range: z.range,
      tag: z.tag
    }));

    RNEstimoteProximity.startObservingZones(zonesJSON);
    this.isObserving = true;
  },

  stopObservingZones() {
    RNEstimoteProximity.stopObservingZones();
    this.isObserving = false;

    this.onEnterSubscription.remove();
    this.onExitSubscription.remove();
    this.onChangeSubscription.remove();
  }
};

type PermissionStatus = "always" | "when_in_use" | "denied";

export const locationPermission = {
  ALWAYS: "always",
  WHEN_IN_USE: "when_in_use",
  DENIED: "denied",

  request: async (): Promise<PermissionStatus> => {
    if (Platform.OS === "ios") {
      const result = await RNEstimoteProximity.requestLocationPermission();
      return result;
    } else if (Platform.OS === "android") {
      const result = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION
      );
      return result === "granted" ? "always" : "denied";
    }
    throw "Unsupported platform";
  }
};
