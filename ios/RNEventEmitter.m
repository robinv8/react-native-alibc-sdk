#import "RNEventEmitter.h"

@implementation RNEventEmitter

RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents {
    return @[@"getCartData"]; //这里返回的将是你要发送的消息名的数组。
}
- (void)startObserving
{
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(emitEventInternal:)
                                                 name:@"event-emitted"
                                               object:nil];
}
- (void)stopObserving
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)emitEventInternal:(NSNotification *)notification
{
    [self sendEventWithName:@"getCartData"
                       body:notification.object];
}

- (void)toGetCartData: (NSString*) url
{
    NSMutableDictionary *dic = [NSMutableDictionary dictionary];
    dic[@"data"] = url;
    [[NSNotificationCenter defaultCenter] postNotificationName:@"event-emitted" object:dic];
}
@end
