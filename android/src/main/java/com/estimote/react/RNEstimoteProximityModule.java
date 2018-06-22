package com.estimote.react;

import android.app.Notification;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.estimote.proximity_sdk.proximity.EstimoteCloudCredentials;
import com.estimote.proximity_sdk.proximity.ProximityContext;
import com.estimote.proximity_sdk.proximity.ProximityObserver;
import com.estimote.proximity_sdk.proximity.ProximityObserverBuilder;
import com.estimote.proximity_sdk.proximity.ProximityZone;
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

import java.util.List;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class RNEstimoteProximityModule extends ReactContextBaseJavaModule {

    private static final String TAG = "RNEstimoteProximity";

    private final ReactApplicationContext reactContext;

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

        ProximityObserverBuilder builder = new ProximityObserverBuilder(reactContext, credentials)
                .withBalancedPowerMode()
                .withOnErrorAction(new Function1<Throwable, Unit>() {
                    @Override
                    public Unit invoke(Throwable throwable) {
                        Log.e(TAG, "Proximity Observer error: " + throwable);
                        return null;
                    }
                });


        if (config.hasKey("notification")) {
            ReadableMap notificationConfig = config.getMap("notification");

            String icon = notificationConfig.hasKey("icon")
                    ? notificationConfig.getString("icon")
                    : null;

            String title = notificationConfig.hasKey("title")
                    ? notificationConfig.getString("title")
                    : "Scanning for beacons...";

            String text = notificationConfig.hasKey("text")
                    ? notificationConfig.getString("text")
                    : null;

            builder.withScannerInForegroundService(createNotification(icon, title, text));
        }

        observer = builder.build();
    }

    @ReactMethod
    public void startObservingZones(ReadableArray zonesJSON) {
        for (int i = 0; i < zonesJSON.size(); i++) {
            ReadableMap zoneJSON = zonesJSON.getMap(i);

            final String _id = zoneJSON.getString("_id");
            double range = zoneJSON.getDouble("range");
            String tag = zoneJSON.getString("tag");

            ProximityZone zone = observer.zoneBuilder()
                    .forTag(tag)
                    .inCustomRange(range)
                    .withOnEnterAction(new Function1<ProximityContext, Unit>() {
                        @Override
                        public Unit invoke(ProximityContext context) {
                            Log.i(TAG, "onEnterAction, zoneId = " + _id + ", context = " + context.toString());
                            WritableMap map = new WritableNativeMap();
                            map.putString("zoneId", _id);
                            map.putMap("context", contextToMap(context));
                            sendEvent("Enter", map);
                            return null;
                        }
                    })
                    .withOnExitAction(new Function1<ProximityContext, Unit>() {
                        @Override
                        public Unit invoke(ProximityContext context) {
                            Log.i(TAG, "onExitAction, zoneId = " + _id + ", context = " + context.toString());
                            WritableMap map = new WritableNativeMap();
                            map.putString("zoneId", _id);
                            map.putMap("context", contextToMap(context));
                            sendEvent("Exit", map);
                            return null;
                        }
                    })
                    .withOnChangeAction(new Function1<List<? extends ProximityContext>, Unit>() {
                        @Override
                        public Unit invoke(List<? extends ProximityContext> contexts) {
                            Log.i(TAG, "onChangeAction, zoneId = " + _id + ", contexts = " + contexts.toString());
                            WritableMap map = new WritableNativeMap();
                            map.putString("zoneId", _id);
                            map.putArray("contexts", contextsToArray(contexts));
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

    // serialization helpers

    private WritableMap attachmentsToMap(Map<String, String> attachments) {
        WritableMap map = new WritableNativeMap();
        for (Map.Entry<String, String> entry : attachments.entrySet()) {
            map.putString(entry.getKey(), entry.getValue());
        }
        return map;
    }

    private WritableMap contextToMap(ProximityContext context) {
        WritableMap map = new WritableNativeMap();
        map.putString("tag", context.getTag());
        map.putMap("attachments", attachmentsToMap(context.getAttachments()));
        map.putString("deviceIdentifier", context.getInfo().getDeviceId());
        return map;
    }

    private WritableArray contextsToArray(List<? extends ProximityContext> contexts) {
        WritableArray array = new WritableNativeArray();
        for (ProximityContext context : contexts) {
            array.pushMap(contextToMap(context));
        }
        return array;
    }

    // event helper

    private void sendEvent(String eventName, Object params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    // notification helper

    private Notification createNotification(String icon, String title, String text) {

        int iconRes = 0;
        if (icon != null) {
            iconRes = reactContext.getResources().getIdentifier(icon, "drawable", reactContext.getPackageName());
        }
        if (iconRes == 0) {
            iconRes = reactContext.getResources().getIdentifier("ic_launcher", "mipmap", reactContext.getPackageName());
        }

        return new NotificationCompat.Builder(reactContext)
                .setSmallIcon(iconRes)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
}
