<template>
  <div class="page-container">
    <div class="headers">
      <div class="company-logo">
        <img :src="companyInfo?.logo_url" alt="公司logo" class="logo-image" />
      </div>
      <div class="company-name">{{ companyInfo?.company_name }}</div>
    </div>

    <div class="content">
      <!-- 左-公司基本信息 -->
      <div class="col left-col">
        <!-- 第一个卡片：公司信息介绍 -->
        <div class="card first-card">
          <div class="info-header" @click="handleHideConsole">
            <div class="info-logo">
              <img :src="companyInfo?.logo_url" alt="公司logo" class="logo-image" />
            </div>
            <span class="info-title">{{ companyInfo?.company_name }}</span>
          </div>

          <div class="company-details">
            <div class="detail-item">
              <span class="label">单位行业：</span
              ><span>{{ companyInfo?.company_property || '' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">单位性质：</span
              ><span>{{ companyInfo?.industry_category || '' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">单位规模：</span><span>{{ companyInfo?.scale || '' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">单位地址：</span>
              <span>{{ companyInfo?.address || '' }}</span>
            </div>
          </div>

          <div class="circle-wrap">
            <CircleProgress :percentage="companyReputation?.percent_beat || 0" type="arc">
              <div class="circle-box">
                <div class="circle-content">
                  <div class="rating-img">
                    <img src="../assets/svgs/qx.svg" alt="qr" class="svg" />
                    <div class="rating-value">{{ companyReputation?.level_name || '' }}</div>
                  </div>
                  <span class="rating-text">云研企信等级</span>
                  <span class="rating-desc"
                    >超越
                    <span class="highlight">{{ companyReputation?.percent_beat || '' }}%</span>
                    的校园招聘企业</span
                  >
                </div>
              </div>
            </CircleProgress>
          </div>
        </div>

        <!-- 第二个卡片：二维码 -->
        <div class="card second-card">
          <!-- 登录成功状态 -->
          <div v-if="isStudentLoggedIn" class="login-success">
            <div class="success-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="48"
                height="48"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
                style="color: #4caf50">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
              </svg>
            </div>
            <div class="success-text">学生已登录</div>
            <div class="logout-btn" @click="handleStudentLogout">退出登录</div>
          </div>

          <!-- 二维码状态 -->
          <div v-else>
            <div class="qr-placeholder">
              <div class="qr-box" @click="getQRCode">
                <div class="corner-left-top"></div>
                <div class="corner-right-top"></div>
                <div class="corner-left-bottom"></div>
                <div class="corner-right-bottom"></div>
                <div v-if="qrCodeLoading" class="loading-container">
                  <div class="loading-spinner"></div>
                  <div class="loading-text">加载中...</div>
                </div>
                <QrcodeVue v-else-if="qrCodeUrl" :value="qrCodeUrl" :size="120" class="qrcode" />
                <div v-else-if="qrCodeError" class="error-text">{{ qrCodeError }}</div>
                <img v-else src="../assets/images/test-code.png" class="img" alt="二维码" />
              </div>
            </div>
            <!-- 下方提示 -->
            <div class="tip-section">
              <div class="tip-img">
                <img src="../assets/images/sm.png" class="img" alt="sm" />
              </div>
              <span class="tip-text">学生，请扫码</span>
              <span class="tip-text">HR屏可弹出电子简历</span>
            </div>
          </div>
        </div>
      </div>
      <!-- 中-公司简介和招聘简章 -->
      <div class="col middle-col">
        <div class="section-title">单位简介</div>
        <div class="section-box introduction-box" v-html="companyInfo?.introduction"></div>
        <div class="section-title">招聘简章</div>
        <div
          class="section-box recruitment-brochure-box"
          v-html="companyInfo?.recruitment_brochure"></div>
      </div>
      <!-- 右-招聘岗位 -->
      <div class="col right-col">
        <div class="section-title">招聘职位</div>
        <div class="section-list-box">
          <div class="job-item" v-for="(job, idx) in jobs" :key="idx" @click="handleJobClick(job)">
            <div class="job-title">
              <div class="text-overflow">{{ job.job_name }}</div>
              <div class="job-salary">{{ job.salary }}</div>
            </div>
            <div class="job-info">
              <div>
                <div>
                  招聘人数：{{ job.job_number }} | {{ job.degree_require }} | {{ job.city_name }}
                </div>
                <div>招聘专业：{{ job.about_major }}</div>
              </div>
              <div class="apply-btn">投递简历</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <van-popup v-model:show="dialogPopupOptions.show" round :close-on-click-overlay="false">
      <div class="dialog-content" :style="{ width: dialogPopupOptions.width }">
        <div class="dialog-title">
          <div class="text-title">{{ dialogPopupOptions.title }}</div>
          <img
            v-if="dialogPopupOptions.type !== 3"
            src="../assets/svgs/close.svg"
            class="svg-icon"
            @click="closeDialog" />
        </div>
        <template v-if="dialogPopupOptions.type === 1">
          <van-checkbox-group
            v-model="checkboxValue"
            activeColor="#ffffff"
            iconColor="#0080FF"
            @change="handleCheckboxChange">
            <div class="section-list-box">
              <div
                :class="['job-item', { checked: job.isChecked }]"
                v-for="(job, idx) in jobs"
                :key="idx"
                @click="handleJobCheckSelect(idx)">
                <div class="check-sele">
                  <van-checkbox :name="idx" customStyle="margin-top: 2px;" @click.stop />
                </div>
                <div style="flex: 1; width: 280px">
                  <div class="job-title">
                    <div class="text-overflow">{{ job.job_name }}</div>
                    <div class="job-salary">{{ job.salary }}</div>
                  </div>
                  <div class="job-info">
                    <div>
                      <div>
                        招聘人数：{{ job.job_number }} | {{ job.degree_require }} |
                        {{ job.city_name }}
                      </div>
                      <div>招聘专业：{{ job.about_major }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </van-checkbox-group>
          <div class="dialog-footer">
            <div class="btn-delivery" @click="deliveryFun">投递简历</div>
          </div>
        </template>
        <template v-else-if="dialogPopupOptions.type === 2">
          <div class="cv-list">
            <div class="cv-item" v-for="cv in cvList" :key="cv.id">
              <div class="cv-info text-overflow">
                <div v-if="cv.is_default === '1'" class="default-tag">默认</div>
                <div class="cv-name">{{ cv.title }}</div>
              </div>
              <div class="delivery-btn" @click="handleCVDelivery(cv)">投递</div>
            </div>
          </div>
        </template>
        <template v-else-if="dialogPopupOptions.type === 3">
          <div class="delivery-tip-content">
            <div class="success-message">
              <div class="success-text">简历投递成功！</div>
              <div class="tip-text">
                继续投递简历，请点击<text class="highlight">【继续投递简历】</text>
              </div>
              <div class="tip-text">
                若不再投递简历，请点击<text class="highlight">【退出登录】</text>或
                <text class="time-text">{{ timer }}S</text> 后系统自动退出登录
              </div>
            </div>
            <div class="tip-buttons">
              <div class="btn-exit" @click="handleExitLogin">退出登录</div>
              <div class="btn-continue" @click="handleContinueDelivery">继续投递简历</div>
            </div>
          </div>
        </template>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useResumeStore } from '../store/resume';
import { useRouter } from 'vue-router';
import { cardReaderApi, loginApi } from '../services/api';
import CircleProgress from '../components/CircleProgress.vue';
import QrcodeVue from 'qrcode.vue';
import { showToast } from 'vant';
import { getDevScreen } from '@/utils/index.js';

const resumeStore = useResumeStore();
const router = useRouter();
const checkboxValue = ref([]);
const timer = ref(0);
const timeNum = ref(null);
const schoolId = ref(resumeStore.schoolId);
const dialogPopupOptions = ref({
  title: '',
  type: 1,
  width: '711px',
  show: false,
});
const deviceId = ref(resumeStore.deviceId);

// 卡片信息相关状态
const cardInfo = ref(null);
const testStatus = ref('');

// 二维码登录相关状态
const qrCodeUrl = ref('');
const qrCodeExpireTime = ref(0);
const qrCodeTimer = ref(null);
const loginPollingTimer = ref(null);
const isStudentLoggedIn = ref(false);
const qrCodeLoading = ref(false);
const qrCodeError = ref('');
const qrUuid = ref('');

// 企业信息
const companyInfo = ref(null);
const positionCategories = ref([]);
const companyReputation = ref(null);
const jobs = ref(null);

// 学生信息
const studentInfo = ref(
  null,
  //   {
  //   student_id: 75047917,
  //   student_key: '1dd3c08e2831bebad1d59df9962b4f11',
  //   student_no: '123456',
  //   user_name: '魏振',
  // }
);
// 目标职位
const targetJobs = ref([]);
// 简历列表
const cvList = ref([
  {
    resume_id: '243438',
    title: '我的简历20250920',
    percent_complete: '85',
    modify_time: '2026-04-02 14:55:55',
    is_default: '1',
    thumb_url: 'https://o.bysjy.com.cn/kzp/template/1626314203-9234.png',
    resume_thumb_url: '',
  },
]);

// 监听卡片信息事件
const handleCardInfoReceived = async (event) => {
  console.log('收到卡片信息:', event.detail);
  const cardInfo = event.detail;
  if (cardInfo && cardInfo.idNumber) {
    try {
      // 调用身份证登录接口
      const response = await loginApi.loginByIdCard({
        school_id: '120222199611296435' || schoolId.value,
        id_card_no: cardInfo.idNumber,
      });
      console.log('身份证登录响应:', response);
      if (response.code === 1) {
        // 登录成功，更新学生信息
        studentInfo.value = response.data.student_info;
        isStudentLoggedIn.value = true;
        showToast('登录成功');
        // 停止二维码登录轮询
        if (loginPollingTimer.value) {
          clearInterval(loginPollingTimer.value);
          loginPollingTimer.value = null;
        }
        // 登录成功后，拉取数据
        handleLoginSuccess();
      } else {
        showToast('登录失败：' + response.msg);
      }
    } catch (error) {
      console.error('身份证登录失败:', error);
      showToast('登录失败，请稍后重试');
    }
  }
};

// 监听状态信息事件
const handleStatusMessage = (event) => {
  console.log('收到状态信息:', event.detail);
  // 可以在这里处理状态信息，比如显示toast等
};
// 副屏企业登录成功
const handleSecondaryScreenLoginSuccess = () => {
  console.log('副屏企业登录成功');
  pageInfo();
};

// 处理设备ID事件
const handleDeviceIdReceived = (event) => {
  const id = event.detail;
  deviceId.value = id;
  resumeStore.setDeviceId(id);
  console.log('ResumeSubmission页面收到设备ID:', id);
};

// 处理屏幕更新事件
const handleScreenUpdate = (message) => {
  console.log('收到屏幕更新消息:', message);
  try {
    // 要解析2次
    const zhstr = JSON.parse(`"${message}"`);
    // 解析消息
    const parsedMessage = JSON.parse(zhstr);
    if (parsedMessage.type === 'logout') {
      // 处理退出登录消息
      console.log('收到退出登录消息');
      // 清空localStorage内容
      localStorage.setItem('companyLoginInfo', '');
      // 跳转到home.vue页面
      console.log('准备跳转到home页面');
      if (router) {
        router.replace('/');
        console.log('跳转命令已执行');
      } else {
        console.error('router未定义，无法跳转');
        // 备用方案：使用window.location.href
        window.location.href = '#/';
        console.log('使用备用方案跳转');
      }
    }
  } catch (error) {
    console.error('处理屏幕更新消息失败:', error);
  }
};

onMounted(async () => {
  // 监听卡片信息事件
  window.addEventListener('cardInfoReceived', handleCardInfoReceived);
  window.addEventListener('statusMessage', handleStatusMessage);
  window.addEventListener('secondaryScreenLoginSuccess', handleSecondaryScreenLoginSuccess);
  window.addEventListener('deviceIdReceived', handleDeviceIdReceived);

  // 在全局作用域中定义onScreenUpdate函数，确保原生代码可以调用
  if (typeof window !== 'undefined') {
    window.onScreenUpdate = function (message) {
      console.log('原生调用onScreenUpdate:', message);
      // 直接处理消息
      handleScreenUpdate(message);
    };
  }

  getDevScreen();
  await pageInfo();
  // 检查是否已有存储的卡片信息
  if (window.cardInfo) {
    console.log('window.cardInfo', window.cardInfo);
    cardInfo.value = window.cardInfo;
  }

  // 获取登录二维码
  if (companyInfo.value) {
    getQRCode();
  }
});

onUnmounted(() => {
  // 移除事件监听
  window.removeEventListener('cardInfoReceived', handleCardInfoReceived);
  window.removeEventListener('statusMessage', handleStatusMessage);
  window.removeEventListener('secondaryScreenLoginSuccess', handleSecondaryScreenLoginSuccess);
  window.removeEventListener('deviceIdReceived', handleDeviceIdReceived);
  // 移除屏幕更新事件监听
  window.removeEventListener('screenUpdate', (event) => {
    handleScreenUpdate(event.detail);
  });

  // 清除定时器
  if (qrCodeTimer.value) {
    clearInterval(qrCodeTimer.value);
  }
  if (loginPollingTimer.value) {
    clearInterval(loginPollingTimer.value);
  }
});

const pageInfo = async () => {
  if (!deviceId.value) {
    if (window.android && window.android.getDeviceId) {
      resumeStore.setDeviceId(window.android.getDeviceId());
      if (id) {
        deviceId.value = id;
        resumeStore.setDeviceId(id);
        console.log('初始化获取到设备ID:', id);
      }
    }
  }
  const companyLoginInfoStr = localStorage.getItem('companyLoginInfo');
  console.log('companyLoginInfoStr', companyLoginInfoStr);
  if (companyLoginInfoStr) {
    const companyLoginInfo = JSON.parse(companyLoginInfoStr);
    const res = await loginApi.getCompanyDetail({
      device_id: deviceId.value,
      company_id: companyLoginInfo.company_id,
      school_id: resumeStore.schoolId,
      page: 1,
      page_size: 9999,
    });
    if (res.code === 1) {
      companyInfo.value = res.data.company;
      positionCategories.value = res.data.jobs.items;
      companyReputation.value = res.data.credit;
      jobs.value = res.data.jobs.items;
    }
    console.log(res);
  }
};

const openDialog = (type) => {
  if (type === 1) {
    dialogPopupOptions.value.title = '请选择你需要投递的职位';
    dialogPopupOptions.value.type = 1;
    checkboxValue.value = [];
    dialogPopupOptions.value.width = '711px';
    // 初始化职位的isChecked属性
    if (jobs.value) {
      jobs.value.forEach((item, index) => {
        if (!item.hasOwnProperty('isChecked')) {
          item.isChecked = false;
        }
        if (item.isChecked) {
          checkboxValue.value.push(index);
        }
      });
    }
  } else if (type === 2) {
    dialogPopupOptions.value.title = '选择简历';
    dialogPopupOptions.value.type = 2;
    dialogPopupOptions.value.width = '430px';
  } else {
    dialogPopupOptions.value.title = '投递简历提示';
    dialogPopupOptions.value.type = 3;
    dialogPopupOptions.value.width = '400px';
    timer.value = 60;
    if (timeNum.value) {
      clearInterval(timeNum.value);
      timeNum.value = null;
    }
    timeNum.value = setInterval(() => {
      timer.value--;
      if (timer.value <= 0) {
        clearInterval(timeNum.value);
        timeNum.value = null;
        timer.value = 60;
        handleExitLogin();
      }
    }, 1000);
  }
  dialogPopupOptions.value.show = true;
  console.log('dialogPopupOptions', dialogPopupOptions.value);
};
// 投递简历
const handleJobClick = (job) => {
  // 如果没有学生登录则提示用户登录
  if (!studentInfo.value) {
    showToast('请先登录');
    return;
  }
  // 存储选中的职位
  targetJobs.value = [job];
  // 打开简历选择弹窗
  openDialog(2);
};

const handleCVDelivery = async (cv) => {
  try {
    // 准备投递参数
    const jobIds = targetJobs.value.map((job) => job.publish_id);
    const response = await loginApi.batchDelivery({
      device_id: deviceId.value,
      resume_id: cv.resume_id,
      job_ids: jobIds.join(','),
      school_id: schoolId.value,
      student_id: studentInfo.value.student_id,
      student_key: studentInfo.value.student_key,
    });
    if (response.code === 1) {
      // 投递成功，打开投递成功弹窗
      if (response.data.fail_count === 0) {
        openDialog(3);
        // 通知副屏更新数据
        try {
          if (window.android && window.android.notifySecondaryScreenUpdate) {
            window.android.notifySecondaryScreenUpdate('resume_delivered');
            console.log('已通知副屏更新数据');
          }
        } catch (notifyError) {
          console.error('通知副屏失败:', notifyError);
        }
      } else {
        response.data.details.forEach((item) => {
          if (item.status === 'fail') {
            showToast(item.msg || '投递失败');
          }
        });
      }
    } else {
      // 投递失败，提示用户
      showToast('投递失败：' + response.msg);
    }
  } catch (error) {
    console.error('投递简历失败:', error);
    showToast('投递失败，请稍后重试');
  }
};
// 弹窗退出登录按钮
const handleExitLogin = () => {
  if (timeNum.value) {
    clearInterval(timeNum.value);
    timeNum.value = null;
  }
  studentInfo.value = null;
  getQRCode();
  closeDialog();
  console.log('退出登录');
};

const handleContinueDelivery = () => {
  console.log('继续投递简历');
  closeDialog();
};
const closeDialog = () => {
  dialogPopupOptions.value.show = false;
};
// 选择职位
const handleJobCheckSelect = (index) => {
  jobs.value[index].isChecked = !jobs.value[index].isChecked;
  const idx = checkboxValue.value.indexOf(index);
  if (idx > -1) {
    checkboxValue.value.splice(idx, 1);
  } else {
    checkboxValue.value.push(index);
  }
};
// 设置选择样式
const handleCheckboxChange = (val) => {
  jobs.value.forEach((item, index) => {
    item.isChecked = val.includes(index);
  });
};

// 投递简历
const deliveryFun = () => {
  // 获取选中的职位
  const selectedJobs = jobs.value.filter((job, index) => checkboxValue.value.includes(index));
  if (selectedJobs.length === 0) {
    // 如果没有选择职位，提示用户
    showToast('请选择至少一个职位');
    return;
  }
  // 存储选中的职位
  targetJobs.value = selectedJobs;
  // 打开简历选择弹窗
  openDialog(2);
};

// 确认卡片信息
const handleConfirmCardInfo = () => {
  console.log('确认卡片信息:', cardInfo.value);
};

// 获取登录二维码
const getQRCode = async () => {
  qrCodeLoading.value = true;
  qrCodeError.value = '';
  try {
    const response = await loginApi.getQRCode({ token: schoolId.value });
    console.log('获取二维码响应:', response);
    if (response.code === 1) {
      // 直接使用API返回的URL作为二维码值
      qrCodeUrl.value = response.data.url;
      // 计算二维码过期时间，减少10秒
      qrCodeExpireTime.value = (response.data.expire_time - 10000) / 1000;
      // 启动二维码过期定时器
      startQRCodeTimer();
      qrUuid.value = response.data.uuid;
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
      getQRCode();
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
      const response = await loginApi.checkLoginStatus({ uuid: qrUuid.value });
      console.log('检查登录状态响应:', response);
      // 测试用,写死
      // response.code = 1;
      if (response.code === 1) {
        // 检查登录状态
        // 测试用,写死,正确为status为4,登录成功
        if (response.data && response.data.status === 4) {
          // 登录成功
          isStudentLoggedIn.value = true;
          // studentInfo.value = response.data.student_info;
          // 停止轮询
          clearInterval(loginPollingTimer.value);
          loginPollingTimer.value = null;
          handleLoginSuccess();
        }
      }
    } catch (error) {
      console.error('检查登录状态失败:', error);
    }
  }, 2000000);
};

// 登录成功后，拉取数据
const handleLoginSuccess = async () => {
  try {
    // 获取学生简历列表
    const response = await loginApi.getStudentResumes({
      device_id: deviceId.value,
      school_id: schoolId.value,
      student_id: studentInfo.value.student_id,
      student_key: studentInfo.value.student_key,
    });
    if (response.code === 1) {
      // 存储简历列表
      cvList.value = response.data;
      console.log('cvList', cvList.value);
      // 打开职位选择弹窗
      openDialog(1);
    }
  } catch (error) {
    console.error('获取简历列表失败:', error);
  }
};

// 处理学生退出登录
const handleStudentLogout = () => {
  isStudentLoggedIn.value = false;
  // 重新获取二维码
  getQRCode();
};
const handleHideConsole = () => {
  window.vconsole.destroy();
};
</script>

<style lang="scss" scoped>
@import '../assets/mixins.scss';

.page-container {
  @include flex-fun(column, flex-start, flex-start);
}

.headers {
  width: 100%;
  @include flex-fun(row, flex-start, center);
  padding: 10px 20px 0;
  background-color: #fff;
  flex-shrink: 0;
}

.company-logo {
  .logo-image {
    width: auto;
    max-width: 150px;
    max-height: 60px;
  }
}

.company-name {
  font-size: 16px;
  position: relative;
  padding-left: 48px;
}

.company-name::before {
  content: '';
  position: absolute;
  left: 24px;
  top: 50%;
  transform: translateY(-50%);
  background-color: #dddddd;
  width: 1px;
  height: 14px;
}

.content {
  width: 100%;
  flex: 1;
  box-sizing: border-box;
  @include flex-fun(row, flex-start, stretch);
  padding: 12px 15px;
}

.col {
  background-color: #fff;
  border: 1px solid #e8ecf4;
  border-radius: 20px;
  box-sizing: border-box;
  min-height: 600px;
}

.left-col {
  background: none;
  border: none;
  width: 320px;
  display: flex;
  flex-direction: column;
  margin-right: 10px;
}

.card {
  background-color: #fff;
  border: 1px solid #e8ecf4;
  border-radius: 10px;
  padding: 20px;
  box-sizing: border-box;
}

.first-card {
  padding: 14px 20px;
}

.circle-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 4px;
  margin-bottom: -50px;
}

.second-card {
  flex-grow: 1;
  padding: 16px 20px;
  margin-top: 10px;
}

.circle-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  margin-top: -12px;
}

.rating-img {
  width: 100%;
  height: 40px;
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
}

.svg {
  width: 40px;
  height: 100%;
}

.rating-value {
  font-size: 32px;
  font-weight: 600;
  color: #e98f36;
}

.rating-text {
  font-size: 12px;
  color: #555555;
}

.rating-desc {
  font-size: 12px;
  color: #555555;
  text-align: center;
}

.highlight {
  color: #e98f36;
}

.info-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding-bottom: 12px;
  border-bottom: 1px dashed #eeeeee;
}

.info-logo {
  width: 48px;
  height: 48px;
  border: 1px solid #eeeeee;
  overflow: hidden;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 14px;
  .logo-image {
    width: 100%;
  }
}

.info-title {
  font-size: 16px;
  font-weight: 700;
  color: #161c2b;
  text-align: center;
  line-height: 1.3;
}

.company-details {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-item {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  font-size: 13px;
  color: #555555;
  line-height: 1.4;
  word-wrap: break-word;
}

.label {
  margin-right: 4px;
  white-space: nowrap;
}

.qr-placeholder {
  display: flex;
  justify-content: center;
  background-color: #e2f1ff;
  border-radius: 6px;
  width: 160px;
  height: 160px;
  margin: 0 auto;
  align-items: center;
}

.qr-box {
  width: 140px;
  height: 140px;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #a8b4c7;
  font-size: 12px;
  background-color: #fff;
  overflow: hidden;
}

.img {
  width: 100px;
  height: 100px;
}

.qr-box .corner-left-top,
.qr-box .corner-right-top,
.qr-box .corner-left-bottom,
.qr-box .corner-right-bottom {
  position: absolute;
  border: 1px solid #0180ff;
  border-width: 0;
  width: 10px;
  height: 10px;
}

.qr-box .corner-left-top {
  left: 0;
  top: 0;
  border-top-width: 2px;
  border-left-width: 2px;
}

.qr-box .corner-right-top {
  right: 0;
  top: 0;
  border-top-width: 2px;
  border-right-width: 2px;
}

.qr-box .corner-left-bottom {
  left: 0;
  bottom: 0;
  border-bottom-width: 2px;
  border-left-width: 2px;
}

.qr-box .corner-right-bottom {
  right: 0;
  bottom: 0;
  border-bottom-width: 2px;
  border-right-width: 2px;
}

.tip-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
}

.tip-img {
  width: 40px;
  height: 40px;
  background-color: #0080ff;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #a8b4c7;
  font-size: 0;
}

.tip-img .img {
  width: 24px;
  height: 24px;
}

.tip-text {
  font-size: 12px;
  color: #666;
}

.expire-time {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}

/* 登录成功状态样式 */
.login-success {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px 0;
  text-align: center;
}

.success-icon {
  margin-bottom: 16px;
}

.success-text {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

.logout-btn {
  background-color: #0080ff;
  color: #ffffff;
  padding: 8px 24px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.logout-btn:hover {
  background-color: #0066cc;
}

/* 加载状态样式 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100px;
}

.loading-spinner {
  width: 30px;
  height: 30px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #0080ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 8px;
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
  font-size: 12px;
  color: #666;
}

.error-text {
  font-size: 12px;
  color: #ff4d4f;
  text-align: center;
  padding: 20px;
}

.qrcode {
  margin: 10px auto;
}

.middle-col {
  padding: 12px;
  margin-right: 10px;
  flex-basis: 38%;
  flex-grow: 1;
  flex-shrink: 1;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  background-color: #f7fbff;
  height: 28px;
  line-height: 28px;
  padding: 0 26px;
  position: relative;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  background-color: #0080ff;
  width: 2px;
  height: 11px;
}

.section-box {
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  color: #4a5768;
  line-height: 1.6;
  white-space: pre-wrap;

  &.introduction-box {
    height: 300px;
    overflow-y: auto;
  }
  &.recruitment-brochure-box {
    height: 260px;
    overflow-y: auto;
    margin-bottom: 0;
  }
}

.right-col {
  padding: 12px;
  flex-basis: 38%;
  flex-grow: 1;
  flex-shrink: 1;
}

.section-list-box {
  height: 610px;
  overflow-y: auto;
}

.job-item {
  border: 1px solid #e2f1ff;
  border-radius: 8px;
  padding: 14px;
  margin-top: 16px;
  background-color: #f7fbff;
  .check-sele {
    padding-right: 8px;
  }
}

.job-title {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  min-width: 0;
  font-weight: 600;
  color: #333333;
  margin-bottom: 6px;
}

.text-overflow {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding-right: 6px;
  display: inline-block;
}

.job-salary {
  flex-shrink: 0;
  font-size: 14px;
  color: #1171ff;
  font-weight: 600;
}

.job-info {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #555555;
}

.apply-btn {
  background-color: #0080ff;
  color: #fff;
  border: none;
  padding: 14px 20px;
  border-radius: 4px;
  font-size: 12px;
  flex-shrink: 0;
}

.dialog-content {
  max-height: 92vh;
  min-height: 200px;

  .dialog-title {
    @include flex-fun(row, space-between, center);
    font-size: 16px;
    font-weight: 600;
    padding: 10px;
    border-bottom: 1px solid #eeeeee;

    .text-title {
      flex: 1;
      text-align: center;
    }

    .svg-icon {
      width: 16px;
      height: 16px;
    }
  }

  .section-list-box {
    // 2列布局
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    grid-auto-rows: min-content;
    padding: 10px;
    max-height: 580px;
    height: auto;
    overflow: auto;
    gap: 10px;
  }

  .job-item {
    border: 1px solid #e2f1ff;
    border-radius: 8px;
    padding: 14px;
    background-color: #f7fbff;
    width: 100%;
    box-sizing: border-box;
    @include flex-fun(row, flex-start);
    overflow: hidden;
    margin-top: 0;

    &.checked {
      color: #fff;
      border-color: #0080ff;
      background-color: #0080ff;

      .job-title,
      .job-salary,
      .job-info {
        color: #fff !important;
      }
    }

    .job-title {
      @include flex-fun(row, space-between, center);
      font-size: 14px;
      font-weight: 600;
      color: #333333;
      margin-bottom: 6px;
      width: 100%;

      .text-overflow {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        padding-right: 6px;
      }

      .job-salary {
        flex-shrink: 0;
        font-size: 12px;
        color: #1171ff;
        font-weight: 600;
      }
    }

    .job-info {
      @include flex-fun(row, space-between, center);
      font-size: 12px;
      color: #555555;

      .apply-btn {
        background-color: #0080ff;
        color: #fff;
        border: none;
        padding: 6px 12px;
        border-radius: 4px;
        font-size: 12px;
      }
    }
  }

  .cv-list {
    padding: 15px;
  }

  .cv-item {
    @include flex-fun(row, space-between, center);
    padding: 16px 0;
    border-bottom: 1px dashed #eeeeee;

    &:last-child {
      border-bottom: none;
    }
  }

  .cv-info {
    @include flex-fun(row, flex-start, center);
    gap: 10px;
  }

  .default-tag {
    background-color: #0080ff;
    color: #fff;
    font-size: 12px;
    padding: 2px 8px;
    border-radius: 4px;
  }

  .cv-name {
    font-size: 14px;
    color: #333333;
  }

  .delivery-btn {
    background-color: #0080ff;
    color: #fff;
    font-size: 14px;
    padding: 10px 24px;
    border-radius: 4px;
  }

  .delivery-tip-content {
    padding: 15px 20px;
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .success-message {
    display: flex;
    flex-direction: column;
    gap: 15px;
    color: #333333;

    .highlight {
      color: #555555;
      font-weight: 600;
    }
  }

  .success-text {
    font-size: 16px;
    color: #0080ff;
    font-weight: 600;
  }

  .tip-text {
    font-size: 14px;
    line-height: 1.4;
  }

  .time-text {
    color: #0080ff;
    font-weight: 600;
  }

  .tip-buttons {
    @include flex-fun(row, flex-end, center);

    .btn-exit {
      background-color: #ffffff;
      color: #333333;
      border: 1px solid #dddddd;
      font-size: 14px;
      padding: 10px 24px;
      border-radius: 4px;
      margin-right: 12px;
    }

    .btn-continue {
      background-color: #0080ff;
      color: #fff;
      font-size: 14px;
      padding: 10px 24px;
      border-radius: 4px;
    }
  }

  .dialog-footer {
    padding: 15px 0;
    @include flex-fun(row, center, center);
  }

  .btn-delivery {
    @include flex-fun(row, center, center);
    background-color: #0080ff;
    color: #fff;
    border: none;
    padding: 10px 16px;
    border-radius: 4px;
    font-size: 16px;
  }
}

.card-info-dialog {
  max-height: 90vh;
  min-height: 200px;
  width: 600px;
  max-width: 90vw;

  .dialog-title {
    @include flex-fun(row, space-between, center);
    font-size: 16px;
    font-weight: 600;
    padding: 10px;
    border-bottom: 1px solid #eeeeee;

    .text-title {
      flex: 1;
      text-align: center;
    }

    .svg-icon {
      width: 16px;
      height: 16px;
    }
  }

  .card-info-content {
    padding: 20px;
    max-height: 70vh;
    overflow-y: auto;
  }

  .card-info-header {
    display: flex;
    gap: 20px;
    margin-bottom: 20px;
    padding-bottom: 20px;
    border-bottom: 1px solid #eeeeee;
  }

  .photo-section {
    flex-shrink: 0;
    width: 120px;
    height: 150px;
    border: 1px solid #e2f1ff;
    border-radius: 8px;
    overflow: hidden;
    background-color: #f7fbff;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .id-photo {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .info-section {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .card-info-body {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .info-row {
    display: flex;
    align-items: flex-start;
    font-size: 14px;
    line-height: 1.5;
  }

  .label {
    flex-shrink: 0;
    color: #666;
    font-weight: 500;
    min-width: 80px;
  }

  .value {
    flex: 1;
    color: #333;
    word-break: break-all;
  }

  .card-info-footer {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #eeeeee;
    display: flex;
    justify-content: center;
  }

  .btn-confirm {
    background-color: #0080ff;
    color: #fff;
    border: none;
    padding: 10px 30px;
    border-radius: 4px;
    font-size: 14px;
    cursor: pointer;
    transition: background-color 0.3s;

    &:hover {
      background-color: #0066cc;
    }
  }
}
</style>
