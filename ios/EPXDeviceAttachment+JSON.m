#import "EPXDeviceAttachment+JSON.h"

@implementation EPXDeviceAttachment (JSON)

- (NSDictionary *)toJSON {
    return @{@"deviceIdentifier": self.deviceIdentifier,
             @"payload": self.payload};
}

@end
