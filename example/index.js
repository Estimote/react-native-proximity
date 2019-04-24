/**
 * @format
 */

import { AppRegistry } from "react-native";
import App from "./App";
import { name as appName } from "./app.json";
import {startProximityObserver, stopProximityObserver} from './proximityObserver';

AppRegistry.registerComponent(appName, () => App);

/* if you want the Proximity Observer constantly running, even if the user swipes the app away, start it here: */
startProximityObserver();
/* see also: https://github.com/Estimote/react-native-proximity#already-observing */
