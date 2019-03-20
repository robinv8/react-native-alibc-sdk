
#import <React/RCTViewManager.h>
#import <React/RCTUIManager.h>
#import <UIKit/UIKit.h>
#import "AlibcWebView.h"

#import <React/RCTLog.h>
#import "RNEventEmitter.h"
@interface AlibcTradeWebViewManager : RCTViewManager<UIWebViewDelegate>
@property (nonatomic, assign) BOOL messagingEnabled;

@end

@implementation AlibcTradeWebViewManager
{
    UIWebView *_webView;
}
RCT_EXPORT_MODULE()

- (UIView *)view
{
    AlibcWebView *webView = [[AlibcWebView alloc] initWithFrame:CGRectZero];
    webView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    webView.scrollView.scrollEnabled = YES;
    webView.delegate = self;
    return webView;
}

RCT_EXPORT_VIEW_PROPERTY(param, NSDictionary)
RCT_EXPORT_VIEW_PROPERTY(onTradeResult, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onStateChange, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(injectedJavaScript, NSString)

RCT_EXPORT_METHOD(goBack:(nonnull NSNumber *)reactTag)
{
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, AlibcWebView *> *viewRegistry) {
        AlibcWebView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[AlibcWebView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting AlibcWebView, got: %@", view);
        } else {
            [view goBack];
        }
    }];
}

RCT_EXPORT_METHOD(goForward:(nonnull NSNumber *)reactTag)
{
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, AlibcWebView *> *viewRegistry) {
        AlibcWebView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[AlibcWebView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting AlibcWebView, got: %@", view);
        } else {
            [view goForward];
        }
    }];
}

RCT_EXPORT_METHOD(reload:(nonnull NSNumber *)reactTag)
{
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, AlibcWebView *> *viewRegistry) {
        AlibcWebView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[AlibcWebView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting AlibcWebView, got: %@", view);
        } else {
            [view reload];
        }
    }];
}
RCT_EXPORT_METHOD(injectJavaScript:(nonnull NSNumber *)reactTag script:(NSString *)script)
{
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, AlibcWebView *> *viewRegistry) {
        AlibcWebView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[AlibcWebView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting RNCUIWebView, got: %@", view);
        } else {
            [view stringByEvaluatingJavaScriptFromString: script];
        }
    }];
}
- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType
{
    RCTLog(@"Loading URL :%@",request.URL.absoluteString);
    if ([request.URL.host isEqualToString:@"ReactNativeWebView"]) {
        NSString *data = request.URL.query;
        data = [data stringByReplacingOccurrencesOfString:@"+" withString:@" "];
        data = [data stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        
        NSString *source = [NSString stringWithFormat:@"window.%@.messageReceived();", @"ReactNativeWebView"];
        
        [webView stringByEvaluatingJavaScriptFromString:source];
        RNEventEmitter *rnEventEmitter = [[RNEventEmitter alloc] init];
        [rnEventEmitter onMessage: data];
    }
    
    NSString* url = request.URL.absoluteString;
    if ([url hasPrefix:@"http://"]  ||
            [url hasPrefix:@"https://"] ||
            [url hasPrefix:@"file://"]) {
        return YES;
    } else {
        return FALSE; //to stop loading
    }
}

- (void)webViewDidStartLoad:(AlibcWebView *)webView
{
    webView.onStateChange(@{
                            @"loading": @(true),
                            @"url": webView.request.URL.absoluteString,
                            @"canGoBack": @([webView canGoBack]),
                            });
}
- (void)webViewDidFinishLoad:(AlibcWebView *)webView
{
    if (YES) {
        NSString *source = [NSString stringWithFormat:
                            @"(function() {"
                            "  var messageQueue = [];"
                            "  var messagePending = false;"
                            
                            "  function processQueue () {"
                            "    if (!messageQueue.length || messagePending) return;"
                            "    messagePending = true;"
                            "    document.location = '%@://%@?' + encodeURIComponent(messageQueue.shift());"
                            "  }"
                            
                            "  window.%@ = {"
                            "    postMessage: function (data) {"
                            "      messageQueue.push(String(data));"
                            "      processQueue();"
                            "    },"
                            "    messageReceived: function () {"
                            "      messagePending = false;"
                            "      processQueue();"
                            "    }"
                            "  };"
                            "})();", @"react-js-navigation", @"ReactNativeWebView", @"ReactNativeWebView"
                            ];
        [webView stringByEvaluatingJavaScriptFromString:source];
    }
    webView.onStateChange(@{
                            @"loading": @(false),
                            @"url": webView.request.URL.absoluteString,
                            @"canGoBack": @([webView canGoBack]),
                            @"title": [webView stringByEvaluatingJavaScriptFromString:@"document.title"],
                            });
}

- (void)webView:(AlibcWebView *)webView didFailLoadWithError:(NSError *)error
{
    /*webView.onStateChange(@{
                            @"loading": @(false),
                            @"error": @(true),
                            @"canGoBack": @([webView canGoBack]),
                            });
    RCTLog(@"Failed to load with error :%@",[error debugDescription]);*/
}

@end
