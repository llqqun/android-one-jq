<template>
  <div class="home-container">
    <div class="img-box">
      <img src="../assets/images/home_2.png" class="home-image-logo" alt="logo" />
      <img src="../assets/images/home_3.png" class="home-image-text" alt="text" />
    </div>

    <!-- 功能调试区域 -->
    <div class="debug-section">
      <div class="debug-header">
        <span class="section-title">功能调试</span>
        <div class="debug-buttons">
          <van-button
            type="warning"
            size="small"
            @click="viewQRCodeLog">
            二维码日志
          </van-button>
          <van-button
            type="warning"
            size="small"
            @click="viewCardReaderLog">
            读卡器日志
          </van-button>
          <van-button
            type="warning"
            size="small"
            @click="clearLog">
            清除日志
          </van-button>
        </div>
      </div>

      <div v-if="qrCodeData" class="scan-result">
        <div class="result-title">识别结果</div>
        <div class="result-content">{{ qrCodeData }}</div>
        <van-button type="primary" size="small" @click="clearQRCodeResult">清除结果</van-button>
      </div>
    </div>

    <!-- 企业登录弹窗 -->
    <van-popup
      v-model:show="showLoginDialog"
      round
      :close-on-click-overlay="false"
      :closeable="false">
      <div class="login-dialog">
        <div class="dialog-title">企业登录</div>

        <!-- 登录方式切换 -->
        <div class="login-tabs">
          <div
            class="tab-item"
            :class="{ active: loginType === 'password' }"
            @click="loginType = 'password'">
            账号密码登录
          </div>
          <div
            class="tab-item"
            :class="{ active: loginType === 'qrcode' }"
            @click="loginType = 'qrcode'">
            扫码登录
          </div>
        </div>

        <!-- 账号密码登录 -->
        <div v-if="loginType === 'password'" class="login-form">
          <van-field v-model="loginForm.username" label="手机号" placeholder="请输入手机号" />
          <van-field
            v-model="loginForm.password"
            label="密码"
            type="password"
            placeholder="请输入密码" />
          <van-button
            type="primary"
            class="login-btn"
            :loading="loginLoading"
            @click="handlePasswordLogin">
            登录
          </van-button>
          <div class="device-id">设备编号：{{ deviceId }}</div>
        </div>

        <!-- 扫码登录 -->
        <div v-else-if="loginType === 'qrcode'" class="qrcode-login">
          <div class="qrcode-container">
            <div v-if="qrCodeLoading" class="loading-container">
              <div class="loading-spinner"></div>
              <div class="loading-text">加载中...</div>
            </div>
            <QrcodeVue v-else-if="qrCodeUrl" :value="qrCodeUrl" :size="160" class="qrcode" />
            <div v-else-if="qrCodeError" class="error-text">{{ qrCodeError }}</div>
          </div>
          <div class="qrcode-tip">请企业HR扫码登录</div>
        </div>
      </div>
    </van-popup>

    <!-- 日志查看弹窗 -->
    <van-popup
      v-model:show="showLogDialog"
      round
      position="center"
      :style="{ width: '80%', maxHeight: '80%' }">
      <div class="log-dialog">
        <div class="log-header">
          <span class="log-title">{{ logType === 'qrcode' ? '二维码日志' : '读卡器日志' }}</span>
          <van-icon name="cross" @click="showLogDialog = false" />
        </div>
        <div class="log-content">
          <pre>{{ logContent }}</pre>
        </div>
        <div class="log-footer">
          <van-button type="primary" size="small" @click="showLogDialog = false">关闭</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import QrcodeVue from 'qrcode.vue';
import { loginApi } from '../services/api';
import { useResumeStore } from '../store/resume';
import { showToast } from 'vant';
import { encryptPassword, getDevScreen } from '@/utils/index.js';

const resumeStore = useResumeStore();

const router = useRouter();

// 屏幕类型
const screenType = ref('');
const deviceId = ref('');

// 登录弹窗状态
const showLoginDialog = ref(false);
const loginType = ref('qrcode');

// 监听登录类型变化
watch(loginType, (newType) => {
  if (newType === 'qrcode') {
    getHRQRCode();
  }
});

// 账号密码登录表单
const loginForm = ref({
  username: '15173608575',
  password: 'Yy147258',
});
const loginLoading = ref(false);

// 扫码登录状态
const qrCodeUrl = ref('');
const qrCodeExpireTime = ref(0);
const qrCodeTimer = ref(null);
const loginPollingTimer = ref(null);
const qrCodeLoading = ref(false);
const qrCodeError = ref('');
const qrUuid = ref('');

// 二维码扫描状态
const qrCodeData = ref('');
const showLogDialog = ref(false);
const logContent = ref('');
const logType = ref('qrcode'); // 'qrcode' | 'cardreader'


// 清除二维码结果
const clearQRCodeResult = () => {
  qrCodeData.value = '';
};

// 查看二维码日志
const viewQRCodeLog = () => {
  logType.value = 'qrcode';
  viewLog();
};

// 查看读卡器日志
const viewCardReaderLog = () => {
  logType.value = 'cardreader';
  viewLog();
};

// 通用查看日志方法
const viewLog = () => {
  try {
    if (window.android) {
      let log = '';
      if (logType.value === 'qrcode' && window.android.getQRCodeLog) {
        log = window.android.getQRCodeLog();
      } else if (logType.value === 'cardreader' && window.android.getCardReaderLog) {
        log = window.android.getCardReaderLog();
      }
      logContent.value = log;
      showLogDialog.value = true;
    } else {
      showToast('设备不支持查看日志');
    }
  } catch (error) {
    console.error('查看日志失败:', error);
    showToast('查看日志失败');
  }
};

// 清除日志
const clearLog = () => {
  try {
    if (window.android) {
      if (window.android.clearQRCodeLog) {
        window.android.clearQRCodeLog();
      }
      if (window.android.clearCardReaderLog) {
        window.android.clearCardReaderLog();
      }
      showToast('日志已清空');
    } else {
      showToast('设备不支持清空日志');
    }
  } catch (error) {
    console.error('清空日志失败:', error);
    showToast('清空日志失败');
  }
};

// 获取HR登录二维码
const getHRQRCode = async () => {
  qrCodeLoading.value = true;
  qrCodeError.value = '';
  try {
    const response = await loginApi.getHRQRCode({ token: resumeStore.schoolToken });
    console.log('获取HR二维码响应:', response);
    if (response.code === 1) {
      qrCodeUrl.value = response.data.url;
      qrCodeExpireTime.value = (response.data.expire_time - 10000) / 1000;
      startQRCodeTimer();
      qrUuid.value = response.data.ticket;
      startLoginPolling();
    } else {
      qrCodeError.value = response.msg || '获取二维码失败';
    }
  } catch (error) {
    console.error('获取二维码失败:', error);
    qrCodeError.value = '获取二维码失败';
  } finally {
    qrCodeLoading.value = false;
  }
};

// 启动二维码过期定时器
const startQRCodeTimer = () => {
  if (qrCodeTimer.value) {
    clearInterval(qrCodeTimer.value);
  }

  qrCodeTimer.value = setInterval(() => {
    qrCodeExpireTime.value--;
    if (qrCodeExpireTime.value <= 0) {
      clearInterval(qrCodeTimer.value);
      qrCodeTimer.value = null;
      getHRQRCode();
    }
  }, 1000);
};

// 启动登录状态轮询
const startLoginPolling = () => {
  if (loginPollingTimer.value) {
    clearInterval(loginPollingTimer.value);
  }

  loginPollingTimer.value = setInterval(async () => {
    try {
      const response = await loginApi.checkHRLoginStatus({ ticket: qrUuid.value });
      if (response.code === 1) {
        if (response.data && response.data.logged_in) {
          handleLoginSuccess();
        }
      } else {
        clearInterval(loginPollingTimer.value);
        showToast(response.msg || '登录失败');
        getHRQRCode();
      }
    } catch (error) {
      console.error('检查登录状态失败:', error);
    }
  }, 2000);
};

// 处理密码登录
const handlePasswordLogin = async () => {
  if (loginLoading.value) {
    return;
  }

  let isValid = true;
  if (!loginForm.value.username) {
    showToast('请输入用户名');
    isValid = false;
  }
  if (!loginForm.value.password) {
    showToast('请输入密码');
    isValid = false;
  }
  if (!isValid) {
    return;
  }

  document.activeElement.blur();
  
  if (window.android && window.android.hideKeyboard) {
    try {
      window.android.hideKeyboard();
    } catch (error) {
      console.error('调用hideKeyboard失败:', error);
    }
  }

  loginLoading.value = true;

  try {
    const encryptedPassword = encryptPassword(loginForm.value.password).encryptedPassword;
    if (!encryptedPassword) {
      showToast('登录失败::密码错误');
      return;
    }
    const bodyData = {
      user_name: loginForm.value.username,
      password: encryptedPassword,
    };
    const response = await loginApi.validateHRLogin(bodyData);
    if (response.code === 1) {
      resumeStore.setCompanyLoginInfo(response.data);
      try {
        localStorage.setItem('companyLoginInfo', JSON.stringify(response.data));
      } catch (storageError) {
        console.error('localStorage存储失败:', storageError);
      }
      handleLoginSuccess();
    } else {
      showToast(response.msg || '登录失败');
    }
  } catch (error) {
    console.error('登录失败:', error);
    showToast('登录失败');
  } finally {
    loginLoading.value = false;
  }
};

// 处理登录成功
const handleLoginSuccess = async () => {
  if (loginPollingTimer.value) {
    clearInterval(loginPollingTimer.value);
    loginPollingTimer.value = null;
  }

  if (qrCodeTimer.value) {
    clearInterval(qrCodeTimer.value);
    qrCodeTimer.value = null;
  }

  showLoginDialog.value = false;

  if (screenType.value === 'secondary') {
    try {
      if (window.android && window.android.notifyMainScreenUpdate) {
        const loginData = {
          type: 'loginSuccess',
          data: resumeStore.companyLoginInfo
        };
        const message = JSON.stringify(loginData);
        console.log('准备发送的消息:', message);
        window.android.notifyMainScreenUpdate(message);
        console.log('副屏登录成功，已通知主屏');
      } else {
        console.error('notifyMainScreenUpdate方法未定义');
      }
    } catch (error) {
      console.error('通知主屏失败:', error);
    }
  }

  setTimeout(() => {
    router.push('/cvList');
  }, 500);
};

// 处理收到的屏幕交互消息
const handleScreenUpdate = (message) => {
  console.log('收到屏幕交互消息:handleScreenUpdate:', message);
  try {
    let cleanedMessage = message;
    cleanedMessage = cleanedMessage.replace(/\\\\/g, '\\');
    const parsedMessage = JSON.parse(cleanedMessage);
    console.log('解析后的消息:', parsedMessage);
    
    if (parsedMessage.type === 'loginSuccess') {
      console.log('收到副屏登录成功消息:', parsedMessage);
      try {
        let companyLoginInfo;
        if (typeof parsedMessage.data === 'string') {
          companyLoginInfo = JSON.parse(parsedMessage.data);
        } else {
          companyLoginInfo = parsedMessage.data;
        }
        console.log('解析后的企业登录信息:', companyLoginInfo);
        localStorage.setItem('companyLoginInfo', JSON.stringify(companyLoginInfo));
        resumeStore.setCompanyLoginInfo(companyLoginInfo);
        console.log('准备跳转到resumeSubmission页面');
        if (router) {
          router.push('/resumeSubmission');
          console.log('跳转命令已执行');
        } else {
          console.error('router未定义，无法跳转');
          window.location.href = '#/resumeSubmission';
          console.log('使用备用方案跳转');
        }
      } catch (dataError) {
        console.error('解析企业登录信息失败:', dataError);
        console.error('原始data字段:', parsedMessage.data);
      }
    }
  } catch (error) {
    console.error('处理屏幕交互消息失败:', error);
    console.error('原始消息:', message);
    try {
      if (message.includes('loginSuccess')) {
        console.log('尝试使用备用方式解析登录成功消息');
        const dataStart = message.indexOf('"data":"') + 7;
        const dataEnd = message.lastIndexOf('"');
        if (dataStart > 7 && dataEnd > dataStart) {
          let dataStr = message.substring(dataStart, dataEnd);
          dataStr = dataStr.replace(/\\"/g, '"');
          dataStr = dataStr.replace(/\\\\/g, '\\');
          console.log('提取的data字符串:', dataStr);
          const companyLoginInfo = JSON.parse(dataStr);
          console.log('解析后的企业登录信息:', companyLoginInfo);
          localStorage.setItem('companyLoginInfo', JSON.stringify(companyLoginInfo));
          resumeStore.setCompanyLoginInfo(companyLoginInfo);
          if (router) {
            router.push('/resumeSubmission');
          } else {
            window.location.href = '#/resumeSubmission';
          }
        }
      }
    } catch (fallbackError) {
      console.error('备用解析方式也失败:', fallbackError);
    }
  }
};

// 处理二维码数据
const handleQRCodeData = (data) => {
  console.log('处理二维码数据:', data);
  qrCodeData.value = data;
  showToast('二维码识别成功');
};

// 处理二维码连接状态
const handleQRCodeConnected = (success) => {
  console.log('二维码设备连接状态:', success);
  if (success) {
    showToast('二维码设备连接成功');
  } else {
    showToast('二维码设备连接失败');
  }
};

onMounted(() => {
  if (window.android && window.android.getDeviceId) {
    const id = window.android.getDeviceId();
    if (id) {
      deviceId.value = id;
      console.log('初始化获取到设备ID:', id);
    }
  }
  
  getDevScreen();
  
  if (window.android && window.android.getScreenType) {
    screenType.value = window.android.getScreenType();
    console.log('Screen type:', screenType.value);

    if (screenType.value === 'secondary') {
      setTimeout(() => {
        showLoginDialog.value = true;
        getHRQRCode();
      }, 1500);
    }
  }

  window.addEventListener('deviceIdReceived', (event) => {
    const id = event.detail;
    deviceId.value = id;
    resumeStore.setDeviceId(id);
    console.log('Home页面收到设备ID:', id);
  });

  window.addEventListener('secondaryScreenLoginSuccess', () => {
    console.log('Home页面收到副屏登录成功通知');
    try {
      console.log('准备跳转到resumeSubmission页面');
      if (router) {
        router.push('/resumeSubmission');
        console.log('跳转命令已执行');
      } else {
        console.error('router未定义，无法跳转');
        window.location.href = '#/resumeSubmission';
        console.log('使用备用方案跳转');
      }
    } catch (error) {
      console.error('跳转失败:', error);
      window.location.href = '#/resumeSubmission';
      console.log('使用备用方案跳转');
    }
  });

  window.addEventListener('cardInfoReceived', (event) => {
    const cardInfo = event.detail;
    console.log('Home页面收到卡片信息:', cardInfo);
  });

  window.addEventListener('statusMessage', (event) => {
    const { message, type } = event.detail;
    console.log('Home页面收到状态信息:', message, type);
  });
  
  window.addEventListener('screenUpdate', (event) => {
    console.log('Home页面收到屏幕更新消息:screenUpdate:', event);
    const message = event.detail;
    handleScreenUpdate(message);
  });

  // 监听二维码相关事件
  window.addEventListener('qrCodeReceived', (event) => {
    console.log('收到二维码事件:', event.detail);
    handleQRCodeData(event.detail);
  });

  window.addEventListener('qrCodeConnected', (event) => {
    console.log('收到二维码连接事件:', event.detail);
    handleQRCodeConnected(event.detail);
  });

  window.addEventListener('qrCodeDisconnected', () => {
    console.log('收到二维码断开事件');
  });
});

onUnmounted(() => {
  if (qrCodeTimer.value) {
    clearInterval(qrCodeTimer.value);
  }
  if (loginPollingTimer.value) {
    clearInterval(loginPollingTimer.value);
  }
});
</script>

<style lang="scss" scoped>
@import '../assets/mixins.scss';
.device-id {
  font-size: 14px;
  color: #333;
  text-align: center;
  margin-top: 20px;
}
.van-popup {
  opacity: 0.85;
}

.home-container {
  height: 100%;
  min-height: 100vh;
  width: 100%;
  background-color: #f8f8f8;
  @include flex-fun(column, flex-start, flex-start);
  background-image: url('../assets/images/home_1.png');
  background-size: 100% 100%;
  background-repeat: no-repeat;
  color: #fff;
  overflow: hidden;
}

.img-box {
  position: relative;
  width: 100%;
  height: 100vh;
  cursor: pointer;
}

.home-image-logo {
  position: absolute;
  width: 240px;
  left: 40px;
  top: 40px;
}

.home-image-text {
  position: absolute;
  width: 600px;
  left: 40px;
  top: 80px;
}

/* 登录弹窗样式 */
.login-dialog {
  width: 400px;
  padding: 20px;
  margin: 0 auto;
}

.dialog-title {
  font-size: 18px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}

.login-tabs {
  display: flex;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.tab-item {
  flex: 1;
  padding: 10px;
  text-align: center;
  cursor: pointer;
  color: #666;
  transition: all 0.3s;
  position: relative;

  &.active {
    color: #1989fa;
    font-weight: bold;
  }

  &.active::after {
    content: '';
    position: absolute;
    bottom: -1px;
    left: 0;
    right: 0;
    height: 2px;
    background-color: #1989fa;
  }
}

.login-form {
  margin-bottom: 20px;
}

.login-btn {
  margin-top: 20px;
  width: 100%;
}

.qrcode-login {
  text-align: center;
  padding: 20px 0;
}

.qrcode-container {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 160px;
}

.qrcode {
  margin: 0 auto;
}

.loading-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 14px;
  color: #666;
}

.error-text {
  font-size: 14px;
  color: #ff4d4f;
  text-align: center;
  padding: 20px;
}

.qrcode-tip {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.expire-time {
  font-size: 12px;
  color: #999;
}

.login-error {
  font-size: 14px;
  color: #ff4d4f;
  text-align: center;
  margin-top: 10px;
}

/* 功能调试区域样式 */
.debug-section {
  position: fixed;
  bottom: 20px;
  left: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.debug-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.debug-buttons {
  display: flex;
  gap: 8px;
}

.status-row {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  
  &.connected {
    color: #07c160;
    .status-dot {
      background-color: #07c160;
    }
  }
  
  &.disconnected {
    color: #999;
    .status-dot {
      background-color: #999;
    }
  }
  
  &.scanning {
    color: #1989fa;
  }
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.scan-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #1989fa;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0.3;
  }
}

.scan-result {
  margin-top: 12px;
  padding: 12px;
  background-color: #f5f5f5;
  border-radius: 8px;
}

.result-title {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.result-content {
  font-size: 14px;
  color: #333;
  word-break: break-all;
  margin-bottom: 12px;
}

/* 日志弹窗样式 */
.log-dialog {
  display: flex;
  flex-direction: column;
  max-height: 80vh;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.log-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.log-content {
  padding: 16px;
  overflow-y: auto;
  flex: 1;
  max-height: 60vh;
  background-color: #f5f5f5;
  border-radius: 4px;
  margin: 16px;
}

.log-content pre {
  font-size: 12px;
  color: #333;
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
  font-family: 'Courier New', monospace;
}

.log-footer {
  padding: 16px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: center;
}
</style>