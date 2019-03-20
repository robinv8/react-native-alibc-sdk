#import "RNEventEmitter.h"

@implementation RNEventEmitter

RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents {
    return @[@"getCartData", @"onMessage"]; //这里返回的将是你要发送的消息名的数组。
}
- (void)startObserving
{
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(emitEventInternal:)
                                                 name:@"event-emitted1"
                                               object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(emitEventInternal2:)
                                                 name:@"event-emitted2"
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
- (void)emitEventInternal2:(NSNotification *)notification
{
    [self sendEventWithName:@"onMessage"
                       body:notification.object];
}
- (void)toGetCartData: (NSString*) url
{
    NSMutableDictionary *dic = [NSMutableDictionary dictionary];
    dic[@"data"] = url;
    [[NSNotificationCenter defaultCenter] postNotificationName:@"event-emitted1" object:dic];
}
- (void)onMessage: (NSString*) data
{
    NSMutableDictionary *dic = [NSMutableDictionary dictionary];
    dic[@"data"] = data;
    dic[@"type"] = @"cartData";
    [[NSNotificationCenter defaultCenter] postNotificationName:@"event-emitted2" object:dic];
}
@end
