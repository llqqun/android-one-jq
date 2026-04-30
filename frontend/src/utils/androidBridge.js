export function initAndroidBridge() {
  if (typeof window === 'undefined') return;

  const dispatchEvent = (eventName, detail = null) => {
    try {
      console.log(`[Android Bridge] 派发事件: ${eventName}`, detail);
      window.dispatchEvent(new CustomEvent(eventName, { detail }));
    } catch (error) {
      console.error(`[Android Bridge] 派发事件 ${eventName} 失败:`, error);
    }
  };

  const dispatchScreenEvent = (action, data = null) => {
    const eventData = { action, data };
    console.log(`[Android Bridge] 派发屏幕事件:`, eventData);
    dispatchEvent('screenEvent', eventData);
  };

  window.showQRCodeInfo = function(data) {
    console.log('[Android Bridge] 收到二维码数据:', data);
    dispatchEvent('qrCodeReceived', data);
  };

  window.onQRCodeDeviceConnected = function(success) {
    console.log('[Android Bridge] 二维码设备连接状态:', success);
    dispatchEvent('qrCodeConnected', success);
  };

  window.onQRCodeDeviceDisconnected = function() {
    console.log('[Android Bridge] 二维码设备断开连接');
    dispatchEvent('qrCodeDisconnected');
  };

  window.setDeviceId = function(deviceId) {
    console.log('[Android Bridge] 收到设备ID:', deviceId);
    dispatchEvent('deviceIdReceived', deviceId);
  };

  window.showStatus = function(message, type) {
    console.log('[Android Bridge] 收到状态消息:', message, type);
    dispatchEvent('statusMessage', { message, type });
  };

  window.showIdCardInfo = function(cardInfo) {
    console.log('[Android Bridge] 收到身份证信息:', cardInfo);
    dispatchEvent('cardInfoReceived', cardInfo);
  };

  window.onSecondaryScreenLoginSuccess = function(companyLoginInfo) {
    console.log('[Android Bridge] 收到副屏登录成功通知:', companyLoginInfo);
    let parsedData = companyLoginInfo;
    if (typeof companyLoginInfo === 'string') {
      try {
        parsedData = JSON.parse(companyLoginInfo);
      } catch (e) {
        console.error('[Android Bridge] 解析登录信息失败:', e);
      }
    }
    dispatchScreenEvent('loginSuccess', parsedData);
  };

  window.onScreenUpdate = function(message) {
    console.log('[Android Bridge] 收到屏幕更新消息:', message);
    try {
      let parsedMessage = message;
      if (typeof message === 'string') {
        parsedMessage = JSON.parse(`"${message}"`);
        parsedMessage = JSON.parse(parsedMessage);
      }

      if (parsedMessage.action) {
        dispatchScreenEvent(parsedMessage.action, parsedMessage.data);
      } else if (parsedMessage.type) {
        dispatchScreenEvent(parsedMessage.type, parsedMessage.data);
      } else if (message === 'resume_delivered') {
        dispatchScreenEvent('resumeDelivered');
      } else {
        console.warn('[Android Bridge] 无法解析消息格式:', message);
      }
    } catch (error) {
      console.error('[Android Bridge] 解析屏幕更新消息失败:', error);
      if (message === 'resume_delivered') {
        dispatchScreenEvent('resumeDelivered');
      }
    }
  };

  window.notifyMainScreen = function(message) {
    console.log('[Android Bridge] 收到通知主屏消息:', message);
    try {
      let parsedMessage = message;
      if (typeof message === 'string') {
        parsedMessage = JSON.parse(message);
      }

      if (parsedMessage.action) {
        dispatchScreenEvent(parsedMessage.action, parsedMessage.data);
      } else if (parsedMessage.type) {
        dispatchScreenEvent(parsedMessage.type, parsedMessage.data);
      }
    } catch (error) {
      console.error('[Android Bridge] 解析通知主屏消息失败:', error);
    }
  };

  console.log('[Android Bridge] 初始化完成');
}

export const sendToSecondaryScreen = (action, data = null) => {
  try {
    if (window.android && window.android.notifySecondaryScreenUpdate) {
      const message = JSON.stringify({ action, data });
      console.log('[Android Bridge] 发送消息到副屏:', message);
      window.android.notifySecondaryScreenUpdate(message);
      return true;
    } else {
      console.error('[Android Bridge] notifySecondaryScreenUpdate 方法未定义');
      return false;
    }
  } catch (error) {
    console.error('[Android Bridge] 发送消息到副屏失败:', error);
    return false;
  }
};

export const sendToMainScreen = (action, data = null) => {
  try {
    if (window.android && window.android.notifyMainScreenUpdate) {
      const message = JSON.stringify({ action, data });
      console.log('[Android Bridge] 发送消息到主屏:', message);
      window.android.notifyMainScreenUpdate(message);
      return true;
    } else {
      console.error('[Android Bridge] notifyMainScreenUpdate 方法未定义');
      return false;
    }
  } catch (error) {
    console.error('[Android Bridge] 发送消息到主屏失败:', error);
    return false;
  }
};