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
  tag: String;
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
  initialize(credentials: CloudCredentials, config: ProximityObserverConfig) {
    RNEstimoteProximity.initialize(
      Object.assign({}, config, {
        appId: credentials.appId,
        appToken: credentials.appToken
      })
    );
  },

  startObservingZones(zones: Array<ProximityZone>) {
    const zonesById: Map<string, ProximityZone> = zones.reduce((map, z) => {
      return map.set(z._id, z);
    }, new Map());

    this.onEnterSubscription = RNEstimoteProximityEmitter.addListener(
      `Enter`,
      event => {
        // $FlowFixMe
        const onEnterAction = zonesById.get(event.zoneId).onEnterAction;
        if (typeof onEnterAction === "function") {
          const context = ProximityContext.fromJSON(event.context);
          onEnterAction(context);
        }
      }
    );

    this.onExitSubscription = RNEstimoteProximityEmitter.addListener(
      `Exit`,
      event => {
        // $FlowFixMe
        const onExitAction = zonesById.get(event.zoneId).onExitAction;
        if (typeof onExitAction === "function") {
          const context = ProximityContext.fromJSON(event.context);
          onExitAction(context);
        }
      }
    );

    this.onChangeSubscription = RNEstimoteProximityEmitter.addListener(
      `Change`,
      event => {
        // $FlowFixMe
        const onChangeAction = zonesById.get(event.zoneId).onChangeAction;
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
  },

  stopObservingZones() {
    RNEstimoteProximity.stopObservingZones();

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
