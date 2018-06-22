// @flow
"use strict";

import { AppRegistry } from "react-native";
import App from "./App";

import * as RNEP from "@estimote/react-native-proximity";

AppRegistry.registerComponent("example", () => App);

// will trigger when the user is within ~ 5 m of any beacon with tag "lobby"
// you can add attachments to your beacons on https://cloud.estimote.com, in Beacon Settings
const zone1 = new RNEP.ProximityZone(5, "lobby");
zone1.onEnterAction = context => {
  console.log("zone1 onEnter", context);
};
zone1.onExitAction = context => {
  console.log("zone1 onExit", context);
};
zone1.onChangeAction = contexts => {
  console.log("zone1 onChange", contexts);
};

const zone2 = new RNEP.ProximityZone(5, "conf-room");
zone2.onEnterAction = context => {
  console.log("zone2 onEnter", context);
};
zone2.onExitAction = context => {
  console.log("zone2 onExit", context);
};
zone2.onChangeAction = contexts => {
  console.log("zone2 onChange", contexts);
};

RNEP.locationPermission.request().then(
  permission => {
    console.log(`location permission: ${permission}`);
    if (permission !== RNEP.locationPermission.DENIED) {
      // generate Estimote Cloud credentials for your app at:
      // https://cloud.estimote.com/#/apps/add/your-own-app
      const credentials = new RNEP.CloudCredentials(
        "<#APP_ID#>",
        "<#APP_TOKEN#>"
      );

      const config = {
        // modern versions of Android require a notification informing the user that the app is active in the background
        // if you don't need proximity observation to work in the background, you can omit the entire `notification` config
        notification: {
          title: "Exploration mode is on",
          text: "We'll notify you when you're next to something interesting."
          //icon: 'my_drawable' // if omitted, will default to the app icon (i.e., mipmap/ic_launcher)
        }
      };

      RNEP.proximityObserver.initialize(credentials, config);
      RNEP.proximityObserver.startObservingZones([zone1, zone2]);
    }
  },
  error => {
    console.error("Error when trying to obtain location permission", error);
  }
);
