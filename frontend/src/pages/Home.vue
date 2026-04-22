<template>
  <div class="home-container">
    <div class="img-box" @click="handleRoute">
      <img src="../assets/images/home_2.png" class="home-image-logo" alt="logo" />
      <img src="../assets/images/home_3.png" class="home-image-text" alt="text" />
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
          <van-field v-model="loginForm.username" label="用户名" placeholder="请输入用户名" />
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
const deviceId = ref('0513201062000003180902854042382');

// 登录弹窗状态
const showLoginDialog = ref(false);
const loginType = ref('password'); // 'password' 或 'qrcode'

// 监听登录类型变化
watch(loginType, (newType) => {
  if (newType === 'qrcode') {
    // 切换到扫码登录时获取二维码
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

const handleRoute = () => {
  // 调用 Android 原生方法获取屏幕类型
  if (window.android && window.android.getScreenType) {
    screenType.value = window.android.getScreenType();
    console.log('Screen type:', screenType.value);

    // 根据屏幕类型跳转到不同页面
    if (screenType.value === 'main') {
      // 主屏跳转到 resumeSubmission
      router.push('/resumeSubmission');
    }
  } else {
    // 如果没有 Android 原生方法，默认跳转到 cvList
    router.push('/resumeSubmission');
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
      // 计算二维码过期时间，减少10秒
      qrCodeExpireTime.value = (response.data.expire_time - 10000) / 1000;
      // 启动二维码过期定时器
      startQRCodeTimer();
      qrUuid.value = response.data.ticket;
      // 启动登录状态轮询
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
  // 清除之前的定时器
  if (qrCodeTimer.value) {
    clearInterval(qrCodeTimer.value);
  }

  // 设置定时器，当二维码过期时重新获取
  qrCodeTimer.value = setInterval(() => {
    qrCodeExpireTime.value--;
    if (qrCodeExpireTime.value <= 0) {
      clearInterval(qrCodeTimer.value);
      qrCodeTimer.value = null;
      // 重新获取二维码
      getHRQRCode();
    }
  }, 1000);
};

// 启动登录状态轮询
const startLoginPolling = () => {
  // 清除之前的轮询
  if (loginPollingTimer.value) {
    clearInterval(loginPollingTimer.value);
  }

  // 设置轮询，每2秒检查一次登录状态
  loginPollingTimer.value = setInterval(async () => {
    try {
      const response = await loginApi.checkHRLoginStatus({ ticket: qrUuid.value });
      if (response.code === 1) {
        // 检查登录状态
        if (response.data && response.data.logged_in) {
          // 登录成功
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
  // 表单验证
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
    console.log('登录请求数据:', bodyData);
    const response = await loginApi.validateHRLogin(bodyData);
    console.log('密码登录响应:', response);
    if (response.code === 1) {
      // 存储企业信息
      resumeStore.setCompanyLoginInfo(response.data);
      try {
        localStorage.setItem('companyLoginInfo', JSON.stringify(response.data));
        handleLoginSuccess();
      } catch (storageError) {
        console.error('localStorage存储失败:', storageError);
        // 即使存储失败也继续登录流程
        handleLoginSuccess();
      }
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
  // 停止轮询
  if (loginPollingTimer.value) {
    clearInterval(loginPollingTimer.value);
    loginPollingTimer.value = null;
  }

  // 停止二维码过期定时器
  if (qrCodeTimer.value) {
    clearInterval(qrCodeTimer.value);
    qrCodeTimer.value = null;
  }

  // 关闭登录弹窗
  showLoginDialog.value = false;

  // 检查当前是否为副屏
  if (screenType.value === 'secondary') {
    // 副屏登录成功，通知主屏
    try {
      if (window.android && window.android.notifyMainScreenLoginSuccess) {
        window.android.notifyMainScreenLoginSuccess();
        console.log('副屏登录成功，已通知主屏');
      }
    } catch (error) {
      console.error('通知主屏失败:', error);
    }
  }

  // 跳转到cvList页面
  setTimeout(() => {
    router.push('/cvList');
  }, 1000);
  // router.push('/resumeSubmission');
};
// 处理双屏不能互通的问题
const localhostChange = () => {
  let timer = setInterval(() => {
    try {
      const companyLoginInfo = localStorage.getItem('companyLoginInfo');
      if (companyLoginInfo) {
        clearInterval(timer);
        timer = null;
        router.push('/resumeSubmission');
      }
    } catch (error) {
      console.log('111', error)
    }
  }, 1500);
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
      // 副屏显示登录弹窗
      setTimeout(() => {
        showLoginDialog.value = true;
      }, 3000);
    } else {
      localhostChange();
    }
    // else {
    //   // 其他屏幕直接跳转
    //   setTimeout(() => {
    //    handleRoute();
    //   }, 3000);
    // }
  }

  // 监听设备ID事件
  window.addEventListener('deviceIdReceived', (event) => {
    const id = event.detail;
    deviceId.value = id;
    resumeStore.setDeviceId(id);
    console.log('Home页面收到设备ID:', id);
  });

  // 监听副屏登录成功事件
  window.addEventListener('secondaryScreenLoginSuccess', () => {
    console.log('Home页面收到副屏登录成功通知');
    // 跳转到resumeSubmission页面
    try {
      console.log('准备跳转到resumeSubmission页面');
      if (router) {
        router.push('/resumeSubmission');
        console.log('跳转命令已执行');
      } else {
        console.error('router未定义，无法跳转');
        // 备用方案：使用window.location.href
        window.location.href = '#/resumeSubmission';
        console.log('使用备用方案跳转');
      }
    } catch (error) {
      console.error('跳转失败:', error);
      // 备用方案：使用window.location.href
      window.location.href = '#/resumeSubmission';
      console.log('使用备用方案跳转');
    }
  });

  // 监听卡片信息事件
  window.addEventListener('cardInfoReceived', (event) => {
    const cardInfo = event.detail;
    console.log('Home页面收到卡片信息:', cardInfo);
    // 这里可以添加处理卡片信息的逻辑
  });

  // 监听状态信息事件
  window.addEventListener('statusMessage', (event) => {
    const { message, type } = event.detail;
    console.log('Home页面收到状态信息:', message, type);
    // 这里可以添加处理状态信息的逻辑
  });
});

onUnmounted(() => {
  // 清除定时器
  if (qrCodeTimer.value) {
    clearInterval(qrCodeTimer.value);
  }
  if (loginPollingTimer.value) {
    clearInterval(loginPollingTimer.value);
  }
});
</script>

<style lang="scss" scoped>
.device-id {
  font-size: 14px;
  color: #333;
  text-align: center;
  margin-top: 20px;
}
@import '../assets/mixins.scss';
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
</style>
