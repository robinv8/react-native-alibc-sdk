package com.daiyan;

import android.os.Build;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.facebook.common.logging.FLog;
import com.facebook.react.common.build.ReactBuildConfig;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.common.ReactConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import android.graphics.Bitmap;
import android.net.Uri;
import javax.annotation.Nullable;
import android.view.ViewGroup.LayoutParams;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class AlibcTradeWebViewManager extends SimpleViewManager<WebView> {
	private final static String REACT_CLASS = "AlibcTradeWebView";
  protected static final String BRIDGE_NAME = "__REACT_WEB_VIEW_BRIDGE";

	public static final int COMMAND_GO_BACK = 1;
	public static final int COMMAND_GO_FORWARD = 2;
	public static final int COMMAND_RELOAD = 3;
  public static final int COMMAND_POST_MESSAGE = 5;
	public static final int COMMAND_INJECT_JAVASCRIPT = 6;
	private RNAlibcSdkModule mModule;

	private class AlibcWebView extends WebView {
		private ReactContext mContext;
		protected @Nullable String injectedJS;
    protected boolean messagingEnabled = false;

		AlibcWebView(ReactContext context){
			super(context.getCurrentActivity());
			mContext = context;
		}

    protected class RNCWebViewBridge {
      AlibcWebView mContext;

      RNCWebViewBridge(AlibcWebView c) {
        mContext = c;
      }

      @JavascriptInterface
      public void postMessage(String message) {
        mContext.onMessage(message);
      }
    }
    public void onMessage(String message) {
      WritableMap event = Arguments.createMap();
      event.putString("data", message);
      event.putString("type", "cartData");
      ReactContext reactContext = this.getReactContext();
      reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(this.getId(), "onMessage", event);
    }
    protected RNCWebViewBridge createRNCWebViewBridge(AlibcWebView webView) {
      return new RNCWebViewBridge(webView);
    }
		public ReactContext getReactContext(){
			return mContext;
		}

		public void setInjectedJavaScript(@Nullable String js) {
			injectedJS = js;
		}
    public void setMessagingEnabled(boolean enabled) {
      if (messagingEnabled == enabled) {
        return;
      }

      messagingEnabled = enabled;
      if (enabled) {
        addJavascriptInterface(createRNCWebViewBridge(this), BRIDGE_NAME);
        linkBridge();
      } else {
        removeJavascriptInterface(BRIDGE_NAME);
      }
    }
		protected void evaluateJavascriptWithFallback(String script) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				evaluateJavascript(script, null);
				return;
			}

			try {
				loadUrl("javascript:" + URLEncoder.encode(script, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// UTF-8 should always be supported
				throw new RuntimeException(e);
			}
		}
		public void callInjectedJavaScript() {
			if (getSettings().getJavaScriptEnabled() &&
					injectedJS != null &&
					!TextUtils.isEmpty(injectedJS)) {
				evaluateJavascriptWithFallback("(function() {\n" + injectedJS + ";\n})();");
			}
		}
    public void linkBridge() {
      if (messagingEnabled) {
        if (ReactBuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
          // See isNative in lodash
          String testPostMessageNative = "String(window.postMessage) === String(Object.hasOwnProperty).replace('hasOwnProperty', 'postMessage')";
          evaluateJavascript(testPostMessageNative, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
              if (value.equals("true")) {
                FLog.w(ReactConstants.TAG, "Setting onMessage on a WebView overrides existing values of window.postMessage, but a previous value was defined");
              }
            }
          });
        }

        evaluateJavascriptWithFallback("(" +
          "window.originalPostMessage = window.postMessage," +
          "window.postMessage = function(data) {" +
          BRIDGE_NAME + ".postMessage(String(data));" +
          "}" +
          ")");
      }
    }
	}

	private class AlibcWebViewClient extends WebViewClient {

		protected boolean mLastLoadFailed = false;
		@Override
		public void onPageFinished(WebView webView, String url) {
			super.onPageFinished(webView, url);
			if (!mLastLoadFailed) {
				AlibcWebView reactWebView = (AlibcWebView) webView;
				reactWebView.callInjectedJavaScript();
				reactWebView.linkBridge();
			}

			WritableMap event = Arguments.createMap();
			event.putBoolean("loading", false);
			event.putString("url", webView.getUrl());
			event.putBoolean("canGoBack", webView.canGoBack());
			event.putString("title", webView.getTitle());
			ReactContext reactContext = ((AlibcWebView)webView).getReactContext();
			reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(webView.getId(), "onStateChange", event);
		}

		@Override
		public void onPageStarted(WebView webView, String url, Bitmap favicon) {
			super.onPageStarted(webView, url, favicon);

			mLastLoadFailed = false;

			WritableMap event = Arguments.createMap();
			event.putBoolean("loading", true);
			event.putString("url", webView.getUrl());
			event.putBoolean("canGoBack", webView.canGoBack());
			ReactContext reactContext = ((AlibcWebView)webView).getReactContext();
			reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(webView.getId(), "onStateChange", event);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.v(ReactConstants.TAG, REACT_CLASS + " shouldOverrideUrlLoading:" + url);
			if (url.startsWith("http://") || url.startsWith("https://") ||
					url.startsWith("file://")) {
				return false;
			} else {
				return true;
			}
		}
		@Override
		public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
			if (!TextUtils.isEmpty(url) && Uri.parse(url).getScheme() != null) {
				String scheme = Uri.parse(url).getScheme().trim();
				if (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) {
					if(url.indexOf("querybag") > -1) {
						WritableMap event = Arguments.createMap();
						event.putString("data", url);
						ReactContext reactContext = ((AlibcWebView)view).getReactContext();
						reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(view.getId(), "onGetCartData", event);
					}
					return super.shouldInterceptRequest(view, url);
				}
			}
			return super.shouldInterceptRequest(view, url);
		}
	}

	AlibcTradeWebViewManager(RNAlibcSdkModule module) {
		mModule = module;
	}

	@Override
	public String getName() {
		return REACT_CLASS;
	}

	@Override
	protected WebView createViewInstance(ThemedReactContext themedReactContext) {
		WebView webView = new AlibcWebView(themedReactContext);
		webView.getSettings().setJavaScriptEnabled(true);

		/*webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setDisplayZoomControls(false);
		webView.getSettings().setDomStorageEnabled(true);*/

		// Fixes broken full-screen modals/galleries due to body height being 0.
		webView.setLayoutParams(
				new LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT));

		return webView;
	}

	@Override
	public Map getExportedCustomDirectEventTypeConstants() {
		return MapBuilder.of(
				"onStateChange", MapBuilder.of("registrationName", "onStateChange"),
				"onGetCartData", MapBuilder.of("registrationName", "onGetCartData"),
        "onMessage", MapBuilder.of("registrationName", "onMessage"),
				"onTradeResult", MapBuilder.of("registrationName", "onTradeResult"));
	}

	@Override
	public @Nullable Map<String, Integer> getCommandsMap() {
		return MapBuilder.of(
				"goBack", COMMAND_GO_BACK,
				"goForward", COMMAND_GO_FORWARD,
        "postMessage", COMMAND_POST_MESSAGE,
				"injectJavaScript", COMMAND_INJECT_JAVASCRIPT,
				"reload", COMMAND_RELOAD
		);
	}

	@Override
	public void receiveCommand(WebView root, int commandId, @Nullable ReadableArray args) {
		switch (commandId) {
			case COMMAND_GO_BACK:
				root.goBack();
				break;
			case COMMAND_GO_FORWARD:
				root.goForward();
				break;
			case COMMAND_RELOAD:
				root.reload();
				break;
      case COMMAND_POST_MESSAGE:
        try {
          AlibcWebView reactWebView = (AlibcWebView) root;
          JSONObject eventInitDict = new JSONObject();
          eventInitDict.put("data", args.getString(0));
          reactWebView.evaluateJavascriptWithFallback("(function () {" +
            "var event;" +
            "var data = " + eventInitDict.toString() + ";" +
            "try {" +
            "event = new MessageEvent('message', data);" +
            "} catch (e) {" +
            "event = document.createEvent('MessageEvent');" +
            "event.initMessageEvent('message', true, true, data.data, data.origin, data.lastEventId, data.source);" +
            "}" +
            "document.dispatchEvent(event);" +
            "})();");
        } catch (JSONException e) {
          throw new RuntimeException(e);
        }
        break;
			case COMMAND_INJECT_JAVASCRIPT:
				AlibcWebView reactWebView = (AlibcWebView) root;
        reactWebView.evaluateJavascriptWithFallback(args.getString(0));
        break;
			default:
				//do nothing!!!!
		}
	}
	@ReactProp(name = "injectedJavaScript")
	public void setInjectedJavaScript(WebView view, @Nullable String injectedJavaScript) {
		((AlibcWebView) view).setInjectedJavaScript(injectedJavaScript);
	}
	@ReactProp(name = "javaScriptEnabled")
	public void setJavaScriptEnabled(WebView view, boolean enabled) {
		view.getSettings().setJavaScriptEnabled(enabled);
	}
  @ReactProp(name = "messagingEnabled")
  public void setMessagingEnabled(WebView view, boolean enabled) {
    ((AlibcWebView) view).setMessagingEnabled(enabled);
  }
	@ReactProp(name = "param")
	public void propSetParam(WebView view, ReadableMap param) {
		mModule.showInWebView(view, new AlibcWebViewClient(), param);
	}
}
