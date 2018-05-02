package com.estimote.react;

import android.util.Log;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

import java.util.List;
import java.util.Map;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import com.facebook.react.modules.core.DeviceEventManagerModule;

import com.estimote.proximity_sdk.proximity.EstimoteCloudCredentials;
import com.estimote.proximity_sdk.proximity.ProximityAttachment;
import com.estimote.proximity_sdk.proximity.ProximityObserver;
import com.estimote.proximity_sdk.proximity.ProximityObserverBuilder;
import com.estimote.proximity_sdk.proximity.ProximityZone;

public class RNEstimoteProximityModule extends ReactContextBaseJavaModule {

  private static final String TAG = "RNEstimoteProximity";

  private final ReactApplicationContext reactContext;

  private ReadableMap config;
  private ProximityObserver observer;
  private ProximityObserver.Handler observationHandler;

  public RNEstimoteProximityModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNEstimoteProximity";
  }

  @ReactMethod
  public void initialize(ReadableMap config) {
    Log.i(TAG, "Initializing with config: " + config.toString());

    EstimoteCloudCredentials credentials = new EstimoteCloudCredentials(
        config.getString("appId"), config.getString("appToken"));

    observer = new ProximityObserverBuilder(reactContext, credentials)
        .withBalancedPowerMode()
        .withOnErrorAction(new Function1<Throwable, Unit>() {
            @Override
            public Unit invoke(Throwable throwable) {
                Log.e(TAG, "Proximity Observer error: " + throwable);
                return null;
            }
        })
        .build();
  }

  @ReactMethod
  public void startObservingZones(ReadableArray zonesJSON) {
    for (int i = 0; i < zonesJSON.size(); i++) {
      ReadableMap zoneJSON = zonesJSON.getMap(i);

      final String _id = zoneJSON.getString("_id");
      double range = zoneJSON.getDouble("range");
      String attachmentKey = zoneJSON.getString("attachmentKey");
      String attachmentValue = zoneJSON.getString("attachmentValue");

      ProximityZone zone = observer.zoneBuilder()
          .forAttachmentKeyAndValue(attachmentKey, attachmentValue)
          .inCustomRange(range)
          .withOnEnterAction(new Function1<ProximityAttachment, Unit>() {
            @Override
            public Unit invoke(ProximityAttachment attachment) {
              Log.i(TAG, "onEnterAction, zoneId = " + _id + ", attachment = " + attachment.toString());
              WritableMap map = new WritableNativeMap();
              map.putString("zoneId", _id);
              map.putMap("attachment", attachmentToMap(attachment));
              sendEvent("Enter", map);
              return null;
            }
          })
          .withOnExitAction(new Function1<ProximityAttachment, Unit>() {
            @Override
            public Unit invoke(ProximityAttachment attachment) {
              Log.i(TAG, "onExitAction, zoneId = " + _id + ", attachment = " + attachment.toString());
              WritableMap map = new WritableNativeMap();
              map.putString("zoneId", _id);
              map.putMap("attachment", attachmentToMap(attachment));
              sendEvent("Exit", map);
              return null;
            }
          })
          .withOnChangeAction(new Function1<List<? extends ProximityAttachment>, Unit>() {
            @Override
            public Unit invoke(List<? extends ProximityAttachment> attachments) {
              Log.i(TAG, "onChangeAction, zoneId = " + _id + ", attachments = " + attachments.toString());
              WritableMap map = new WritableNativeMap();
              map.putString("zoneId", _id);
              map.putArray("attachments", attachmentsToArray(attachments));
              sendEvent("Change", map);
              return null;
            }
          })
          .create();

      observer.addProximityZone(zone);
    }

    // clean up after the previous observer, if any
    if (observationHandler != null) {
      observationHandler.stop();
    }

    observationHandler = observer.start();
  }

  @ReactMethod
  public void stopObservingZones() {
    if (observationHandler != null) {
      observationHandler.stop();
    }
  }

  @Override
  public void onCatalystInstanceDestroy() {
    if (observationHandler != null) {
      observationHandler.stop();
    }
  }

  // helpers

  private WritableMap payloadToMap(Map<String, String> payload) {
    WritableMap map = new WritableNativeMap();
    for (Map.Entry<String, String> entry : payload.entrySet()) {
      map.putString(entry.getKey(), entry.getValue());
    }
    return map;
  }

  private WritableMap attachmentToMap(ProximityAttachment attachment) {
    WritableMap map = new WritableNativeMap();
    map.putString("deviceIdentifier", attachment.getDeviceId());
    map.putMap("payload", payloadToMap(attachment.getPayload()));
    return map;
  }

  private WritableArray attachmentsToArray(List<? extends ProximityAttachment> attachments) {
    WritableArray array = new WritableNativeArray();
    for (ProximityAttachment attachment : attachments) {
      array.pushMap(attachmentToMap(attachment));
    }
    return array;
  }

  private void sendEvent(String eventName, Object params) {
    reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
  }
}
