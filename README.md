# react-native-estimote-proximity

## Getting started

`$ npm install react-native-estimote-proximity --save`

### Mostly automatic installation

`$ react-native link react-native-estimote-proximity`

### Manual installation

#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-estimote-proximity` and add `RNEstimoteProximity.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNEstimoteProximity.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.estimote.react.RNEstimoteProximityPackage;` to the imports at the top of the file
  - Add `new RNEstimoteProximityPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-estimote-proximity'
  	project(':react-native-estimote-proximity').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-estimote-proximity/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
    compile project(':react-native-estimote-proximity')
  	```

## Usage

```javascript
import RNEstimoteProximity from 'react-native-estimote-proximity';

// TODO: What to do with the module?
RNEstimoteProximity;
```
