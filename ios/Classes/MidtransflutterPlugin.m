#import "MidtransflutterPlugin.h"
#if __has_include(<midtransflutter/midtransflutter-Swift.h>)
#import <midtransflutter/midtransflutter-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "midtransflutter-Swift.h"
#endif

@implementation MidtransflutterPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftMidtransflutterPlugin registerWithRegistrar:registrar];
}
@end
