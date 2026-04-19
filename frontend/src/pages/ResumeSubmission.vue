<template>
  <div class="page-container">
    <div class="headers">
      <div class="company-logo">公司logo</div>
      <div class="company-name">长沙市云聘教育科技有限公司</div>
    </div>

    <div class="content">
      <!-- 公司基本信息 -->
      <div class="col left-col">
        <!-- 第一个卡片：公司信息介绍 -->
        <div class="card first-card">
          <div class="info-header">
            <div class="info-logo">LOGO</div>
            <span class="info-title">长沙市云聘教育科技有限公司</span>
          </div>

          <div class="company-details">
            <div class="detail-item"><span class="label">单位行业：</span><span>教育</span></div>
            <div class="detail-item"><span class="label">单位性质：</span><span>教育单位</span></div>
            <div class="detail-item"><span class="label">单位规模：</span><span>150-500人</span></div>
            <div class="detail-item">
              <span class="label">单位地址：</span>
              <span>湖南省长沙市芙蓉区定王台街道东泰街附023号</span>
            </div>
          </div>

          <div class="circle-wrap">
            <CircleProgress percentage="66" type="arc">
              <div class="circle-box">
                <div class="circle-content">
                  <div class="rating-img">
                    <img src="../assets/svgs/qx.svg" alt="qr" class="svg" />
                    <div class="rating-value">BAA</div>
                  </div>
                  <span class="rating-text">云研企信等级</span>
                  <span class="rating-desc">超越 <span class="highlight">88.35%</span> 的校园招聘企业</span>
                </div>
              </div>
            </CircleProgress>
          </div>
        </div>

        <!-- 第二个卡片：二维码 -->
        <div class="card second-card">
          <div class="qr-placeholder">
            <div class="qr-box">
              <div class="corner-left-top"></div>
              <div class="corner-right-top"></div>
              <div class="corner-left-bottom"></div>
              <div class="corner-right-bottom"></div>
              <img src="../assets/images/test-code.png" class="img" alt="二维码" />
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
      <!-- 公司简介和招聘简章 -->
      <div class="col middle-col">
        <div class="section-title">单位简介</div>
        <div class="section-box">
          <span>WEGYMER健圈”成立于2016年，是中国“大健康、大体育”产业细分领域的先行企业，公司核心...</span>
        </div>
        <div class="section-title">招聘简章</div>
        <div class="section-box">
          <span>公司始终以“正道成功”的理念，致力于开发中国体育人才未来，帮助更多中国人养成受益一生的运动习惯。</span>
          <button @click="openDialog(1)">测试弹窗按钮1</button>
          <button @click="openDialog(2)">测试弹窗按钮2</button>
          <button @click="openDialog(3)">测试弹窗按钮3</button>
        </div>
        
        <!-- 测试功能按钮 -->
        <div class="section-title">读卡器测试</div>
        <div class="section-box test-area">
          <div class="test-buttons">
            <button class="test-btn" @click="connectDevice">连接设备</button>
            <button class="test-btn" @click="startAutoRead">开始自动读卡</button>
            <button class="test-btn" @click="stopAutoRead">停止自动读卡</button>
            <button class="test-btn" @click="readCard">手动读卡</button>
            <button class="test-btn" @click="disconnectDevice">断开设备</button>
            <button class="test-btn" @click="getFirmwareVersion">获取固件版本</button>
          </div>
          <div class="test-status" v-if="testStatus">
            {{ testStatus }}
          </div>
        </div>
      </div>
      <!-- 招聘岗位 -->
      <div class="col right-col">
        <div class="section-title">招聘职位</div>
        <div class="section-list-box">
          <div class="job-item" v-for="(job, idx) in jobs" :key="idx" @click="handleJobClick(job)">
            <div class="job-title">
              <div class="text-overflow">{{ job.title }}</div>
              <div class="job-salary">{{ job.salary }}</div>
            </div>
            <div class="job-info">
              <div>
                <div> 招聘人数：{{ job.count }} | {{ job.degree }} | {{ job.city }} </div>
                <div> 招聘专业：英语、商务英语 </div>
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
          <img v-if="dialogPopupOptions.type !== 3" src="../assets/svgs/close.svg" class="svg-icon"
            @click="closeDialog" />
        </div>
        <template v-if="dialogPopupOptions.type === 1">
          <van-checkbox-group v-model="checkboxValue" activeColor="#ffffff" iconColor="#0080FF"
            @change="handleCheckboxChange">
            <div class="section-list-box">
              <div :class="['job-item', { 'checked': job.isChecked }]" v-for="(job, idx) in jobs" :key="idx"
                @click="handleJobCheckSelect(idx)">
                <div class="check-sele">
                  <van-checkbox :name="idx" customStyle="margin-top: 2px;" @click.stop />
                </div>
                <div style="flex: 1; width: 280px">
                  <div class="job-title">
                    <div class="text-overflow">{{ job.title }}</div>
                    <div class="job-salary">{{ job.salary }}</div>
                  </div>
                  <div class="job-info">
                    <div>
                      <div> 招聘人数：{{ job.count }} | {{ job.degree }} | {{ job.city }} </div>
                      <div> 招聘专业：英语、商务英语 </div>
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
                <div v-if="cv.isDefault" class="default-tag">默认</div>
                <div class="cv-name">{{ cv.name }}</div>
              </div>
              <div class="delivery-btn" @click="handleCVDelivery(cv)">投递</div>
            </div>
          </div>
        </template>
        <template v-else-if="dialogPopupOptions.type === 3">
          <div class="delivery-tip-content">
            <div class="success-message">
              <div class="success-text">简历投递成功！</div>
              <div class="tip-text">继续投递简历，请点击<text class="highlight">【继续投递简历】</text></div>
              <div class="tip-text">若不再投递简历，请点击<text class="highlight">【退出登录】</text>或 <text class="time-text">{{ timer
                  }}S</text> 后系统自动退出登录</div>
            </div>
            <div class="tip-buttons">
              <div class="btn-exit" @click="handleExitLogin">退出登录</div>
              <div class="btn-continue" @click="handleContinueDelivery">继续投递简历</div>
            </div>
          </div>
        </template>
      </div>
    </van-popup>

    <!-- 卡片信息弹窗 -->
    <van-popup v-model:show="showCardInfoDialog" round>
      <div class="card-info-dialog">
        <div class="dialog-title">
          <div class="text-title">身份证信息</div>
          <img src="../assets/svgs/close.svg" class="svg-icon" @click="showCardInfoDialog = false" />
        </div>
        <div class="card-info-content" v-if="cardInfo">
          <div class="card-info-header">
            <div class="photo-section" v-if="cardInfo.photo">
              <img :src="'data:image/jpeg;base64,' + cardInfo.photo" alt="身份证照片" class="id-photo" />
            </div>
            <div class="info-section">
              <div class="info-row">
                <span class="label">姓名：</span>
                <span class="value">{{ cardInfo.name }}</span>
              </div>
              <div class="info-row">
                <span class="label">性别：</span>
                <span class="value">{{ cardInfo.sex }}</span>
              </div>
              <div class="info-row">
                <span class="label">民族：</span>
                <span class="value">{{ cardInfo.nation }}</span>
              </div>
              <div class="info-row">
                <span class="label">出生日期：</span>
                <span class="value">{{ cardInfo.birth }}</span>
              </div>
            </div>
          </div>
          <div class="card-info-body">
            <div class="info-row">
              <span class="label">身份证号：</span>
              <span class="value">{{ cardInfo.idNumber }}</span>
            </div>
            <div class="info-row">
              <span class="label">地址：</span>
              <span class="value">{{ cardInfo.address }}</span>
            </div>
            <div class="info-row">
              <span class="label">签发机关：</span>
              <span class="value">{{ cardInfo.depart }}</span>
            </div>
            <div class="info-row">
              <span class="label">有效期：</span>
              <span class="value">{{ cardInfo.validity }}</span>
            </div>
          </div>
          <div class="card-info-footer">
            <div class="btn-confirm" @click="handleConfirmCardInfo">确认信息</div>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useResumeStore } from '../store/resume'
import CircleProgress from '../components/CircleProgress.vue'

const resumeStore = useResumeStore()
const checkboxValue = ref([]);
const jobs = resumeStore.jobs
const cvList = resumeStore.cvList
const timer = ref(0)
const timeNum = ref(null)
const dialogPopupOptions = ref({
  title: '',
  type: 1,
  width: '711px',
  show: false
})

// 卡片信息相关状态
const cardInfo = ref(null)
const showCardInfoDialog = ref(false)
const testStatus = ref('')

// 监听卡片信息事件
const handleCardInfoReceived = (event) => {
  console.log('收到卡片信息:', event.detail)
  cardInfo.value = event.detail
  showCardInfoDialog.value = true
}

// 监听状态信息事件
const handleStatusMessage = (event) => {
  console.log('收到状态信息:', event.detail)
  // 可以在这里处理状态信息，比如显示toast等
}

onMounted(() => {
  // 监听卡片信息事件
  window.addEventListener('cardInfoReceived', handleCardInfoReceived)
  window.addEventListener('statusMessage', handleStatusMessage)
  
  // 检查是否已有存储的卡片信息
  if (window.cardInfo) {
    cardInfo.value = window.cardInfo
  }
})

onUnmounted(() => {
  // 移除事件监听
  window.removeEventListener('cardInfoReceived', handleCardInfoReceived)
  window.removeEventListener('statusMessage', handleStatusMessage)
})

const openDialog = (type) => {
  if (type === 1) {
    dialogPopupOptions.value.title = '请选择你需要投递的职位';
    dialogPopupOptions.value.type = 1;
    checkboxValue.value = [];
    dialogPopupOptions.value.width = '711px';
    console.log('1111', jobs)
    jobs.forEach((item, index) => {
      if (item.isChecked) {
        checkboxValue.value.push(index);
      }
    })
  } else if (type === 2) {
    dialogPopupOptions.value.title = '选择简历';
    dialogPopupOptions.value.type = 2;
    dialogPopupOptions.value.width = '430px';
  } else {
    dialogPopupOptions.value.title = '投递简历提示';
    dialogPopupOptions.value.type = 3;
    dialogPopupOptions.value.width = '400px';
    timer.value = 60;
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
  console.log('dialogPopupOptions', dialogPopupOptions.value)
}

const handleJobClick = (job) => {
  console.log('点击职位：', job)
  openDialog(2)
}

const handleCVDelivery = (cv) => {
  console.log('投递的简历：', cv)
  openDialog(3)
}

const handleExitLogin = () => {
  if(timeNum.value) {
    clearInterval(timeNum.value)
    timeNum.value = null
  }
  closeDialog();
  console.log('退出登录')
}

const handleContinueDelivery = () => {
  console.log('继续投递简历')
  closeDialog();
}
const closeDialog = () => {
  dialogPopupOptions.value.show = false;
}
const handleJobCheckSelect = (index) => {
  console.log('点击职位：',index , checkboxValue.value)
  jobs[index].isChecked = !jobs[index].isChecked;
  const idx = checkboxValue.value.indexOf(index);
  console.log(idx)
  if (idx > -1) {
    checkboxValue.value.splice(idx, 1);
  } else {
    checkboxValue.value.push(index);
  }
  console.log('点击职位：',index , checkboxValue.value)
};
const handleCheckboxChange = (val) => {
  console.log('checkboxValue', val)
  jobs.forEach((item, index) => {
    item.isChecked = val.includes(index);
  })
}

// 确认卡片信息
const handleConfirmCardInfo = () => {
  console.log('确认卡片信息:', cardInfo.value)
  showCardInfoDialog.value = false
  // 这里可以添加后续处理逻辑，比如自动填写表单等
}

// 测试功能方法
const connectDevice = () => {
  if (window.android) {
    testStatus.value = '正在连接设备...'
    window.android.connectDevice('/dev/ttyS3')
  } else {
    testStatus.value = 'Android接口不可用'
  }
}

const disconnectDevice = () => {
  if (window.android) {
    testStatus.value = '正在断开设备...'
    window.android.disconnectDevice()
  } else {
    testStatus.value = 'Android接口不可用'
  }
}

const readCard = () => {
  if (window.android) {
    testStatus.value = '正在读卡...'
    window.android.readCard()
  } else {
    testStatus.value = 'Android接口不可用'
  }
}

const startAutoRead = () => {
  if (window.android) {
    testStatus.value = '开始自动读卡...'
    window.android.startAutoRead()
  } else {
    testStatus.value = 'Android接口不可用'
  }
}

const stopAutoRead = () => {
  if (window.android) {
    testStatus.value = '停止自动读卡...'
    window.android.stopAutoRead()
  } else {
    testStatus.value = 'Android接口不可用'
  }
}

const getFirmwareVersion = () => {
  if (window.android) {
    testStatus.value = '正在获取固件版本...'
    window.android.getFirmwareVersion()
  } else {
    testStatus.value = 'Android接口不可用'
  }
}
</script>

<style lang="scss" scoped>
@import '../assets/mixins.scss';

.page-container {
  @include flex-fun(column, flex-start, flex-start);
}

.headers {
  width: 100%;
  @include flex-fun(row, flex-start, center);
  padding: 0 20px;
  background-color: #fff;
  height: 50px;
  flex-shrink: 0;
}

.company-logo {}

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
  gap: 10px;
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
  padding: 10px 20px;
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
  font-size: 10px;
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
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #eeeeee;
}

.info-logo {
  width: 48px;
  height: 48px;
  border: 1px solid #79afff;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #3f8bff;
  font-size: 14px;
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
  width: 140px;
  height: 140px;
  margin: 0 auto;
  align-items: center;
}

.qr-box {
  width: 120px;
  height: 120px;
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

.middle-col {
  width: 38%;
  padding: 12px;
  margin-right: 10px;
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
}

.right-col {
  width: 38%;
  padding: 12px;
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
  font-size: 14px;
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
  font-size: 12px;
  color: #1171ff;
  font-weight: 600;
}

.job-info {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #555555;
}

.apply-btn {
  background-color: #0080ff;
  color: #fff;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 12px;
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

// 测试功能样式
.test-area {
  .test-buttons {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 15px;
  }

  .test-btn {
    background-color: #f0f0f0;
    border: 1px solid #ddd;
    padding: 8px 16px;
    border-radius: 4px;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      background-color: #e0e0e0;
      border-color: #ccc;
    }
  }

  .test-status {
    margin-top: 10px;
    padding: 10px;
    background-color: #f5f5f5;
    border-radius: 4px;
    font-size: 14px;
    color: #333;
  }
}
</style>