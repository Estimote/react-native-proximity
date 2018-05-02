#import "RNEstimoteProximity.h"

#import <React/RCTLog.h>

#import <CoreLocation/CoreLocation.h>

#import "EPXCloudCredentials.h"
#import "EPXDeviceAttachment.h"
#import "EPXProximityObserver.h"
#import "EPXProximityZone.h"

#import "EPXDeviceAttachment+JSON.h"

NSString * authStringForCurrentAuthStatus() {
    switch ([CLLocationManager authorizationStatus]) {
        case kCLAuthorizationStatusNotDetermined:
            return nil;
        case kCLAuthorizationStatusRestricted:
        case kCLAuthorizationStatusDenied:
            return @"denied";
        case kCLAuthorizationStatusAuthorizedAlways:
            return @"always";
        case kCLAuthorizationStatusAuthorizedWhenInUse:
            return @"when_in_use";
    }
}

@interface RNEstimoteProximity () <CLLocationManagerDelegate>

@property (nonatomic, strong) EPXProximityObserver *observer;

@property (nonatomic, strong) CLLocationManager *locationManager;
@property (nonatomic, strong) RCTPromiseResolveBlock locationPermissionResolver;

@end

@implementation RNEstimoteProximity

RCT_EXPORT_MODULE()

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

- (NSArray<NSString *> *)supportedEvents {
    return @[@"Enter", @"Exit", @"Change"];
}

- (void)dealloc {
    [self.observer stopObservingZones];
}

RCT_EXPORT_METHOD(initialize:(NSDictionary *)config) {
    RCTLogInfo(@"Initializing with config: %@", config);

    EPXCloudCredentials *credentials = [[EPXCloudCredentials alloc] initWithAppID:config[@"appId"] appToken:config[@"appToken"]];

    self.observer = [[EPXProximityObserver alloc] initWithCredentials:credentials errorBlock:^(NSError * _Nonnull error) {
        RCTLogError(@"Proximity Observer error: %@", error);
    }];
}

RCT_EXPORT_METHOD(startObservingZones:(NSArray *)zonesJSON) {
    NSMutableArray *zones = [NSMutableArray arrayWithCapacity:zonesJSON.count];

    for (NSDictionary *zoneJSON in zonesJSON) {
        NSString *_id = zoneJSON[@"_id"];
        NSNumber *range = zoneJSON[@"range"];
        NSString *attachmentKey = zoneJSON[@"attachmentKey"];
        NSString *attachmentValue = zoneJSON[@"attachmentValue"];

        RCTLogInfo(@"Creating Proximity Zone _id = %@, range = %@, attachmentKey = %@, attachmentValue = %@", _id, range, attachmentKey, attachmentValue);

        EPXProximityZone *zone = [[EPXProximityZone alloc]
                                  initWithRange:[EPXProximityRange customRangeWithDesiredMeanTriggerDistance:range.doubleValue]
                                  attachmentKey:attachmentKey
                                  attachmentValue:attachmentValue];

        __weak __typeof(self) weakSelf = self;

        zone.onEnterAction = ^(EPXDeviceAttachment *attachment) {
            RCTLogInfo(@"onEnterAction, zoneId = %@, attachment = %@", _id, attachment);

            [weakSelf sendEventWithName:@"Enter" body:@{@"zoneId": _id,
                                                        @"attachment": [attachment toJSON]}];
        };

        zone.onExitAction = ^(EPXDeviceAttachment *attachment) {
            RCTLogInfo(@"onExitAction, zoneId = %@, attachment = %@", _id, attachment);

            [weakSelf sendEventWithName:@"Exit" body:@{@"zoneId": _id,
                                                       @"attachment": [attachment toJSON]}];
        };

        zone.onChangeAction = ^(NSSet<EPXDeviceAttachment *> *attachments) {
            RCTLogInfo(@"onChangeAction, zoneId = %@, attachments = %@", _id, attachments);

            NSMutableArray *convertedAttachments = [NSMutableArray arrayWithCapacity:attachments.count];
            for (EPXDeviceAttachment *attachment in attachments) {
                [convertedAttachments addObject:[attachment toJSON]];
            }
            [weakSelf sendEventWithName:@"Change" body:@{@"zoneId": _id,
                                                         @"attachments": convertedAttachments}];
        };

        [zones addObject:zone];
    }

    [self.observer startObservingZones:zones];

    RCTLogInfo(@"Started observing for %lu zone(s)", (unsigned long) zones.count);
}

RCT_EXPORT_METHOD(stopObservingZones) {
    [self.observer stopObservingZones];

    RCTLogInfo(@"Stopped observing");
}

RCT_REMAP_METHOD(requestLocationPermission,
                 requestLocationPermissionWithResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    if ([CLLocationManager authorizationStatus] == kCLAuthorizationStatusNotDetermined) {
        self.locationPermissionResolver = resolve;
        self.locationManager = [CLLocationManager new];
        self.locationManager.delegate = self;
        [self.locationManager requestAlwaysAuthorization];
    } else {
        resolve(authStringForCurrentAuthStatus());
    }
}

- (void)locationManager:(CLLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status {
    if (status != kCLAuthorizationStatusNotDetermined) {
        self.locationPermissionResolver(authStringForCurrentAuthStatus());
        self.locationPermissionResolver = nil;
        self.locationManager = nil;
    }
}

@end
