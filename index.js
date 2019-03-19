import React, { Component, PureComponent } from 'react';
import PropTypes from 'prop-types'
import {
  View,
  Button,
  UIManager,
  NativeModules,
  requireNativeComponent,
  findNodeHandle,
} from 'react-native';

const ALIBC_TRADEWEBVIEW_REF = 'ALIBCTRADEWEBVIEW_REF';

export class AlibcTradeWebView extends React.Component {
  constructor(props) {
    super(props);
    this._onTradeResult = this._onTradeResult.bind(this);
    this._onStateChange = this._onStateChange.bind(this);
    this._onGetCartData = this._onGetCartData.bind(this);
    this._onMessage = this._onMessage.bind(this);
    this.goForward = this.goForward.bind(this);
    this.goBack = this.goBack.bind(this);
    this.reload = this.reload.bind(this);
    this._getWebViewBridgeHandle = this._getWebViewBridgeHandle.bind(this);
  }
  _onTradeResult(event) {
    if (!this.props.onTradeResult) {
      return;
    }
    this.props.onTradeResult(event.nativeEvent);
  }

  _onStateChange(event) {
    if (!this.props.onStateChange) {
      return;
    }
    this.props.onStateChange(event.nativeEvent);
  }
  _onGetCartData(event) {
    if (!this.props.onGetCartData) {
      return;
    }
    this.props.onGetCartData(event.nativeEvent);
  }
  _onMessage(event) {
    if (!this.props.onMessage) {
      return;
    }
    this.props.onMessage(event.nativeEvent);
  }
  goForward() {
    UIManager.dispatchViewManagerCommand(
      this._getWebViewBridgeHandle(),
      UIManager.AlibcTradeWebView.Commands.goForward,
      null
    );
  }

  goBack() {
    UIManager.dispatchViewManagerCommand(
      this._getWebViewBridgeHandle(),
      UIManager.AlibcTradeWebView.Commands.goBack,
      null
    );
  }

  reload() {
    UIManager.dispatchViewManagerCommand(
      this._getWebViewBridgeHandle(),
      UIManager.AlibcTradeWebView.Commands.reload,
      null
    );
  }

  _getWebViewBridgeHandle() {
    return findNodeHandle(this.refs[ALIBC_TRADEWEBVIEW_REF]);
  }

  injectJavaScript = (data: string) => {
    UIManager.dispatchViewManagerCommand(
      this._getWebViewBridgeHandle(),
      UIManager.AlibcTradeWebView.Commands.injectJavaScript,
      [data],
    );
  };
  render() {
    return <NativeComponent ref={ALIBC_TRADEWEBVIEW_REF} {...this.props} 
                            onTradeResult={this._onTradeResult} 
                            onGetCartData={this._onGetCartData} 
                            injectedJavaScript={this.props.injectedJavaScript}
                            onMessage={this._onMessage}
                            messagingEnabled={typeof this.props.onMessage === 'function'}
                            onStateChange={this._onStateChange}/>;
  }
}

AlibcTradeWebView.propTypes = {
  param: PropTypes.object,
  onTradeResult: PropTypes.func,
  onStateChange: PropTypes.func,
  ...View.propTypes,
};

const NativeComponent = requireNativeComponent("AlibcTradeWebView", AlibcTradeWebView);

const { RNAlibcSdk } = NativeModules;

export default RNAlibcSdk;
