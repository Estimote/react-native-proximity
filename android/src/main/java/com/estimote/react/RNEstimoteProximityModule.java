package com.estimote.react;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;
import com.estimote.proximity_sdk.api.ProximityObserver;
import com.estimote.proximity_sdk.api.ProximityObserverBuilder;
import com.estimote.proximity_sdk.api.ProximityZone;
import com.estimote.proximity_sdk.api.ProximityZoneBuilder;
import com.estimote.proximity_sdk.api.ProximityZoneContext;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                .onError(new Function1<Throwable, Unit>() {
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

            String channelId = "est-proximity-sdk";
            String channelName = "Beacon Scanning";

            if (notificationConfig.hasKey("channel")) {
                ReadableMap channelConfig = notificationConfig.getMap("channel");

                if (channelConfig.hasKey("id")) {
                    channelId = channelConfig.getString("id");
                }
                if (channelConfig.hasKey("name")) {
                    channelName = channelConfig.getString("name");
                }
            }

            createNotificationChannel(channelId, channelName);
            builder.withScannerInForegroundService(createNotification(icon, title, text, channelId));
        }

        observer = builder.build();
    }

    @ReactMethod
    public void startObservingZones(ReadableArray zonesJSON) {
        Log.d(TAG, "startObservingZones: " + zonesJSON);

        List<ProximityZone> zones = new ArrayList<ProximityZone>(zonesJSON.size());

        for (int i = 0; i < zonesJSON.size(); i++) {
            ReadableMap zoneJSON = zonesJSON.getMap(i);

            final String _id = zoneJSON.getString("_id");
            double range = zoneJSON.getDouble("range");
            String tag = zoneJSON.getString("tag");

            ProximityZone zone = new ProximityZoneBuilder()
                    .forTag(tag)
                    .inCustomRange(range)
                    .onEnter(new Function1<ProximityZoneContext, Unit>() {
                        @Override
                        public Unit invoke(ProximityZoneContext context) {
                            Log.i(TAG, "onEnter, zoneId = " + _id + ", context = " + context.toString());
                            WritableMap map = new WritableNativeMap();
                            map.putString("zoneId", _id);
                            map.putMap("context", contextToMap(context));
                            sendEvent("Enter", map);
                            return null;
                        }
                    })
                    .onExit(new Function1<ProximityZoneContext, Unit>() {
                        @Override
                        public Unit invoke(ProximityZoneContext context) {
                            Log.i(TAG, "onExit, zoneId = " + _id + ", context = " + context.toString());
                            WritableMap map = new WritableNativeMap();
                            map.putString("zoneId", _id);
                            map.putMap("context", contextToMap(context));
                            sendEvent("Exit", map);
                            return null;
                        }
                    })
                    .onContextChange(new Function1<Set<? extends ProximityZoneContext>, Unit>() {
                        @Override
                        public Unit invoke(Set<? extends ProximityZoneContext> contexts) {
                            Log.i(TAG, "onContextChange, zoneId = " + _id + ", contexts = " + contexts.toString());
                            WritableMap map = new WritableNativeMap();
                            map.putString("zoneId", _id);
                            map.putArray("contexts", contextsToArray(contexts));
                            sendEvent("Change", map);
                            return null;
                        }
                    })
                    .build();

            zones.add(zone);
        }

        // clean up after the previous observer, if any
        if (observationHandler != null) {
            Log.e(TAG, "startObservingZones called without stopping the previous observer; this will cause problems");
            observationHandler.stop();
        }

        observationHandler = observer.startObserving(zones);
    }

    @ReactMethod
    public void stopObservingZones() {
        Log.d(TAG, "stopObservingZones");
        if (observationHandler != null) {
            Log.d(TAG, "observationHandler.stop()");
            observationHandler.stop();
            observationHandler = null;
        }
    }

    @Override
    public void onCatalystInstanceDestroy() {
        Log.d(TAG, "onCatalystInstanceDestroy");
        if (observationHandler != null) {
            Log.d(TAG, "observationHandler.stop()");
            observationHandler.stop();
            observationHandler = null;
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

    private WritableMap contextToMap(ProximityZoneContext context) {
        WritableMap map = new WritableNativeMap();
        map.putString("tag", context.getTag());
        map.putMap("attachments", attachmentsToMap(context.getAttachments()));
        map.putString("deviceIdentifier", context.getDeviceId());
        return map;
    }

    private WritableArray contextsToArray(Set<? extends ProximityZoneContext> contexts) {
        WritableArray array = new WritableNativeArray();
        for (ProximityZoneContext context : contexts) {
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

    private Notification createNotification(String icon, String title, String text, String channelId) {

        int iconRes = 0;
        if (icon != null) {
            iconRes = reactContext.getResources().getIdentifier(icon, "drawable", reactContext.getPackageName());
        }
        if (iconRes == 0) {
            iconRes = reactContext.getResources().getIdentifier("ic_launcher", "mipmap", reactContext.getPackageName());
        }

        return new NotificationCompat.Builder(reactContext, channelId)
                .setSmallIcon(iconRes)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel(String id, String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = reactContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
