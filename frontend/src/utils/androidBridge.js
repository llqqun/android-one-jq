export function initAndroidBridge() {
  if (typeof window === 'undefined') return;

  window.showQRCodeInfo = function(data) {
    try {
      console.log('[Android Bridge] 收到二维码数据:', data);
      window.dispatchEvent(new CustomEvent('qrCodeReceived', { detail: data }));
    } catch (error) {
      console.error('[Android Bridge] 发送二维码事件失败:', error);
    }
  };

  window.onQRCodeDeviceConnected = function(success) {
    try {
      console.log('[Android Bridge] 二维码设备连接状态:', success);
      window.dispatchEvent(new CustomEvent('qrCodeConnected', { detail: success }));
    } catch (error) {
      console.error('[Android Bridge] 发送连接事件失败:', error);
    }
  };

  window.onQRCodeDeviceDisconnected = function() {
    try {
      console.log('[Android Bridge] 二维码设备断开连接');
      window.dispatchEvent(new CustomEvent('qrCodeDisconnected'));
    } catch (error) {
      console.error('[Android Bridge] 发送断开事件失败:', error);
    }
  };

  window.setDeviceId = function(deviceId) {
    try {
      console.log('[Android Bridge] 收到设备ID:', deviceId);
      window.dispatchEvent(new CustomEvent('deviceIdReceived', { detail: deviceId }));
    } catch (error) {
      console.error('[Android Bridge] 发送设备ID事件失败:', error);
    }
  };

  window.showStatus = function(message, type) {
    try {
      console.log('[Android Bridge] 收到状态消息:', message, type);
      window.dispatchEvent(new CustomEvent('statusMessage', { detail: { message, type } }));
    } catch (error) {
      console.error('[Android Bridge] 发送状态消息失败:', error);
    }
  };

  window.showIdCardInfo = function(cardInfo) {
    try {
      console.log('[Android Bridge] 收到身份证信息:', cardInfo);
      window.dispatchEvent(new CustomEvent('cardInfoReceived', { detail: cardInfo }));
    } catch (error) {
      console.error('[Android Bridge] 发送身份证信息失败:', error);
    }
  };

  window.onSecondaryScreenLoginSuccess = function() {
    try {
      console.log('[Android Bridge] 收到副屏登录成功通知');
      window.dispatchEvent(new CustomEvent('secondaryScreenLoginSuccess'));
    } catch (error) {
      console.error('[Android Bridge] 发送登录成功事件失败:', error);
    }
  };

  window.onScreenUpdate = function(message) {
    try {
      console.log('[Android Bridge] 收到屏幕更新消息:', message);
      window.dispatchEvent(new CustomEvent('screenUpdate', { detail: message }));
    } catch (error) {
      console.error('[Android Bridge] 发送屏幕更新事件失败:', error);
    }
  };

  console.log('[Android Bridge] 初始化完成');
}
